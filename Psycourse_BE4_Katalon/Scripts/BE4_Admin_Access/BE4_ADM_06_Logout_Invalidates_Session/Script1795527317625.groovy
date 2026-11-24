
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import groovy.json.JsonSlurper
import internal.GlobalVariable
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObjectProperty

// BE4-ADM-06: Session admin tidak valid setelah logout

// 1. Login sebagai ADMIN
def loginResponse = WS.sendRequest(
	findTestObject('API/Auth/Login', [
		'email'   : GlobalVariable.adminEmail,
		'password': GlobalVariable.adminPassword
	])
)

WS.verifyResponseStatusCode(loginResponse, 200)

// 2. Pastikan role ADMIN
def loginData = new JsonSlurper().parseText(
	loginResponse.getResponseBodyContent()
)

assert loginData.user?.role == 'ADMIN' :
	'Akun pengujian harus memiliki role ADMIN'

// 3. Ambil session cookie hasil login
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

// 4. Pastikan session valid sebelum logout
def beforeLogout = WS.sendRequest(
	findTestObject('API/Admin/GetCourses', [
		'sessionCookie': adminCookie
	])
)

WS.verifyResponseStatusCode(beforeLogout, 200)

// 5. Logout menggunakan session yang sama

def logoutRequest = findTestObject('API/Auth/Logout', [
    'sessionCookie': adminCookie
])

// Tetapkan Cookie secara eksplisit pada request logout.
// POST /api/auth/logout tidak membutuhkan body.
logoutRequest.setHttpHeaderProperties([
    new TestObjectProperty(
        'Cookie',
        ConditionType.EQUALS,
        adminCookie
    )
])

def logoutResponse = WS.sendRequest(logoutRequest)

WS.verifyResponseStatusCode(logoutResponse, 204)

// 6. Gunakan kembali cookie lama setelah logout
def afterLogout = WS.sendRequest(
	findTestObject('API/Admin/GetCourses', [
		'sessionCookie': adminCookie
	])
)

// 7. Cookie lama harus ditolak
WS.verifyResponseStatusCode(afterLogout, 401)

// 8. Catat hasil tanpa mencetak nilai cookie
println('BE4-ADM-06: Logout session invalidation PASSED')
println('Before Logout HTTP Status: ' + beforeLogout.getStatusCode())
println('Logout HTTP Status: ' + logoutResponse.getStatusCode())
println('After Logout HTTP Status: ' + afterLogout.getStatusCode())
println('Old session cookie rejected successfully')
