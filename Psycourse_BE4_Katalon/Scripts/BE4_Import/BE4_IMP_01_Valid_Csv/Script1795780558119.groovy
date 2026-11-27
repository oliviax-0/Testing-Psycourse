import qa.CourseApi
import qa.QuestionApi
import qa.ImportApi

Map f = QuestionApi.fixture('imp-01')
try {
	def targets = CourseApi.json(CourseApi.request(
		'GET', '/api/admin/questions/import', f.cookie)).targets
	assert targets.any { it.id == f.testId }

	int before = QuestionApi.count(f.cookie, f.testId)
	Set idsBefore = ImportApi.questionIds(f.cookie)
	String marker = UUID.randomUUID().toString().substring(0, 8)
	String q1 = 'QA IMP 01 A ' + marker
	String q2 = 'QA IMP 01 B ' + marker
	String csv = 'question,choices,answer,explanation,status\n' +
		q1 + ',A|B|C|D,2,Pembahasan A,draft\n' +
		q2 + ',A|B|C|D,3,Pembahasan B,draft\n'

	Map result = ImportApi.upload(f.cookie, f.testId, 'valid.csv', csv)
	println('IMP-01 HTTP: ' + result.status)
	println('IMP-01 imported: ' + result.body.imported)
	assert result.status == 201
	assert result.body.imported == 2
	assert QuestionApi.count(f.cookie, f.testId) == before + 2

	Set newIds = ImportApi.questionIds(f.cookie) - idsBefore
	assert newIds.size() == 2
	def details = newIds.collect { QuestionApi.detail(f.cookie, it.toString()) }
	assert details.collect { it.question }.toSet() == [q1, q2].toSet()
	assert details.every { it.choices == ['A','B','C','D'] }
	assert details.collect { it.answer }.toSet() == [2, 3].toSet()
	println('BE4-IMP-01 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
