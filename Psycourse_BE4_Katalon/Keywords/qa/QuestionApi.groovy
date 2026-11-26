
package qa

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.testobject.ResponseObject

class QuestionApi {

    static Map fixture(String code) {
        Map ctx = TestApi.createActiveCategory('qst-' + code)

        try {
            def test = CourseApi.tests(ctx.cookie).find {
                it.categoryId == ctx.categoryId &&
                it.type == 'TEST_1'
            }

            assert test != null
            assert test.status == 'DRAFT'
            assert test.questionCount == 0

            def response = CourseApi.request(
                'PATCH',
                '/api/admin/tests/' + test.id,
                ctx.cookie,
                [status: 'AKTIF']
            )
            WS.verifyResponseStatusCode(response, 200)

            def activated = TestApi.findTest(ctx.cookie, test.id)
            assert activated.status == 'AKTIF'

            ctx.testId = test.id
            ctx.judul = CourseApi.courses(ctx.cookie).find {
                it.id == ctx.categoryId
            }.judul

            return ctx
        } catch (Throwable error) {
            CourseApi.cleanup(ctx.cookie, ctx.categoryId)
            throw error
        }
    }

    static Map payload(String code, String testId) {
        String unique = UUID.randomUUID()
            .toString().replace('-', '').substring(0, 10)

        return [
            testId: testId,
            question: 'QA Question ' + code + ' ' + unique,
            choices: [
                'Pilihan A',
                'Pilihan B',
                'Pilihan C',
                'Pilihan D'
            ],
            answer: 2,
            explanation: 'Pembahasan QA ' + code,
            status: 'draft'
        ]
    }

    static ResponseObject post(String cookie, Map body) {
        return CourseApi.request(
            'POST', '/api/admin/questions', cookie, body
        )
    }

    static String create(String cookie, Map body) {
        def response = post(cookie, body)
        WS.verifyResponseStatusCode(response, 201)

        String id = CourseApi.json(response).id
        assert id
        return id
    }

    static ResponseObject get(String cookie, String id) {
        return CourseApi.request(
            'GET', '/api/admin/questions/' + id, cookie
        )
    }

    static Map detail(String cookie, String id) {
        def response = get(cookie, id)
        WS.verifyResponseStatusCode(response, 200)
        return CourseApi.json(response)
    }

    static int count(String cookie, String testId) {
        def test = TestApi.findTest(cookie, testId)
        return test.questionCount as int
    }

    static String qaSql(String sql) {
        def command = [
            'docker', 'exec', '-i',
            'psycourse-be4-qa-db',
            'psql', '-X', '-A', '-t',
            '-v', 'ON_ERROR_STOP=1',
            '-U', 'psycourse_qa',
            '-d', 'psycourse_be4_qa'
        ]

        def process = new ProcessBuilder(command)
            .redirectErrorStream(true).start()

        process.outputStream.withWriter('UTF-8') {
            it.write(sql)
            it.write('\n')
        }

        String output = process.inputStream
            .getText('UTF-8').trim()

        int exitCode = process.waitFor()

        assert exitCode == 0 :
            'Fixture PostgreSQL QA gagal: ' + output

        return output
    }

    static Map seedHistoricalAnswer(
        String testId,
        String questionId
    ) {
        assert testId ==~ /[a-zA-Z0-9]+/
        assert questionId ==~ /[a-zA-Z0-9]+/

        String attemptId = 'qaattempt' +
            UUID.randomUUID().toString().replace('-', '')

        String answerId = 'qaanswer' +
            UUID.randomUUID().toString().replace('-', '')

        String sql = """
WITH student AS (
    SELECT id
    FROM public."User"
    WHERE email = 'siswa.demo@example.com'
),
new_attempt AS (
    INSERT INTO public."Attempt"
        ("id", "userId", "testId", "status", "totalSoal")
    SELECT
        '${attemptId}', student.id,
        '${testId}', 'BERLANGSUNG', 1
    FROM student
    RETURNING id
),
new_answer AS (
    INSERT INTO public."AttemptAnswer"
        ("id", "attemptId", "questionId", "pilihan")
    SELECT
        '${answerId}', new_attempt.id,
        '${questionId}', 0
    FROM new_attempt
    RETURNING id
)
SELECT COUNT(*) FROM new_answer;
"""

        assert qaSql(sql) == '1' :
            'Fixture tidak membuat tepat satu jawaban'

        return [
            attemptId: attemptId,
            answerId: answerId
        ]
    }

    static void removeHistoricalAnswer(Map history) {
        if (history == null) return

        assert history.answerId ==~ /[a-zA-Z0-9]+/
        assert history.attemptId ==~ /[a-zA-Z0-9]+/

        String sql = """
DELETE FROM public."AttemptAnswer"
WHERE id = '${history.answerId}';

DELETE FROM public."Attempt"
WHERE id = '${history.attemptId}';
"""

        qaSql(sql)
        println('Riwayat fixture QA dibersihkan')
    }
}
