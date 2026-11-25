
import qa.CourseApi
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = CourseApi.loginAdmin()
Map payload = CourseApi.newCourse('04')
String createdId = null

try {
	def first = CourseApi.request(
		'POST', '/api/admin/courses', cookie, payload
	)

	WS.verifyResponseStatusCode(first, 201)
	createdId = CourseApi.json(first).id

	Map duplicate = new LinkedHashMap(payload)
	duplicate.judul = 'QA Duplicate Slug Attempt'

	def second = CourseApi.request(
		'POST', '/api/admin/courses', cookie, duplicate
	)

	WS.verifyResponseStatusCode(second, 409)

	def matching = CourseApi.courses(cookie).findAll {
		it.slug == payload.slug
	}

	assert matching.size() == 1 :
		'Ditemukan lebih dari satu course dengan slug sama'

	assert matching[0].id == createdId

	println('BE4-CRS-04 PASSED')
	println('Original create: HTTP 201')
	println('Duplicate create: HTTP 409')
	println('Matching slug records: ' + matching.size())

} finally {
	CourseApi.cleanup(cookie, createdId)
}
