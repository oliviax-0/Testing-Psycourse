
import qa.CourseApi
import qa.QuestionApi

Map f = QuestionApi.fixture('09')

try {
	String id = QuestionApi.create(
		f.cookie, QuestionApi.payload('09', f.testId)
	)

	Map before = QuestionApi.detail(f.cookie, id)
	assert before.status == 'DRAFT'
	assert before.hasAnswers == false

	def response = CourseApi.request(
		'DELETE',
		'/api/admin/questions/' + id,
		f.cookie
	)

	println('DELETE HTTP: ' + response.getStatusCode())

	assert response.getStatusCode() == 200
	assert CourseApi.json(response).deleted == true

	def after = QuestionApi.get(f.cookie, id)
	assert after.getStatusCode() == 404

	assert QuestionApi.count(f.cookie, f.testId) == 0

	println('BE4-QST-09 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
