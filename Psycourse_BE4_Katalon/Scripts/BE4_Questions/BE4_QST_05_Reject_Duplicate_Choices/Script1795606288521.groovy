
import qa.CourseApi
import qa.QuestionApi

Map f = QuestionApi.fixture('05')

try {
	Map body = QuestionApi.payload('05', f.testId)
	body.choices = [
		'Jawaban sama',
		'Jawaban sama',
		'Pilihan C',
		'Pilihan D'
	]

	int before = QuestionApi.count(f.cookie, f.testId)
	def response = QuestionApi.post(f.cookie, body)

	println('Actual HTTP: ' + response.getStatusCode())

	assert response.getStatusCode() in [400, 422] :
		'Pilihan jawaban identik diterima backend'

	assert QuestionApi.count(f.cookie, f.testId) == before

	println('BE4-QST-05 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
