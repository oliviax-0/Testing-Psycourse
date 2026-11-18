-- Data uji lokal (idempotent). Test 1 & Test 2 kategori Logika diberi 5 soal AKTIF.
UPDATE "Question" SET status='AKTIF' WHERE "testId"=(SELECT id FROM "Test" WHERE slug='logika-test-1');
INSERT INTO "Question"(id,"testId",urutan,teks,pilihan,"kunciJawaban",penjelasan,status,"updatedAt")
SELECT md5(random()::text||q.id), (SELECT id FROM "Test" WHERE slug='logika-test-2'), q.urutan, q.teks, q.pilihan, q."kunciJawaban", q.penjelasan, 'AKTIF', now()
FROM "Question" q WHERE q."testId"=(SELECT id FROM "Test" WHERE slug='logika-test-1')
ON CONFLICT ("testId",urutan) DO NOTHING;
UPDATE "Test" SET "timerDetik"=600 WHERE slug='logika-test-2';
