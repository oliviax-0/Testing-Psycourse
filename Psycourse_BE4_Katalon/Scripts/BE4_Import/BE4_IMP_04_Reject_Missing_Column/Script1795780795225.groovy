import qa.CourseApi
import qa.QuestionApi
import qa.ImportApi

Map f = QuestionApi.fixture('imp-04')
try {
	int before = QuestionApi.count(f.cookie, f.testId)
	// Kolom wajib answer sengaja tidak ada.
	String csv = 'question,choices,explanation\n' +
		'QA IMP 04 missing answer,A|B|C|D,Pembahasan\n'
	Map result = ImportApi.upload(f.cookie, f.testId, 'missing.csv', csv)
	println('IMP-04 HTTP: ' + result.status)
	assert result.status in [400, 422]
	assert QuestionApi.count(f.cookie, f.testId) == before
	println('BE4-IMP-04 PASSED')
} finally {
	CourseApi.cleanup(f.cookie, f.categoryId)
}
