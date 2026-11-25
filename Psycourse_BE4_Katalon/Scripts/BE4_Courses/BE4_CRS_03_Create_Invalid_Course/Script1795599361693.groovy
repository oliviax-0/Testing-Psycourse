
import qa.CourseApi

String cookie = CourseApi.loginAdmin()

def beforeIds = CourseApi.courses(cookie)
	.collect { it.id }
	.toSet()

Map emptyName = CourseApi.newCourse('03a')
emptyName.judul = ''

Map missingName = CourseApi.newCourse('03b')
missingName.remove('judul')

[emptyName, missingName].eachWithIndex { payload, index ->

	def response = CourseApi.request(
		'POST', '/api/admin/courses', cookie, payload
	)

	assert response.getStatusCode() in [400, 422] :
		'Invalid payload ke-' + (index + 1) +
		' menghasilkan HTTP ' + response.getStatusCode()

	println('Invalid variant ' + (index + 1) +
		': HTTP ' + response.getStatusCode())
}

def afterIds = CourseApi.courses(cookie)
	.collect { it.id }
	.toSet()

assert beforeIds == afterIds :
	'Ada perubahan daftar course setelah input invalid'

println('BE4-CRS-03 PASSED')
println('No course created from invalid payload')
