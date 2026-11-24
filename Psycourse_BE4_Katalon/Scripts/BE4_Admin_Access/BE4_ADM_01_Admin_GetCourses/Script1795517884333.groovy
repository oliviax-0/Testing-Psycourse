
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import groovy.json.JsonSlurper
import internal.GlobalVariable

// BE4-ADM-01: Admin dapat mengakses API admin courses

// 1. Login sebagai admin
def loginResponse = WS.sendRequest(
	findTestObject('API/Auth/Login', [
		'email'   : GlobalVariable.adminEmail,
		'password': GlobalVariable.adminPassword
	])
)

WS.verifyResponseStatusCode(loginResponse, 200)

// 2. Pastikan login menghasilkan role ADMIN
def loginData = new JsonSlurper().parseText(
	loginResponse.getResponseBodyContent()
)

assert loginData.user?.role == 'ADMIN' :
	'Akun yang digunakan bukan ADMIN'

// 3. Ambil session cookie dari response login
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

// 4. Akses endpoint admin dengan session cookie
def coursesResponse = WS.sendRequest(
	findTestObject('API/Admin/GetCourses', [
		'sessionCookie': adminCookie
	])
)

// 5. Verifikasi HTTP 200
WS.verifyResponseStatusCode(coursesResponse, 200)

// 6. Verifikasi struktur JSON response
def coursesData = new JsonSlurper().parseText(
	coursesResponse.getResponseBodyContent()
)

assert coursesData instanceof Map :
	'Response harus berupa JSON object'

assert coursesData.containsKey('courses') :
	'Response tidak memiliki properti courses'

assert coursesData.courses instanceof List :
	'Properti courses harus berupa array'

// 7. Tampilkan hasil aman tanpa data sensitif
println('BE4-ADM-01: Admin access verification PASSED')
println('HTTP Status: ' + WS.getResponseStatusCode(coursesResponse))
println('Courses array ditemukan')
println('Jumlah courses: ' + coursesData.courses.size())
