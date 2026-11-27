import qa.CourseApi
import qa.QuestionApi
import qa.ImportApi

Map f = QuestionApi.fixture('imp-07')
try {
	int before = QuestionApi.count(f.cookie, f.testId)
	Set idsBefore = ImportApi.questionIds(f.cookie)
	String marker = UUID.randomUUID().toString().substring(0, 8)
	// Baris pertama valid; baris kedua memiliki answer di luar rentang.
	String csv = 'question,choices,answer,explanation\n' +
		'QA IMP 07 good ' + marker + ',A|B|C|D,2,Pembahasan\n' +
		'QA IMP 07 bad ' + marker + ',A|B|C|D,99,Pembahasan\n'

	Map result = ImportApi.upload(f.cookie, f.testId, 'mixed.csv', csv)
	println('IMP-07 HTTP: ' + result.status)
	assert result.status in [400, 422] :
		'File campuran seharusnya ditolak secara atomik'
	assert QuestionApi.count(f.cookie, f.testId) == before
	assert ImportApi.questionIds(f.cookie) == idsBefore
	println('BE4-IMP-07 PASSED (atomic: no rows imported)')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
