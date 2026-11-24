
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import internal.GlobalVariable
import groovy.json.JsonSlurper

// LANGKAH 1: Login sebagai siswa
def studentLoginRequest = findTestObject('API/Auth/Login', [
	'email': GlobalVariable.studentEmail,
	'password': GlobalVariable.studentPassword
])

def studentLoginResponse = WS.sendRequest(studentLoginRequest)

// LANGKAH 2: Verifikasi login berhasil
WS.verifyResponseStatusCode(studentLoginResponse, 200)

def studentLoginData = new JsonSlurper().parseText(
	studentLoginResponse.getResponseText()
)

// Role siswa di PsyCourse bernama USER
assert studentLoginData.user.role == 'USER'

// LANGKAH 3: Ambil session cookie siswa
def setCookieValues = studentLoginResponse.getHeaderFields()
	.findAll { key, value ->
		key != null && key.equalsIgnoreCase('Set-Cookie')
	}
	.values()
	.flatten()

def studentCookie = setCookieValues
	.collect { it.toString().split(';', 2)[0].trim() }
	.find { it.startsWith('psycourse_session=') }

assert studentCookie != null : 'Cookie siswa tidak ditemukan'
assert studentCookie.length() > 'psycourse_session='.length()

// LANGKAH 4: Kirim cookie siswa ke /api/me
def studentMeRequest = findTestObject('API/Auth/GetMe', [
	'sessionCookie': studentCookie
])

assert studentMeRequest.getRestUrl() ==
	GlobalVariable.baseUrl + '/api/me'

def studentMeResponse = WS.sendRequest(studentMeRequest)

// LANGKAH 5: Verifikasi sesi siswa
WS.verifyResponseStatusCode(studentMeResponse, 200)

def studentMeData = new JsonSlurper().parseText(
	studentMeResponse.getResponseText()
)

assert studentMeData.role == 'USER'

assert studentMeData.email.equalsIgnoreCase(
	GlobalVariable.studentEmail
)

println('PASS: Login USER berhasil')
println('PASS: Session cookie USER tersedia')
println('PASS: GET /api/me mengembalikan USER')
