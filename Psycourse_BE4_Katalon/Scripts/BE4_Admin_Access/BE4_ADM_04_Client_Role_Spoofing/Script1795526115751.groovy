
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import groovy.json.JsonSlurper
import internal.GlobalVariable

// BE4-ADM-04: Siswa memalsukan role ADMIN pada request

// 1. Login sebagai siswa
def loginResponse = WS.sendRequest(
	findTestObject('API/Auth/Login', [
		'email'   : GlobalVariable.studentEmail,
		'password': GlobalVariable.studentPassword
	])
)

WS.verifyResponseStatusCode(loginResponse, 200)

// 2. Pastikan akun sebenarnya memiliki role USER
def loginData = new JsonSlurper().parseText(
	loginResponse.getResponseBodyContent()
)

assert loginData.user?.role == 'USER' :
	'Akun pengujian harus memiliki role USER'

// 3. Ambil session cookie siswa
def setCookieValues = loginResponse.getHeaderFields()
	.findAll { name, value ->
		name != null && name.equalsIgnoreCase('Set-Cookie')
	}
	.values()
	.flatten()
	.collect { it.toString() }

def studentCookie = setCookieValues
	.collect { it.split(';')[0].trim() }
	.find { it.startsWith('psycourse_session=') }

assert studentCookie != null :
	'Session cookie siswa tidak ditemukan'

assert studentCookie.length() > 'psycourse_session='.length() :
	'Session cookie siswa kosong'

// 4. Kirim request dengan klaim role ADMIN dari klien
def response = WS.sendRequest(
	findTestObject('API/Admin/GetCoursesRoleSpoof', [
		'sessionCookie': studentCookie
	])
)

// 5. Backend harus tetap menolak akses siswa
WS.verifyResponseStatusCode(response, 403)

// 6. Catat hasil tanpa mencetak cookie
println('BE4-ADM-04: Client role spoofing verification PASSED')
println('Actual HTTP Status: ' + response.getStatusCode())
println('Expected HTTP Status: 403')
println('Student role remains USER')
