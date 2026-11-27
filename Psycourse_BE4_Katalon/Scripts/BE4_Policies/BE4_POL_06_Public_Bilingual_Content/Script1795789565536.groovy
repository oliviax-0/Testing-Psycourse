
import qa.PolicyApi

[
	'/api/legal/terms',
	'/api/legal/privacy'
].each { path ->

	Map data = PolicyApi.publicJson(path)

	assert data.version?.toString()?.trim() :
		'Versi dokumen tidak tersedia: ' + path

	assert data.id instanceof String &&
		data.id.trim() :
		'Konten Bahasa Indonesia kosong: ' + path

	assert data.en instanceof String &&
		data.en.trim() :
		'Konten Bahasa Inggris kosong: ' + path

	assert data.id.trim() != data.en.trim() :
		'Konten ID dan EN identik: ' + path

	println('Bilingual content available: ' + path)
}

println('BE4-POL-06 API CONTENT CHECK PASSED')
println('UI language switch: NOT VERIFIED')
