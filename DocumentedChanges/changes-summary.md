# Documented Changes — Apache Commons Net 3.5

Two architectural changes were implemented. This document describes what changed and what each change allows or locks off for future work.

---

## Change 1 — Command Representation Unified to Enum (Part 1 Problem 1)

### What changed
- `SMTPCommand.java` — converted from `final class` with `static final int` constants to a proper `enum`
- `NNTPCommand.java` — same conversion
- `POP3Command.java` — same conversion
- `SMTP.java` — added `sendCommand(SMTPCommand, String)` and `sendCommand(SMTPCommand)` overloads; old `int`-based overloads deprecated
- `NNTP.java` — added `sendCommand(NNTPCommand, String)` and `sendCommand(NNTPCommand)` overloads; old `int`-based overloads deprecated
- `POP3.java` — added `sendCommand(POP3Command, String)` and `sendCommand(POP3Command)` overloads; old `int`-based overloads deprecated

### What this enables
All future pattern work on these three protocols now has a type-safe, consistent command representation to build on. Extending `NNTPCommand` with new commands (e.g. for RFC 3977 NNTP extensions) can be done by adding enum constants rather than remembering integer offsets.

### What this locks off
- The old `sendCommand(int, String)` overloads are deprecated but kept for binary compatibility. **Do not remove them** without a major-version bump since external callers may use them.
- The enum ordinal values (`ARTICLE.ordinal() == 0`, etc.) are now an implementation detail. Never persist or transmit enum ordinals — use `getCommand()` for the wire string.

---

## Change 2 — Builder Added to FTPClientConfig (Part 2 Builder)

### What changed
- `FTPClientConfig.java` — added inner class `FTPClientConfig.Builder` with fluent setter methods for all optional configuration fields and a `build()` method that returns a fully configured `FTPClientConfig`

### What this enables
Client code can now construct `FTPClientConfig` without positional-parameter guessing:
```java
FTPClientConfig config = new FTPClientConfig.Builder(FTPClientConfig.SYST_UNIX)
    .serverTimeZoneId("America/New_York")
    .defaultDateFormatStr("MMM d yyyy")
    .build();
```
This is additive and backwards compatible — all existing constructors and setters on `FTPClientConfig` remain in place.

### What this locks off
- The `Builder` is the intended construction path going forward. The old telescoping constructors are still present but should be considered legacy.
- If full immutability is desired (removing the public `setXxx()` methods), that is a separate breaking-change decision and must not be made without a major-version bump.

---

## Files Deleted

| File / Folder | Reason |
|---|---|
| `ftp-client/` folder | Flat-package duplicate of `src/main/java/.../ftp/` — not canonical source |
| All `package-info.java` files | Javadoc-only; no executable code |
| `src/main/java/examples/` | Demo code, not part of the library |
| `checkstyle.xml`, `checkstyle-suppressions.xml`, `findbugs-exclude-filter.xml` | CI/build tooling only |
| `org/apache/commons/net/bsd/` | Legacy BSD r-commands (rexec, rlogin, rsh) — obsolete protocols |
| `FTPCommand.java` | Deprecated; superseded by `FTPCmd.java` enum which already existed |
| `ArticlePointer.java` | Deprecated; superseded by `ArticleInfo.java` which already existed |

---

## Remaining Proposed Changes (Not Yet Implemented)

The following items from `proposed-changes.md` are still outstanding. They are safe to implement independently of each other and of the two changes above.

| # | Change | Pattern | Notes |
|---|---|---|---|
| P2 | Observer firing incomplete | Observer | Wire `fireCommandSent()` / `fireReplyReceived()` into SMTP, IMAP, NNTP, Telnet base classes. Infrastructure exists on `SocketClient`; only the call sites are missing. |
| P3 | Facade boundary blurry in non-FTP | Facade | `SMTPClient`, `IMAPClient`, `NNTPClient` mix raw commands with business logic. Enforce base = raw, client = high-level. Touches three files. |
| P4 | Factory + Strategy parsing only in FTP | Factory / Strategy | Add `NNTPResponseParser` interface + factory to `NNTPClient`. Does not affect any currently-touched files. |
| P5 | SSL variants inconsistent | Template | Add `NNTPSClient` / `TelnetSClient`, or document the omission as intentional. |
| N2 | Singleton for socket factories | Singleton | Formalize `SocketFactoryProvider` with `getInstance()`. Low risk, touches `SocketClient` and `DatagramSocketClient`. |

**Bridge Pattern is out of scope** and should not be implemented.
