# Secure Coding Training — Project Rules

## Project Overview
This is the hands-on exercise repository for Victor Rentea's Secure Coding training for backend developers (mostly Java/Spring). The codebase contains intentional vulnerabilities that students must identify and fix.

## Branch Strategy
- **`main`** — Unsolved exercises with vulnerabilities. Tests are `@Disabled`. This is what students clone.
- **`solutions`** — Fixed vulnerabilities with tests enabled (green). The trainer pushes this branch with the solved code.
- Work through vulnerabilities one by one. Each gets its own commit on the solutions branch.

## Testing Strategy
- Each vulnerability exercise has a corresponding **integration test**.
- On `main`: tests are **`@Disabled("TODO: fix the ... vulnerability")`**.
- On `solutions`: the `@Disabled` annotation is removed and tests pass green.
- Tests prove the fix works by:
  - Verifying that malicious input is rejected or neutralized
  - Verifying that legitimate input still works correctly
- Students enable tests one by one as they fix each exercise.

## Exercise Structure
Each vulnerability exercise should have:
1. A **vulnerable endpoint** — clean, minimal code showing the problem
2. A **brief markdown description** (`<VulnerabilityName>.md`) next to the Java file explaining what the problem is (very concise)
3. Clear **TODO comments** with numbered steps in the code
4. **No noise** — strip away anything not directly related to demonstrating the vulnerability
5. Both **backend and frontend** parts where applicable

## Code Conventions
- All vulnerability exercises live in `secure-app/src/main/java/victor/training/vulnerability/`
- Tests live in `secure-app/src/test/java/victor/training/`
- Each exercise is self-contained in a single file (entity, repo, controller, DTOs all together)
- Use Spring Boot test slices (`@SpringBootTest` + `@AutoConfigureMockMvc`) for integration tests
- Use `@WithMockUser` for auth-related tests
- Profile `disabled` disables all security for focusing on vulnerability exercises
- Each vulnerability gets a concise `.md` file describing the problem

## Training Agenda
Reference: victorrentea.ro/catalog (Secure Coding training page)
<!-- TODO: paste the actual agenda from the catalog here -->

## Key Decisions
- Solutions go on a separate `solutions` branch (named exactly `solutions`), not inline as comments
- Tests are disabled on main, enabled on solutions
- Exercise ordering follows the TRAINING-PLAN.md progression
- Strip all noise — keep only the vulnerability itself, as minimal and focused as possible
- Do NOT change code without discussing the plan first — we work through vulnerabilities one by one
- Each vulnerability gets a brief, concise `.md` description file next to the Java file
- Frontend HTML pages exist for all 12 vulnerability exercises (in static/vulnerability/)
- The attacker app (secure-app-attacker, port 8081/9999) demonstrates CSRF/CORS attacks
- BruteForceAttacker.java is a standalone main() that brute-forces the password reset
