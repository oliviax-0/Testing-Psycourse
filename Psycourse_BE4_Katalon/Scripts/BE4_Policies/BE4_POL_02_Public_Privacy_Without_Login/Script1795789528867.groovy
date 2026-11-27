
import qa.PolicyApi

Map page = PolicyApi.publicGet('/privacy')

println('POL-02 Page HTTP: ' + page.status)

assert page.status == 200 :
	'Halaman Privacy tidak tersedia tanpa login'

assert page.contentType.toLowerCase()
	.contains('text/html') :
	'Response bukan halaman HTML'

assert page.body.toLowerCase()
	.contains('<html') :
	'Konten HTML halaman tidak ditemukan'

Map document = PolicyApi.publicJson(
	'/api/legal/privacy'
)

assert document.version?.toString()?.trim()
assert document.id instanceof String
assert document.id.trim()

println('Published Privacy version available')
println('BE4-POL-02 PASSED')
