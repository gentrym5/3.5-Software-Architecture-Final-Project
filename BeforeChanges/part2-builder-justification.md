# Part 2 — Builder Pattern Justification

**Change:** Introduce a `FTPClientConfig.Builder` inner class to replace the current mutable-setter construction model.

---

## The Pattern

**Builder (GoF Creational Pattern)**

The Builder pattern separates the construction of a complex object from its representation, allowing the same construction process to produce different configurations. A fluent builder collects parameters through a chain of named setter-like methods, then produces the final object in a single `build()` call.

---

## Why This Pattern

`FTPClientConfig` has 7+ configurable fields (`serverSystemKey`, `defaultDateFormatStr`, `recentDateFormatStr`, `serverLanguageCode`, `shortMonthNames`, `serverTimeZoneId`, `lenientFutureDates`, `saveUnparseableEntries`). The current design has two problems:

1. **Mutable setters** — `FTPClientConfig` is constructed and then mutated via `setXxx()` calls. The object can be in a partially-configured, inconsistent state between construction and use.
2. **Telescoping constructors** — there are three overloaded constructors taking 1, 6, and 8 parameters. Calling the 8-parameter constructor requires remembering positional order; mistakes are caught only at runtime.

The Builder pattern solves both: parameters are named at the call site (no positional guessing), and the constructed `FTPClientConfig` is valid and complete once `build()` returns. If immutability is required, `build()` can remove the public setters.

```java
FTPClientConfig config = new FTPClientConfig.Builder("UNIX")
    .serverTimeZoneId("America/New_York")
    .defaultDateFormatStr("MMM d yyyy")
    .lenientFutureDates(false)
    .build();
```

---

## Why Not Other Patterns

### 1. Factory Method
A static factory method like `FTPClientConfig.forUnixServer(tz, dateFormat)` could reduce the telescoping-constructor problem for common cases. However, every unique combination of the 8 parameters would need its own factory method — the combinatorial explosion makes this unmanageable. Builder lets the caller mix and match freely. **Not appropriate** for a configuration object with many independent optional fields.

### 2. Prototype (Clone)
A prototype approach would give callers a canonical instance to clone and then mutate. This still leaves the mutable-setter problem intact and adds the overhead of clone management. **Not appropriate** because it does not solve either of the two root problems.

### 3. Abstract Factory
An Abstract Factory creates *families* of related objects. `FTPClientConfig` is a single configuration object, not a family. Applying Abstract Factory here would add an unnecessary layer of abstraction with no benefit. **Not appropriate** because there is no family of products to vary.

### 4. Fluent Setters (no separate Builder)
Instead of a Builder inner class, the existing setters could return `this` to enable chaining: `config.setTimeZoneId(...).setDateFormat(...)`. This is simpler to implement but does not enforce valid-state-on-construction — the object is still mutable after `build()`, and there is no single point at which completeness is verified. **Acceptable as a lightweight alternative** but the dedicated Builder inner class is cleaner because it clearly separates the mutable building phase from the immutable configured object.

### 5. Configuration File / External Config
Configuration could be loaded from a `.properties` or YAML file rather than built in code. This trades compile-time safety for runtime flexibility. Since `FTPClientConfig` is used as an in-process API (not a deployment artifact), code-level configuration is the right scope. **Not appropriate** for this in-memory API object.

---

## Components Needed to Implement the Pattern

| Component | Role |
|---|---|
| `FTPClientConfig.Builder` (inner class) | Accumulates optional fields with fluent setter methods |
| `Builder(String serverSystemKey)` constructor | Takes the one required field; all others are optional |
| Fluent methods on Builder | `serverTimeZoneId(String)`, `defaultDateFormatStr(String)`, `recentDateFormatStr(String)`, `serverLanguageCode(String)`, `shortMonthNames(String)`, `lenientFutureDates(boolean)`, `saveUnparseableEntries(boolean)` — each returns `this` |
| `Builder.build()` | Calls the package-private `FTPClientConfig(String, FTPClientConfig)` copy constructor that already exists, producing the final configured object |

---

## Pros and Cons

### Pros
- **Named parameters at call site** — `serverTimeZoneId("America/New_York")` is unambiguous; positional parameter confusion is eliminated
- **Valid on construction** — `build()` is the single point of assembly; the resulting `FTPClientConfig` is complete
- **Backwards compatible** — existing constructors and setters remain intact; the Builder is additive
- **Extensible** — adding a new optional field requires only one new method on `Builder`, not a new constructor overload

### Cons
- **Two objects per config** — a `Builder` is created and then discarded to produce one `FTPClientConfig`; negligible overhead but worth noting
- **Partial immutability only** — the existing `setXxx()` methods on `FTPClientConfig` are still public; full immutability would require making those methods package-private or removing them, which is a breaking API change
- **Slightly more code** — the Builder inner class duplicates the field declarations; this is the standard tradeoff for the Builder pattern
