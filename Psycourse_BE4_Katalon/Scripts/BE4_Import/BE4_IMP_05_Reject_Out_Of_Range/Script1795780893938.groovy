import qa.CourseApi
import qa.QuestionApi
import qa.ImportApi

Map f = QuestionApi.fixture('imp-05')
try {
	int before = QuestionApi.count(f.cookie, f.testId)
	String csv = 'question,choices,answer,explanation\n' +
		'QA IMP 05 invalid key,A|B|C|D,99,Pembahasan\n'
	Map result = ImportApi.upload(f.cookie, f.testId, 'bad-answer.csv', csv)
	println('IMP-05 HTTP: ' + result.status)
	assert result.status in [400, 422]
	assert QuestionApi.count(f.cookie, f.testId) == before
	println('BE4-IMP-05 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
