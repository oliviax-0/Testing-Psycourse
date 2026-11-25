
import qa.CourseApi
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = CourseApi.loginAdmin()
Map payload = CourseApi.newCourse('02')
String createdId = null

try {
	def response = CourseApi.request(
		'POST', '/api/admin/courses', cookie, payload
	)

	WS.verifyResponseStatusCode(response, 201)

	createdId = CourseApi.json(response).id

	assert createdId instanceof String && createdId

	def course = CourseApi.courses(cookie).find {
		it.id == createdId
	}

	assert course != null : 'Course baru tidak ditemukan'
	assert course.slug == payload.slug
	assert course.judul == payload.judul
	assert course.status == 'DRAFT'

	def relatedTests = CourseApi.tests(cookie).findAll {
		it.categoryId == createdId
	}

	assert relatedTests.size() == 3
	assert relatedTests.every { it.status == 'DRAFT' }

	assert relatedTests.collect { it.type }.toSet() ==
		['TEST_1', 'TEST_2', 'PRACTICE'].toSet()

	println('BE4-CRS-02 PASSED')
	println('Create HTTP Status: 201')
	println('Course readable: true')
	println('Related draft tests: ' + relatedTests.size())

} finally {
	CourseApi.cleanup(cookie, createdId)
}
