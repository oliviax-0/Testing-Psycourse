
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import internal.GlobalVariable
import groovy.json.JsonSlurper

// LANGKAH 1: Login sebagai ADMIN
def adminLoginRequest = findTestObject('API/Auth/Login', [
	'email': GlobalVariable.adminEmail,
	'password': GlobalVariable.adminPassword
])

def adminLoginResponse = WS.sendRequest(adminLoginRequest)

// LANGKAH 2: Verifikasi login
WS.verifyResponseStatusCode(adminLoginResponse, 200)

def adminLoginData = new JsonSlurper().parseText(
	adminLoginResponse.getResponseText()
)

assert adminLoginData.user.role == 'ADMIN'

// LANGKAH 3: Cari cookie dari response login
def setCookieValues = adminLoginResponse.getHeaderFields()
	.findAll { key, value ->
		key != null && key.equalsIgnoreCase('Set-Cookie')
	}
	.values()
	.flatten()

def adminCookie = setCookieValues
	.collect { it.toString().split(';', 2)[0].trim() }
	.find { it.startsWith('psycourse_session=') }

assert adminCookie != null : 'Session cookie tidak ditemukan'
assert adminCookie.length() > 'psycourse_session='.length()

// LANGKAH 4: Siapkan GET /api/me
def adminMeRequest = findTestObject('API/Auth/GetMe', [
	'sessionCookie': adminCookie
])

assert adminMeRequest.getRestUrl() ==
	GlobalVariable.baseUrl + '/api/me'

// LANGKAH 5: Kirim request dengan cookie admin
def adminMeResponse = WS.sendRequest(adminMeRequest)

// LANGKAH 6: Verifikasi sesi admin
WS.verifyResponseStatusCode(adminMeResponse, 200)

def adminMeData = new JsonSlurper().parseText(
	adminMeResponse.getResponseText()
)

// GET /api/me mengembalikan role langsung
assert adminMeData.role == 'ADMIN'

assert adminMeData.email.equalsIgnoreCase(
	GlobalVariable.adminEmail
)

// LANGKAH 7: Tampilkan status, tanpa cookie/password
println('PASS: Login ADMIN berhasil')
println('PASS: Session cookie ADMIN tersedia')
println('PASS: GET /api/me mengembalikan ADMIN')
