# Changes Related to Builder Pattern

## 1. Pattern

This change introduces the Builder Pattern through the new `FTPClientConfig.Builder` inner class.

The Builder Pattern separates the construction of a complex object from its representation, allowing the same construction process to produce different configurations step by step. In this implementation, `FTPClientConfig.Builder` accumulates all optional configuration fields through fluent setter calls and produces a fully configured `FTPClientConfig` through a single `build()` call.

## 2. Why this pattern

Before this change, `FTPClientConfig` relied on a set of telescoping constructors. To create a configuration object, callers had to pick one of several overloaded constructors and pass arguments by position:

```java
new FTPClientConfig(FTPClientConfig.SYST_UNIX)
new FTPClientConfig(FTPClientConfig.SYST_UNIX, "MMM d yyyy", null, null, null, null)
```

`FTPClientConfig` has six configurable fields beyond the required system key: `defaultDateFormatStr`, `recentDateFormatStr`, `serverTimeZoneId`, `shortMonthNames`, `serverLanguageCode`, and `lenientFutureDates`. With positional constructors, it is easy to pass arguments in the wrong order, pass `null` for fields you do not need, or miss a field entirely without a compile error. The problem grows with each additional optional field.

The Builder Pattern is appropriate here because the object being constructed has many optional fields, the order of those fields does not matter to the caller, and callers benefit from being able to name each field explicitly at the call site. The result is self-documenting construction code:

```java
FTPClientConfig config = new FTPClientConfig.Builder(FTPClientConfig.SYST_UNIX)
    .serverTimeZoneId("America/New_York")
    .defaultDateFormatStr("MMM d yyyy")
    .build();
```

## 3. Why not other patterns

### Factory Method

Factory Method was not selected because the goal is not to delegate object creation to a subclass. The problem is not which type of `FTPClientConfig` to create — it is how to handle the many optional fields when creating one. Factory Method does not address the optional-parameter problem. **Not appropriate** because it solves a different construction problem.

### Abstract Factory

Abstract Factory was not selected because it is designed to create families of related objects. `FTPClientConfig` is a single configuration object with no family relationship to other types. **Not appropriate** because there is no product family involved.

### Prototype

Prototype clones an existing object to produce a new one. This could work as a copy-and-modify approach, but it requires an existing fully-configured instance to clone from and provides no named-setter syntax at the call site. **Not appropriate** because it does not address construction clarity.

### Singleton

Singleton was not selected because `FTPClientConfig` is a per-use configuration object. Every `FTPClient` may need a different configuration. A shared single instance would prevent independent configuration of separate clients. **Not appropriate** because the pattern fundamentally conflicts with per-instance use.

### Strategy

Strategy was not selected because the goal is not to swap an algorithm at runtime. The fields on `FTPClientConfig` configure how FTP directory listing timestamps are parsed, not which parsing algorithm to use. **Not appropriate** because construction of a configuration object is not an interchangeable algorithm.

## 4. Components needed to implement the pattern

The Builder Pattern implementation includes the following components:

- Product: `FTPClientConfig` — the fully configured object produced at the end of the build
- Builder: `FTPClientConfig.Builder` — the inner class that accumulates optional fields step by step and calls `build()` to produce the product
- Required parameter: the `systemKey` constructor argument to `FTPClientConfig.Builder` — forces callers to provide the one field that is not optional
- Fluent setters on the Builder:
  - `serverTimeZoneId(String)`
  - `defaultDateFormatStr(String)`
  - `recentDateFormatStr(String)`
  - `shortMonthNames(String)`
  - `serverLanguageCode(String)`
  - `lenientFutureDates(boolean)`
- Terminal method: `build()` — constructs the `FTPClientConfig` from the accumulated state and returns it
- Client code: any caller that constructs an `FTPClientConfig` — uses the Builder instead of the telescoping constructors

## 5. Pros and cons

### Pros

- Eliminates positional-parameter guessing at every `FTPClientConfig` construction site.
- Each field is named at the call site, making construction self-documenting and easier to review.
- Callers can omit optional fields with no need to pass `null` placeholders.
- The required `systemKey` is enforced through the Builder constructor, so it cannot be accidentally omitted.
- Fully backwards compatible — all existing `FTPClientConfig` constructors and `setXxx()` methods remain unchanged. No existing caller needs to change.
- The Builder is additive — it is a new entry point, not a replacement, so it can be adopted incrementally.

### Cons

- The old telescoping constructors remain in place and are still callable, which means two construction paths now exist. The legacy path is not formally deprecated, which may cause inconsistency across the codebase over time.
- Full immutability — removing the `setXxx()` methods from `FTPClientConfig` so that the Builder is the only way to produce a configured object — would require a breaking API change and cannot be done without a major version bump.
- Adds an inner class to `FTPClientConfig.java`, slightly increasing the file size and class count, though the added complexity is minimal.

## Summary of code changes

**`FTPClientConfig.java`** — added the inner class `FTPClientConfig.Builder`. The Builder holds a private copy of each optional configuration field, initialized to its default value. Each fluent setter assigns the field and returns `this` to allow chaining. The `build()` method constructs a new `FTPClientConfig` using the required `systemKey` and then calls the appropriate setter on it for each non-default field before returning the finished object.

No other files were modified. No completed code from Part 1 Problem 1, Problem 2, Problem 3, Problem 5, or the Singleton change was touched.
