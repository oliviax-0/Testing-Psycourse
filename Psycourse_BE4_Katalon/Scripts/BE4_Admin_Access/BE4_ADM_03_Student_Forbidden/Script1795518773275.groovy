
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import groovy.json.JsonSlurper
import internal.GlobalVariable

// BE4-ADM-03: Siswa tidak boleh mengakses API admin

// 1. Login menggunakan akun siswa
def loginResponse = WS.sendRequest(
	findTestObject('API/Auth/Login', [
		'email'   : GlobalVariable.studentEmail,
		'password': GlobalVariable.studentPassword
	])
)

// 2. Pastikan login berhasil
WS.verifyResponseStatusCode(loginResponse, 200)

// 3. Verifikasi role siswa
def loginData = new JsonSlurper().parseText(
	loginResponse.getResponseBodyContent()
)

assert loginData.user?.role == 'USER' :
	'Akun pengujian harus memiliki role USER'

// 4. Ambil session cookie siswa
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

// 5. Siswa mencoba mengakses API admin
def adminResponse = WS.sendRequest(
	findTestObject('API/Admin/GetCourses', [
		'sessionCookie': studentCookie
	])
)

// 6. Server harus menolak dengan HTTP 403
WS.verifyResponseStatusCode(adminResponse, 403)

// 7. Cetak hasil tanpa menampilkan cookie
println('BE4-ADM-03: Student access restriction verification PASSED')
println('Actual HTTP Status: ' + adminResponse.getStatusCode())
println('Expected HTTP Status: 403')
