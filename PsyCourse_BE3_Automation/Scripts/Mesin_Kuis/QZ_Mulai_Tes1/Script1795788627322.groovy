import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.impl.HttpTextBodyContent
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.testobject.TestObjectProperty
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.util.KeywordUtil

// Base URL aplikasi
def baseUrl = 'http://localhost:3000'

// Masukkan cookie baru langsung di komputer.
// Jangan membagikan nilainya ke chat.
def sessionCookie = System.getenv('PSYCOURSE_SESSION')
if (!sessionCookie) {
    KeywordUtil.markFailed('Environment variable PSYCOURSE_SESSION belum diatur.')
}

// Siapkan request GET
def request = new RequestObject('GET_Test_1')
request.setRestUrl(baseUrl + '/api/tests/logika-test-1')
request.setRestRequestMethod('GET')

// Tambahkan autentikasi
def cookie = new TestObjectProperty(
	'Cookie',
	ConditionType.EQUALS,
	'psycourse_session=' + sessionCookie
)
request.setHttpHeaderProperties([cookie])

// Kirim request
def response = WS.sendRequest(request)

// Tampilkan hasil
println('HTTP Status: ' + response.getStatusCode())
println('Response Body: ' + response.getResponseBodyContent())

// Validasi
WS.verifyResponseStatusCode(response, 200)