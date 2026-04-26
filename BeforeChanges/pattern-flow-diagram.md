# Pattern Flow Diagram — Apache Commons Net 3.5

Paste the block below into any Mermaid renderer (e.g. mermaid.live).

```mermaid
flowchart TB
    classDef patternStyle fill:#ffe6cc,stroke:#d79b00,stroke-width:2px,color:#000
    classDef mainStyle fill:#dae8fc,stroke:#6c8ebf,color:#000
    classDef interfaceStyle fill:#d5e8d4,stroke:#82b366,color:#000
    classDef utilStyle fill:#fff2cc,stroke:#d6b656,color:#000

    %% ── Main System Flow ──────────────────────────────────────────────

    APP([Your Application])

    subgraph ClientLayer["Transport Layer"]
        SC["SocketClient (abstract)"]
        DSC["DatagramSocketClient (abstract)"]
    end

    subgraph ProtocolLayer["Protocol Layer"]
        FTP["FTP\ncommand layer"]
        FTPC["FTPClient\nhigh-level API"]
        FTPS["FTPSClient\nSSL variant"]
        OTHER["SMTP · IMAP · NNTP\nTelnet · BSD r-commands"]
        UDP["UDP Clients\nNTP · Echo · Daytime..."]
    end

    subgraph ParserLayer["FTP Parser Layer"]
        FACTORY["DefaultFTPFileEntryParserFactory"]
        PIFACE["FTPFileEntryParser\ninterface"]
        COMPOSITE["CompositeFileEntryParser"]
        PARSERS["UnixFTPEntryParser\nNTFTPEntryParser\nVMSFTPEntryParser\nOS400FTPEntryParser\n..."]
    end

    subgraph EventLayer["Event Layer"]
        CMDSUPPORT["ProtocolCommandSupport"]
        LISTENERS["ProtocolCommandListener\nPrintCommandListener"]
    end

    subgraph IOLayer["I/O Layer"]
        STREAMS["NetASCII Streams\nSocket Streams\nDot-Terminated Wrappers"]
        COPYADAPTER["CopyStreamAdapter"]
    end

    subgraph IterLayer["NNTP Iterators"]
        ITERATORS["ArticleIterator\nNewsgroupIterator\nReplyIterator"]
    end

    %% Main flow connections
    APP --> SC & DSC
    SC --> FTP --> FTPC --> FTPS
    SC --> OTHER
    SC --> NNTP_NODE["NNTPClient"]
    DSC --> UDP
    FTPC --> FACTORY --> PIFACE --> COMPOSITE --> PARSERS
    SC --> CMDSUPPORT --> LISTENERS
    FTPC --> STREAMS --> COPYADAPTER
    NNTP_NODE --> ITERATORS

    %% ── Pattern Annotation Circles ────────────────────────────────────

    P_TEMPLATE(["🔵 Template"])
    P_FACADE(["🟢 Facade"])
    P_FACTORY(["🟠 Factory &\nAbstract Factory"])
    P_STRATEGY(["🔴 Strategy"])
    P_COMPOSITE(["🟣 Composite"])
    P_OBSERVER(["🟡 Observer"])
    P_DECORATOR(["🟤 Decorator"])
    P_ADAPTER(["⚪ Adapter"])
    P_ITERATOR(["🔶 Iterator"])

    %% Template — defines the connect skeleton; subclasses fill in _connectAction_
    P_TEMPLATE -..-> SC
    P_TEMPLATE -..-> FTP
    P_TEMPLATE -..-> FTPS

    %% Facade — FTPClient hides raw FTP command complexity
    P_FACADE -..-> FTPC

    %% Factory & Abstract Factory — factory creates the right parser for the server OS
    P_FACTORY -..-> FACTORY
    P_FACTORY -..-> PIFACE

    %% Strategy — each parser is a swappable algorithm (overlaps with Factory & Composite)
    P_STRATEGY -..-> PIFACE
    P_STRATEGY -..-> PARSERS

    %% Composite — CompositeFileEntryParser wraps many parsers as one (overlaps with Strategy)
    P_COMPOSITE -..-> COMPOSITE
    P_COMPOSITE -..-> PARSERS

    %% Observer — command events fired to all registered listeners
    P_OBSERVER -..-> CMDSUPPORT
    P_OBSERVER -..-> LISTENERS

    %% Decorator — stream classes wrap existing streams to add behavior
    P_DECORATOR -..-> STREAMS

    %% Adapter — CopyStreamAdapter provides default no-ops for the listener interface
    P_ADAPTER -..-> COPYADAPTER

    %% Iterator — typed iterators over raw NNTP server responses
    P_ITERATOR -..-> ITERATORS

    class P_TEMPLATE,P_FACADE,P_FACTORY,P_STRATEGY,P_COMPOSITE,P_OBSERVER,P_DECORATOR,P_ADAPTER,P_ITERATOR patternStyle
```

---

## Reading This Diagram

- **Boxes with solid borders** are the system's components grouped by layer (top to bottom = the flow of a request through the library).
- **Orange dashed circles** are pattern annotations. The dashed arrows show which components each pattern governs.
- **Overlaps** are visible where multiple pattern circles point to the same component:
  - `FTPFileEntryParser interface` ← Factory/Abstract Factory **and** Strategy
  - Concrete parsers (`Unix`, `NT`, `VMS`...) ← Strategy **and** Composite
  - `CompositeFileEntryParser` ← Composite **and** (implicitly) Strategy
