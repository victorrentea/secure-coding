# Secure Coding Training — Hands-On Exercise Plan

## Overview

This training teaches backend developers to identify and fix common security vulnerabilities in a Spring Boot application. Each exercise follows the pattern: **read vulnerable code → exploit it → fix it → verify with tests**.

**Target audience:** Backend Java/Spring developers
**Duration:** ~4 hours hands-on (adjust by skipping modules)
**Prerequisites:** Basic Spring Boot, REST APIs, JPA

---

## Setup

1. Start the app: `./mvnw spring-boot:run -pl secure-app` (uses profile `disabled` for exercises, no auth needed initially)
2. H2 console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, user: `sa`, password: `sa`)
3. HTTP requests for testing in `requests.http` (use IntelliJ HTTP client or curl)

---

## Module 1: Input Validation & Injection (~60 min)

### Exercise 1.1 — SQL Injection
**File:** `vulnerability/SQLInjection.java`
**Difficulty:** ★☆☆

**Context:** A student search endpoint builds SQL by concatenating user input.

**Steps:**
1. Call `GET /api/vulnerability/sql-injection?name=Ana&orderBy=NAME` — observe normal behavior
2. **Exploit the WHERE clause:** Try `name=' OR '1'='1` — observe it returns all students, bypassing the tenant filter
3. **Exploit the ORDER BY clause:** Try `orderBy=NAME; DROP TABLE STUDENT--` or `orderBy=(CASE WHEN 1=1 THEN NAME ELSE TENANT_ID END)` for data exfiltration
4. **Exploit JPQL injection:** Call `GET /api/vulnerability/jpql-injection?name=' OR '1'='1` — observe it leaks students from other tenants (ASE, UNIBUC)

**Fix tasks:**
- [ ] Replace string concatenation in `sqlInjection()` with parameterized queries using `jdbcTemplate.query()` with `?` placeholders
- [ ] For the ORDER BY clause: use an enum whitelist (`OrderByColumn`) and validate against it before building the query
- [ ] Replace string concatenation in `jpqlInjection()` with JPA query parameters (`:name`)

**Key takeaway:** Never concatenate user input into SQL/JPQL. Use parameterized queries. For ORDER BY (which can't be parameterized), use a strict whitelist.

---

### Exercise 1.2 — Mass Assignment
**File:** `vulnerability/MassAssignment.java`
**Difficulty:** ★☆☆

**Context:** A game player can update their profile (name, country). The endpoint accepts a full `PlayerDto` and maps ALL fields to the entity — including `cash` and `username`.

**Steps:**
1. Call `GET /api/vulnerability/player/details` — note the player has `cash: 100`
2. **Exploit:** Call `PUT /api/vulnerability/player/details` with body `{"fullName":"Hacker","country":"RO","cash":999999}` — observe that cash is now 999999
3. You gave yourself money by modifying a field the API wasn't supposed to accept

**Fix tasks:**
- [ ] Create a separate `UpdatePlayerDetailsCommand` DTO with only the fields the user should be allowed to change (`fullName`, `country`)
- [ ] Update the `PUT` endpoint to accept this new DTO instead of `PlayerDto`
- [ ] Write the mapping manually or add a dedicated mapper method that only copies safe fields

**Key takeaway:** Never bind a full entity/DTO when only a subset of fields should be updatable. Use a dedicated command object.

---

### Exercise 1.3 — XSS (Cross-Site Scripting)
**Files:** `vulnerability/XSS.java` + `web/controller/TrainingController.java` + `security/RichTextSanitizer.java` + `static/vulnerability/xss.html`
**Difficulty:** ★★☆

**Context:** This exercise covers all 3 types of XSS. Students discover the type as a side conclusion.

#### Phase 1 — Stored XSS
**Steps:**
1. Open `xss.html` in the browser — use the Phase 1 section
2. Create a training with description: `<b>Bold</b> <img src=x onerror=alert('StoredXSS')>`
3. Load trainings — the script executes in the browser because the description is stored in the DB and served as-is

**Fix tasks:**
- [ ] Apply `sanitizeRichText()` in TrainingController's `create()` and `update()` methods before saving
- [ ] Or: uncomment `@RichText` on `TrainingDto.description` to enable the AOP-based `RichTextSanitizer`
- [ ] Explore `RichTextSanitizer.java` — understand the allow-list approach (FORMATTING + BLOCKS only)

#### Phase 2 — Reflected XSS
**Steps:**
1. Open `xss.html` — use the Phase 2 section
2. Enter `<script>alert('ReflectedXSS')</script>` as the search query
3. The endpoint echoes the query directly into the HTML response — the script executes

**Fix tasks:**
- [ ] In `XSS.search()`, HTML-escape the query before embedding: `HtmlUtils.htmlEscape(query)`

#### Phase 3 — DOM-based XSS
**Steps:**
1. Open `xss.html#<img src=x onerror=alert('DomXSS')>` in the browser
2. The page reads `location.hash` and injects it into the DOM via `innerHTML` — the script executes
3. Note: this attack never reaches the server — it's purely client-side

**Fix tasks:**
- [ ] In `xss.html`, change `innerHTML` to `textContent` in the `renderFragment()` function

**Key takeaway:** XSS comes in 3 flavors — stored (persisted in DB), reflected (echoed in response), and DOM-based (client-side only). Each requires a different fix: server-side HTML sanitization, HTML escaping, and safe DOM APIs respectively.

---

## Module 2: Access Control (~60 min)

### Exercise 2.1 — Role-Based Authorization (RBAC)
**File:** `web/controller/TrainingController.java` + `AuthorizationTest.java`
**Difficulty:** ★☆☆

**Context:** The `DELETE /api/trainings/{id}` endpoint should only be accessible to admins, but the authorization annotation needs to be wired correctly.

**Steps:**
1. Read the TODO comments (1-3) in `TrainingController.delete()`
2. Fix `AuthorizationTest.java` — the test URL is wrong (`/delete` suffix shouldn't be there) and the test assertions need updating

**Fix tasks:**
- [ ] **TODO 1:** Verify that `@Secured("ROLE_ADMIN")` is active and `@EnableMethodSecurity(securedEnabled = true)` is configured
- [ ] **TODO 2:** Evolve to a fine-grained permission: allow both ADMIN and POWER roles. Options: `@Secured({"ROLE_ADMIN", "ROLE_POWER"})` or move to a fine-grained role like `ROLE_TRAINING_DELETE`
- [ ] **TODO 3:** Add object-level check: current user must manage the training's teacher. Implement via one of:
  - (a) Ad-hoc check in the controller method
  - (b) `@PreAuthorize("@permissionService.canDeleteTraining(#trainingId)")`
  - (c) `@PreAuthorize("hasPermission(#trainingId, 'TRAINING', 'WRITE')")` with `PermissionEvaluatorImpl`
- [ ] Fix and run `AuthorizationTest` — make both tests pass

**Key takeaway:** Use Spring Security's method-level annotations. Start with RBAC, evolve to fine-grained permissions and object-level authorization when needed.

---

### Exercise 2.2 — IDOR: Creating Resources for Other Users (Appointments)
**File:** `vulnerability/ObjectAuthorizationAppointments.java`
**Difficulty:** ★★☆

**Context:** A patient portal lets users create medical appointments. The `createAppointment` endpoint accepts a `patientId` in the request body — but doesn't verify the current user is that patient.

**Steps:**
1. Create a patient: `POST /api/vulnerability/appointments/patient` with your info
2. Note the returned patient ID (e.g., 1001)
3. Create an appointment for yourself — works fine
4. **Exploit:** Create an appointment with `patientId: 500` (another patient) — it succeeds, and the response leaks that patient's name, email, and phone number!

**Fix tasks:**
- [ ] Resolve the current user's patient ID from the security context (not from the request body)
- [ ] Remove `patientId` from `CreateAppointmentRequest` or ignore it
- [ ] Ensure the response doesn't leak other patients' PII

**Key takeaway:** For resource creation, derive ownership from the authenticated session, not from user-supplied input.

---

## Module 3: Data & API Protection (~45 min)

### Exercise 3.1 — Sensitive Data Exposure
**File:** `vulnerability/DataExposure.java`
**Difficulty:** ★☆☆

**Context:** The blog article detail endpoint returns the full JPA entity, including nested comment authors with their email addresses.

**Steps:**
1. Call `GET /api/vulnerability/articles` — list looks fine (only id + title)
2. Call `GET /api/vulnerability/articles/1` — the full entity is returned with comment authors' emails (PII leak!)

**Fix tasks:**
- [ ] Create a `BlogArticleDetailDto` (or record) that includes only the fields safe to expose
- [ ] Map the entity to the DTO before returning
- [ ] Discussion: three approaches — `@JsonIgnore`, DTOs, or `@VisibleForRole` annotation

**Key takeaway:** Never return JPA entities directly from controllers. Use DTOs to control exactly what data leaves your API.

---

### Exercise 3.2 — Brute Force on Password Reset
**File:** `vulnerability/BruteForce.java`
**Difficulty:** ★★☆

**Context:** The password reset flow sends a 5-digit SMS code (10k-99k range). Step 2 verifies the code but has no attempt limit — an attacker can try all 90,000 possibilities.

**Steps:**
1. Trigger reset: `GET /user/testuser/reset-password/step1` — note the code in logs
2. Try wrong codes: `POST /user/testuser/reset-password/step2` — no penalty
3. **Exploit:** A script trying all 90,000 codes would crack it in seconds

**Fix tasks:**
- [ ] **Option A (simple):** Uncomment the `@RateLimiter` annotation (Resilience4j) — but this is too global
- [ ] **Option B (better):** Uncomment the per-attempt tracking (`attemptsLeft` counter) — limit to 3 attempts per reset request
- [ ] **Bonus:** Show the remaining attempts in the error response to help legitimate users

**Key takeaway:** Any endpoint that verifies a secret (OTP, password, code) must have rate limiting or attempt counting. Prefer per-session limits over global rate limits.

---

### Exercise 3.3 — Unbounded Queries (DoS)
**File:** `vulnerability/Limits.java`
**Difficulty:** ★☆☆

**Context:** The leaderboard endpoint accepts a `size` query parameter with no upper bound. A malicious request with `size=20000000` causes the server to process millions of rows.

**Steps:**
1. Call `GET /api/vulnerability/leaderboard?page=1&size=20` — fast
2. **Exploit:** Call `GET /api/vulnerability/leaderboard?page=1&size=20000000` — server hangs

**Fix tasks:**
- [ ] Add a max page size validation: `size = Math.min(size, 100)`
- [ ] Return 400 Bad Request if the requested size exceeds the max
- [ ] Discussion: What about `page` parameter? Can requesting page 999999999 be abused?

**Key takeaway:** Always validate and cap pagination parameters. Consider all user-controlled values that affect resource consumption.

---

## Module 4: File & Network Security (~45 min)

### Exercise 4.1 — Path Traversal via File Upload
**File:** `vulnerability/Upload.java`
**Difficulty:** ★★☆

**Context:** The upload endpoint accepts a `fileName` parameter and writes the file to disk at that path. No sanitization is performed.

**Steps:**
1. Get a traversal path hint: `GET /api/vulnerability/vulnerable-file-name`
2. Upload a file with `fileName=../pom.xml` — you've overwritten the project's pom.xml!
3. Upload with `fileName=../../../../etc/cron.d/evil` — potential system compromise

**Fix tasks:**
- [ ] **Level 1:** Validate filename against a whitelist regex: `[a-zA-Z0-9._-]+`
- [ ] **Level 2:** Ignore user filename entirely — generate a UUID-based name and store the original filename in a database
- [ ] **Level 3:** Add file type validation (e.g., only allow images/PDFs based on content type detection via Apache Tika)
- [ ] **Bonus:** Discuss antivirus scanning for uploaded files in production

**Key takeaway:** Never trust user-supplied filenames. Generate server-side names and validate file content types.

---

### Exercise 4.2 — Server-Side Request Forgery (SSRF)
**File:** `vulnerability/ServerSideRequestForgery.java`
**Difficulty:** ★★★

**Context:** An image proxy endpoint fetches an image from a user-provided URL. It uses `URL.openStream()` which accepts any protocol including `file://`.

**Steps:**
1. Get the pom.xml path: `GET /api/vulnerability/pom-absolute-path` (e.g., `/home/user/secure-coding/pom.xml`)
2. **Exploit:** Call `GET /api/vulnerability/fetch-image?url=file:///home/user/secure-coding/pom.xml` — reads a local file!
3. Try `file:///etc/passwd` for system files
4. In cloud environments, try `http://169.254.169.254/latest/meta-data/` to read instance metadata (AWS credentials!)

**Fix tasks:**
- [ ] **Step 1:** Switch from `URL.openStream()` to `RestTemplate` (only supports http/https)
- [ ] **Step 2:** Validate URL scheme is http or https
- [ ] **Step 3:** Resolve the hostname and block private/loopback IPs (see `validateUrl()` method)
- [ ] **Step 4:** Validate response content type using Apache Tika (ensure it's actually an image)
- [ ] **Step 5:** Set an `Accept` header in outgoing requests

**Key takeaway:** Validate protocol, hostname, resolved IP, and response content type. Defense in depth — no single check is sufficient.

---

## Module 5: Advanced Topics (Demo / Discussion, ~30 min)

### Demo 5.1 — Insecure Deserialization
**File:** `vulnerability/InsecureDeserialization.java`
**Difficulty:** Demo only

**Walkthrough:**
1. Run `InsecureDeserialization.Generate.main()` — creates `deserialization-bomb.dat`
2. Run `InsecureDeserialization.Read.main()` — observe that a `pwned` file appears. Code was executed during deserialization!
3. Examine the exploit chain: `PriorityQueue → TransformingComparator → ChainedTransformer → Runtime.exec()`
4. Discuss the fix: `ValidatingObjectInputStream` with an allow-list of expected classes

**Key takeaway:** Never deserialize untrusted binary data. If you must, use `ValidatingObjectInputStream` or avoid Java serialization entirely (use JSON).

### Demo 5.2 — Cryptography Basics
**Files:** `crypto/Hashing.java`, `crypto/SymmetricEncryption.java`, `crypto/ASymmetricEncryption.java`

Quick walkthroughs of hashing (SHA-256), symmetric encryption (AES), and asymmetric encryption (RSA). Focus on when to use which.

---

## Suggested Timing for a 4-Hour Workshop

| Time | Activity |
|------|----------|
| 0:00 - 0:15 | Introduction, setup, security context |
| 0:15 - 1:15 | **Module 1:** SQL Injection + Mass Assignment + XSS |
| 1:15 - 1:30 | Break |
| 1:30 - 2:30 | **Module 2:** RBAC + IDOR (Shops + Appointments) |
| 2:30 - 2:45 | Break |
| 2:45 - 3:30 | **Module 3:** Data Exposure + Brute Force + Limits |
| 3:30 - 4:00 | **Module 4:** Upload + SSRF (or Module 5 demos if time) |

For a shorter 2-hour session, focus on Modules 1 + 2 (the most impactful topics).

---

## Branch Strategy

- **`main`** — Starting code with vulnerabilities (student works here)
- **`solutions`** — Branch with all fixes applied (trainer reference)

Students should never look at the solutions branch during the exercise. The trainer can show specific solutions after each module for discussion.

---

## Next Steps (for the trainer)

1. Add failing integration tests for each exercise (students run tests to verify their fix)
2. Clean up AuthorizationTest (wrong URL, disabled test)
3. Add a simple frontend or Postman collection for exploiting each vulnerability interactively
4. Consider adding: CSRF exercise (attacker app exists but no exercise structure), logging/monitoring exercise (Prometheus + Grafana are set up)
