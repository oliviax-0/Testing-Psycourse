
import qa.PolicyApi
import qa.CourseApi

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

String cookie = PolicyApi.loginAdminQa()
String version = PolicyApi.uniqueVersion()

try {
	List before = PolicyApi.policies(cookie)
	Map payload = PolicyApi.validPayload(version)

	def response = CourseApi.request(
		'POST',
		'/api/admin/policies',
		cookie,
		payload
	)

	println('POL-04 POST HTTP: ' +
		response.getStatusCode())

	WS.verifyResponseStatusCode(response, 201)

	String id = CourseApi.json(response).id
	assert id

	List after = PolicyApi.policies(cookie)

	def created = after.find {
		it.id == id
	}

	assert created != null
	assert created.jenis == 'KETENTUAN'
	assert created.versi == version
	assert created.status == 'DRAFT'
	assert created.contentId == payload.content.id
	assert created.contentEn == payload.content.en

	assert after.size() == before.size() + 1

	println('New version persisted: true')
	println('BE4-POL-04 PASSED')

} finally {
	PolicyApi.cleanupVersion(cookie, version)
}
