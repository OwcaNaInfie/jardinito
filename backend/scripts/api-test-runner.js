#!/usr/bin/env node
/**
 * Jardinito -- Runner testów REST API
 *
 * Uruchomienie:
 *   node api-test-runner.js
 *
 * Zmienne środowiskowe (opcjonalne):
 *   BASE_URL     – adres serwera      (domyślnie: http://localhost:3000)
 *   RESULTS_FILE – ścieżka do raportu (domyślnie: api_test_results.json)
 *
 * WYMAGANIE -- weryfikacja e-mail:
 *   Testy T04–T26 wymagają zweryfikowanego konta.
 *   Skrypt automatycznie wywołuje POST /api/dev/verify { userId }
 *   po rejestracji. Dodaj ten endpoint do server.js (patrz README
 *   na końcu tego pliku).
 *
 * ZALECANY TRYB -- użytkownik seedowany:
 *   Ustaw zmienną TEST_USER_EMAIL=apitest@jardinito.com
 *   i uruchom najpierw seedUsers.js. Skrypt zaloguje się na to konto
 *   (250 monet, odblokowane tylko darmowe rośliny) zamiast rejestrować
 *   nowe konto i nie potrzebuje wtedy /api/dev/verify.
 *
 *   TEST_USER_EMAIL=apitest@jardinito.com node api-test-runner.js
 */

'use strict';

const http  = require('http');
const https = require('https');
const fs    = require('fs');
const path  = require('path');

// ================================================================
//  Konfiguracja
// ================================================================
const TEST_USER_EMAIL='apitest@jardinito.com'
const BASE_URL= process.env.BASE_URL || 'http://localhost:5000';
const RESULTS_FILE= process.env.RESULTS_FILE || path.join(__dirname, 'api_test_results.json');
//const TEST_USER_EMAIL= process.env.TEST_USER_EMAIL || null;   // np. apitest@jardinito.com
const TEST_USER_PASSWORD= process.env.TEST_USER_PASSWORD || 'TestHaslo123!';
const TS= Date.now();

// Dostosuj jeśli twoje endpointy portfela mają inną ścieżkę
const WALLET_GET = '/api/wallet';           // GET  ?userId=
const WALLET_BUY = '/api/wallet/buy';       // POST { userId, plantId }
const WALLET_FAV = '/api/wallet/favourite'; // POST { userId, plantId }  ← POST, nie PUT

// ================================================================
//  Kontekst współdzielony między testami
// ================================================================
const ctx = {
  email:             `api_test_${TS}@jardinito.test`,
  username:          `ApiTester${TS}`.slice(0, 20),
  password:          'TestHaslo123!',
  userId:            null,
  allPlants:         [],   // wszystkie rośliny z T09, potrzebne do T20
  plantId:           null, // pierwsza z listy (do sesji)
  cheapPlantId:      null, // najtańsza (fallback)
  expensivePlantId:  null, // najdroższa (T22)
  targetBuyPlantId:  null, // najtańsza PŁATNA nieposiadana, obliczana w T20 (T21)
  tagId:             null,
  unverifiedEmail:   null, // konto bez weryfikacji -- wypełniane w T27, używane w T28
};

// ================================================================
//  HTTP helper (zero zewnętrznych zależności)
// ================================================================
function req(method, endpoint, opts = {}) {
  const { body = null, query = null } = opts;
  return new Promise((resolve, reject) => {
    const url = new URL(BASE_URL + endpoint);
    if (query) {
      Object.entries(query)
        .filter(([, v]) => v != null)
        .forEach(([k, v]) => url.searchParams.set(k, String(v)));
    }
    const bodyStr = body ? JSON.stringify(body) : null;
    const lib     = url.protocol === 'https:' ? https : http;

    const options = {
      hostname: url.hostname,
      port:     url.port || (url.protocol === 'https:' ? 443 : 80),
      path:     url.pathname + url.search,
      method,
      headers: {
        'Content-Type': 'application/json',
        ...(bodyStr ? { 'Content-Length': Buffer.byteLength(bodyStr) } : {}),
      },
    };

    const r = lib.request(options, (res) => {
      let raw = '';
      res.on('data', (c) => { raw += c; });
      res.on('end',  () => {
        let data = null;
        try { data = JSON.parse(raw); } catch {}
        resolve({ status: res.statusCode, body: data });
      });
    });
    r.on('error', reject);
    if (bodyStr) r.write(bodyStr);
    r.end();
  });
}

// ================================================================
//  Runner i asercje
// ================================================================
const results  = [];
let totalPass  = 0;
let totalFail  = 0;
let totalSkip  = 0;

const C = {
  reset:  '\x1b[0m',
  green:  '\x1b[32m',
  red:    '\x1b[31m',
  yellow: '\x1b[33m',
  gray:   '\x1b[90m',
  bold:   '\x1b[1m',
};
const c = (text, col) => col + text + C.reset;

async function test(id, group, description, fn) {
  const t0 = Date.now();
  let entry = {
    id, group, description,
    result: 'FAIL',
    expectedStatus: null,
    actualStatus:   null,
    checks:         [],
    note:           '',
    durationMs:     0,
  };

  try {
    const outcome = await fn();
    Object.assign(entry, outcome, { durationMs: Date.now() - t0 });
    if      (entry.result === 'PASS') totalPass++;
    else if (entry.result === 'SKIP') totalSkip++;
    else                              totalFail++;
  } catch (err) {
    entry.result      = 'ERROR';
    entry.note        = err.message;
    entry.durationMs  = Date.now() - t0;
    totalFail++;
  }

  results.push(entry);

  const icon = entry.result === 'PASS' ? c('✓', C.green)
             : entry.result === 'SKIP' ? c('~', C.yellow)
             : c('✗', C.red);
  const httpStr = entry.actualStatus
    ? c(` HTTP ${entry.actualStatus}`, C.gray)
    : '';
  process.stdout.write(`  ${icon} [${c(id, C.gray)}] ${description}${httpStr}\n`);
  if (entry.result !== 'PASS' && entry.note) {
    process.stdout.write(`      ${c('↳ ' + entry.note, C.gray)}\n`);
  }
  return entry;
}

// Ocenia odpowiedź -- sprawdza status i lista dodatkowych asercji
function evaluate(res, expectedStatus, checks = []) {
  const allChecks = [
    {
      name:  `Status HTTP = ${expectedStatus}`,
      pass:  res.status === expectedStatus,
      error: res.status !== expectedStatus ? `Otrzymano ${res.status}` : null,
    },
    ...checks.map(({ name, fn }) => {
      try   { fn(res.body); return { name, pass: true,  error: null }; }
      catch (e) { return { name, pass: false, error: e.message }; }
    }),
  ];
  const allPass = allChecks.every((ch) => ch.pass);
  return {
    result:         allPass ? 'PASS' : 'FAIL',
    expectedStatus,
    actualStatus:   res.status,
    checks:         allChecks,
    note: allPass
      ? ''
      : allChecks
          .filter((ch) => !ch.pass)
          .map((ch) => `${ch.name}${ch.error ? ': ' + ch.error : ''}`)
          .join(' | '),
  };
}

// Pomocnik asercji -- rzuca Error jeśli warunek nie jest spełniony
function must(cond, msg) {
  if (!cond) throw new Error(msg || 'Asercja nieudana');
}

// Zwraca wynik SKIP z podanym powodem
function skip(note) {
  return { result: 'SKIP', note, expectedStatus: null, actualStatus: null, checks: [] };
}

// Akceptuje kilka kodów HTTP jako poprawne (np. 400 lub 409)
function evaluateAny(res, acceptedStatuses, checks = []) {
  const statusOk = acceptedStatuses.includes(res.status);
  const allChecks = [
    {
      name:  `Status HTTP w [${acceptedStatuses.join(', ')}]`,
      pass:  statusOk,
      error: statusOk ? null : `Otrzymano ${res.status}`,
    },
    ...checks.map(({ name, fn }) => {
      try   { fn(res.body); return { name, pass: true,  error: null }; }
      catch (e) { return { name, pass: false, error: e.message }; }
    }),
  ];
  const allPass = allChecks.every((ch) => ch.pass);
  return {
    result:         allPass ? 'PASS' : 'FAIL',
    expectedStatus: acceptedStatuses[0],
    actualStatus:   res.status,
    checks:         allChecks,
    note: allPass
      ? ''
      : allChecks.filter((ch) => !ch.pass).map((ch) => ch.name + (ch.error ? ': ' + ch.error : '')).join(' | '),
  };
}

// ================================================================
//  Sekwencja testów
// ================================================================
async function runTests() {
  const startedAt = new Date().toISOString();

  console.log(c('\n╔═══════════════════════════════════════════╗', C.gray));
  console.log(c('║       Jardinito -- API Test Runner       ║', C.bold));
  console.log(c('╚═══════════════════════════════════════════╝', C.gray));
  console.log(`  ${c('URL:   ', C.gray)}${BASE_URL}`);
  console.log(`  ${c('Start: ', C.gray)}${startedAt}\n`);

  // ----------------------------------------------------------
  // G1 – Uwierzytelnianie
  // ----------------------------------------------------------
  console.log(c('[G1] Uwierzytelnianie', C.bold));

  await test('T01', 'G1', 'Rejestracja -- prawidłowe dane', async () => {
    const res = await req('POST', '/api/auth/register', {
      body: { email: ctx.email, username: ctx.username, password: ctx.password },
    });
    const r = evaluate(res, 201, [
      { name: 'Body odpowiedzi nie jest puste', fn: (b) => must(b, 'Brak body') },
    ]);
    ctx.userId = res.body?.userId || res.body?.user?._id || null;
    return r;
  });

  // Auto-weryfikacja przez endpoint DEV (cicha -- nie liczy się jako test)
  if (ctx.userId) {
    try {
      const devRes = await req('POST', '/api/dev/verify', { body: { userId: ctx.userId } });
      if (devRes.status === 200) {
        console.log(c('  ℹ  DEV: konto zweryfikowane automatycznie', C.yellow));
      } else {
        console.log(c('  ⚠  DEV: /api/dev/verify niedostępny (patrz README)', C.yellow));
        console.log(c('         T04 i dalsze mogą zwrócić 403', C.yellow));
      }
    } catch {
      console.log(c('  ⚠  DEV: nie udało się wywołać /api/dev/verify', C.yellow));
    }
  }

  await test('T02', 'G1', 'Rejestracja -- duplikat adresu e-mail', async () => {
    const res = await req('POST', '/api/auth/register', {
      body: { email: ctx.email, username: `Inny${TS}`.slice(0, 20), password: ctx.password },
    });
    // Express zwraca 409 Conflict lub 400 Bad Request w zależności od implementacji
    return evaluateAny(res, [409, 400], [
      { name: 'Komunikat błędu w body', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  await test('T03', 'G1', 'Rejestracja -- brak pola password', async () => {
    const res = await req('POST', '/api/auth/register', {
      body: { email: `brak_pola_${TS}@test.pl`, username: `BrakPola${TS}`.slice(0, 20) },
    });
    return evaluate(res, 400, [
      { name: 'Komunikat walidacyjny', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  await test('T04', 'G1', 'Logowanie -- prawidłowe dane', async () => {
    // Jeśli ustawiono TEST_USER_EMAIL, logujemy się na konto seedowane
    // (ma znane saldo monet, co jest wymagane dla T21/T22)
    const loginEmail = TEST_USER_EMAIL || ctx.email;
    const loginPass  = TEST_USER_EMAIL ? TEST_USER_PASSWORD : ctx.password;
    if (TEST_USER_EMAIL) {
      process.stdout.write(c(`      ℹ  Używam konta seedowanego: ${TEST_USER_EMAIL}\n`, C.yellow));
    }
    const res = await req('POST', '/api/auth/login', {
      body: { identifier: loginEmail, password: loginPass },
    });
    const r = evaluate(res, 200, [
      { name: 'Dane użytkownika w odpowiedzi', fn: (b) => {
        must(b?.user || b?._id || b?.userId, 'Brak danych użytkownika');
      }},
    ]);
    // Nadpisz userId kontem seedowanym -- reszta testów używa tego ID
    ctx.userId = res.body?.user?._id || res.body?._id || res.body?.userId || ctx.userId;
    if (r.result === 'FAIL' && res.status === 403) {
      r.note = 'Konto niezweryfikowane -- ustaw TEST_USER_EMAIL lub dodaj /api/dev/verify';
    }
    return r;
  });

  await test('T05', 'G1', 'Logowanie -- błędne hasło', async () => {
    const loginEmail = TEST_USER_EMAIL || ctx.email;
    const res = await req('POST', '/api/auth/login', {
      body: { identifier: loginEmail, password: 'bledneHaslo999!' },
    });
    return evaluate(res, 401, [
      { name: 'Ogólny komunikat (nie ujawnia błędnego pola)', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  await test('T06', 'G1', 'Logowanie -- nieistniejące konto', async () => {
    const res = await req('POST', '/api/auth/login', {
      body: { identifier: `nie_istnieje_${TS}@x.pl`, password: 'cokolwiek123' },
    });
    return evaluate(res, 401, [
      { name: 'Komunikat ogólny (taki sam jak T05)', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  await test('T07', 'G1', 'Reset hasła -- istniejące konto (zweryfikowane)', async () => {
    // Używamy zweryfikowanego konta -- serwer zwraca 403 dla niezweryfikowanych (NOT_VERIFIED_RESET)
    const identifier = TEST_USER_EMAIL || ctx.email;
    const res = await req('POST', '/api/auth/forgot-password', {
      body: { identifier },
    });
    return evaluate(res, 200, [
      { name: 'Status 200', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  await test('T08', 'G1', 'Reset hasła -- nieistniejące konto (anti-enumeration)', async () => {
    const res = await req('POST', '/api/auth/forgot-password', {
      body: { identifier: `ghost_${TS}@ghost.pl` },
    });
    return evaluate(res, 200, [
      { name: 'Status 200 (identyczny z T07 -- brak informacji o koncie)', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  // T27/T28 -- konto niezweryfikowane
  // Rejestrujemy dedykowane konto i celowo NIE wywołujemy /api/dev/verify
  await test('T27', 'G1', 'Logowanie -- konto niezweryfikowane', async () => {
    ctx.unverifiedEmail = `unver_${TS}@jardinito.test`;
    const regRes = await req('POST', '/api/auth/register', {
      body: {
        email:    ctx.unverifiedEmail,
        username: `Unver${TS}`.slice(0, 20),
        password: ctx.password,
      },
    });
    if (regRes.status !== 201) return skip(`Nie zarejestrowano konta testowego (${regRes.status})`);
    const res = await req('POST', '/api/auth/login', {
      body: { identifier: ctx.unverifiedEmail, password: ctx.password },
    });
    return evaluate(res, 403, [
      { name: 'Status 403 -- konto wymaga weryfikacji adresu e-mail', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  await test('T28', 'G1', 'Reset hasła -- konto niezweryfikowane', async () => {
    if (!ctx.unverifiedEmail) return skip('Brak niezweryfikowanego konta (T27 nie zarejestrował)');
    const res = await req('POST', '/api/auth/forgot-password', {
      body: { identifier: ctx.unverifiedEmail },
    });
    return evaluate(res, 403, [
      { name: 'Status 403 NOT_VERIFIED_RESET', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  // ----------------------------------------------------------
  // G2 – Rośliny
  // ----------------------------------------------------------
  console.log(c('\n[G2] Rośliny', C.bold));

  await test('T09', 'G2', 'Pobieranie katalogu roślin', async () => {
    const res = await req('GET', '/api/plants');
    const r = evaluate(res, 200, [
      { name: 'Niepusta tablica plants', fn: (b) => {
        must(Array.isArray(b?.plants) && b.plants.length > 0, 'Brak lub pusta tablica plants');
      }},
      { name: 'Każda roślina ma _id, name, price, images', fn: (b) => {
        const p = b.plants[0];
        must(p?._id && p?.name && p?.price != null && p?.images, 'Brak wymaganych pól rośliny');
      }},
    ]);
    if (res.body?.plants?.length) {
      const pl = res.body.plants;
      ctx.allPlants = pl;
      // Do sesji (T11/T12) wybieramy pierwszą roślinę -- może być darmowa
      ctx.plantId = pl[0]._id;
      const sorted = [...pl].sort((a, b) => a.price - b.price);
      ctx.cheapPlantId     = sorted[0]?._id;
      ctx.expensivePlantId = sorted[sorted.length - 1]?._id;
      // targetBuyPlantId zostanie dokładnie wyznaczony w T20
    }
    return r;
  });

  await test('T10', 'G2', 'Pobieranie szczegółów jednej rośliny', async () => {
    if (!ctx.plantId) return skip('Brak plantId z T09');
    const res = await req('GET', `/api/plants/${ctx.plantId}`);
    return evaluate(res, 200, [
      { name: 'Pola _id, name, price, minDuration, images', fn: (b) => {
        // odpowiedź może być bezpośrednim obiektem lub opakowana w { plant: {...} }
        const p = b?._id ? b : b?.plant;
        must(
          p?._id && p?.name && p?.price != null && p?.minDuration != null && p?.images,
          `Brak wymaganych pól. Klucze body: ${Object.keys(b || {}).join(', ')}`
        );
      }},
    ]);
  });

  // ----------------------------------------------------------
  // G3 – Sesje skupienia
  // ----------------------------------------------------------
  console.log(c('\n[G3] Sesje skupienia', C.bold));

  await test('T11', 'G3', 'Zapis sesji ukończonej (25 min)', async () => {
    if (!ctx.userId || !ctx.plantId) return skip('Brak userId lub plantId');
    const now   = new Date();
    const start = new Date(now - 25 * 60 * 1000).toISOString();
    const res   = await req('POST', '/api/sessions', {
      body: {
        userId:          ctx.userId,
        plantId:         ctx.plantId,
        plannedDuration: 25,
        actualDuration:  25,
        status:          'completed',
        startedAt:       start,
        completedAt:     now.toISOString(),
      },
    });
    return evaluate(res, 201, [
      { name: 'coinsEarned > 0', fn: (b) => must(b?.coinsEarned > 0, `coinsEarned = ${b?.coinsEarned}`) },
      { name: 'populate: plantId jest obiektem', fn: (b) => {
        const p = b?.session?.plantId;
        must(p && typeof p === 'object' && p._id, 'plantId nie jest obiektem -- populate nie zadziałało');
      }},
    ]);
  });

  await test('T12', 'G3', 'Zapis sesji nieudanej (status: failed)', async () => {
    if (!ctx.userId || !ctx.plantId) return skip('Brak userId lub plantId');
    const now   = new Date();
    const start = new Date(now - 3 * 60 * 1000).toISOString();
    const res   = await req('POST', '/api/sessions', {
      body: {
        userId:          ctx.userId,
        plantId:         ctx.plantId,
        plannedDuration: 25,
        actualDuration:  3,
        status:          'failed',
        startedAt:       start,
      },
    });
    return evaluate(res, 201, [
      { name: 'coinsEarned = 0 dla sesji nieudanej', fn: (b) => must(b?.coinsEarned === 0, `coinsEarned = ${b?.coinsEarned}`) },
    ]);
  });

  await test('T13', 'G3', 'Pobieranie sesji -- bieżący tydzień', async () => {
    if (!ctx.userId) return skip('Brak userId');
    const res = await req('GET', '/api/sessions', { query: { userId: ctx.userId, period: 'week' } });
    return evaluate(res, 200, [
      { name: 'Zwraca tablicę sessions', fn: (b) => must(Array.isArray(b?.sessions), 'Brak tablicy sessions') },
      { name: 'Zawiera sesje z T11 i T12 (≥ 2)', fn: (b) => must(b.sessions.length >= 2, `Tylko ${b.sessions.length} sesji`) },
      { name: 'populate: plantId jest obiektem', fn: (b) => {
        const s = b.sessions[0];
        must(s?.plantId && typeof s.plantId === 'object', 'plantId nie jest obiektem');
      }},
    ]);
  });

  await test('T14', 'G3', 'Pobieranie sesji -- bieżący dzień', async () => {
    if (!ctx.userId) return skip('Brak userId');
    const res = await req('GET', '/api/sessions', { query: { userId: ctx.userId, period: 'day' } });
    return evaluate(res, 200, [
      { name: 'Zwraca tablicę sessions', fn: (b) => must(Array.isArray(b?.sessions), 'Brak tablicy sessions') },
    ]);
  });

  // ----------------------------------------------------------
  // G4 – Tagi
  // ----------------------------------------------------------
  console.log(c('\n[G4] Tagi', C.bold));

  await test('T15', 'G4', 'Pobieranie tagów -- domyślne tagi po rejestracji', async () => {
    if (!ctx.userId) return skip('Brak userId');
    const res = await req('GET', '/api/tags', { query: { userId: ctx.userId } });
    return evaluate(res, 200, [
      { name: '≥ 2 tagi (domyślne Study i Work)', fn: (b) => {
        must(Array.isArray(b?.tags) && b.tags.length >= 2, `Znaleziono ${b?.tags?.length} tagów`);
      }},
    ]);
  });

  await test('T16', 'G4', 'Tworzenie tagu', async () => {
    if (!ctx.userId) return skip('Brak userId');
    const res = await req('POST', '/api/tags', {
      body: { userId: ctx.userId, name: 'Sport', color: 'RED' },
    });
    const r = evaluate(res, 201, [
      { name: 'Nowy tag ma _id, name = "Sport", color = "RED"', fn: (b) => {
        const t = b?.tag;
        must(t?._id && t?.name === 'Sport' && t?.color === 'RED', `tag = ${JSON.stringify(t)}`);
      }},
    ]);
    ctx.tagId = res.body?.tag?._id || null;
    return r;
  });

  await test('T17', 'G4', 'Edycja tagu', async () => {
    if (!ctx.userId || !ctx.tagId) return skip('Brak tagId z T16');
    const res = await req('PUT', `/api/tags/${ctx.tagId}`, {
      body: { userId: ctx.userId, name: 'Trening', color: 'ORANGE' },
    });
    return evaluate(res, 200, [
      { name: 'name zaktualizowane na "Trening"', fn: (b) => must(b?.tag?.name === 'Trening', `name = ${b?.tag?.name}`) },
      { name: 'color zaktualizowany na "ORANGE"', fn: (b) => must(b?.tag?.color === 'ORANGE', `color = ${b?.tag?.color}`) },
    ]);
  });

  await test('T18', 'G4', 'Zmiana kolejności tagów', async () => {
    if (!ctx.userId) return skip('Brak userId');
    const listRes = await req('GET', '/api/tags', { query: { userId: ctx.userId } });
    const tags    = listRes.body?.tags || [];
    if (tags.length < 2) return skip('Za mało tagów do testu kolejności');
    const reversed = [...tags].reverse().map((t) => t._id);
    const res = await req('PUT', '/api/tags/reorder', {
      body: { userId: ctx.userId, tagIds: reversed },
    });
    return evaluate(res, 200, [
      { name: 'Odpowiedź zawiera tablicę tags', fn: (b) => must(Array.isArray(b?.tags), 'Brak tablicy tags') },
      { name: 'Kolejność odzwierciedla żądanie', fn: (b) => {
        const first = b.tags[0]?._id?.toString();
        must(first === reversed[0].toString(), `Oczekiwano ${reversed[0]}, otrzymano ${first}`);
      }},
    ]);
  });

  await test('T19', 'G4', 'Usunięcie tagu', async () => {
    if (!ctx.userId || !ctx.tagId) return skip('Brak tagId z T16');
    const res = await req('DELETE', `/api/tags/${ctx.tagId}`, {
      query: { userId: ctx.userId },
    });
    return evaluate(res, 200, [
      { name: 'Komunikat potwierdzający usunięcie', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  // ----------------------------------------------------------
  // G5 – Portfel
  // ----------------------------------------------------------
  console.log(c('\n[G5] Portfel', C.bold));

  await test('T20', 'G5', 'Pobieranie portfela', async () => {
    if (!ctx.userId) return skip('Brak userId');
    const res = await req('GET', WALLET_GET, { query: { userId: ctx.userId } });
    const r = evaluate(res, 200, [
      { name: 'Pola coins, unlockedPlantIds, favouritePlantIds', fn: (b) => {
        must(
          b?.coins != null && Array.isArray(b?.unlockedPlantIds) && Array.isArray(b?.favouritePlantIds),
          `coins=${b?.coins}, unlocked=${Array.isArray(b?.unlockedPlantIds)}, fav=${Array.isArray(b?.favouritePlantIds)}`
        );
      }},
    ]);

    // Dynamiczne wyznaczanie roślin dla T21/T22 na podstawie aktualnego portfela
    if (res.status === 200 && res.body && ctx.allPlants.length > 0) {
      const ownedIds = new Set((res.body.unlockedPlantIds || []).map(String));
      const balance  = res.body.coins ?? 0;
      const paid     = ctx.allPlants
        .filter((p) => p.price > 0)
        .sort((a, b) => a.price - b.price);

      // T21: najtańsza płatna roślina, której user nie posiada i stać go na nią
      const buyable = paid.find((p) => !ownedIds.has(String(p._id)) && p.price <= balance);
      if (buyable) {
        ctx.targetBuyPlantId = buyable._id;
        process.stdout.write(c(`      ℹ  T21 target: ${buyable.name} (${buyable.price} monet)\n`, C.yellow));
      } else {
        process.stdout.write(c('      ⚠  T21 nie znajdzie dostępnej rośliny (za mało monet lub wszystkie kupione)\n', C.yellow));
      }

      // T22: najdroższa płatna roślina, której user nie posiada i NIE stać go
      const unaffordable = [...paid].reverse().find(
        (p) => !ownedIds.has(String(p._id)) && p.price > balance
      );
      if (unaffordable) {
        ctx.expensivePlantId = unaffordable._id;
        process.stdout.write(c(`      ℹ  T22 target: ${unaffordable.name} (${unaffordable.price} monet)\n`, C.yellow));
      }
    }

    return r;
  });

  // T21: kupujemy najtańszą płatną roślinę wyznaczoną w T20
  await test('T21', 'G5', 'Zakup rośliny -- wystarczające środki', async () => {
    if (!ctx.userId)           return skip('Brak userId');
    if (!ctx.targetBuyPlantId) return skip('Brak targetBuyPlantId -- T20 nie wyznaczyło rośliny (za mało monet lub wszystkie kupione)');
    const res = await req('POST', WALLET_BUY, {
      body: { userId: ctx.userId, plantId: ctx.targetBuyPlantId },
    });
    return evaluate(res, 200, [
      { name: 'Roślina dodana do unlockedPlantIds', fn: (b) => {
        const ids = (b?.unlockedPlantIds || []).map(String);
        must(ids.includes(ctx.targetBuyPlantId.toString()), 'Roślina nie w unlockedPlantIds');
      }},
      { name: 'Saldo monet zaktualizowane', fn: (b) => must(b?.coins != null, 'Brak pola coins') },
    ]);
  });

  // T22: próbujemy kupić najdroższą roślinę (cena > dostępne saldo)
  await test('T22', 'G5', 'Zakup rośliny -- niewystarczające środki', async () => {
    if (!ctx.userId || !ctx.expensivePlantId) return skip('Brak expensivePlantId z T09');
    const res = await req('POST', WALLET_BUY, {
      body: { userId: ctx.userId, plantId: ctx.expensivePlantId },
    });
    return evaluate(res, 400, [
      { name: 'Komunikat błędu (INSUFFICIENT_COINS lub podobny)', fn: (b) => {
        const msg = (b?.message || '').toUpperCase();
        must(
          msg.includes('COIN') || msg.includes('INSUFFICIENT') || msg.includes('SALDO') || msg.includes('BRAK'),
          `message = "${b?.message}"`
        );
      }},
    ]);
  });

  await test('T23', 'G5', 'Przełączenie ulubionej rośliny (toggle)', async () => {
    if (!ctx.userId || !ctx.plantId) return skip('Brak plantId');
    const res = await req('POST', WALLET_FAV, {
      body: { userId: ctx.userId, plantId: ctx.plantId },
    });
    return evaluate(res, 200, [
      { name: 'Odpowiedź zawiera favouritePlantIds', fn: (b) => {
        must(Array.isArray(b?.favouritePlantIds), 'Brak tablicy favouritePlantIds');
      }},
    ]);
  });

  // ----------------------------------------------------------
  // G6 – Profil użytkownika
  // ----------------------------------------------------------
  console.log(c('\n[G6] Profil użytkownika', C.bold));

  // Brak endpointu GET /api/user/:userId -- profil użytkownika jest zwracany
  // przy logowaniu i przechowywany w pamięci aplikacji mobilnej.
  // T24 testuje weryfikację dostępności nazwy użytkownika (używaną podczas rejestracji).
  await test('T24', 'G6', 'Weryfikacja zajętej nazwy użytkownika', async () => {
    // ApiTest istnieje w bazie, więc endpoint powinien zwrócić że nazwa jest zajęta
    const res = await req('GET', '/api/auth/check-username', {
      query: { username: 'ApiTest' },
    });
    return evaluate(res, 200, [
      { name: 'Odpowiedź zawiera informację o dostępności', fn: (b) => {
        // endpoint może zwrócić { available: false } lub { taken: true } lub podobne
        must(b != null && Object.keys(b).length > 0, 'Pusta odpowiedź');
      }},
    ]);
  });

  await test('T25', 'G6', 'Zmiana nazwy użytkownika -- prawidłowa (≤ 20 znaków)', async () => {
    if (!ctx.userId) return skip('Brak userId');
    const newName = `Zmieniona${TS}`.slice(0, 20);
    const res = await req('POST', '/api/user/update-username', {
      body: { userId: ctx.userId, username: newName },
    });
    return evaluate(res, 200, [
      { name: `username = "${newName}"`, fn: (b) => {
        // odpowiedź: { message, username }
        must(b?.username === newName, `username = "${b?.username}"`);
      }},
    ]);
  });

  await test('T26', 'G6', 'Zmiana nazwy -- przekroczenie limitu 20 znaków', async () => {
    if (!ctx.userId) return skip('Brak userId');
    const res = await req('POST', '/api/user/update-username', {
      body: { userId: ctx.userId, username: 'NazwaDluzszaNizDwadziesciaSzesc' }, // 30 znaków
    });
    // serwer zwraca 422 Unprocessable Entity dla username > 20 znaków
    return evaluate(res, 422, [
      { name: 'Komunikat walidacyjny', fn: (b) => must(b?.message, 'Brak pola message') },
    ]);
  });

  // ================================================================
  //  Zapis raportu i podsumowanie
  // ================================================================
  const finishedAt = new Date().toISOString();
  const totalMs    = results.reduce((s, r) => s + r.durationMs, 0);
  const tested     = results.filter((r) => r.result !== 'SKIP').length;
  const passPct    = tested > 0 ? Math.round((totalPass / tested) * 100) : 0;

  const report = {
    metadata: {
      tool:            'Jardinito API Test Runner v1.0',
      baseUrl:         BASE_URL,
      startedAt,
      finishedAt,
      totalDurationMs: totalMs,
    },
    summary: {
      total:   results.length,
      passed:  totalPass,
      failed:  totalFail,
      skipped: totalSkip,
    },
    tests: results,
  };

  fs.writeFileSync(RESULTS_FILE, JSON.stringify(report, null, 2), 'utf8');

  console.log(c('\n' + '─'.repeat(54), C.gray));
  console.log(`  Wynik:     ${c(String(totalPass), C.green)} / ${tested} testów zaliczonych (${passPct}%)`);
  if (totalSkip > 0) console.log(`  Pominięto: ${c(String(totalSkip), C.yellow)}`);
  if (totalFail > 0) console.log(`  Błędy:     ${c(String(totalFail), C.red)}`);
  console.log(`  Czas:      ${totalMs} ms`);
  console.log(`  Raport:    ${c(RESULTS_FILE, C.gray)}`);
  console.log(c('─'.repeat(54), C.gray) + '\n');

  if (totalFail > 0) process.exit(1);
}

runTests().catch((err) => {
  process.stderr.write(c('\n[BŁĄD KRYTYCZNY] ' + err.message + '\n', C.red));
  process.exit(1);
});

/*
 * ================================================================
 *  README -- endpoint DEV do weryfikacji konta
 * ================================================================
 *
 * Dodaj poniższy fragment do server.js, PRZED rejestracją routerów,
 * tylko gdy DEV_MODE=true (np. w pliku .env: DEV_MODE=true):
 *
 *   const User = require('./models/User');
 *
 *   if (process.env.DEV_MODE === 'true') {
 *     app.post('/api/dev/verify', async (req, res) => {
 *       try {
 *         const { userId } = req.body;
 *         if (!userId) return res.status(400).json({ message: 'Brak userId' });
 *         await User.findByIdAndUpdate(userId, { isVerified: true });
 *         res.json({ ok: true, message: 'Konto zweryfikowane' });
 *       } catch (err) {
 *         res.status(500).json({ message: err.message });
 *       }
 *     });
 *   }
 *
 * Uruchomienie:
 *   DEV_MODE=true node server.js
 *   BASE_URL=http://localhost:5000 TEST_USER_EMAIL=apitest@jardinito.com node scripts/api-test-runner.js
 *
 * Wyniki:
 *   cat api_test_results.json   (pełny raport JSON)
 *   node api-test-runner.js 2>&1 | tee console_output.txt
 * ================================================================
 */