
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

// BE4-ADM-02: Pengguna tanpa login tidak boleh mengakses API admin

// 1. Kirim GET tanpa session cookie
def response = WS.sendRequest(
	findTestObject('API/Admin/GetCoursesNoAuth')
)

// 2. Pastikan server menolak akses dengan HTTP 401
WS.verifyResponseStatusCode(response, 401)

// 3. Catat hasil tanpa menampilkan data sensitif
println('BE4-ADM-02: Unauthenticated access verification PASSED')
println('Actual HTTP Status: ' + response.getStatusCode())
println('Expected HTTP Status: 401')
