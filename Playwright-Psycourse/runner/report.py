"""Gabungkan hasil runner + spec menjadi nilai kolom F:H spreadsheet dan evidence.html."""
import json, os, html
HERE = os.path.dirname(os.path.abspath(__file__)); R = f"{HERE}/../evidence"
tcs = {t["id"]: t for t in json.load(open(f"{HERE}/spec/tcs.json"))}
res = {r["id"]: r for r in json.load(open(f"{R}/pw-results.json"))}
ORDER = [("NAV",8),("REG",15),("OTP",10),("WEL",2),("LGN",12),("RST",7),("DSB",5),("AKS",13),("KAT",7),("TES",15),("HSL",5),("PKT",11),("SET",8),("ADM",7),("BHS",4)]
ids = [f"{s}_{i:03d}" for s, n in ORDER for i in range(1, n + 1)]
rows, pos, row = [], {}, 3
MANUAL = {"PKT_008": [2, "Blocked", "Butuh simulator Midtrans Sandbox (layanan eksternal) dan penyelesaian pembayaran manual; tidak diotomasi."],
          "PKT_009": [1, "Blocked", "Butuh server tanpa kunci Midtrans; konfigurasi lingkungan terpisah, tidak diotomasi."]}
CUR = {"NAV_007": "scrollWidth 451 lebih besar dari 375: ada scroll horizontal di beranda mobile.",
       "AKS_013": "Cookie sesi palsu tidak diarahkan ke /login; /dashboard tetap tampil/error server.",
       "KAT_006": "Kartu Test 1 tidak menampilkan '5 soal' (jumlah soal aktif = 5).",
       "TES_005": "Tes langsung terkirim dengan skor 0 dan pindah ke /results/..., tanpa konfirmasi soal belum dijawab.",
       "TES_014": "Test 1 dibuka lagi tanpa pemberitahuan bahwa percobaan resmi sudah dipakai.",
       "PKT_005": "Halaman diam-diam menampilkan Premium Access, tidak ada pesan paket tidak ditemukan.",
       "ADM_004": "Akun siswa tetap bisa membuka /admin/dashboard (tidak diarahkan ke /admin).",
       "ADM_006": "/admin/abc menampilkan halaman 'Kelola kategori', bukan 404."}
for tid in ids:
    if tid in MANUAL:
        n, st, why = MANUAL[tid]; pos[tid] = (row, n)
        for k in range(1, n + 1): rows.append([why, st, ""])
        row += n; continue
    t, r = tcs[tid], res[tid]
    n = max(s["r"] for s in t["steps"])
    pos[tid] = (row, n)
    for k in range(1, n + 1):
        f = [x for x in r["fails"] if x.get("row") == k]
        shot = next((s["file"] for s in r["shots"] if s["row"] == k), "")
        actual = CUR.get(tid, "; ".join(x["actual"] for x in f)) if f else "Sesuai expected result."
        rows.append([actual, "Fail" if f else "Pass", shot])
    row += n
json.dump({"rows": rows, "pos": pos, "last": row - 1}, open(f"{R}/sheet-values.json", "w"), ensure_ascii=False)
# evidence.html
out = ["<meta charset=utf-8><title>Bukti Pengujian Psycourse</title><style>body{font-family:sans-serif;max-width:1000px;margin:24px auto}.f{color:#b42318}.p{color:#067647}img{max-width:480px;border:1px solid #ccc;margin:4px}</style><h1>Bukti Pengujian FrontEnd Psycourse</h1>"]
p = sum(1 for r in res.values() if r["status"] == "Passed")
out.append(f"<p>Total {len(res)} TC: <b class=p>{p} Passed</b>, <b class=f>{len(res)-p} Failed</b></p>")
for tid in [i for i in ids if i in res]:
    r = res[tid]; cls = "p" if r["status"] == "Passed" else "f"
    out.append(f"<h3 class={cls}>{tid} · {html.escape(tcs[tid]['title'])} · {r['status']}</h3>")
    for x in r["fails"]: out.append(f"<div class=f>Baris {x.get('row')}: {html.escape(x['actual'])}</div>")
    for s in r["shots"]: out.append(f"<a href='screenshots/{s['file']}'><img loading=lazy src='screenshots/{s['file']}' title='baris {s['row']}'></a>")
open(f"{R}/evidence.html", "w").write("\n".join(out))
print(len(rows), "baris; last row", row - 1)
print({k: v[0] for k, v in pos.items() if k.endswith("_001")})
