# Part 1 Problem 1 — Command Representation Justification

**Change:** Convert `SMTPCommand`, `NNTPCommand`, and `POP3Command` from legacy static-int-constant classes to Java enums, matching the structure of `FTPCmd`.

---

## The Pattern

**Type-Safe Enum (Java `enum`)**

A Java `enum` is a specialized class that represents a fixed set of named constants with a type. Each constant is an instance of the enum class, providing compile-time type safety and a built-in `name()` method that returns the constant's declared name as a String.

---

## Why This Pattern

The existing classes (`SMTPCommand`, `NNTPCommand`, `POP3Command`) use `public static final int` constants. This approach was standard before Java 5 but has well-known weaknesses:

- **No type safety** — any int can be passed where a command is expected; the compiler cannot catch errors
- **No namespace** — integer constants collide across classes; `NNTPCommand.HELP == 4` and `SMTPCommand.HELP == 10` are unrelated ints
- **No iterability** — you cannot enumerate all valid commands without maintaining a separate array
- **Inconsistency** — `FTPCmd` and `IMAPCommand` are already proper enums; the other three protocols lag behind

Converting to enums fixes all four issues and makes the entire library consistent. `FTPCmd` and `IMAPCommand` already prove this approach works in this codebase.

---

## Why Not Other Patterns

### 1. Static Inner Interface with Constants (current approach)
The existing `public static final int` pattern inside a `final class` mimics an interface of constants. It lacks type safety — any arbitrary int is accepted. It also requires a parallel String array to map ints to protocol strings, which is an error-prone indirection. **Not appropriate** because it is exactly the problem being fixed.

### 2. Static Factory Method (Command Objects)
Each command could be a full class with a static factory method returning an instance, similar to `java.nio.charset.Charset.forName()`. This provides type safety but is overkill — SMTP/NNTP/POP3 commands are fixed sets with no variant behavior or state. A full class hierarchy per command adds unnecessary complexity with no benefit over an enum. **Not appropriate** because enums already ARE a closed set of typed constants.

### 3. Strategy Pattern (Command Objects as Behavior Carriers)
Each command could be a strategy object that carries its own send behavior, removing the need for a command string. This is powerful for extensible command sets but these protocols are RFC-defined and closed — no extension is expected. Making each command an executable strategy object adds complexity that provides no value. **Not appropriate** for a closed, behavior-free set of protocol command names.

### 4. String Constants
Simple `public static final String` constants like `public static final String HELO = "HELO"` would fix the string-lookup problem and give type-safety-by-naming-convention, but strings are not type-safe — you can still pass any String. Enums are strictly better because the type system enforces that only valid commands are passed. **Not appropriate** because it does not provide compile-time type safety.

---

## Components Needed to Implement the Pattern

| Component | Role |
|---|---|
| `SMTPCommand` enum | Replaces the old `final class SMTPCommand`; each constant carries its protocol string |
| `NNTPCommand` enum | Replaces the old `final class NNTPCommand` |
| `POP3Command` enum | Replaces the old `final class POP3Command` |
| `getCommand()` method | Instance method on each enum that returns the protocol wire string (e.g. `"MAIL FROM:"`) |
| Updated `sendCommand()` in `SMTP`, `NNTP`, `POP3` | New overloads accepting the enum type instead of `int` |

---

## Pros and Cons

### Pros
- **Type safety** — the compiler rejects any non-`SMTPCommand` value where an SMTP command is expected
- **No parallel array** — the protocol string is stored directly on the enum constant; no index-to-string lookup table
- **Iterability** — `SMTPCommand.values()` enumerates all valid commands
- **Consistency** — matches `FTPCmd` and `IMAPCommand`, which are already enums in this codebase
- **Readable** — `sendCommand(SMTPCommand.EHLO, host)` is self-documenting; `sendCommand(15, host)` is not

### Cons
- **Breaking API change** — any external caller passing `SMTPCommand.HELO` as an int to the old `sendCommand(int, String)` must update to the new `sendCommand(SMTPCommand, String)` overload
- **Serialization** — enum ordinals are fragile across versions; this is irrelevant for command codes (they are never serialized), but worth noting as a general enum caveat
- **No runtime extension** — enums are closed; adding a new SMTP command requires recompilation (acceptable because these are RFC-defined and stable)
