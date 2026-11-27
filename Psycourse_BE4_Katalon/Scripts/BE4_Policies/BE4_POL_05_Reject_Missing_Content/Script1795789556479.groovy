
import qa.PolicyApi
import qa.CourseApi

String cookie = PolicyApi.loginAdminQa()
String version = PolicyApi.uniqueVersion()

try {
	List before = PolicyApi.policies(cookie)

	Map invalidPayload = [
		type: 'KETENTUAN',
		version: version,
		status: 'DRAFT'
		// Field content sengaja tidak dikirim.
	]

	def response = CourseApi.request(
		'POST',
		'/api/admin/policies',
		cookie,
		invalidPayload
	)

	int actualStatus = response.getStatusCode()

	println('POL-05 POST HTTP: ' + actualStatus)

	List after = PolicyApi.policies(cookie)

	assert actualStatus in [400, 422] :
		'Dokumen tanpa konten diterima backend. ' +
		'Actual HTTP: ' + actualStatus

	assert after.size() == before.size() :
		'Jumlah dokumen berubah setelah input invalid'

	assert !after.any {
		it.versi == version &&
		it.jenis == 'KETENTUAN'
	}

	println('No invalid policy persisted: true')
	println('BE4-POL-05 PASSED')

} finally {
	// Jika terjadi defect dan dokumen tetap dibuat,
	// hanya data milik QA yang dibersihkan.
	PolicyApi.cleanupVersion(cookie, version)
}
