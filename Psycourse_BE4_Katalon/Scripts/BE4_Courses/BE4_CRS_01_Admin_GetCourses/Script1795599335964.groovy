
import qa.CourseApi
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = CourseApi.loginAdmin()

def response = CourseApi.request(
	'GET', '/api/admin/courses', cookie
)

WS.verifyResponseStatusCode(response, 200)

def data = CourseApi.json(response)

assert data.courses instanceof List
assert data.courses.every {
	it.id != null && it.slug != null && it.judul != null
}

def ordering = data.courses.collect { it.urutan }

assert ordering == ordering.sort(false) :
	'Daftar course tidak terurut berdasarkan urutan'

println('BE4-CRS-01 PASSED')
println('HTTP Status: ' + response.getStatusCode())
println('Courses count: ' + data.courses.size())
