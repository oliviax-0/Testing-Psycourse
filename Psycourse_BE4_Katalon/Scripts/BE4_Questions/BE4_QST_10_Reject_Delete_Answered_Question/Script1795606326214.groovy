
import qa.CourseApi
import qa.QuestionApi

Map f = QuestionApi.fixture('10')
Map history = null

try {
	Map body = QuestionApi.payload('10', f.testId)
	body.status = 'aktif'

	String questionId = QuestionApi.create(f.cookie, body)

	Map initial = QuestionApi.detail(f.cookie, questionId)
	assert initial.hasAnswers == false

	// Fixture hanya pada PostgreSQL QA lokal.
	// Ini bukan simulasi request API siswa.
	history = QuestionApi.seedHistoricalAnswer(
		f.testId, questionId
	)

	Map withHistory = QuestionApi.detail(
		f.cookie, questionId
	)

	assert withHistory.hasAnswers == true :
		'Fixture riwayat jawaban tidak terdeteksi'

	def response = CourseApi.request(
		'DELETE',
		'/api/admin/questions/' + questionId,
		f.cookie
	)

	println('Actual DELETE HTTP: ' + response.getStatusCode())

	assert response.getStatusCode() == 409 :
		'Soal yang memiliki jawaban siswa berhasil dihapus'

	assert response.getResponseBodyContent()
		.contains('QUESTION_HAS_ANSWERS')

	Map after = QuestionApi.detail(f.cookie, questionId)

	assert after.hasAnswers == true
	assert after.question == initial.question
	assert QuestionApi.count(f.cookie, f.testId) == 1

	println('BE4-QST-10 PASSED')
	println('Referenced question preserved: true')

} finally {
	if (history != null) {
		QuestionApi.removeHistoricalAnswer(history)
	}
	CourseApi.cleanup(f.cookie, f.categoryId)
}
