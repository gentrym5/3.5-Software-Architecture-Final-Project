# Proposed Changes — Apache Commons Net 3.5

Two sections:
1. **Consistency fixes** — problems with the patterns as they exist today
2. **New patterns after deletion** — what to introduce once the files in `files-to-remove.md` are gone

---

## Part 1 — Making Existing Patterns Consistent

### ~~Problem 1: Command Representation Is Split~~ ✅ DONE
**Pattern affected:** Observer

`FTP` uses `FTPCmd` — a proper Java **enum** (modern, type-safe).
Every other protocol (`SMTPCommand`, `NNTPCommand`, `IMAPCommand`, `POP3Command`) still uses an **interface of static integer constants** — the old pre-Java-5 approach.

**Implemented:** `SMTPCommand`, `NNTPCommand`, and `POP3Command` converted to enums matching the structure of `FTPCmd`. `IMAPCommand` was already an enum and required no change. Type-safe `sendCommand(XxxCommand, String)` overloads added to `SMTP`, `NNTP`, and `POP3` base classes. Old `int`-based overloads deprecated for backwards compatibility.

See `DocumentedChanges/changes-related-to-problem-one.md` for full details.

---

### Problem 2: Observer Firing Is Incomplete
**Pattern affected:** Observer

`ProtocolCommandSupport` lives on `SocketClient`, so every TCP client *technically* supports it. However, only the `FTP` base class consistently fires `commandSent` and `replyReceived` events. `SMTP`, `IMAP`, `NNTP`, and `Telnet` base classes do not fire these events uniformly.

**Proposal:** Add `fireCommandSent()` and `fireReplyReceived()` calls in all protocol base classes (`SMTP`, `IMAP`, `NNTP`, `Telnet`) at every command send and reply receive point. The Observer infrastructure is already there — it just needs to be wired consistently.

---

### Problem 3: Facade Is Only Explicit in FTP
**Pattern affected:** Facade

`FTPClient` is a clear Facade over the raw `FTP` command layer. The two-tier separation (low-level command class → high-level client facade) is well-defined there.

For `SMTP`, `IMAP`, and `NNTP`, the base class and client class exist but the facade boundary is blurry — business logic and raw command sending are mixed in the client class rather than cleanly separated.

**Proposal:** Enforce the two-tier boundary consistently. The base class (`SMTP`, `IMAP`, `NNTP`) should only expose raw command send/receive. The client class (`SMTPClient`, `IMAPClient`, `NNTPClient`) should only expose high-level operations and delegate all protocol commands to the base.

---

### Problem 4: Factory + Strategy Parsing Only Exists in FTP
**Pattern affected:** Factory, Strategy

`FTPClient` uses `FTPFileEntryParserFactory` + `FTPFileEntryParser` to handle different server directory listing formats. This is a clean Factory + Strategy combination.

`NNTPClient` parses article and newsgroup listings inline with no abstraction. If the server returns an unexpected format, there is no clean extension point.

**Proposal:** Apply the same Factory + Strategy parser pattern to `NNTPClient`. Define an `NNTPResponseParser` interface with implementations for standard and extended NNTP response formats, and a factory to select the right one.

---

### Problem 5: SSL Variants Are Inconsistent Across Protocols
**Pattern affected:** Template

FTP, SMTP, IMAP, and POP3 all have SSL variants (`FTPSClient`, `SMTPSClient`, `IMAPSClient`, `POP3SClient`). NNTP and Telnet do not.

**Proposal:** Either add `NNTPSClient` and `TelnetSClient` (extending `NNTPClient` and `TelnetClient`) to complete the pattern, or explicitly document that NNTP-over-SSL and Telnet-over-SSL are out of scope. The inconsistency should be a deliberate decision, not an accident.

---

## Part 2 — New Patterns to Introduce After Deletions

Once the files in `files-to-remove.md` are removed, the following patterns become appropriate to add.

---

### ~~New Pattern 1: Builder~~ ✅ DONE
**Replaces:** `FTPClientConfig` plain configuration object

`FTPClientConfig` is currently a mutable bean with dozens of setters (server system type, date format, timezone, recent date format, short month names, etc.). This is hard to use correctly and makes configuration sequences hard to follow.

**Implemented:** `FTPClientConfig.Builder` inner class added with fluent setter methods and a `build()` method. All existing constructors and setters remain intact — the Builder is fully additive.

See `DocumentedChanges/changes-related-to-builder-pattern.md` for full details.

---

### New Pattern 2: Singleton
**Applies to:** Default socket factories

`SocketClient` and `DatagramSocketClient` both hold static default factory references (`SocketFactory.getDefault()`, `ServerSocketFactory.getDefault()`). These are currently raw static fields with no lifecycle control.

**After cleanup**, formalize these as **Singleton** instances via a dedicated `SocketFactoryProvider` class with a controlled `getInstance()` method. This makes the factory lifecycle explicit and testable.

---


### Summary Table

| # | Pattern | Status | Action |
|---|---|---|---|
| 1 | Factory | Exists (FTP only) | Extend to NNTP |
| 2 | Abstract Factory | Exists (FTP only) | Extend to NNTP |
| 3 | Strategy | Exists (FTP parsers) | Extend to NNTP response parsing |
| 4 | Observer | ✅ Command path standardized | Full event wiring still needed (Problems 2–5) |
| 5 | Template | Consistent | No change needed |
| 6 | Facade | Exists (FTP only, cleanly) | Enforce boundary in SMTP / IMAP / NNTP |
| 7 | Composite | Consistent | No change needed |
| 8 | Decorator | Consistent | No change needed |
| 9 | Adapter | Consistent | No change needed |
| 10 | Iterator | Consistent (NNTP) | No change needed |
| 11 | **Builder** | ✅ Done | `FTPClientConfig.Builder` implemented |
| 12 | **Singleton** | Does not exist | Add for default socket factories |
| 13 | **Bridge** | Does not exist | Add for shared TLS negotiation logic |
