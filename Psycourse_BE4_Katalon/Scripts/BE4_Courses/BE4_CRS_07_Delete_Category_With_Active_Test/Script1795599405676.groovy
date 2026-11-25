
import qa.CourseApi
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = CourseApi.loginAdmin()
Map payload = CourseApi.newCourse('07')

String createdId = null
boolean deleted = false

try {
	def created = CourseApi.request(
		'POST', '/api/admin/courses', cookie, payload
	)

	WS.verifyResponseStatusCode(created, 201)
	createdId = CourseApi.json(created).id

	def initialTests = CourseApi.tests(cookie).findAll {
		it.categoryId == createdId
	}

	assert initialTests.size() == 3

	def test = initialTests.find {
		it.type == 'TEST_1'
	}

	assert test != null
	assert test.attemptCount == 0

	def activated = CourseApi.request(
		'PATCH',
		'/api/admin/tests/' + test.id,
		cookie,
		[status: 'AKTIF']
	)

	WS.verifyResponseStatusCode(activated, 200)

	def activeTest = CourseApi.tests(cookie).find {
		it.id == test.id
	}

	assert activeTest != null
	assert activeTest.status == 'AKTIF'
	assert activeTest.attemptCount == 0

	println('Active test confirmed before deletion')

	def deletion = CourseApi.request(
		'DELETE',
		'/api/admin/courses/' + createdId,
		cookie
	)

	println('DELETE HTTP Status: ' + deletion.getStatusCode())

	// Dalam kondisi tanpa Attempt dan Entitlement,
	// implementasi backend saat ini mengizinkan DELETE.
	WS.verifyResponseStatusCode(deletion, 200)

	assert CourseApi.json(deletion).deleted == true
	deleted = true

	def remainingCourses = CourseApi.courses(cookie)
	def remainingTests = CourseApi.tests(cookie)

	assert !remainingCourses.any {
		it.id == createdId
	} : 'Kategori masih ada setelah penghapusan'

	assert !remainingTests.any {
		it.categoryId == createdId
	} : 'Masih ada tes terkait kategori yang sudah dihapus'

	println('BE4-CRS-07: Technical checks PASSED')
	println('Active test removed by category deletion: true')
	println('Orphaned tests in admin API response: 0')
	println('BUSINESS RULE REVIEW REQUIRED')

} finally {
	if (!deleted) {
		CourseApi.cleanup(cookie, createdId)
	}
}
