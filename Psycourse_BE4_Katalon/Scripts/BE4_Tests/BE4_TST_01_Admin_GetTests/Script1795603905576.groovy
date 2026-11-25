
import qa.CourseApi

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = CourseApi.loginAdmin()

def response = CourseApi.request(
	'GET',
	'/api/admin/tests',
	cookie
)

WS.verifyResponseStatusCode(response, 200)

def data = CourseApi.json(response)

assert data.tests instanceof List
assert data.categories instanceof List

assert data.tests.every { test ->
	test.id &&
	test.slug &&
	test.type in ['TEST_1', 'TEST_2', 'PRACTICE'] &&
	test.status in ['DRAFT', 'AKTIF', 'NONAKTIF'] &&
	test.categoryId &&
	test.questionCount instanceof Number &&
	test.attemptCount instanceof Number
}

println('BE4-TST-01 PASSED')
println('GET HTTP Status: ' + response.getStatusCode())
println('Total tests: ' + data.tests.size())
println('Available categories: ' + data.categories.size())
