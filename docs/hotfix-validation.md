# 1.0.0-hotfix.1 validation

Base: `543d0ef38d5e8f6676e29cfe507fdacdb296ec0c`.
The incident included keepalive timeouts before closed-channel errors, followed by
synchronous webhook HTTP 429 failures propagating through the logging/Netty path.
This change removes that propagation and network wait. It does not establish the
cause of the initial keepalive timeouts.

## Regression and build gate

On 2026-09-09 JST, using Java 21.0.2, the existing Gradle 8.5 wrapper, and Windows:

```powershell
mise.exe exec java@21.0.2 -- ./gradlew.bat --no-daemon :common:test build
```

- 16 tests passed, with zero failures, errors, or skipped tests.
- All-platform, Bukkit, BungeeCord, Velocity, and their test plugin builds passed.
- Tests cover blocked HTTP with concurrent logging callers and queue saturation,
  bounded immutable formatting, 60-second aggregation, 429 cooldown and retry
  limits, incomplete 429 responses, response timeout, refused connections, secret
  redaction, shutdown cancellation, and appender registration ownership.
- Existing CI runs the same `build` gate, including the new common tests.
- Diff review and `git diff --check` passed. This repository has no feature specs;
  the notification contract is documented in README.md.

## Paper smoke check

An isolated Paper 26.2 build 121 instance used only the hotfix and the existing
Bukkit exception test plugin. It bound to loopback and used a loopback-only dummy
HTTPS endpoint. No production Discord webhook was used.

- Startup completed and `MinecraftServerExceptionDiscordSender v1.0.0-hotfix.1`
  enabled successfully.
- Both `Test exception (bukkit)` and `java.lang.StackOverflowError` were exercised.
- The worker recorded delivery failure counts; no `AppenderLoggingException` or
  appender processing failure was emitted.
- The plugin disabled successfully and the server exited with code 0, about
  0.9 seconds after `stop`.
- BungeeCord and Velocity were built, but no runtime smoke test was performed on
  those platforms.

Paper JAR SHA-256:
`0de30efb024bc8b83c9c7d507d11802897ad8056b6110ec09fe1a91d126ccb54`

Tested Bukkit hotfix JAR SHA-256:
`8711e3804cdab498976ec155fe5837a6ba29df44b0778b91d0bc34949174e7f8`

Deploy only this tested notifier artifact at the scheduled restart. Preserve the
previous notifier artifact and checksum for rollback; do not update unrelated
server plugins, the server JAR, client distributions, or server configuration.
