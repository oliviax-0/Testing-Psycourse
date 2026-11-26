
import qa.CourseApi
import qa.QuestionApi

Map f = QuestionApi.fixture('04')

try {
	Map body = QuestionApi.payload('04', f.testId)
	body.choices = ['Pilihan A', 'Pilihan B']
	body.answer = 1

	int before = QuestionApi.count(f.cookie, f.testId)
	def response = QuestionApi.post(f.cookie, body)

	println('Actual HTTP: ' + response.getStatusCode())
	println('Actual question count: ' +
		QuestionApi.count(f.cookie, f.testId))

	assert response.getStatusCode() in [400, 422] :
		'Soal dua opsi diterima, padahal skenario ' +
		'mensyaratkan empat pilihan'

	assert QuestionApi.count(f.cookie, f.testId) == before

	println('BE4-QST-04 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
