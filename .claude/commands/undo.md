Revert this project from the "solutions" state to the "exercise" state — ready for participants to hunt, exploit, and fix security vulnerabilities.

Work through ALL files matching `SV*` in the project (Java, .http, .html, tests). Do NOT touch `.md` files.

## Rules for Java files (`SV*.java`)

1. **Lines with `// ✅` at the end of active code**: Remove the entire line.
   - If the ✅ is on a class/record/enum definition, remove the entire type (including its body and closing brace).
2. **Lines with `// ✅` that are standalone comments** (the line is a comment starting with `//` and containing ✅): Remove the line AND any adjacent consecutive commented-out code lines that form the same block. "Adjacent" means immediately before or after, with no blank line separating them. This ensures fix code grouped under a ✅ section header is fully removed.
3. **Lines starting with `// ❌`** (commented-out vulnerable code): Uncomment them — remove the `// ❌` prefix and leading `//` to make the code active again. Adjust indentation to match surrounding code.
4. **Lines where `// ❌` appears at the end of active code**: Just remove the `// ❌...` suffix. Keep the code active.
5. **After all ✅/❌ processing**: Clean up unused imports. The code MUST compile.

## Rules for .http files (`SV*.http`)

1. **Lines/blocks marked `### ✅`**: Remove the `### ✅` comment line AND the request URL(s) and body that follow it (up to the next `###` section header or double blank line).
2. Keep all unmarked requests (investigation/normal requests).
3. After cleanup, collapse any runs of 3+ consecutive blank lines down to 2.

## Rules for .html files (in `static/vulnerability/`)

1. Same convention: if you see ✅, remove (including adjacent block); if you see ❌, uncomment.

## Rules for test files (`SV*Test.java`)

1. **Re-add `@Disabled` annotations** on every `@Test` method. Use the existing `@Disabled("TODO ...")` message if one is visible in a comment; otherwise write a sensible one like `@Disabled("TODO: fix the vulnerability")`.
2. Make sure the `@Disabled` import is present.
3. All tests must pass (the disabled ones are skipped, the rest are green).

## Final verification

After all transformations:
1. Run `mvn -pl secure-app compile` — must succeed with no errors.
2. Run `mvn -pl secure-app test` — must succeed (all tests green, disabled ones skipped).
3. If anything fails, fix it (likely an import or a missing line).

## Important

- Do NOT commit. Leave all changes uncommitted on the current branch.
- Do NOT touch `.md` walkthrough files.
- Process ALL `SV*` exercises, not just SV1–SV4.
