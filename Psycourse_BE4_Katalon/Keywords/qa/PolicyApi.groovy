
package qa

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import groovy.json.JsonSlurper
import internal.GlobalVariable
import java.security.MessageDigest
import java.net.HttpURLConnection

class PolicyApi {

	// Request publik tanpa session cookie.
	static Map publicGet(String path) {
		String base = GlobalVariable.baseUrl.toString()
			.replaceAll('/+$', '')

		String url = base + path
		HttpURLConnection conn =
			(HttpURLConnection) new URL(url).openConnection()

		conn.setRequestMethod('GET')
		conn.setInstanceFollowRedirects(false)
		conn.setConnectTimeout(10000)
		conn.setReadTimeout(10000)
		conn.setRequestProperty('Accept', '*/*')

		try {
			int status = conn.getResponseCode()
			def stream = status >= 400 ?
				conn.getErrorStream() :
				conn.getInputStream()

			String body = stream == null ?
				'' : stream.getText('UTF-8')

			return [
				status: status,
				body: body,
				contentType: conn.getContentType() ?: ''
			]
		} finally {
			conn.disconnect()
		}
	}

	static Map publicJson(String path) {
		Map response = publicGet(path)

		assert response.status == 200 :
			'API publik gagal. HTTP ' + response.status

		Map data = new JsonSlurper()
			.parseText(response.body) as Map

		return data
	}

	// Memastikan login ADMIN benar-benar membuat sesi
	// di database QA lokal, bukan database remote.
	static String loginAdminQa() {
		String cookie = CourseApi.loginAdmin()

		def matcher = cookie =~
			/(?:^|;\s*)psycourse_session=([^;\s]+)/

		assert matcher.find() :
			'Cookie session ADMIN tidak ditemukan'

		String token = matcher.group(1)

		String hash = MessageDigest
			.getInstance('SHA-256')
			.digest(token.getBytes('UTF-8'))
			.encodeHex()
			.toString()

		String count = QuestionApi.qaSql(
			"""SELECT COUNT(*)
               FROM public."Session"
               WHERE "tokenHash" = '${hash}'
               AND "expiresAt" > NOW();"""
		)

		assert count == '1' :
			'STOP: Server tidak terbukti menggunakan ' +
			'database QA lokal'

		println('QA local database session verified')

		return cookie
	}

	static List policies(String cookie) {
		def response = CourseApi.request(
			'GET',
			'/api/admin/policies',
			cookie
		)

		WS.verifyResponseStatusCode(response, 200)

		def data = CourseApi.json(response)

		assert data.policies instanceof List
		return data.policies
	}

	static String uniqueVersion() {
		return 'qa-pol-' +
			UUID.randomUUID()
				.toString()
				.replace('-', '')
				.substring(0, 12)
	}

	static Map validContent(String marker) {
		return [
			id: 'Dokumen QA ' + marker +
				'. Ini adalah konten Bahasa Indonesia.',
			en: 'QA Document ' + marker +
				'. This is English content.'
		]
	}

	static Map validPayload(String version) {
		return [
			type: 'KETENTUAN',
			version: version,
			content: validContent(version),
			status: 'DRAFT'
		]
	}

	// Hanya menghapus versi QA yang dibuat oleh test.
	static void cleanupVersion(
		String cookie,
		String version
	) {
		assert version.startsWith('qa-pol-')

		def matches = policies(cookie).findAll {
			it.versi == version &&
			it.jenis == 'KETENTUAN'
		}

		matches.each { policy ->
			def response = CourseApi.request(
				'DELETE',
				'/api/admin/policies/' + policy.id,
				cookie
			)

			WS.verifyResponseStatusCode(response, 200)
		}

		assert !policies(cookie).any {
			it.versi == version &&
			it.jenis == 'KETENTUAN'
		}

		println('Cleanup Policy QA selesai')
	}
}
