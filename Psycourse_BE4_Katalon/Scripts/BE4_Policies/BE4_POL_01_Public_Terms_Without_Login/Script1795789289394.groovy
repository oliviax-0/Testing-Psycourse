
import qa.PolicyApi

Map page = PolicyApi.publicGet('/terms')

println('POL-01 Page HTTP: ' + page.status)

assert page.status == 200 :
	'Halaman Terms tidak tersedia tanpa login'

assert page.contentType.toLowerCase()
	.contains('text/html') :
	'Response bukan halaman HTML'

assert page.body.toLowerCase()
	.contains('<html') :
	'Konten HTML halaman tidak ditemukan'

// Periksa ketersediaan konten yang diterbitkan.
Map document = PolicyApi.publicJson(
	'/api/legal/terms'
)

assert document.version?.toString()?.trim()
assert document.id instanceof String
assert document.id.trim()

println('Published Terms version available')
println('BE4-POL-01 PASSED')
