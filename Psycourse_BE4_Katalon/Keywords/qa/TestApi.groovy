
package qa

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

class TestApi {

	// Membuat kategori QA baru, kemudian mengaktifkannya.
	// Kategori ini otomatis memiliki 3 tes DRAFT.
	static Map createActiveCategory(String caseId) {
		String cookie = CourseApi.loginAdmin()
		Map payload = CourseApi.newCourse("tst-${caseId}")

		String categoryId = null

		try {
			def createResponse = CourseApi.request(
				'POST',
				'/api/admin/courses',
				cookie,
				payload
			)

			WS.verifyResponseStatusCode(createResponse, 201)

			categoryId = CourseApi.json(createResponse).id
			assert categoryId instanceof String && categoryId

			def activateResponse = CourseApi.request(
				'PATCH',
				'/api/admin/courses/' + categoryId,
				cookie,
				[status: 'AKTIF']
			)

			WS.verifyResponseStatusCode(activateResponse, 200)

			def category = CourseApi.courses(cookie).find {
				it.id == categoryId
			}

			assert category != null
			assert category.status == 'AKTIF'

			def related = CourseApi.tests(cookie).findAll {
				it.categoryId == categoryId
			}

			assert related.size() == 3
			assert related.every { it.status == 'DRAFT' }

			return [
				cookie: cookie,
				categoryId: categoryId,
				slug: payload.slug
			]

		} catch (Throwable error) {
			if (categoryId != null) {
				CourseApi.cleanup(cookie, categoryId)
			}
			throw error
		}
	}

	// Menghapus SATU versi tes yang otomatis dibuat
	// pada kategori QA milik test case ini.
	static void removeGeneratedDraft(
		String cookie,
		String categoryId,
		String type
	) {
		assert type in ['TEST_1', 'TEST_2', 'PRACTICE']

		def matching = CourseApi.tests(cookie).findAll {
			it.categoryId == categoryId && it.type == type
		}

		assert matching.size() == 1 :
			'Versi DRAFT untuk fixture tidak ditemukan'

		def test = matching[0]

		assert test.status == 'DRAFT'
		assert test.questionCount == 0
		assert test.attemptCount == 0

		def response = CourseApi.request(
			'DELETE',
			'/api/admin/tests/' + test.id,
			cookie
		)

		WS.verifyResponseStatusCode(response, 200)
		assert CourseApi.json(response).deleted == true

		assert !CourseApi.tests(cookie).any {
			it.id == test.id
		}

		println('Generated QA draft removed: ' + type)
	}

	static Map findTest(String cookie, String testId) {
		def test = CourseApi.tests(cookie).find {
			it.id == testId
		}

		assert test != null :
			'Test tidak ditemukan setelah GET ulang'

		return test
	}
}
