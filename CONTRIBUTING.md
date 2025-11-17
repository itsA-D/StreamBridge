# Contributing and Submission Guide

This guide helps you maintain a clear commit history and prepare the repository for submission.

## Commit message conventions

Use short, descriptive messages. Conventional Commits are recommended:

- feat(scope): add a user-visible feature
- fix(scope): a bug fix
- docs(scope): documentation only changes
- refactor(scope): code change that neither fixes a bug nor adds a feature
- perf(scope): performance improvement
- test(scope): adding or fixing tests
- build(scope): build system or external dependency changes
- ci(scope): CI configuration changes
- chore(scope): housekeeping, no src or test changes

Examples:
- feat(native): add JNI bridge for frame processing
- fix(camera): correct NV21 buffer stride handling
- docs(readme): add setup summary and screenshots

## Branch naming

- feature/<short-name>
- fix/<short-name>
- docs/<short-name>

Example: `feature/jni-bridge`, `fix/opencv-link`, `docs/screenshots`.

## Pull Request notes (optional)

- What changed and why
- How to test (steps + expected result)
- Screenshots/GIFs (if UI/visual)

## Submission preparation checklist

- [ ] README updated (features, setup, architecture, screenshots/GIF)
- [ ] Project builds (Android) with NDK + OpenCV configured
- [ ] Web viewer builds with `tsc`
- [ ] Commit history shows incremental development (no single "final" commit)
- [ ] Push to GitHub and provide the repository link

## Tip: Generating a dev-style commit history

If you already implemented most features, you can still create a readable commit timeline by running the helper script:

```
./scripts/create_dev_commits.ps1
```

Review the log with:

```
git log --oneline --decorate -n 20
```

Tweak messages or squash as needed before pushing.
