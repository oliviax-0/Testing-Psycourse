import qa.CourseApi
import qa.QuestionApi
import qa.ImportApi

Map f = QuestionApi.fixture('imp-03')
try {
	int before = QuestionApi.count(f.cookie, f.testId)
	Map result = ImportApi.upload(f.cookie, f.testId, 'empty.csv', '')
	println('IMP-03 HTTP: ' + result.status)
	assert result.status in [400, 422]
	assert QuestionApi.count(f.cookie, f.testId) == before
	println('BE4-IMP-03 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
