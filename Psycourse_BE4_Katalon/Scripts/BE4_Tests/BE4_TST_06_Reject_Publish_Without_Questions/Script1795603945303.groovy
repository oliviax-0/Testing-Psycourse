
import qa.CourseApi
import qa.TestApi

Map fixture = TestApi.createActiveCategory('06')

String cookie = fixture.cookie
String categoryId = fixture.categoryId

try {
	def test = CourseApi.tests(cookie).find {
		it.categoryId == categoryId &&
		it.type == 'TEST_1'
	}

	assert test != null
	assert test.status == 'DRAFT'
	assert test.questionCount == 0

	println('Before publish: DRAFT, 0 questions')

	def response = CourseApi.request(
		'PATCH',
		'/api/admin/tests/' + test.id,
		cookie,
		[status: 'AKTIF']
	)

	def after = TestApi.findTest(cookie, test.id)

	println(
		'Actual PATCH HTTP Status: ' +
		response.getStatusCode()
	)
	println('Actual test status: ' + after.status)
	println('Question count: ' + after.questionCount)

	// Expected dari Excel:
	// Publikasi tes tanpa bank soal lengkap harus ditolak.
	assert response.getStatusCode() in [400, 409, 422] :
		'POTENTIAL BACKEND DEFECT: Tes tanpa soal ' +
		'dapat dipublikasikan. Actual HTTP ' +
		response.getStatusCode() +
		', status=' + after.status

	assert after.status == 'DRAFT' :
		'Tes berubah menjadi AKTIF meskipun soal belum lengkap'

	println('BE4-TST-06 PASSED')

} finally {
	CourseApi.cleanup(cookie, categoryId)
}
