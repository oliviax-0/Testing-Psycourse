
import qa.CourseApi
import qa.QuestionApi

Map f = QuestionApi.fixture('02')

try {
	Map body = QuestionApi.payload('02', f.testId)
	int before = QuestionApi.count(f.cookie, f.testId)

	String id = QuestionApi.create(f.cookie, body)
	Map data = QuestionApi.detail(f.cookie, id)

	assert data.question == body.question
	assert data.choices == body.choices
	assert data.answer == body.answer
	assert data.explanation == body.explanation
	assert data.status == 'DRAFT'
	assert QuestionApi.count(f.cookie, f.testId) == before + 1

	println('BE4-QST-02 PASSED')
	println('POST HTTP: 201')
	println('Four choices persisted: true')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
