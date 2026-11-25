
import qa.CourseApi
import qa.TestApi

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

Map fixture = TestApi.createActiveCategory('07')

String cookie = fixture.cookie
String categoryId = fixture.categoryId

try {
	def beforeTests = CourseApi.tests(cookie)

	def target = beforeTests.find {
		it.categoryId == categoryId &&
		it.type == 'TEST_2'
	}

	assert target != null

	def beforeCounts = beforeTests.collectEntries { item ->
		[(item.id): [
			questions: item.questionCount,
			attempts: item.attemptCount
		]]
	}

	def response = CourseApi.request(
		'PATCH',
		'/api/admin/tests/' + target.id,
		cookie,
		[
			status: 'NONAKTIF',
			timerSeconds: 1200
		]
	)

	WS.verifyResponseStatusCode(response, 200)

	def afterTests = CourseApi.tests(cookie)

	def updated = afterTests.find {
		it.id == target.id
	}

	assert updated != null
	assert updated.status == 'NONAKTIF'
	assert updated.timerSeconds == 1200
	assert updated.categoryId == categoryId
	assert updated.type == 'TEST_2'

	assert beforeTests.collect { it.id }.toSet() ==
		afterTests.collect { it.id }.toSet()

	beforeCounts.each { id, counts ->
		def current = afterTests.find { it.id == id }

		assert current != null
		assert current.questionCount == counts.questions
		assert current.attemptCount == counts.attempts
	}

	println('BE4-TST-07 PASSED')
	println('PATCH HTTP Status: 200')
	println('Updated status: ' + updated.status)
	println('Updated timer: ' + updated.timerSeconds)
	println('Question/attempt counts unchanged: true')

} finally {
	CourseApi.cleanup(cookie, categoryId)
}
