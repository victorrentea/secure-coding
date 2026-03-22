# Secure Coding Training — Project Rules

## Project Overview
This is the hands-on exercise repository for Victor Rentea's Secure Coding training for backend developers (mostly Java/Spring). The codebase contains intentional vulnerabilities that students must identify and fix.

## Pedagogic Philosophy
This is NOT an academic course. It's a **hands-on workshop** where developers **attack code, feel the impact, then fix it**.

### Flow per exercise:
1. **Read the code** — what does the endpoint do?
2. **Attack it** — using `.http` file, browser, curl
3. **Feel the impact** — alert pops, data leaks, auth fails
4. **Fix it** — with the correct solution (allow-list not block-list; parameterization not concatenation)
5. **Verify** — enable the `@Disabled` test, run it, green

### Core feeling: False security → surprise
The student fixes something, feels safe, then discovers they're still vulnerable on another vector. This "I thought it was safe but it wasn't" pattern is the strongest teaching tool.

### Progressive discovery (cascade pattern)
Complex exercises unfold in **waves**, each building on the previous fix:
- Each wave is based on the previous fix
- Between waves: reflection moment ("is it safe now? are you sure?")
- Last wave introduces a new concept (e.g., client-side vs server-side)
- Final takeaway ties all waves into a unifying principle (defense in depth)

### From obvious to subtle
- First attack vector is always **obvious** (script tag, SQL union)
- Second is **more subtle** (different field, different encoding)
- Third is **counterintuitive** (doesn't go through the server at all)

## Branch Strategy
- **`master`** — Unsolved exercises with vulnerabilities. Tests are `@Disabled`. This is what students clone.
- **`solutions`** — Fixed vulnerabilities with tests enabled (green). The trainer pushes this branch with the solved code.
- Work through vulnerabilities one by one. Each gets its own commit on the solutions branch.

## Solution vs. Vulnerable Code Convention

On **solutions** branch, the fix is applied and the original vulnerable line is commented with `❌`:
```java
// ❌ comment.setBody(comment.getBody());           // vulnerable — no sanitization
comment.setBody(sanitizeHtml(comment.getBody()));    // ✅ allow-list sanitization
```

On **master** (exercise), code is vulnerable with only TODO comments guiding the student:
```java
// TODO Wave 1: The body field stores raw HTML — fix it using OWASP HTML Sanitizer
return commentRepo.save(comment);
```

Rules:
- `❌` = vulnerable line, commented out (visible on `solutions` as reference)
- `✅` = the fix line
- TODOs are **numbered by wave** and give **direction, not the solution**
- Never put the solution as a comment on `master` — it goes on `solutions` branch
- For initial workshop setup code (not vulnerability fixes): use `TODO X` marker with `// SOLUTION:` comment

## Testing Strategy
- Each vulnerability exercise has a corresponding **integration test**.
- On `master`: tests are **`@Disabled("TODO Wave N: fix the ...")`**.
- On `solutions`: the `@Disabled` annotation is removed and tests pass green.
- Tests prove the fix works by:
  - Sending malicious input (attack payload)
  - Verifying it's neutralized (no script, onerror, etc.)
  - Verifying legitimate input still works (bold/italic survives sanitization)
- Tests are grouped by waves with section comments (`// ====== Wave 1: ... ======`)
- Test naming: prefix = vulnerable field: `body_scriptTagsAreStripped`, `author_htmlIsStrippedEntirely`
- Students enable tests one by one as they fix each exercise.
- Some attacks (e.g., DOM-based XSS) have no backend test — testable only manually in browser. This is a lesson in itself.

## Exercise Structure
Each vulnerability exercise should have:

| File | Role |
|------|------|
| `SV{N}_{Name}.java` | Vulnerable endpoint + entity + repo (all in one file) |
| `SV{N}_{Name}.http` | Investigation and attack requests, with scrolling-hidden hints |
| `SV{N}_{Name}.md` | Walkthrough with `<details>` collapsible sections per phase |
| `SV{N}_{Name}Test.java` | Tests: `@Disabled` on master, enabled on solutions |
| `{name}.html` | Frontend page (in `static/vulnerability/`) |

### Markdown walkthrough rules
- Each `.md` file starts with a note telling the participant to **render the markdown visually** (e.g., IntelliJ preview, GitHub, or a markdown viewer) rather than reading the raw text — so that `<details>` blocks work and content is revealed progressively.
- Each fix section must include the **relevant code snippet** (brief, a few lines) showing exactly what the fix looks like. Don't just describe it — show the code.

### Scrolling hints in .http files
Hints are separated from the challenge by a long block of empty comments. The student must scroll deliberately — they won't see the hint by accident.

## Code Conventions
- All vulnerability exercises live in `secure-app/src/main/java/victor/training/vulnerability/`
- Tests live in `secure-app/src/test/java/victor/training/vulnerability/`
- File naming: `V{N}_{VulnerabilityName}` (e.g., `V1_SQLInjection`, `V3_XSS`) — the number guides exercise order
- Each exercise is self-contained in a single file (entity, repo, controller, DTOs all together)
- Use Spring Boot test slices (`@SpringBootTest` + `@AutoConfigureMockMvc`) for integration tests
- Use `@MockBean JwtDecoder` + `@WithMockUser` + `@ActiveProfiles("test")` for test security setup
- Profile `disabled` disables all security for focusing on vulnerability exercises
- Package: `victor.training` (NOT `victor.training.spring`)

## Design Principles
1. **Minimal noise** — only the vulnerability, nothing else. Inline entities, no artificial layers.
2. **Self-contained** — each exercise works independently.
3. **Browser-first** — student sees visual impact (alert, exposed data), not just JSON output.
4. **Cascade > one-shot** — complex exercises unfold progressively.
5. **Don't change code without discussing the plan first** — each exercise is planned in dialogue, not unilaterally.

## Key Decisions
- Solutions go on a separate `solutions` branch (named exactly `solutions`), not inline as comments
- Exercise ordering follows the TRAINING-PLAN.md progression
- Frontend HTML pages exist for vulnerability exercises (in `static/vulnerability/`)
- The attacker app (secure-app-attacker, port 8081/9999) demonstrates CSRF/CORS attacks
- BruteForceAttacker.java is a standalone main() that brute-forces the password reset

## Stack
- Java 17+ / Spring Boot 3
- Spring Security (profile `disabled` for injection exercises)
- H2 in-memory database
- OWASP HTML Sanitizer (dependency in pom.xml)
- `.http` files (IntelliJ HTTP client)
- Frontend: vanilla HTML/JS (no framework — so innerHTML vs textContent is visible)
