# Handover: Secure Coding Training Workshop — Design Philosophy

Document de context pentru sesiuni viitoare de lucru cu un agent AI.
Sintetizează deciziile, filosofia și principiile stabilite de Victor Rentea în designul acestui workshop.

---

## 1. Atitudinea workshopului

Acesta NU este un curs academic despre securitate. Este un **workshop practic, hands-on**, pentru developeri backend (Java/Spring). Scopul nu este ca participanții să memoreze liste de vulnerabilități, ci **să le simtă pe pielea lor** — să atace codul, să vadă impactul, și abia apoi să fixeze.

Fluxul pedagogic al fiecărui exercițiu:
1. **Citește codul** — ce face endpoint-ul?
2. **Atacă-l** — folosind `.http` file, browser, curl
3. **Simte impactul** — alert-ul poppează, datele leak-uiesc, autorizarea eșuează
4. **Fixează** — cu soluția corectă (allow-list, nu block-list; parametrizare, nu concatenare)
5. **Verifică** — enable testul `@Disabled`, rulează-l, verde

## 2. Sentimentul pe care vrem să-l inducem

**Falsa securitate urmată de surpriză.** Studentul fixează ceva, se simte bine ("gata, am rezolvat!"), și apoi descoperă că e încă vulnerabil pe alt vector. Acest pattern de "credeam că e safe, dar nu era" este cel mai puternic instrument pedagogic al workshopului.

Exemplul perfect este exercițiul **XSS în cascadă** (3 wave-uri):
- **Wave 1:** Fixează XSS în `body` → "Am rezolvat!"
- **Wave 2:** Surpriză — `author` e tot vulnerabil → "Ah, nu m-am gândit la ăsta..."
- **Wave 3:** Surpriză iar — DOM-based XSS din URL, server-ul nici nu vede payload-ul → "Serverul meu e perfect, dar pagina e vulnerabilă?!"

Fiecare wave **demolează o presupunere falsă**: că fixarea unui câmp = securitate completă, că orice trafic trece prin server, că sanitizarea pe backend e suficientă.

## 3. Dezvoltarea progresivă a ideilor

### Pattern: Cascadă (multi-wave)
Exercițiile complexe nu sunt prezentate dintr-o dată. Se construiesc **progresiv**, fiecare val adăugând un nivel de sofisticare:

- Fiecare val se bazează pe fix-ul anterior
- Între valuri: moment de reflecție ("acum e safe? ești sigur?")
- Ultimul val introduce un concept nou (ex: client-side vs server-side)
- Takeaway-ul final leagă toate valurile într-un principiu unificator (defense in depth)

### Pattern: De la evident la subtil
- Primul vector de atac e mereu **cel evident** (script tag, SQL union)
- Al doilea e **mai subtil** (alt câmp, alt encoding)
- Al treilea e **contraintuitiv** (nu trece deloc prin server)

### Pattern: Scrolling hints
În fișierele `.http`, hint-urile sunt separate de challenge printr-un bloc lung de comentarii goale. Studentul trebuie să scrolleze deliberat — nu vede hint-ul din greșeală.

## 4. Cum testăm

### Branch strategy
- **`main`** — cod vulnerabil, teste `@Disabled("TODO: fix the ...")`
- **`solutions`** — cod fixat, teste enabled și green

### Pattern de test
```java
@Disabled("TODO Wave 1: fix the Stored XSS in the body field")
@Test
void body_scriptTagsAreStripped() throws Exception { ... }
```

Fiecare test:
1. **Trimite input malițios** (payload de atac)
2. **Verifică că e neutralizat** (nu conține script, onerror, etc.)
3. **Verifică că input-ul legitim funcționează** (bold/italic supraviețuiește sanitizării)

Testele sunt grupate pe wave-uri cu comentarii de secțiune (`// ====== Wave 1: ... ======`).

Wave 3 (DOM-based XSS) nu are test backend — e vulnerabilitate pur client-side, testabilă doar manual în browser. Aceasta e o lecție în sine: nu totul se poate testa cu MockMvc.

### Naming convention
Prefixul testului = câmpul vulnerabil: `body_scriptTagsAreStripped`, `author_htmlIsStrippedEntirely`

## 5. Convenția soluție vs. greșeală în cod

Pe branch-ul **solutions**, codul arată fix-ul aplicat. Linia originală vulnerabilă este comentată cu `❌`, iar fix-ul are `✅`:

```java
// ❌ comment.setBody(comment.getBody());           // vulnerable — no sanitization
comment.setBody(sanitizeHtml(comment.getBody()));    // ✅ allow-list sanitization
```

Pe branch-ul **main** (exercițiu), codul este vulnerabil fără marcaj — doar `TODO` comments ghidează studentul:

```java
// TODO Wave 1: The body field stores raw HTML — fix it using OWASP HTML Sanitizer
return commentRepo.save(comment);
```

Regulile:
- `❌` = linia vulnerabilă, comentată (vizibilă pe `solutions` ca referință)
- `✅` = linia fix-ului
- TODO-urile sunt **numerotate pe wave-uri** și dau **direcția, nu soluția**
- Niciodată nu punem soluția ca și comentariu pe `main` — merge pe `solutions` branch

## 6. Structura fișierelor per exercițiu

Fiecare vulnerabilitate are:

| Fișier | Rol |
|--------|-----|
| `VulnerabilityName.java` | Endpoint vulnerabil + entity + repo (tot într-un fișier) |
| `VulnerabilityName.http` | Requests de investigare și atac, cu hints ascunse (scrolling) |
| `VulnerabilityName.md` | Walkthrough cu `<details>` colapsabile per fază |
| `VulnerabilityNameTest.java` | Teste `@Disabled` pe main, enabled pe solutions |
| `vulnerabilityname.html` | Pagină frontend (în `static/vulnerability/`) |

## 7. Principii de design

1. **Minimal noise** — doar vulnerabilitatea, nimic altceva. Entități inline, fără layere artificiale.
2. **Self-contained** — fiecare exercițiu funcționează independent.
3. **Browser-first** — studentul vede impactul vizual (alert, date expuse), nu doar output JSON.
4. **Cascadă > one-shot** — exercițiile complexe se desfășoară progresiv.
5. **Nu discuta planul fără sa-l discutăm împreună mai întâi** — fiecare exercițiu se plănuiește în dialog, nu unilateral.

## 8. Ordinea exercițiilor

Urmează `TRAINING-PLAN.md`. Modulele progresează de la injecții (unde fix-ul e clar) la probleme mai nuanțate (access control, data exposure) la cele avansate (SSRF, deserialization).

## 9. Stack tehnic

- Java 17+ / Spring Boot 3
- Spring Security (profil `disabled` pentru exerciții de injecții)
- H2 in-memory database
- OWASP HTML Sanitizer (dependency în pom.xml)
- Attacker app pe port 8081/9999 (pentru CSRF/CORS)
- `.http` files (IntelliJ HTTP client)
- Frontend: vanilla HTML/JS (fără framework — ca să se vadă innerHTML vs textContent clar)
