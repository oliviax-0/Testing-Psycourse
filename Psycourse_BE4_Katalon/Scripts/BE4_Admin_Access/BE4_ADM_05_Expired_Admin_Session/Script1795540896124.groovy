
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import groovy.json.JsonSlurper
import internal.GlobalVariable
import java.security.MessageDigest

// BE4-ADM-05: Expired Admin Session
// Fixture hanya mengakses container Docker QA lokal.
// Tidak mengubah database PostgreSQL bersama.

// 1. Login sebagai ADMIN
def loginResponse = WS.sendRequest(
	findTestObject('API/Auth/Login', [
		'email': GlobalVariable.adminEmail,
		'password': GlobalVariable.adminPassword
	])
)

WS.verifyResponseStatusCode(loginResponse, 200)

// 2. Pastikan akun memiliki role ADMIN
def loginData = new JsonSlurper().parseText(
	loginResponse.getResponseBodyContent()
)

assert loginData.user?.role == 'ADMIN' :
	'Akun pengujian harus memiliki role ADMIN'

// 3. Ambil session cookie admin
def setCookieValues = loginResponse.getHeaderFields()
	.findAll { name, value ->
		name != null && name.equalsIgnoreCase('Set-Cookie')
	}
	.values()
	.flatten()
	.collect { it.toString() }

def adminCookie = setCookieValues
	.collect { it.split(';')[0].trim() }
	.find { it.startsWith('psycourse_session=') }

assert adminCookie != null :
	'Session cookie admin tidak ditemukan'

assert adminCookie.length() > 'psycourse_session='.length() :
	'Session cookie admin kosong'

// 4. Verifikasi session masih aktif sebelum expired
def beforeExpiry = WS.sendRequest(
	findTestObject('API/Admin/GetCourses', [
		'sessionCookie': adminCookie
	])
)

WS.verifyResponseStatusCode(beforeExpiry, 200)

// 5. Hitung SHA-256 token sesuai implementasi backend
def rawToken = adminCookie.substring(
	'psycourse_session='.length()
)

def tokenHash = MessageDigest.getInstance('SHA-256')
	.digest(rawToken.getBytes('UTF-8'))
	.encodeHex()
	.toString()

assert tokenHash ==~ /[0-9a-f]{64}/ :
	'Hash token tidak valid'

// 6. SQL fixture: expire hanya satu session yang cocok
def sql = """
WITH expired AS (
    UPDATE public."Session"
    SET "expiresAt" = NOW() - INTERVAL '1 minute'
    WHERE "tokenHash" = '${tokenHash}'
      AND "expiresAt" > NOW()
    RETURNING 1
)
SELECT COUNT(*) FROM expired;
"""

// 7. Jalankan fixture pada container QA lokal
def command = [
	'docker',
	'exec',
	'-i',
	'psycourse-be4-qa-db',
	'psql',
	'-X',
	'-A',
	'-t',
	'-v',
	'ON_ERROR_STOP=1',
	'-U',
	'psycourse_qa',
	'-d',
	'psycourse_be4_qa'
]

def process = new ProcessBuilder(command)
	.redirectErrorStream(true)
	.start()

process.outputStream.withWriter('UTF-8') { writer ->
	writer.write(sql)
	writer.write('\n')
}

def fixtureOutput = process.inputStream
	.getText('UTF-8')
	.trim()

def exitCode = process.waitFor()

assert exitCode == 0 :
	'Fixture database QA gagal dijalankan'

assert fixtureOutput == '1' :
	'Fixture tidak menemukan tepat satu session pada database QA'

// 8. Gunakan kembali cookie yang sudah expired
def afterExpiry = WS.sendRequest(
	findTestObject('API/Admin/GetCourses', [
		'sessionCookie': adminCookie
	])
)

// 9. Backend harus menolak expired session
WS.verifyResponseStatusCode(afterExpiry, 401)

// 10. Catat hasil tanpa mencetak session cookie
println('BE4-ADM-05: Expired admin session verification PASSED')
println('Before Expiry HTTP Status: ' + beforeExpiry.getStatusCode())
println('Fixture Updated Sessions: 1')
println('After Expiry HTTP Status: ' + afterExpiry.getStatusCode())
println('Expected HTTP Status: 401')
