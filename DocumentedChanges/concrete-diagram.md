# Concrete Diagram Apache Commons Net 3.5


```mermaid
classDiagram
    direction TB

    namespace Singleton_Provider {
        class SocketFactoryProvider {
            <<singleton>>
            -SocketFactoryProvider INSTANCE
            -SocketFactory defaultSocketFactory
            -ServerSocketFactory defaultServerSocketFactory
            -DatagramSocketFactory defaultDatagramSocketFactory
            -SocketFactoryProvider()
            +getInstance() SocketFactoryProvider
            +getSocketFactory() SocketFactory
            +getServerSocketFactory() ServerSocketFactory
            +getDatagramSocketFactory() DatagramSocketFactory
        }
    }

    namespace Transport {
        class SocketClient {
            <<abstract>>
            #int _timeout_
            #Socket _socket_
            #SocketFactory _socketFactory_
            #ProtocolCommandSupport _commandSupport_
            +connect(host, port)
            +disconnect()
            +setSocketFactory()
            #_connectAction_()
        }

        class DatagramSocketClient {
            <<abstract>>
            #DatagramSocket _socket_
            #DatagramSocketFactory _socketFactory_
            +open()
            +close()
        }
    }

    namespace FTP_Stack {
        class FTP {
            #BufferedReader _controlInput_
            #BufferedWriter _controlOutput_
            +sendCommand(command, args)
            +getReplyCode()
            +getReplyString()
        }

        class FTPClient {
            -FTPFileEntryParserFactory parserFactory
            -CopyStreamListener copyStreamListener
            +login(user, pass)
            +storeFile(remote, input)
            +retrieveFile(remote, output)
            +listFiles()
            +configure(FTPClientConfig)
        }

        class FTPSClient {
            -SSLContext context
            -TrustManager trustManager
            -KeyManager keyManager
            +execAUTH()
            +execPBSZ()
            +execPROT()
        }

        class FTPHTTPClient {
            -String proxyHost
            -int proxyPort
        }
    }

    namespace FTP_Config {
        class FTPClientConfig {
            -String serverSystemKey
            -String defaultDateFormatStr
            -String recentDateFormatStr
            -String serverTimeZoneId
            -String shortMonthNames
            +FTPClientConfig(systemKey)
            +getServerSystemKey() String
            +setDefaultDateFormatStr(str)
            +setServerTimeZoneId(id)
        }

        class FTPClientConfigBuilder {
            -String systemKey
            -String serverTimeZoneId
            -String defaultDateFormatStr
            -String recentDateFormatStr
            -String shortMonthNames
            +FTPClientConfigBuilder(systemKey)
            +serverTimeZoneId(id) FTPClientConfigBuilder
            +defaultDateFormatStr(fmt) FTPClientConfigBuilder
            +recentDateFormatStr(fmt) FTPClientConfigBuilder
            +shortMonthNames(names) FTPClientConfigBuilder
            +build() FTPClientConfig
        }
    }

    namespace FTP_Parsers {
        class FTPFileEntryParser {
            <<interface>>
            +parseFTPEntry(line) FTPFile
            +preParse(entries) List
        }

        class FTPFileEntryParserFactory {
            <<interface>>
            +createFileEntryParser(key) FTPFileEntryParser
        }

        class DefaultFTPFileEntryParserFactory {
            +createFileEntryParser(key) FTPFileEntryParser
        }

        class CompositeFileEntryParser {
            -FTPFileEntryParser[] parsers
            -FTPFileEntryParser cached
            +parseFTPEntry(line) FTPFile
        }

        class UnixFTPEntryParser
        class NTFTPEntryParser
        class VMSFTPEntryParser
        class OS400FTPEntryParser
        class NetwareFTPEntryParser
        class MVSFTPEntryParser
        class MacOsPeterFTPEntryParser
    }

    namespace SMTP_Stack {
        class SMTP {
            +sendCommand(command)
            +sendCommand(SMTPCommand, args)
            +sendCommand(SMTPCommand)
            +getReplyCode()
            #__sendCommand(id, args)
            #__getReply()
            #getDataWriter() Writer
        }

        class SMTPClient {
            +login()
            +setSender()
            +addRecipient()
            +sendShortMessageData() Writer
            +sendMessageData() Writer
        }

        class SMTPSClient {
            -SSLContext context
            +execTLS()
        }
    }

    namespace IMAP_Stack {
        class IMAP {
            +sendCommand(command)
            +getReplyStrings()
            #sendCommandWithID(tag, command, args)
            #__getReply()
            +appendWithData(args, message) boolean
        }

        class IMAPClient {
            +select(mailbox)
            +fetch(sequenceSet, itemNames)
            +append(mailbox, flags, datetime, message)
        }

        class IMAPSClient {
            +execTLS()
        }
    }

    namespace NNTP_Stack {
        class NNTP {
            +sendCommand(command)
            +sendCommand(NNTPCommand, args)
            +sendCommand(NNTPCommand)
            #__getReply()
            #openMessageReader() BufferedReader
            #getDataWriter() Writer
        }

        class NNTPClient {
            +selectNewsgroup(newsgroup)
            +retrieveArticle(articleNumber)
            +postArticle(writer)
            +forwardArticle(messageId)
            +listNewsgroups()
        }

        class NNTPSClient {
            -SSLContext context
            #_connectAction_()
        }
    }

    namespace Telnet_Stack {
        class Telnet {
            #TelnetOptionHandler[] optionHandlers
        }

        class TelnetClient {
            +getInputStream() InputStream
            +getOutputStream() OutputStream
            +addOptionHandler(TelnetOptionHandler)
        }

        class TelnetSClient {
            -SSLContext context
            #_connectAction_()
        }
    }

    namespace BSD_Commands {
        class RExecClient {
            +rexec(host, port, user, pass, cmd, stderr)
        }

        class RLoginClient
        class RCommandClient
    }

    namespace UDP_Clients {
        class NTPUDPClient {
            +getTime(host) TimeInfo
        }

        class EchoUDPClient
        class DaytimeUDPClient
        class CharGenUDPClient
        class DiscardUDPClient
    }

    namespace Observer_Events {
        class ProtocolCommandListener {
            <<interface>>
            +protocolCommandSent(event)
            +protocolReplyReceived(event)
        }

        class ProtocolCommandSupport {
            -ListenerList listeners
            +addProtocolCommandListener(l)
            +fireCommandSent(command, message)
            +fireReplyReceived(replyCode, message)
        }

        class PrintCommandListener {
            +protocolCommandSent(event)
            +protocolReplyReceived(event)
        }
    }

    namespace IO_Utilities {
        class CopyStreamListener {
            <<interface>>
            +bytesTransferred(event)
        }

        class CopyStreamAdapter {
            +bytesTransferred(event)
        }
    }

    namespace Command_Enums {
        class SMTPCommand {
            <<enumeration>>
            HELO
            EHLO
            MAIL
            RCPT
            DATA
            NOOP
            QUIT
            RSET
            VRFY
            +getCommand() String
        }

        class NNTPCommand {
            <<enumeration>>
            ARTICLE
            BODY
            GROUP
            HELP
            IHAVE
            LIST
            POST
            QUIT
            STAT
            +getCommand() String
        }

        class POP3Command {
            <<enumeration>>
            USER
            PASS
            QUIT
            STAT
            LIST
            RETR
            DELE
            NOOP
            RSET
            +getCommand() String
        }
    }

    %% Singleton provides factories to transport bases
    SocketFactoryProvider ..> SocketClient : provides factories
    SocketFactoryProvider ..> DatagramSocketClient : provides factories

    %% Transport inheritance
    SocketClient <|-- FTP
    FTP <|-- FTPClient
    FTPClient <|-- FTPSClient
    FTPClient <|-- FTPHTTPClient

    SocketClient <|-- SMTP
    SMTP <|-- SMTPClient
    SMTPClient <|-- SMTPSClient

    SocketClient <|-- IMAP
    IMAP <|-- IMAPClient
    IMAPClient <|-- IMAPSClient

    SocketClient <|-- NNTP
    NNTP <|-- NNTPClient
    NNTPClient <|-- NNTPSClient

    SocketClient <|-- Telnet
    Telnet <|-- TelnetClient
    TelnetClient <|-- TelnetSClient

    SocketClient <|-- RExecClient
    RExecClient <|-- RLoginClient
    RExecClient <|-- RCommandClient

    DatagramSocketClient <|-- NTPUDPClient
    DatagramSocketClient <|-- EchoUDPClient
    DatagramSocketClient <|-- DaytimeUDPClient
    DatagramSocketClient <|-- CharGenUDPClient
    DatagramSocketClient <|-- DiscardUDPClient

    %% FTP Parser relationships
    FTPFileEntryParserFactory <|.. DefaultFTPFileEntryParserFactory
    FTPFileEntryParser <|.. CompositeFileEntryParser
    FTPFileEntryParser <|.. UnixFTPEntryParser
    FTPFileEntryParser <|.. NTFTPEntryParser
    FTPFileEntryParser <|.. VMSFTPEntryParser
    FTPFileEntryParser <|.. OS400FTPEntryParser
    FTPFileEntryParser <|.. NetwareFTPEntryParser
    FTPFileEntryParser <|.. MVSFTPEntryParser
    FTPFileEntryParser <|.. MacOsPeterFTPEntryParser
    FTPClient --> FTPFileEntryParserFactory : uses
    DefaultFTPFileEntryParserFactory --> FTPFileEntryParser : creates

    %% FTPClientConfig Builder relationship
    FTPClientConfigBuilder --> FTPClientConfig : builds
    FTPClient --> FTPClientConfig : configured by

    %% Command Enum usage
    SMTP --> SMTPCommand : sendCommand uses
    NNTP --> NNTPCommand : sendCommand uses

    %% Observer relationships
    SocketClient --> ProtocolCommandSupport : owns
    ProtocolCommandSupport --> ProtocolCommandListener : notifies
    ProtocolCommandListener <|.. PrintCommandListener

    %% IO relationships
    CopyStreamListener <|.. CopyStreamAdapter
    FTPClient --> CopyStreamListener : uses
```

