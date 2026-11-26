
import qa.CourseApi
import qa.QuestionApi

Map f = QuestionApi.fixture('08')

try {
	String id = QuestionApi.create(
		f.cookie, QuestionApi.payload('08', f.testId)
	)

	Map before = QuestionApi.detail(f.cookie, id)
	int beforeCount = QuestionApi.count(f.cookie, f.testId)

	Map update = [
		question: 'QA Question Updated',
		choices: [
			'Opsi baru A',
			'Opsi baru B',
			'Opsi baru C',
			'Opsi baru D'
		],
		answer: 3,
		explanation: 'Pembahasan setelah update',
		status: 'DRAFT'
	]

	def response = CourseApi.request(
		'PATCH',
		'/api/admin/questions/' + id,
		f.cookie,
		update
	)

	println('PATCH HTTP: ' + response.getStatusCode())
	assert response.getStatusCode() == 200

	Map after = QuestionApi.detail(f.cookie, id)

	assert after.id == before.id
	assert after.question == update.question
	assert after.choices == update.choices
	assert after.answer == update.answer
	assert after.explanation == update.explanation
	assert after.status == 'DRAFT'

	assert QuestionApi.count(f.cookie, f.testId) == beforeCount

	println('BE4-QST-08 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
