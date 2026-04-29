# Part 1 Problem 3 — Facade Boundary Justification

**Change:** Reinforce the Facade pattern in `SMTPClient`, `IMAPClient`, and `NNTPClient` by moving every remaining piece of raw-protocol logic out of the high-level client classes and into their respective base classes (`SMTP`, `IMAP`, `NNTP`). The client classes now delegate all protocol-stream access through new helper methods on the base classes.

---

## 1. The Pattern

**Facade (GoF Structural Pattern)**

The Facade pattern provides a unified, simplified interface to a complex subsystem. In Apache Commons Net, the canonical example is `FTPClient`, which sits in front of the lower-level `FTP` command class. `FTP` exposes the raw protocol — sending command bytes, reading reply codes, and managing the control stream — while `FTPClient` offers high-level operations such as `login`, `retrieveFile`, `storeFile`, and `listFiles` that hide every wire-level detail from the caller.

For the project's other text-based protocols, the same two-tier structure already existed in name (`SMTP` / `SMTPClient`, `IMAP` / `IMAPClient`, `NNTP` / `NNTPClient`), but the boundary between the two tiers was not enforced. High-level client methods were reaching into the protected control streams of the base class (`_writer`, `_writer_`, `_reader_`) to construct writers and readers themselves, and `IMAPClient.append` was inspecting raw protocol reply codes (`IMAPReply.isContinuation`) and invoking `sendData` directly. These leaks meant the client classes had to know about wire-format details that, by contract, should live in the base.

The contract this change enforces is exactly the one `FTPClient` already follows:

- The **base class** (`SMTP`, `IMAP`, `NNTP`) owns every interaction with the raw protocol streams. It is the only place that constructs `DotTerminatedMessageReader` / `DotTerminatedMessageWriter` instances, the only place that interprets continuation reply codes, and the only place that reads from `_reader_` or writes to `_writer_`.
- The **client class** (`SMTPClient`, `IMAPClient`, `NNTPClient`) calls the base class through high-level convenience methods (`helo`, `mail`, `rcpt`, `data`, `doCommand`, `post`, `ihave`) and the new raw-I/O accessors introduced here (`getDataWriter`, `appendWithData`, `openMessageReader`). It contains no `new DotTerminatedMessage*` constructions and no direct field access to the control streams.

---

## 2. Why This Pattern

The Facade is the right choice for three reasons that are specific to this codebase:

**It is already the design language of the project.** `FTPClient` is a Facade. The teaching value of this final project is that all four protocol clients should follow the same architectural pattern — students reading `FTPClient` and then `SMTPClient` should not have to reverse-engineer two different conventions. Picking any other pattern here would introduce architectural inconsistency for no benefit.

**It hides wire-format details that callers should never need.** A user of `SMTPClient.sendMessageData()` should not have to know that an SMTP DATA payload is dot-terminated, or which buffered writer the protocol stream is wrapped in. Before this change, the client class itself had to know those details so it could call `new DotTerminatedMessageWriter(_writer)`. After this change, that knowledge lives in `SMTP.getDataWriter()`, where it belongs. The same argument applies to `IMAPClient.append()` (which previously had to know about IMAP literal continuation responses) and to `NNTPClient.postArticle()` / `forwardArticle()` / `__retrieve()` (which previously had to construct dot-terminated readers and writers themselves).

**It composes cleanly with the other patterns the project introduces.** The Observer reinforcement in Part 1 Problem 2 fires `commandSent` / `replyReceived` from the base classes; the Facade reinforcement here keeps every raw command path inside those same base classes, so there is exactly one place where Observer events can be emitted. The forthcoming Factory + Strategy parser work in Part 1 Problem 4 will plug into `NNTPClient` to handle response parsing — that work is materially easier when the client class is already a thin Facade rather than a mixed bag of parsing + raw I/O. The Singleton work in Part 2 (`SocketFactoryProvider`) feeds the `SocketClient` infrastructure that the base classes inherit from, and is unaffected by this refactor.

---

## 3. Why Not Other Patterns

### Adapter
An Adapter would translate between two incompatible interfaces. The two tiers here already speak compatible interfaces — the client class already calls methods on the base class. The problem is not interface incompatibility; it is that some calls bypass the intended interface and reach into protected fields. Adapter would add a translation layer where none is needed. **Not appropriate** because there is no interface mismatch to bridge.

### Mediator
A Mediator would centralize communication between many peer components by routing messages through a single dispatcher. The relationship between `SMTPClient` and `SMTP` is not a peer relationship — it is a strict one-way subclass relationship in which the client class is the public Facade and the base class is the implementation it sits on. Introducing a Mediator would require pulling the protocol logic out of `SMTP` into a separate object and rewiring every protocol client to communicate through it. **Not appropriate** because the existing relationship is hierarchical, not peer-to-peer, and Mediator's value (decoupling many-to-many communication) does not apply.

### Decorator
A Decorator could wrap `SMTP` with a class that adds high-level operations like `setSender(...)` or `addRecipient(...)`. Functionally this would produce something that looks similar to the current Facade. The cost, though, is that every existing public subclass relationship (`SMTPClient extends SMTP`, `AuthenticatingSMTPClient extends SMTPClient`, `SMTPSClient extends SMTPClient`) would have to be re-expressed as composition, breaking binary and source compatibility for every external caller. **Not appropriate** because the cost of restructuring the inheritance hierarchy vastly exceeds the benefit, and Facade achieves the same encapsulation outcome without breaking the public API.

### Template Method
A Template Method could define the high-level flow of operations like "send a message" inside the base class as a fixed skeleton and let subclasses fill in steps. This would invert the current direction of control: the base class would call into the client class. That is the wrong direction for a Facade — the whole point is that callers see the client class and never have to touch the base. Template Method also fits poorly because the high-level operations across SMTP, IMAP, and NNTP are not variations of one shared algorithm; they are distinct protocol verbs (RCPT, APPEND, ARTICLE) with fundamentally different semantics. **Not appropriate** because it inverts control and assumes a uniformity across protocols that does not exist.

### Bridge
The proposed-changes document explicitly lists Bridge as out of scope. Bridge would let the abstraction (the high-level operation) and the implementation (the protocol stream) vary independently, but in this codebase the implementation is fixed — there is one socket, one buffered reader, one buffered writer per connection. The variability that Bridge solves does not exist here. **Not appropriate** and **explicitly excluded** by the project plan.

---

## 4. Components Needed to Implement the Pattern

| Component | Role | Status |
|---|---|---|
| `SMTP` (base class) | Owns all raw SMTP protocol I/O. Now also exposes a Facade-friendly accessor for the DATA payload writer. | **Modified** — added `protected Writer getDataWriter()`; added imports for `java.io.Writer` and `org.apache.commons.net.io.DotTerminatedMessageWriter`. |
| `SMTPClient` (Facade) | High-level operations only. Delegates payload-writer construction to `SMTP.getDataWriter()`. | **Modified** — `sendMessageData()` now calls `getDataWriter()` instead of `new DotTerminatedMessageWriter(_writer)`. Removed the now-unused `DotTerminatedMessageWriter` import. |
| `IMAP` (base class) | Owns all raw IMAP protocol I/O. Now exposes a single method that performs the two-step APPEND continuation+literal exchange end-to-end. | **Modified** — added `public boolean appendWithData(String args, String message)`. |
| `IMAPClient` (Facade) | High-level operations only. The literal-message APPEND path now calls `appendWithData(...)` and no longer inspects continuation reply codes itself. | **Modified** — `append(String, String, String, String)` now delegates the continuation+literal protocol to `IMAP.appendWithData()`. |
| `NNTP` (base class) | Owns all raw NNTP protocol I/O. Now exposes accessors that wrap the protected control streams in dot-terminated readers and writers. | **Modified** — added `protected BufferedReader openMessageReader()` and `protected Writer getDataWriter()`; added imports for `java.io.Writer`, `DotTerminatedMessageReader`, and `DotTerminatedMessageWriter`. |
| `NNTPClient` (Facade) | High-level operations only. The article-retrieval and article-posting paths no longer access `_reader_` and `_writer_` directly. | **Modified** — both `__retrieve(...)` overloads now return `openMessageReader()`; `postArticle()` and `forwardArticle(...)` now return `getDataWriter()`. |
| `DotTerminatedMessageReader` / `DotTerminatedMessageWriter` | Stream wrappers that implement the dot-stuffing protocol used by SMTP DATA, IMAP APPEND literals, and NNTP article transfer. | **No change** — used as before, but only constructed inside the base classes. |
| `IMAPReply` | Provides reply-code classification (`isContinuation`, `isSuccess`). | **No change** — these checks now happen only inside `IMAP.appendWithData()`, never in the client. |
| `FTP` / `FTPClient` | The reference implementation of the two-tier Facade boundary. | **No change** — used as the model the other protocols are now aligned with. |

---

## 5. Pros and Cons

### Pros

- **Architectural consistency with FTP.** All four primary protocol clients (`FTPClient`, `SMTPClient`, `IMAPClient`, `NNTPClient`) now follow the same two-tier Facade contract: base class = raw protocol, client class = high-level operations. A reader who learns `FTPClient` can now read `SMTPClient`, `IMAPClient`, or `NNTPClient` and find the same separation of concerns.
- **Single source of truth for raw I/O.** Every dot-terminated reader and writer in the SMTP, IMAP, and NNTP packages is now constructed in exactly one place per protocol. If the wrapping class ever needs to change (for example, to add buffering, instrumentation, or a tracing decorator), the change happens in one method per protocol instead of being scattered across the client class.
- **No public API breakage.** Every modified public method (`SMTPClient.sendMessageData`, `IMAPClient.append`, `NNTPClient.postArticle`, `NNTPClient.forwardArticle`, `NNTPClient.retrieveArticle`, `NNTPClient.retrieveArticleHeader`, `NNTPClient.retrieveArticleBody`) keeps the same signature, the same return contract, and the same observable behavior. External callers do not need to change a single line.
- **The new methods are additive.** `SMTP.getDataWriter()`, `IMAP.appendWithData(...)`, `NNTP.openMessageReader()`, and `NNTP.getDataWriter()` are all new methods that did not previously exist. Subclasses that already extend these base classes (such as `AuthenticatingSMTPClient`, `AuthenticatingIMAPClient`, `SMTPSClient`, `IMAPSClient`, `NNTPSClient`) inherit them automatically and are not affected.
- **Compatible with the in-flight pattern work.** The change does not touch any code already finalized for Part 1 Problem 1 (command enums), Part 1 Problem 2 (Observer firing), Part 1 Problem 5 (SSL Template variants), Part 2 Builder (`FTPClientConfig.Builder`), or Part 2 Singleton (`SocketFactoryProvider`). The NNTP parsing helpers (`__parseArticlePointer`, `__parseGroupReply`, `__parseNewsgroupListEntry`, `__parseArticleEntry`) are intentionally left in place so that Part 1 Problem 4 (Factory + Strategy parsers) can refactor them without merge conflicts.
- **Cleaner extension point for future protocols.** When a future protocol client is added to the library, the established pattern is now unambiguous: declare the base class with the raw protocol I/O, declare the client class as a Facade, expose `getDataWriter()` / `openMessageReader()`-style accessors on the base, and never reach into protected control streams from the client.

### Cons

- **Multiple base classes were modified.** `SMTP`, `IMAP`, and `NNTP` each received new methods. Each addition is small (a single short method plus, where needed, an import) and additive, but the change is not confined to one file.
- **Some private helpers in `NNTPClient` still touch `_reader_` directly.** The `__readNewsgroupListing`, `__retrieveArticleInfo`, and similar private parsing helpers continue to construct `DotTerminatedMessageReader` instances inline. These were left untouched because they are scoped to be replaced by the Factory + Strategy parser work in Part 1 Problem 4. Migrating them now would create a merge collision with that task. The Facade boundary is still cleanly enforced at every public method.
- **Minor risk of double-wrapping.** Subclasses that override `getDataWriter()` or `openMessageReader()` could theoretically introduce extra layers around the stream. This is a deliberate extension point for future use (logging, instrumentation), but it does mean the responsibility for not double-wrapping rests with the override author.
- **No new user-visible feature.** Like Part 1 Problem 2, this change is architectural — the benefit is consistency and maintainability, not a new capability that callers can invoke. The justification is design clarity.

---

## Summary of Code Changes

**`commons-net-3.5-src/src/main/java/org/apache/commons/net/smtp/SMTP.java`** — added `import java.io.Writer;` and `import org.apache.commons.net.io.DotTerminatedMessageWriter;`. Added a new method `protected Writer getDataWriter()` that returns `new DotTerminatedMessageWriter(_writer)`. Inserted directly after the existing `data()` convenience method.

**`commons-net-3.5-src/src/main/java/org/apache/commons/net/smtp/SMTPClient.java`** — removed the now-unused `import org.apache.commons.net.io.DotTerminatedMessageWriter;`. Replaced `return new DotTerminatedMessageWriter(_writer);` inside `sendMessageData()` with `return getDataWriter();`.

**`commons-net-3.5-src/src/main/java/org/apache/commons/net/imap/IMAP.java`** — added a new method `public boolean appendWithData(String args, String message)` that performs the two-step APPEND wire exchange (sends the APPEND command, checks for continuation, sends the literal data, returns success). Inserted directly after the existing `sendData(String)` method.

**`commons-net-3.5-src/src/main/java/org/apache/commons/net/imap/IMAPClient.java`** — replaced the inline two-step block in `append(String, String, String, String)` (the `final int status = sendCommand(...); return IMAPReply.isContinuation(status) && IMAPReply.isSuccess(sendData(message));` lines) with a single call to `appendWithData(args.toString(), message)`.

**`commons-net-3.5-src/src/main/java/org/apache/commons/net/nntp/NNTP.java`** — added `import java.io.Writer;`, `import org.apache.commons.net.io.DotTerminatedMessageReader;`, and `import org.apache.commons.net.io.DotTerminatedMessageWriter;`. Added two new methods, `protected BufferedReader openMessageReader()` and `protected Writer getDataWriter()`, at the end of the class.

**`commons-net-3.5-src/src/main/java/org/apache/commons/net/nntp/NNTPClient.java`** — replaced `return new DotTerminatedMessageReader(_reader_);` with `return openMessageReader();` in both `__retrieve(int, String, ArticleInfo)` and `__retrieve(int, long, ArticleInfo)`. Replaced `return new DotTerminatedMessageWriter(_writer_);` with `return getDataWriter();` in both `postArticle()` and `forwardArticle(String)`. The private parsing helpers (`__parseArticlePointer`, `__parseGroupReply`, `__parseNewsgroupListEntry`, `__parseArticleEntry`) and the remaining stream-wrapping inside other private helpers (`__readNewsgroupListing`, `__retrieveArticleInfo`, `__readReader`) were intentionally left untouched and are scoped to Part 1 Problem 4 (Factory + Strategy parsers).

No code that was finalized for Part 1 Problem 1, Part 1 Problem 2, Part 1 Problem 5, Part 2 Builder, or Part 2 Singleton was modified. No file outside the `org.apache.commons.net.smtp`, `org.apache.commons.net.imap`, and `org.apache.commons.net.nntp` packages was touched.
