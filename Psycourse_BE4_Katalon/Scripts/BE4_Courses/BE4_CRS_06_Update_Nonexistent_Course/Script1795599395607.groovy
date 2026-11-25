
import qa.CourseApi
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = CourseApi.loginAdmin()

def before = CourseApi.courses(cookie)

String missingId = 'qa-missing-' +
	UUID.randomUUID().toString().replace('-', '')

assert !before.any { it.id == missingId }

def response = CourseApi.request(
	'PATCH',
	'/api/admin/courses/' + missingId,
	cookie,
	[judul: 'Should Not Update']
)

WS.verifyResponseStatusCode(response, 404)

def after = CourseApi.courses(cookie)

assert before == after :
	'Data course lain berubah setelah PATCH ID tidak valid'

println('BE4-CRS-06 PASSED')
println('PATCH HTTP Status: ' + response.getStatusCode())
println('Other courses unchanged: true')
