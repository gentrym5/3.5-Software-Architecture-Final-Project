# Program Overview — Apache Commons Net 3.5

## What It Is

Apache Commons Net is a **Java networking library** that provides client-side implementations of common internet protocols. It is not an application you run — it is a reusable library that other Java programs depend on to communicate over a network.

---

## What It Does

It implements the client side of 10+ standard internet protocols, letting Java applications:

| Protocol | Class | Purpose |
|---|---|---|
| FTP / FTPS | `FTPClient`, `FTPSClient` | Upload/download files from FTP servers |
| SMTP / SMTPS | `SMTPClient`, `SMTPSClient` | Send email |
| IMAP / IMAPS | `IMAPClient`, `IMAPSClient` | Read email from a mail server |
| POP3 / POP3S | `POP3Client`, `POP3SClient` | Retrieve email |
| NNTP | `NNTPClient` | Read/post Usenet newsgroup articles |
| Telnet | `TelnetClient` | Remote terminal sessions |
| NTP / Time | `NTPUDPClient`, `TimeTCPClient` | Synchronize clocks, get network time |
| Echo | `EchoTCPClient`, `EchoUDPClient` | Test connectivity (echo back data) |
| Finger | `FingerClient` | Look up user info on a server |
| Daytime | `DaytimeTCPClient` | Get human-readable time from server |
| Chargen | `CharGenTCPClient` | Character generator (testing) |
| Discard | `DiscardTCPClient` | Discard data (testing) |
| BSD r-commands | `RExecClient`, `RLoginClient`, `RCommandClient` | Legacy remote execution |
| CIDR / Subnet | `SubnetUtils` | IP address/subnet math utilities |

---

## Structural Organization

The codebase splits into two transport-level bases:

- **`SocketClient`** — abstract base for all TCP-based protocol clients
- **`DatagramSocketClient`** — abstract base for all UDP-based protocol clients

Every protocol client extends one of these two. The library also includes:

- **`org.apache.commons.net.io`** — custom I/O stream wrappers (NetASCII conversion, dot-terminated readers/writers, copy stream utilities)
- **`org.apache.commons.net.ftp.parser`** — pluggable FTP directory listing parsers for different OS formats (Unix, Windows NT, VMS, OS/400, Netware, etc.)
- **`org.apache.commons.net.util`** — SSL utilities, Base64, subnet utils, listener list

---

## Notable Submodule: `ftp-client/`

There is a standalone `ftp-client/` folder at the project root that contains a **flat-package copy** of just the FTP-related classes. This is a study/extraction artifact — the canonical source lives at `src/main/java/org/apache/commons/net/ftp/`.
