
import qa.CourseApi
import qa.QuestionApi

String cookie = CourseApi.loginAdmin()

String missingTestId = 'qa-missing-' +
	UUID.randomUUID().toString().replace('-', '')

Map body = QuestionApi.payload('07', missingTestId)

def beforeIds = CourseApi.json(
	CourseApi.request(
		'GET', '/api/admin/questions', cookie
	)
).questions.collect { it.id }.toSet()

def response = QuestionApi.post(cookie, body)

println('Actual HTTP: ' + response.getStatusCode())

assert response.getStatusCode() in [400, 404, 422] :
	'Target tes tidak tersedia tetapi request diterima'

assert response.getResponseBodyContent()
	.contains('IMPORT_TARGET_UNAVAILABLE')

def afterIds = CourseApi.json(
	CourseApi.request(
		'GET', '/api/admin/questions', cookie
	)
).questions.collect { it.id }.toSet()

assert beforeIds == afterIds :
	'Daftar soal berubah setelah target invalid'

println('BE4-QST-07 technical check PASSED')
println('Original categoryId requirement needs review')
