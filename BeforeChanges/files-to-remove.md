# Potential Files to Remove — Apache Commons Net 3.5

The items below serve no unique architectural purpose and are candidates for removal when trimming the project to its essential core. Each is labeled with the reason and risk level.

---

## 1. The Entire `ftp-client/` Folder
**Path:** `commons-net-3.5-src/commons-net-3.5-src/ftp-client/`
**Reason:** This is a flat-package copy of the FTP classes (`FTP.java`, `FTPClient.java`, all parsers, etc.) that already exist canonically at `src/main/java/org/apache/commons/net/ftp/`. It appears to be a standalone study/extraction submodule. The `.scannerwork/` directory inside it (SonarQube scanner artifacts) confirms this was used as a separate analysis target, not production source.
**Risk:** None — deleting this folder does not affect any class in the main source tree.

---

## 2. `package-info.java` Files
**Paths:** Every `package-info.java` across all packages (e.g., `ftp/package-info.java`, `io/package-info.java`, etc.)
**Reason:** These files contain only Javadoc `@package` descriptions. They have no executable code. They matter for published API documentation but contribute nothing to understanding or running the library.
**Risk:** Low — removing them breaks Javadoc generation but does not break compilation or runtime behavior.

---

## 3. The `examples/` Directory
**Path:** `commons-net-3.5-src/commons-net-3.5-src/src/main/java/examples/`
**Reason:** These are usage demos (`FTPClientExample.java`, `SMTPMail.java`, `IMAPMail.java`, `NTPClient.java`, `TelnetClientExample.java`, etc.). They illustrate how to call the library but are not part of the library itself.
**Risk:** Low — removing them has no impact on the core library.

---

## 5. Build / Static Analysis Config Files
**Paths:**
- `checkstyle.xml`
- `checkstyle-suppressions.xml`
- `findbugs-exclude-filter.xml`
- `ftp-client/.scannerwork/` (SonarQube artifacts)
- `ftp-client/sonar-project.properties`

**Reason:** These are CI/build tool configuration files. They are relevant only when running the Maven build pipeline and static analysis tools. They add no source code value for architecture study.
**Risk:** None for studying the code. Only matters if the Maven build is being run.

---

## 6. Legacy BSD r-commands Package
**Path:** `src/main/java/org/apache/commons/net/bsd/`
**Files:** `RExecClient.java`, `RLoginClient.java`, `RCommandClient.java`
**Reason:** The BSD r-commands (`rexec`, `rlogin`, `rsh`) were replaced by SSH in the 1990s. These protocols are disabled or blocked on virtually all modern systems. They are architectural dead weight for any current use case.
**Risk:** Low for modern environments; only relevant for legacy Unix systems.

---

## Summary Table

| Item | Type | Reason | Risk |
|---|---|---|---|
| `ftp-client/` folder | Duplicate module | Exact copy of `src/main/java/...ftp/` | None |
| `package-info.java` files | Javadoc only | No executable code | Low |
| `examples/` directory | Demo code | Not part of the library | Low |
| `FTPCommand.java` | Deprecated | Replaced by `FTPCmd.java` enum | Medium |
| `ArticlePointer.java` | Deprecated | Replaced by `ArticleInfo.java` | Medium |
| `NNTPCommand.java` | Deprecated | Old integer-constant style | Medium |
| Build config files | CI/tooling | Not source code | None |
| `bsd/` package | Legacy protocols | rexec/rlogin/rsh are obsolete | Low |
