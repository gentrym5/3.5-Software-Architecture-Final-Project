# Documented Changes — Apache Commons Net 3.5

Six architectural changes were implemented across Part 1 and Part 2. This document describes what changed, what each change enables, and what it locks off for future work.

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

## Change 2 — Observer Firing Completed for SMTP (Part 1 Problem 2)

### What changed
- `SMTP.java` — added `fireReplyReceived(_replyCode, line + SocketClient.NETASCII_EOL)` inside `__getReply()`, immediately after the reply code is parsed from the final reply line

### What this enables
All four line-oriented protocols (FTP, SMTP, IMAP, NNTP) now fire both `commandSent` and `replyReceived` events consistently. A single `PrintCommandListener` registered on any of these clients will capture a complete protocol transcript. Before this change, registering a listener on `SMTPClient` would log outgoing commands but silently drop all server replies.

### What this locks off
- `Telnet` remains excluded from Observer coverage by design. Telnet operates on raw binary byte streams with no line-oriented command/reply structure, so `fireCommandSent` and `fireReplyReceived` are architecturally inapplicable and must not be added there without a deeper redesign.
- `IMAP` and `NNTP` required no changes — both were already fully wired before this change.

---

## Change 3 — Facade Boundary Enforced in SMTP, IMAP, NNTP (Part 1 Problem 3)

### What changed
- `SMTP.java` — added `protected Writer getDataWriter()` that returns a `DotTerminatedMessageWriter` wrapping the control writer; added required imports
- `SMTPClient.java` — replaced `new DotTerminatedMessageWriter(_writer)` with `getDataWriter()`; removed the now-unused `DotTerminatedMessageWriter` import
- `IMAP.java` — added `public boolean appendWithData(String args, String message)` that performs the two-step APPEND continuation-and-literal wire exchange end-to-end
- `IMAPClient.java` — replaced the inline two-step APPEND block (manual continuation check + `sendData`) with a single call to `appendWithData(args.toString(), message)`
- `NNTP.java` — added `protected BufferedReader openMessageReader()` and `protected Writer getDataWriter()`; added required imports
- `NNTPClient.java` — replaced all direct `new DotTerminatedMessageReader(_reader_)` and `new DotTerminatedMessageWriter(_writer_)` constructions in public methods with `openMessageReader()` and `getDataWriter()`

### What this enables
All four primary protocol clients (`FTPClient`, `SMTPClient`, `IMAPClient`, `NNTPClient`) now follow the same two-tier Facade contract: the base class owns all raw protocol I/O; the client class is a pure high-level Facade. Adding instrumentation, buffering, or tracing to any dot-terminated stream now requires a change in exactly one place per protocol instead of being scattered across the client class.

### What this locks off
- Private parsing helpers in `NNTPClient` (`__readNewsgroupListing`, `__retrieveArticleInfo`) still access `_reader_` directly. These were intentionally left for Part 1 Problem 4 (Factory + Strategy parsers) to avoid a merge collision. The Facade boundary is enforced at every public method; the private helpers are an internal implementation detail scoped to the next change.
- `DotTerminatedMessageReader` and `DotTerminatedMessageWriter` must now only be constructed inside the base classes. Client-class subclasses that override the new accessor methods must not double-wrap the stream.

---

## Change 4 — SSL Variants Added for NNTP and Telnet (Part 1 Problem 5)

### What changed
- `NNTPSClient.java` — new class; extends `NNTPClient`; overrides `_connectAction_()` to perform an SSL/TLS handshake before the NNTP session begins
- `TelnetSClient.java` — new class; extends `TelnetClient`; overrides `_connectAction_()` to perform an SSL/TLS handshake before the Telnet session begins

### What this enables
Every protocol in the library now has a TLS-capable variant. `NNTPSClient` and `TelnetSClient` complete the set that already included `FTPSClient`, `SMTPSClient`, `IMAPSClient`, and `POP3SClient`. All existing NNTP and Telnet logic is inherited unchanged — only the connection step is overridden.

### What this locks off
- If the `NNTPClient` or `TelnetClient` base classes change their connection handling in a future version, the `_connectAction_()` overrides in the SSL variants must be reviewed for compatibility.
- The Template Method pattern used here (override one hook, inherit everything else) means the SSL variants have no independent test surface beyond the connection step. Integration tests must exercise a live or mock TLS handshake to provide meaningful coverage.

---

## Change 5 — Builder Added to FTPClientConfig (Part 2 Builder)

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

## Change 6 — Singleton Introduced for Socket Factory Management (Part 2 Singleton)

### What changed
- `SocketFactoryProvider.java` — new class; Singleton with a private constructor, a static `INSTANCE`, and a public `getInstance()` method; exposes `getSocketFactory()`, `getServerSocketFactory()`, and `getDatagramSocketFactory()` for the shared default factory instances
- `SocketClient.java` — updated to obtain its default `SocketFactory` and `ServerSocketFactory` from `SocketFactoryProvider.getInstance()` instead of holding static fields directly
- `DatagramSocketClient.java` — updated to obtain its default `DatagramSocketFactory` from `SocketFactoryProvider.getInstance()`

### What this enables
The lifecycle and ownership of all default socket factories are now centralized in one place. Any future change to default factory behavior (e.g. adding connection instrumentation or a factory that enforces timeouts) requires a change in `SocketFactoryProvider` only, not in two separate transport base classes.

### What this locks off
- `SocketFactoryProvider` is a global access point. Code that needs a non-default factory must continue to use the existing `setSocketFactory()` / `setServerSocketFactory()` / `setDatagramSocketFactory()` setters on the respective client — the Singleton is for the shared defaults only, not a replacement for per-instance configuration.
- The runtime behavior of the library is unchanged. The Singleton's primary benefit is architectural clarity; it does not introduce new user-visible functionality.

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

One item from `proposed-changes.md` is still outstanding.

| # | Change | Pattern | Notes |
|---|---|---|---|
| P4 | Factory + Strategy parsing only in FTP | Factory / Strategy | Add `NNTPResponseParser` interface + factory to `NNTPClient`. The private parsing helpers (`__parseArticlePointer`, `__parseGroupReply`, `__parseNewsgroupListEntry`, `__parseArticleEntry`) are already in place as candidates for extraction. Does not conflict with any completed change. |

**Bridge Pattern is out of scope** and should not be implemented.
