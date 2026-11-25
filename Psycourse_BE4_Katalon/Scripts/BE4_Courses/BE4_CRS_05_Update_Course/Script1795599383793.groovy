
import qa.CourseApi
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = CourseApi.loginAdmin()
Map payload = CourseApi.newCourse('05')
String createdId = null

try {
	def created = CourseApi.request(
		'POST', '/api/admin/courses', cookie, payload
	)

	WS.verifyResponseStatusCode(created, 201)
	createdId = CourseApi.json(created).id

	def before = CourseApi.courses(cookie).find {
		it.id == createdId
	}

	assert before != null

	Map changes = [
		judul: 'QA Updated Course Title',
		deskripsi: 'Deskripsi sudah diperbarui oleh test CRS-05'
	]

	def patched = CourseApi.request(
		'PATCH',
		'/api/admin/courses/' + createdId,
		cookie,
		changes
	)

	WS.verifyResponseStatusCode(patched, 200)

	def after = CourseApi.courses(cookie).find {
		it.id == createdId
	}

	assert after != null
	assert after.judul == changes.judul
	assert after.deskripsi == changes.deskripsi

	['id', 'slug', 'subjudul', 'ikon', 'warna',
	 'urutan', 'status'].each { field ->
		assert before[field] == after[field] :
			'Field lain berubah tanpa diminta: ' + field
	}

	println('BE4-CRS-05 PASSED')
	println('PATCH HTTP Status: 200')
	println('Updated values persisted: true')
	println('Other metadata unchanged: true')

} finally {
	CourseApi.cleanup(cookie, createdId)
}
