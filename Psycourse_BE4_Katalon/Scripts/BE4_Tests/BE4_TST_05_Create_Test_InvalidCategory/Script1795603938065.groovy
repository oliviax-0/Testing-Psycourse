
import qa.CourseApi

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = CourseApi.loginAdmin()

def beforeIds = CourseApi.tests(cookie)
	.collect { it.id }
	.toSet()

String missingId = 'qa-missing-' +
	UUID.randomUUID().toString().replace('-', '')

def response = CourseApi.request(
	'POST',
	'/api/admin/tests',
	cookie,
	[
		categoryId: missingId,
		type: 'TEST_1',
		status: 'DRAFT'
	]
)

WS.verifyResponseStatusCode(response, 404)

assert response.getResponseBodyContent()
	.contains('CATEGORY_UNAVAILABLE') :
	'Respons tidak menunjukkan kategori tidak tersedia'

def afterIds = CourseApi.tests(cookie)
	.collect { it.id }
	.toSet()

assert beforeIds == afterIds :
	'Daftar tes berubah setelah POST kategori invalid'

println('BE4-TST-05 PASSED')
println('POST HTTP Status: ' + response.getStatusCode())
println('No test created: true')
