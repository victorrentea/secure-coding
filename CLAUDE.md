# Secure Coding Training — Project Rules

## Project Overview
This is the hands-on exercise repository for Victor Rentea's Secure Coding training for backend developers (mostly Java/Spring). The codebase contains intentional vulnerabilities that students must identify and fix.

## Branch Strategy
- **`main`** — Starting code with vulnerabilities. This is what students clone and work on.
- **`solutions`** — Branch with all fixes applied. Trainer reference only.

## Testing Strategy
- Each vulnerability exercise should have a corresponding **integration test**.
- Tests on `main` branch are **`@Disabled`** with a hint message (e.g., `@Disabled("Fix the SQL injection vulnerability first")`).
- Tests on `solutions` branch are **enabled** (the `@Disabled` annotation is removed).
- Tests demonstrate the vulnerability is fixed by:
  - Verifying that malicious input is rejected or neutralized
  - Verifying that legitimate input still works correctly
- Students can un-comment/enable tests one by one as they fix each exercise.

## Exercise Structure
Each vulnerability exercise file should contain:
1. A **vulnerable endpoint** that students can exploit
2. Clear **TODO comments** with numbered steps
3. A short **context comment** explaining the scenario
4. Commented-out solution hints (kept minimal — full solutions live on `solutions` branch)

## Code Conventions
- All vulnerability exercises live in `secure-app/src/main/java/victor/training/spring/vulnerability/`
- Tests live in `secure-app/src/test/java/victor/training/spring/`
- Each exercise is self-contained in a single file (entity, repo, controller, DTOs all together)
- Use Spring Boot test slices (`@SpringBootTest` + `@AutoConfigureMockMvc`) for integration tests
- Use `@WithMockUser` for auth-related tests
- Profile `disabled` disables all security for focusing on vulnerability exercises

## Training Agenda
Reference: victorrentea.ro/catalog (Secure Coding training page)
<!-- TODO: paste the actual agenda from the catalog here -->

## Key Decisions
- Solutions go on a separate `solutions` branch, not inline as comments
- Tests are disabled on main, enabled on solutions
- Exercise ordering follows the TRAINING-PLAN.md progression
