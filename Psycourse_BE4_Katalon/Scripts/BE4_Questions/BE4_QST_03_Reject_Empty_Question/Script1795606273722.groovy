
import qa.CourseApi
import qa.QuestionApi

Map f = QuestionApi.fixture('03')

try {
	Map body = QuestionApi.payload('03', f.testId)
	body.question = ''

	int before = QuestionApi.count(f.cookie, f.testId)
	def response = QuestionApi.post(f.cookie, body)

	println('Actual HTTP: ' + response.getStatusCode())

	assert response.getStatusCode() in [400, 422]
	assert QuestionApi.count(f.cookie, f.testId) == before

	println('BE4-QST-03 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
