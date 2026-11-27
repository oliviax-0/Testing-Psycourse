import qa.CourseApi
import qa.PolicyApi

import com.kms.katalon.core.annotation.BeforeTestSuite
import com.kms.katalon.core.annotation.AfterTestSuite
import com.kms.katalon.core.context.TestSuiteContext
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

class PolicyPublishedFixtureListener {

	// Hanya ID dan versi dokumen QA yang dibuat listener ini.
	private static final Map<String, Map> created = [:]

	private static boolean isPolicySuite(TestSuiteContext context) {
		return context.getTestSuiteId()?.endsWith('BE4_Policies_Suite')
	}

	@BeforeTestSuite
	def prepare(TestSuiteContext context) {
		if (!isPolicySuite(context)) return

		created.clear()
		String cookie = PolicyApi.loginAdminQa()
		List existing = PolicyApi.policies(cookie)

		try {
			['PRIVASI', 'KETENTUAN'].each { String type ->
				// Jangan mengganti status dokumen terbit milik pengguna.
				if (existing.any {
					it.jenis == type && it.status == 'TERBIT'
				}) {
					println('Sudah ada dokumen TERBIT untuk ' + type +
						'; fixture baru tidak dibuat')
					return
				}

				String version = PolicyApi.uniqueVersion()
				def response = CourseApi.request(
					'POST', '/api/admin/policies', cookie,
					[
						type: type,
						version: version,
						content: PolicyApi.validContent(version),
						status: 'TERBIT'
					]
				)
				WS.verifyResponseStatusCode(response, 201)

				String id = CourseApi.json(response).id
				assert id : 'ID dokumen QA tidak dikembalikan'
				created[type] = [id: id, version: version]
			}

			assert PolicyApi.publicGet('/api/legal/privacy').status == 200 :
				'Dokumen Privacy publik tidak tersedia setelah setup'
			assert PolicyApi.publicGet('/api/legal/terms').status == 200 :
				'Dokumen Terms publik tidak tersedia setelah setup'

			println('Setup POL selesai. Dokumen QA terbit tersedia.')

		} catch (Throwable error) {
			// Jika setup sebagian berhasil, hapus fixture yang sudah dibuat.
			cleanupCreated()
			throw error
		}
	}

	@AfterTestSuite
	def cleanup(TestSuiteContext context) {
		if (!isPolicySuite(context)) return
		cleanupCreated()
	}

	private static void cleanupCreated() {
		if (created.isEmpty()) return

		String cookie = PolicyApi.loginAdminQa()
		List existing = PolicyApi.policies(cookie)

		created.each { String type, Map fixture ->
			def current = existing.find { it.id == fixture.id }
			if (current == null) return

			// Batas pengaman: hanya menghapus dokumen yang cocok
			// dengan ID, tipe, dan versi yang dibuat listener.
			assert current.jenis == type
			assert current.versi == fixture.version
			assert current.versi.startsWith('qa-pol-')

			def response = CourseApi.request(
				'DELETE', '/api/admin/policies/' + fixture.id,
				cookie
			)
			WS.verifyResponseStatusCode(response, 200)
		}

		List remaining = PolicyApi.policies(cookie)
		assert !created.values().any { fixture ->
			remaining.any { it.id == fixture.id }
		} : 'Masih ada dokumen QA sementara setelah cleanup'

		created.clear()
		println('Cleanup POL selesai. Semua dokumen QA sementara dihapus.')
	}
}
