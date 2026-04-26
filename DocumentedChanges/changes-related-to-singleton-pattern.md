# Changes Related to Singleton Pattern

## 1. Pattern

This change introduces the Singleton Pattern through the new `SocketFactoryProvider` class.

The Singleton Pattern ensures that one shared provider instance manages the default socket factories used by the networking client classes.

## 2. Why this pattern

Before this change, `SocketClient` and `DatagramSocketClient` each managed their own static default socket factory fields.

`SocketClient` directly stored the default `SocketFactory` and `ServerSocketFactory`, while `DatagramSocketClient` directly stored the default `DatagramSocketFactory`.

This worked, but the lifecycle and ownership of these default factories were spread across multiple classes. The Singleton Pattern is appropriate because these default factories should be accessed from one shared and controlled location.

The new `SocketFactoryProvider` class centralizes the default factory creation and gives client classes a single access point through `getInstance()`.

## 3. Why not other patterns

### Factory Method

Factory Method was not selected because the goal is not to let subclasses decide which object to create. The default factories are already known, and the main goal is to centralize their access.

### Builder

Builder was not selected because socket factory creation does not require many optional configuration steps. The objects are simple shared defaults, not complex objects built through multiple configuration methods.

### Observer

Observer was not selected because this part of the system does not notify listeners about state changes. It only provides shared factory objects.

### Strategy

Strategy was not selected because the goal is not to switch algorithms at runtime. Users can still provide custom factories through existing setter methods, but this change focuses only on the shared default factory lifecycle.

## 4. Components needed to implement the pattern

The Singleton Pattern implementation includes the following components:

- Singleton class: `SocketFactoryProvider`
- Static instance: `INSTANCE`
- Private constructor: prevents direct object creation from outside the class
- Global access method: `getInstance()`
- Shared resources:
  - `SocketFactory`
  - `ServerSocketFactory`
  - `DatagramSocketFactory`
- Client classes:
  - `SocketClient`
  - `DatagramSocketClient`

## 5. Pros and cons

### Pros

- Centralizes default socket factory management.
- Makes the lifecycle of default factories more explicit.
- Reduces duplicated static default factory fields.
- Keeps the existing public API unchanged.
- Preserves the ability to set custom factories through existing setter methods.
- Makes the design easier to explain as a shared provider for default networking factories.

### Cons

- Adds one new class to the `org.apache.commons.net` package.
- Introduces a global access point, so it should be used carefully.
- The runtime behavior is mostly unchanged, so the main benefit is architectural clarity rather than new functionality.

## Summary of code changes

A new `SocketFactoryProvider` class was added to provide a single shared access point for default socket factories.

`SocketClient` was updated to get its default `SocketFactory` and `ServerSocketFactory` from `SocketFactoryProvider`.

`DatagramSocketClient` was updated to get its default `DatagramSocketFactory` from `SocketFactoryProvider`.

No completed Problem 1 or Builder Pattern code was modified.