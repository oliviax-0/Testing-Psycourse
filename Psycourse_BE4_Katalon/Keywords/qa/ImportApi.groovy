package qa

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import groovy.json.JsonSlurper

class ImportApi {

	// Mengirim multipart/form-data asli. CSV dibuat di memori,
	// tanpa menyimpan file atau menampilkan session cookie.
	static Map upload(String cookie, String testId,
					  String filename, String csvText) {
		assert cookie?.startsWith('psycourse_session=')
		assert testId ==~ /[A-Za-z0-9_-]+/
		assert filename ==~ /[A-Za-z0-9._-]+/
		assert csvText != null

		String boundary = '----PsyCourseQA' +
			UUID.randomUUID().toString().replace('-', '')
		ByteArrayOutputStream multipart = new ByteArrayOutputStream()
		def write = { String s ->
			multipart.write(s.getBytes(StandardCharsets.UTF_8))
		}

		write('--' + boundary + '\r\n')
		write('Content-Disposition: form-data; name="testId"\r\n\r\n')
		write(testId + '\r\n')

		write('--' + boundary + '\r\n')
		write('Content-Disposition: form-data; name="file"; filename="' +
			  filename + '"\r\n')
		write('Content-Type: text/csv; charset=UTF-8\r\n\r\n')
		multipart.write(csvText.getBytes(StandardCharsets.UTF_8))
		write('\r\n--' + boundary + '--\r\n')

		String url = CourseApi.baseUrl() + '/api/admin/questions/import'
		HttpURLConnection connection =
			(HttpURLConnection) new URL(url).openConnection()

		try {
			connection.setRequestMethod('POST')
			connection.setInstanceFollowRedirects(false)
			connection.setConnectTimeout(10000)
			connection.setReadTimeout(30000)
			connection.setDoOutput(true)
			connection.setRequestProperty('Cookie', cookie)
			connection.setRequestProperty('Accept', 'application/json')
			connection.setRequestProperty('Content-Type',
				'multipart/form-data; boundary=' + boundary)
			connection.setFixedLengthStreamingMode(multipart.size())

			connection.outputStream.withCloseable { stream ->
				multipart.writeTo(stream)
			}

			int status = connection.responseCode
			InputStream responseStream = status >= 400 ?
				connection.errorStream : connection.inputStream
			String raw = responseStream == null ? '' :
				responseStream.getText('UTF-8')
			responseStream?.close()

			Map json = [:]
			if (raw.trim()) {
				def parsed = new JsonSlurper().parseText(raw)
				assert parsed instanceof Map : 'Response bukan JSON object'
				json = parsed as Map
			}
			return [status: status, body: json, raw: raw]
		} finally {
			connection.disconnect()
		}
	}

	static Set<String> questionIds(String cookie) {
		def response = CourseApi.request(
			'GET', '/api/admin/questions', cookie)
		assert response.getStatusCode() == 200
		def questions = CourseApi.json(response).questions
		assert questions instanceof List
		return questions.collect { it.id.toString() }.toSet()
	}
}
