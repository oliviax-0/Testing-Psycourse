
import qa.CourseApi
import qa.TestApi

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

Map fixture = TestApi.createActiveCategory('03')

String cookie = fixture.cookie
String categoryId = fixture.categoryId

try {
	TestApi.removeGeneratedDraft(
		cookie, categoryId, 'TEST_2'
	)

	def response = CourseApi.request(
		'POST',
		'/api/admin/tests',
		cookie,
		[
			categoryId: categoryId,
			type: 'TEST_2',
			timerSeconds: 900,
			status: 'DRAFT'
		]
	)

	WS.verifyResponseStatusCode(response, 201)

	String testId = CourseApi.json(response).id
	assert testId

	def test = TestApi.findTest(cookie, testId)

	assert test.categoryId == categoryId
	assert test.type == 'TEST_2'
	assert test.timerSeconds == 900
	assert test.status == 'DRAFT'

	println('BE4-TST-03 PASSED')
	println('POST HTTP Status: 201')
	println('Timer: ' + test.timerSeconds + ' seconds')

} finally {
	CourseApi.cleanup(cookie, categoryId)
}
