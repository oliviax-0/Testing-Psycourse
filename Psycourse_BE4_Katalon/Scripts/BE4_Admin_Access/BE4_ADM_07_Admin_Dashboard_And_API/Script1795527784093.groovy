
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import groovy.json.JsonSlurper
import internal.GlobalVariable

// BE4-ADM-07
// Admin dapat mengakses halaman dashboard dan API admin

// 1. Login sebagai ADMIN
def loginResponse = WS.sendRequest(
	findTestObject('API/Auth/Login', [
		'email': GlobalVariable.adminEmail,
		'password': GlobalVariable.adminPassword
	])
)

WS.verifyResponseStatusCode(loginResponse, 200)

// 2. Verifikasi role ADMIN
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

// 4. Akses halaman dashboard admin
def dashboardResponse = WS.sendRequest(
	findTestObject('API/Admin/GetAdminDashboard', [
		'sessionCookie': adminCookie
	])
)

WS.verifyResponseStatusCode(dashboardResponse, 200)

// 5. Verifikasi konten halaman dashboard
def dashboardHtml = dashboardResponse.getResponseBodyContent()

assert dashboardHtml.contains('Dashboard admin') :
	'Konten dashboard admin tidak ditemukan'

// 6. Akses API Courses dengan session yang sama
def coursesResponse = WS.sendRequest(
	findTestObject('API/Admin/GetCourses', [
		'sessionCookie': adminCookie
	])
)

WS.verifyResponseStatusCode(coursesResponse, 200)

// 7. Verifikasi struktur JSON API
def coursesData = new JsonSlurper().parseText(
	coursesResponse.getResponseBodyContent()
)

assert coursesData instanceof Map :
	'Response API harus berupa JSON object'

assert coursesData.courses instanceof List :
	'Response API harus memiliki array courses'

// 8. Catat hasil tanpa membocorkan session cookie
println('BE4-ADM-07: Admin Dashboard and API verification PASSED')
println('Dashboard HTTP Status: ' + dashboardResponse.getStatusCode())
println('Dashboard content found: true')
println('Admin API HTTP Status: ' + coursesResponse.getStatusCode())
println('Courses count: ' + coursesData.courses.size())
