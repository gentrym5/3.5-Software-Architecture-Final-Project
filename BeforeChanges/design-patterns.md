# Design Patterns — Apache Commons Net 3.5

---

## Creational

### 1. Factory
**Where:** `DefaultFTPFileEntryParserFactory`

`FTPClient` calls `createFileEntryParser(key)` to get a parser for the connected server's OS. The factory inspects the SYST response string and instantiates the correct concrete parser (Unix, NT, VMS, OS/400, etc.) without the client knowing which one it receissved.

- Concrete: `DefaultFTPFileEntryParserFactory.createFileEntryParser(key)`

---

### 2. Abstract Factory
**Where:** `FTPFileEntryParserFactory` (interface)

Defines the contract for all parser factories. `FTPClient` depends on this interface — not the default implementation — so users can swap in an entirely different factory that produces custom parsers. `DefaultFTPFileEntryParserFactory` is the built-in product family.

- Interface: `FTPFileEntryParserFactory`
- Concrete family: `DefaultFTPFileEntryParserFactory`

---

## Structural

### 3. Adapter
**Where:** `CopyStreamAdapter`

`CopyStreamListener` requires two `bytesTransferred` overloads. `CopyStreamAdapter` provides default no-op implementations so callers only override the specific method they need, adapting the interface to a simpler usage contract.

---

### 4. Facade
**Where:** `FTPClient`

`FTP` exposes raw FTP commands (`sendCommand`, `getReplyCode`, etc.) directly mapped to the RFC 959 protocol. `FTPClient` wraps all of that behind a clean, high-level facade: `login()`, `storeFile()`, `retrieveFile()`, `listFiles()` — hiding the multi-step command sequences, reply code checks, data channel setup, and passive/active mode negotiation.

---

### 5. Composite
**Where:** `CompositeFileEntryParser`

Packs multiple `FTPFileEntryParser` implementations into one object that itself implements `FTPFileEntryParser`. It tries each child parser in order, caching the first match. The caller holds a single parser reference and never knows it is talking to a composite of many.

---

### 6. Decorator
**Where:** `org.apache.commons.net.io` stream wrappers

Wraps existing `InputStream`/`OutputStream`/`Reader`/`Writer` instances to add behavior, conforming to the same interface:

| Class | Adds |
|---|---|
| `FromNetASCIIInputStream` | Converts NetASCII `\r\n` → system EOL on read |
| `ToNetASCIIOutputStream` | Converts system EOL → `\r\n` on write |
| `SocketInputStream` / `SocketOutputStream` | Prevents accidental stream close |
| `DotTerminatedMessageReader` | Stops at SMTP/NNTP dot-terminator |
| `DotTerminatedMessageWriter` | Appends dot-terminator on close |

---

## Behavioral

### 7. Strategy
**Where:** `FTPFileEntryParser` and its implementations

Different FTP servers return directory listings in different OS formats. Each format is its own strategy class implementing `FTPFileEntryParser`. The correct strategy is selected at runtime by the factory and injected into `FTPClient`, which calls `parseFTPEntry(line)` without knowing the format.

Concrete strategies: `UnixFTPEntryParser`, `NTFTPEntryParser`, `VMSFTPEntryParser`, `OS400FTPEntryParser`, `NetwareFTPEntryParser`, `MVSFTPEntryParser`, `MacOsPeterFTPEntryParser`, `MLSxEntryParser`

---

### 8. Observer
**Where:** `ProtocolCommandListener`, `ProtocolCommandSupport`, `ProtocolCommandEvent`

`SocketClient` owns a `ProtocolCommandSupport` instance. Any object registers as a `ProtocolCommandListener`. When a command is sent or a reply received, `ProtocolCommandSupport` fires events to all registered listeners — enabling transparent logging (`PrintCommandListener`) without touching the protocol clients.

- Subject: `ProtocolCommandSupport`
- Observer interface: `ProtocolCommandListener`
- Concrete observer: `PrintCommandListener`
- Event: `ProtocolCommandEvent`

---

### 9. Template
**Where:** `SocketClient._connectAction_()`

`SocketClient.connect()` defines the full connection algorithm. Subclasses override the hook method `_connectAction_()` to insert protocol-specific setup without changing the connect/disconnect skeleton.

- `FTP._connectAction_()` — reads the 220 welcome banner
- `FTPSClient._connectAction_()` — adds TLS negotiation on top of FTP's version

---

### 10. Iterator
**Where:** `nntp` package — `ArticleIterator`, `NewsgroupIterator`, `ReplyIterator`

These classes implement `java.util.Iterator<T>` to wrap raw string responses from the NNTP server and expose them as typed, traversable sequences (`Article`, `NewsgroupInfo`) — decoupling the traversal logic from the collection structure.
