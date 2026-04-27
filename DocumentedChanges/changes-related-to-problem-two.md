# Part 1 Problem 2 — Observer Firing Justification

**Change:** Verify and confirm that `fireCommandSent()` and `fireReplyReceived()` are consistently called at every command send and reply receive point across all protocol base classes (`SMTP`, `IMAP`, `NNTP`, `Telnet`).

---

## 1. The Pattern

**Observer (GoF Behavioral Pattern)**

The Observer pattern defines a one-to-many dependency between objects so that when one object (the subject) changes state, all its dependents (observers) are notified automatically. In Apache Commons Net, `ProtocolCommandSupport` is the subject. Any object implementing `ProtocolCommandListener` is an observer. The two events that observers care about are:

- `fireCommandSent(command, message)` — fired every time a command is written to the server
- `fireReplyReceived(replyCode, message)` — fired every time a reply is read from the server

The infrastructure for this is inherited by every TCP client through `SocketClient`, which owns the `ProtocolCommandSupport` instance. The problem was that the infrastructure existed but was not consistently used across all protocol base classes.

---

## 2. Why This Pattern

`SocketClient` owns a `ProtocolCommandSupport` instance and exposes `fireCommandSent()` and `fireReplyReceived()` to all subclasses. This means every protocol client — `FTP`, `SMTP`, `IMAP`, `NNTP`, `Telnet` — technically supports Observer registration out of the box. However, support is meaningless unless the base class actually calls the fire methods at the right points.

Before this change, only `FTP` called both `fireCommandSent()` and `fireReplyReceived()` consistently. The other protocols had partial or missing event firing:

- `SMTP` — `fireCommandSent()` was present in `__sendCommand()` (added in Problem 1), but `fireReplyReceived()` was absent from `__getReply()`.
- `IMAP` — `fireCommandSent()` was present in `sendCommandWithID()` and `fireReplyReceived()` was present in `__getReply()`. Both were already wired.
- `NNTP` — `fireCommandSent()` was present in `sendCommand()` and `fireReplyReceived()` was present in `__getReply()`. Both were already wired.
- `Telnet` — operates on raw binary byte streams rather than line-oriented text commands. There is no concept of a string command being sent or a reply code being received, so Observer event firing does not apply to `Telnet` and is intentionally excluded.

The fix is therefore focused: add `fireReplyReceived()` to `SMTP.__getReply()` so that the Observer contract is complete for SMTP. All other protocols either already fire both events or are architecturally incompatible with the pattern (Telnet).

The Observer pattern is the right choice here because it allows transparent logging, monitoring, and auditing of all protocol traffic without modifying the protocol clients themselves. A `PrintCommandListener` can be registered to log every SMTP command and reply without touching `SMTPClient`.

---

## 3. Why Not Other Patterns

### Decorator
A Decorator could wrap the writer and reader streams to intercept bytes as they are written and read. However, this would capture raw bytes at the stream level, not structured protocol messages. The Observer pattern operates at the protocol level — it receives the command string and the reply code — which is far more useful for logging and monitoring. **Not appropriate** because it operates at the wrong abstraction level.

### Mediator
A Mediator could centralize all protocol communication through a single dispatch object. This would require restructuring the existing class hierarchy and would introduce tight coupling between all protocol classes and the mediator. The Observer pattern already achieves decoupling without restructuring. **Not appropriate** because it requires architectural surgery that adds complexity with no benefit.

### Strategy
Strategy allows swapping algorithms at runtime. The goal here is not to vary how commands are sent — it is to notify listeners whenever a command is sent. These are different concerns. **Not appropriate** because notification is not an interchangeable algorithm.

### Template Method
A Template Method could define `sendCommand()` in `SocketClient` and call `fireCommandSent()` there, forcing all subclasses to go through the hook. This is a valid alternative, but it would require refactoring all protocol base classes to delegate to a shared `sendCommand()` skeleton, which is a much larger change. The Observer pattern achieves the same result with a single line added per class. **Possible but disproportionately invasive** for the scope of this fix.

---

## 4. Components Needed to Implement the Pattern

| Component | Role |
|---|---|
| `ProtocolCommandSupport` | Already exists on `SocketClient`. Manages the list of registered listeners and fires events. No change needed. |
| `fireCommandSent(String command, String message)` | Already called in `SMTP.__sendCommand()`, `IMAP.sendCommandWithID()`, and `NNTP.sendCommand()`. Confirms the command was sent to the server. |
| `fireReplyReceived(int replyCode, String message)` | Added to `SMTP.__getReply()`. Already present in `IMAP.__getReply()` and `NNTP.__getReply()`. Confirms the server reply was received. |
| `SMTP.__getReply()` | **Modified** — added `fireReplyReceived(_replyCode, _replyString + SocketClient.NETASCII_EOL)` after the reply code is parsed, matching the pattern already used in `NNTP.__getReply()`. |
| `Telnet` | **Excluded by design** — operates on raw binary streams with no line-oriented command/reply structure. Observer event firing does not apply. |

---

## 5. Pros and Cons

### Pros
- **Uniform Observer coverage** — after this change, every line-oriented protocol (FTP, SMTP, IMAP, NNTP) fires both `commandSent` and `replyReceived` events consistently. A single `PrintCommandListener` registered on any of these clients will now capture the full conversation.
- **Zero impact on existing callers** — adding `fireReplyReceived()` to `SMTP.__getReply()` is invisible to all existing `SMTPClient` users. No API surface changes.
- **Minimal change** — one line added in one method in one file. The risk of introducing a regression is essentially zero.
- **Consistent with FTP** — `FTP.java` has been the reference implementation for this pattern since the library's inception. This change brings SMTP into alignment with it.
- **Enables transparent logging** — registering a `PrintCommandListener` on an `SMTPClient` will now produce a complete protocol transcript, which is the primary use case for the Observer infrastructure.

### Cons
- **Telnet remains excluded** — the Observer pattern cannot be applied to `Telnet` without a more fundamental redesign of how Telnet handles communication. This leaves one protocol without Observer support, which is an intentional and documented limitation rather than an oversight.
- **No new functionality** — this change does not add any user-visible feature. The benefit is architectural correctness and consistency, which may be less visible than a feature addition.
- **Partial history** — the Observer infrastructure has existed since the library's early versions but was never consistently wired. This fix closes that gap, but the inconsistency existed undetected for many release cycles, suggesting that few users rely on the Observer for non-FTP protocols.

---

## Summary of Code Changes

**`SMTP.java`** — added `fireReplyReceived(_replyCode, line + SocketClient.NETASCII_EOL)` inside `__getReply()`, immediately after `_replyCode` is set from the final reply line. This matches the exact pattern used in `NNTP.__getReply()` (line 155) and `FTP.__getReply()`.

**`IMAP.java`** — no change required. `fireCommandSent()` is called in `sendCommandWithID()` and `fireReplyReceived()` is called in `__getReply()`. Both are already present.

**`NNTP.java`** — no change required. `fireCommandSent()` is called in `sendCommand()` and `fireReplyReceived()` is called in `__getReply()`. Both are already present.

**`Telnet.java`** — no change. Observer event firing is architecturally inapplicable to Telnet's binary stream model and is intentionally excluded.

No code written by teammates (Problem 1 enum changes, Builder pattern, Singleton pattern) was modified.
