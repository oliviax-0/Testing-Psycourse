
import qa.CourseApi
import qa.TestApi

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

Map fixture = TestApi.createActiveCategory('02')

String cookie = fixture.cookie
String categoryId = fixture.categoryId

try {
	TestApi.removeGeneratedDraft(
		cookie, categoryId, 'TEST_1'
	)

	def response = CourseApi.request(
		'POST',
		'/api/admin/tests',
		cookie,
		[
			categoryId: categoryId,
			type: 'TEST_1',
			status: 'DRAFT'
		]
	)

	WS.verifyResponseStatusCode(response, 201)

	String testId = CourseApi.json(response).id
	assert testId

	def test = TestApi.findTest(cookie, testId)

	assert test.categoryId == categoryId
	assert test.type == 'TEST_1'
	assert test.timerSeconds == null
	assert test.status == 'DRAFT'

	def related = CourseApi.tests(cookie).findAll {
		it.categoryId == categoryId
	}

	assert related.size() == 3

	println('BE4-TST-02 PASSED')
	println('POST HTTP Status: 201')
	println('Test type: ' + test.type)
	println('Timer: null (no time limit)')

} finally {
	CourseApi.cleanup(cookie, categoryId)
}
