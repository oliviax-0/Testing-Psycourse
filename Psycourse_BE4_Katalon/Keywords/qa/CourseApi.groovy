
package qa

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.testobject.TestObjectProperty
import com.kms.katalon.core.testobject.impl.HttpTextBodyContent
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import internal.GlobalVariable

class CourseApi {

	static String baseUrl() {
		String url = GlobalVariable.baseUrl.toString()
			.trim()
			.replaceAll('/+$', '')

		URI uri = new URI(url)

		assert uri.scheme == 'http'
		assert uri.host in ['localhost', '127.0.0.1']
		assert uri.port == 3000 :
			'Courses QA hanya boleh dijalankan pada localhost:3000'

		return url
	}

	static String loginAdmin() {
		def response = WS.sendRequest(
			findTestObject('API/Auth/Login', [
				'email': GlobalVariable.adminEmail,
				'password': GlobalVariable.adminPassword
			])
		)

		WS.verifyResponseStatusCode(response, 200)

		def data = json(response)

		assert data.user?.role == 'ADMIN' :
			'Akun untuk Courses harus memiliki role ADMIN'

		def cookies = response.getHeaderFields()
			.findAll { key, value ->
				key != null && key.equalsIgnoreCase('Set-Cookie')
			}
			.values()
			.flatten()
			.collect { it.toString().split(';')[0].trim() }

		String cookie = cookies.find {
			it.startsWith('psycourse_session=')
		}

		assert cookie != null :
			'Session cookie ADMIN tidak ditemukan'

		assert cookie.length() > 'psycourse_session='.length()

		return cookie
	}

	static ResponseObject request(
		String method,
		String path,
		String cookie,
		Object body = null
	) {
		assert method in ['GET', 'POST', 'PATCH', 'DELETE']
		assert path.startsWith('/api/admin/')
		assert cookie?.startsWith('psycourse_session=')

		RequestObject req = new RequestObject('BE4_CRS_Request')

		req.setRestRequestMethod(method)
		req.setRestUrl(baseUrl() + path)

		req.setHttpHeaderProperties([
			new TestObjectProperty(
				'Cookie',
				ConditionType.EQUALS,
				cookie
			),
			new TestObjectProperty(
				'Content-Type',
				ConditionType.EQUALS,
				'application/json'
			)
		])

		if (body != null) {
			req.setBodyContent(
				new HttpTextBodyContent(
					JsonOutput.toJson(body),
					'UTF-8',
					'application/json'
				)
			)
		}

		return WS.sendRequest(req)
	}

	static Map json(ResponseObject response) {
		def parsed = new JsonSlurper().parseText(
			response.getResponseBodyContent()
		)

		assert parsed instanceof Map :
			'Response harus berupa JSON object'

		return parsed
	}

	static List courses(String cookie) {
		def response = request(
			'GET', '/api/admin/courses', cookie
		)

		WS.verifyResponseStatusCode(response, 200)

		def data = json(response)
		assert data.courses instanceof List

		return data.courses
	}

	static List tests(String cookie) {
		def response = request(
			'GET', '/api/admin/tests', cookie
		)

		WS.verifyResponseStatusCode(response, 200)

		def data = json(response)
		assert data.tests instanceof List

		return data.tests
	}

	static Map newCourse(String caseId) {
		String unique = UUID.randomUUID()
			.toString()
			.replace('-', '')
			.substring(0, 10)

		return [
			judul: "QA Course ${caseId}",
			slug: "qa-crs-${caseId}-${unique}",
			ikon: 'book-open',
			warna: '#2F855A',
			subjudul: 'Data pengujian Courses BE4',
			deskripsi: 'Dibuat otomatis di database QA lokal'
		]
	}

	static void cleanup(String cookie, String courseId) {
		if (!courseId) return

		def response = request(
			'DELETE',
			'/api/admin/courses/' + courseId,
			cookie
		)

		assert response.getStatusCode() == 200 :
			'Cleanup course QA gagal dengan HTTP ' +
			response.getStatusCode()

		assert json(response).deleted == true :
			'Backend tidak mengonfirmasi penghapusan course QA'

		println('Cleanup course QA berhasil')
	}
}
