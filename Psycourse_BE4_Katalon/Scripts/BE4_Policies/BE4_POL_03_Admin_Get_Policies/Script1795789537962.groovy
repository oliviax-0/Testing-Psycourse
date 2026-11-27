
import qa.PolicyApi

String cookie = PolicyApi.loginAdminQa()

List policies = PolicyApi.policies(cookie)

assert policies.size() > 0 :
	'Daftar kebijakan kosong'

assert policies.every { policy ->
	policy.id &&
	policy.jenis in ['PRIVASI', 'KETENTUAN'] &&
	policy.versi &&
	policy.status in [
		'DRAFT', 'DITINJAU', 'TERBIT'
	] &&
	policy.containsKey('contentId') &&
	policy.containsKey('contentEn')
}

println('POL-03 GET HTTP: 200')
println('Total policies: ' + policies.size())
println('BE4-POL-03 PASSED')
