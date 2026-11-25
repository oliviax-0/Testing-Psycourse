
import qa.CourseApi
import qa.TestApi

Map fixture = TestApi.createActiveCategory('04')

String cookie = fixture.cookie
String categoryId = fixture.categoryId

try {
	TestApi.removeGeneratedDraft(
		cookie, categoryId, 'TEST_2'
	)

	[0, -60].each { duration ->
		def response = CourseApi.request(
			'POST',
			'/api/admin/tests',
			cookie,
			[
				categoryId: categoryId,
				type: 'TEST_2',
				timerSeconds: duration,
				status: 'DRAFT'
			]
		)

		assert response.getStatusCode() in [400, 422] :
			'Durasi invalid ' + duration +
			' tidak ditolak dengan benar. Actual HTTP ' +
			response.getStatusCode()

		def matching = CourseApi.tests(cookie).findAll {
			it.categoryId == categoryId &&
			it.type == 'TEST_2'
		}

		assert matching.isEmpty() :
			'Test 2 tercipta meskipun durasinya invalid'

		println(
			'Duration ' + duration +
			' rejected: HTTP ' + response.getStatusCode()
		)
	}

	println('BE4-TST-04 PASSED')
	println('No invalid Test 2 created')

} finally {
	CourseApi.cleanup(cookie, categoryId)
}
