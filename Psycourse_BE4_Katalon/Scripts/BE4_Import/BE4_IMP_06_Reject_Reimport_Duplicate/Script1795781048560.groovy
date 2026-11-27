import qa.CourseApi
import qa.QuestionApi
import qa.ImportApi

Map f = QuestionApi.fixture('imp-06')
try {
	int before = QuestionApi.count(f.cookie, f.testId)
	String marker = UUID.randomUUID().toString().substring(0, 8)
	String csv = 'question,choices,answer,explanation\n' +
		'QA IMP 06 ' + marker + ',A|B|C|D,2,Pembahasan\n'

	Map first = ImportApi.upload(f.cookie, f.testId, 'first.csv', csv)
	assert first.status == 201
	assert first.body.imported == 1
	assert QuestionApi.count(f.cookie, f.testId) == before + 1

	Map second = ImportApi.upload(f.cookie, f.testId, 'first.csv', csv)
	println('IMP-06 first HTTP: ' + first.status)
	println('IMP-06 repeat HTTP: ' + second.status)
	assert second.status == 409
	assert second.raw.contains('DUPLICATE_QUESTION')
	assert QuestionApi.count(f.cookie, f.testId) == before + 1
	println('BE4-IMP-06 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
