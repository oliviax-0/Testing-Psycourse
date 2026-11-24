
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import internal.GlobalVariable
import groovy.json.JsonSlurper

// 1. Siapkan request login admin
def loginRequest = findTestObject('API/Auth/Login', [
	'email': GlobalVariable.adminEmail,
	'password': GlobalVariable.adminPassword
])

// 2. Periksa URL yang digunakan
println('URL Login: ' + loginRequest.getRestUrl())

assert loginRequest.getRestUrl() ==
	GlobalVariable.baseUrl + '/api/auth/login'

// 3. Kirim request login
def loginResponse = WS.sendRequest(loginRequest)

// 4. Pastikan HTTP status 200
WS.verifyResponseStatusCode(loginResponse, 200)

// 5. Periksa role akun
def loginData = new JsonSlurper().parseText(
	loginResponse.getResponseText()
)

assert loginData.user.role == 'ADMIN'

// 6. Tampilkan hasil
println('PASS: Login ADMIN berhasil')
