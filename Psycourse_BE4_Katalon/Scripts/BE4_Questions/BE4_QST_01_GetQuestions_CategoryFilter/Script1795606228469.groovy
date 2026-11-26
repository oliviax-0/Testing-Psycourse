
import qa.CourseApi
import qa.QuestionApi

Map a = null
Map b = null

try {
	a = QuestionApi.fixture('01a')
	b = QuestionApi.fixture('01b')

	String idA = QuestionApi.create(
		a.cookie, QuestionApi.payload('01a', a.testId)
	)
	String idB = QuestionApi.create(
		b.cookie, QuestionApi.payload('01b', b.testId)
	)

	def response = CourseApi.request(
		'GET',
		'/api/admin/questions?categoryId=' + a.categoryId,
		a.cookie
	)

	assert response.getStatusCode() == 200
	def questions = CourseApi.json(response).questions

	println('Actual HTTP: ' + response.getStatusCode())
	println('Returned questions: ' + questions.size())
	println('Other category included: ' +
		questions.any { it.id == idB })

	assert questions.any { it.id == idA }
	assert !questions.any { it.id == idB } :
		'Filter kategori tidak diterapkan'

	assert questions.every { it.kategori == a.judul } :
		'Response memuat soal dari kategori lain'

	println('BE4-QST-01 PASSED')
} finally {
	if (b != null) CourseApi.cleanup(b.cookie, b.categoryId)
	if (a != null) CourseApi.cleanup(a.cookie, a.categoryId)
}
