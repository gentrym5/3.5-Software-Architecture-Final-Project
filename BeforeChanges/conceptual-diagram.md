# Conceptual Diagram — Apache Commons Net 3.5

Paste the block below into any Mermaid renderer (e.g. mermaid.live).

```mermaid
graph TD
    subgraph Library["Apache Commons Net Library"]
        direction TB

        subgraph Transport["Transport Layer (Abstractions)"]
            SC[SocketClient\nTCP base]
            DSC[DatagramSocketClient\nUDP base]
        end

        subgraph Protocols["Protocol Clients"]
            FTP[FTP / FTPS\nFile Transfer]
            SMTP[SMTP / SMTPS\nSend Email]
            IMAP[IMAP / IMAPS\nRead Email]
            POP3[POP3 / POP3S\nRetrieve Email]
            NNTP[NNTP\nNewsgroups]
            TELNET[Telnet\nRemote Terminal]
            BSD[BSD r-commands\nrexec / rlogin / rsh]
            SIMPLE_TCP[Simple TCP Services\nEcho / Finger / Daytime / Chargen / Discard]
            SIMPLE_UDP[Simple UDP Services\nEcho / Daytime / Chargen / Discard / NTP / Time]
        end

        subgraph Support["Supporting Systems"]
            IO[I/O Utilities\nNetASCII streams\nCopy stream events]
            FTPPARSER[FTP Parsers\nUnix / NT / VMS / OS400 / etc.]
            FTPCONFIG[FTP Configuration\nFTPClientConfig]
            EVENTS[Protocol Events\nCommand listener / observer]
            UTIL[Utilities\nSSL / Base64 / SubnetUtils]
        end
    end

    SC --> FTP
    SC --> SMTP
    SC --> IMAP
    SC --> POP3
    SC --> NNTP
    SC --> TELNET
    SC --> BSD
    SC --> SIMPLE_TCP

    DSC --> SIMPLE_UDP

    FTP --> FTPPARSER
    FTP --> FTPCONFIG
    FTP --> IO

    SC --> EVENTS
    SC --> UTIL

    APP[Your Application] -->|uses| FTP
    APP -->|uses| SMTP
    APP -->|uses| IMAP
    APP -->|uses| NNTP
    APP -->|uses| TELNET
    APP -->|uses| SIMPLE_UDP
```

---

## Reading This Diagram

- **Transport Layer** — Two abstract bases handle socket lifecycle (open, close, timeout, factory injection).
- **Protocol Clients** — Each protocol is a separate module extending the appropriate transport base. SSL variants (`FTPS`, `SMTPS`, `IMAPS`, `POP3S`) extend their plain counterpart.
- **Supporting Systems** — Shared infrastructure: I/O stream decorators, pluggable FTP directory-listing parsers, a configuration object, an observer/event system for logging commands, and SSL/utility helpers.
- **Your Application** — Consumers import and call the protocol clients directly.
