# 1 — Singleton Pattern

![Singleton Pattern](../../images/singleton.webp)

> Ensure a class has only one instance, and provide a global point of access to it.

## 💡 The idea

Some objects must exist **exactly once** in a running application. Not "usually once", and
not "once if everybody is careful": a second instance would be a bug.

A normal class can't promise that, because anyone can write `new`. The Singleton pattern makes
the class **responsible for its own instance**, using three ingredients:

1. A **private constructor**, so nobody outside the class can call `new`
2. A **private static field** that holds the one instance
3. A **public static method** (`getInstance()`) that hands that instance to everyone

As in the picture above: every client asks the same class, and every client gets the same
object back.

"One instance" is about **correctness**, not about saving memory. Use Singleton when two
instances would give wrong results, not just because one instance happens to be enough.

## Use case

Your database accepts at most **3 connections**. A `ConnectionPool` class enforces that limit
by counting how many connections are in use.

Then `OrderService` creates a pool with `new ConnectionPool()`. `ReportService` does the same.
Now there are two pools, each one counting to 3 on its own. The application can open 6
connections against a database that allows 3, and neither pool notices.

Other objects with the same "there must be only one" nature:

- **Configuration** loaded once at startup and read everywhere
- **A cache or registry** that the whole application must share
- **An ID or sequence generator**, where two instances would hand out the same number twice
- **Access to a single physical resource**: a printer spooler, a log file, a hardware device

## ❌ Bad example

```java
class ConnectionPool {
    private static final int MAX_CONNECTIONS = 3;
    private int inUse = 0;

    public ConnectionPool() {
        System.out.println("Creating a new ConnectionPool (expensive setup...)");
    }

    public void acquire(String who) {
        if (inUse >= MAX_CONNECTIONS) {
            throw new IllegalStateException("Pool exhausted, " + who + " must wait");
        }
        inUse++;
    }
}

class OrderService {
    private final ConnectionPool pool = new ConnectionPool();   // pool #1
}

class ReportService {
    private final ConnectionPool pool = new ConnectionPool();   // pool #2
}
```

👉 Problems:

- Two pools exist, so the 3-connection limit is enforced twice, separately
- Each pool sees only its own count, so the real total is invisible
- The expensive setup runs once per service instead of once per application
- Nothing stops the next developer from writing `new ConnectionPool()` a third time

## ✅ Good example

```java
class ConnectionPool {
    private static final int MAX_CONNECTIONS = 3;

    // 1. the single instance
    private static final ConnectionPool INSTANCE = new ConnectionPool();

    private int inUse = 0;

    // 2. private constructor: nobody else can call `new`
    private ConnectionPool() {
        System.out.println("Creating the ConnectionPool (expensive setup, runs once)");
    }

    // 3. the one public way to reach it
    public static ConnectionPool getInstance() {
        return INSTANCE;
    }

    public synchronized void acquire(String who) {
        if (inUse >= MAX_CONNECTIONS) {
            throw new IllegalStateException("Pool exhausted, " + who + " must wait");
        }
        inUse++;
    }
}

class OrderService {
    private final ConnectionPool pool = ConnectionPool.getInstance();
}

class ReportService {
    private final ConnectionPool pool = ConnectionPool.getInstance();
}
```

👉 Now a second pool is **impossible**, not just discouraged. Both services share one counter,
so the limit is really enforced.

Notice `synchronized` on `acquire()`. A Singleton is shared by the whole application, so many
threads can call its methods at once. The pattern guarantees one *instance*; keeping that
instance's *state* safe is still your job.

## Making a Singleton thread-safe

### The big picture

There are two decisions to make, and they are connected:

1. **When is the instance created?**
   - **Eager:** up front, when the class is first used. Simple.
   - **Lazy:** only when somebody calls `getInstance()` for the first time. Useful when
     creating the object is slow and it may never be needed.
2. **What if two threads ask for it at the same moment?**
   - With **eager**, nothing can go wrong: the JVM finishes creating the instance before any
     thread can use the class.
   - With **lazy**, *you* write the "create it if it doesn't exist yet" code, and that code
     can be run by two threads at once.

So the whole topic comes down to one sentence: **a lazy Singleton must make sure that only
one thread ever runs the "create" step.** Everything below is that one problem and four ways
to solve it.

### The problem: the naive lazy Singleton

This is the first version everybody writes:

```java
class ConnectionPool {
    private static ConnectionPool instance;        // starts as null

    private ConnectionPool() {
        System.out.println("Creating the ConnectionPool");
    }

    // ❌ NOT thread-safe
    public static ConnectionPool getInstance() {
        if (instance == null) {                    // step 1: check
            instance = new ConnectionPool();       // step 2: create
        }
        return instance;
    }
}
```

With one thread it is correct. With two threads, **check** and **create** are separate steps,
and a second thread can slip in between them:

| Time | Thread A | Thread B | `instance` |
|---|---|---|---|
| 1 | checks `instance == null` → **true** | | `null` |
| 2 | | checks `instance == null` → **true** (A hasn't created it yet) | `null` |
| 3 | creates pool **#1** | | pool #1 |
| 4 | | creates pool **#2** and overwrites the field | pool #2 |

Thread A is now using pool #1, thread B is using pool #2, and you have two "singletons". It
passes every single-threaded test and fails in production, only sometimes, which makes it
very hard to find.

Each fix below closes that gap between **check** and **create**. They differ only in how.

### Fix 1 — Synchronized method (simplest)

Add `synchronized`, and only one thread at a time can be inside `getInstance()`:

```java
class ConnectionPool {
    private static ConnectionPool instance;

    private ConnectionPool() {
        System.out.println("Creating the ConnectionPool");
    }

    // ✅ thread-safe: check + create now happen as one uninterruptible step
    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }
}
```

Thread B now has to wait until thread A has left the method. When B gets in, the instance
already exists, so B skips the creation.

- **Good:** obviously correct, one keyword.
- **Cost:** the lock is needed only for the very first call, but *every* call takes it,
  forever. If `getInstance()` is called very often from many threads, they queue up for
  nothing.

### Fix 2 — Double-checked locking (lock only at the start)

The idea: take the lock **only while the instance doesn't exist yet**. Once it exists, return
it without locking.

```java
class ConnectionPool {
    // volatile is REQUIRED here, see below
    private static volatile ConnectionPool instance;

    private ConnectionPool() {
        System.out.println("Creating the ConnectionPool");
    }

    // ✅ thread-safe, and lock-free after the first call
    public static ConnectionPool getInstance() {
        if (instance == null) {                          // 1st check, no lock
            synchronized (ConnectionPool.class) {        // only one thread gets in
                if (instance == null) {                  // 2nd check, inside the lock
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }
}
```

Every piece has a job. Remove any one of them and it breaks:

| Piece | Why it's there |
|---|---|
| **1st check** (outside the lock) | The fast path. After the instance exists, every call ends here and never touches the lock |
| **`synchronized` block** | At the start, several threads may pass the 1st check together. The lock lets them in one at a time |
| **2nd check** (inside the lock) | A thread that *waited* for the lock must look again: the thread before it has probably created the instance already |
| **`volatile`** | Guarantees that other threads see the new instance only when it is **fully built**. Without it, a thread can read a non-null `instance` whose constructor hasn't finished yet |

The same two threads as before:

| Time | Thread A | Thread B |
|---|---|---|
| 1 | 1st check → `null`, goes for the lock | 1st check → `null`, goes for the lock |
| 2 | **gets the lock** | waits |
| 3 | 2nd check → still `null`, creates the pool | waits |
| 4 | releases the lock | **gets the lock** |
| 5 | | 2nd check → **not null**, creates nothing |

- **Good:** lazy, and no locking cost after startup.
- **Cost:** the most code, and the easiest to get wrong (forgetting `volatile` is a classic
  bug). You need to recognize it because it is everywhere in existing code and in interviews.

### Fix 3 — Holder class (let the JVM do it)

The JVM already solves this exact problem for itself: it guarantees that **a class is
initialized exactly once**, even if 100 threads use it for the first time together. The
holder idiom reuses that guarantee:

```java
class ConnectionPool {

    private ConnectionPool() {
        System.out.println("Creating the ConnectionPool");
    }

    // A nested class is NOT loaded together with ConnectionPool.
    // It is loaded the first time somebody touches it...
    private static class Holder {
        static final ConnectionPool INSTANCE = new ConnectionPool();
    }

    // ✅ thread-safe and lazy, with no synchronized and no volatile
    public static ConnectionPool getInstance() {
        return Holder.INSTANCE;                    // ...which happens right here
    }
}
```

Step by step:

1. The program starts. `ConnectionPool` may be loaded, but `Holder` is not, so no pool exists
   yet. That makes it **lazy**.
2. The first call to `getInstance()` touches `Holder.INSTANCE`, so the JVM loads `Holder` and
   runs `new ConnectionPool()`.
3. If other threads call `getInstance()` during that moment, the JVM makes them wait until
   `Holder` is ready. That makes it **thread-safe**.
4. Every later call just returns the finished field. No lock is involved.

- **Good:** lazy, thread-safe, fast, and short. Nothing to get wrong.
- **Cost:** it looks odd until you know the trick.

### Fix 4 — Enum (shortest)

An enum constant is created by the JVM exactly once, under the same class-initialization
guarantee:

```java
enum ConnectionPool {
    INSTANCE;                                      // the one and only instance

    private static final int MAX_CONNECTIONS = 3;
    private int inUse = 0;

    ConnectionPool() {
        System.out.println("Creating the ConnectionPool");
    }

    public synchronized void acquire(String who) {
        if (inUse >= MAX_CONNECTIONS) {
            throw new IllegalStateException("Pool exhausted, " + who + " must wait");
        }
        inUse++;
    }
}

// usage: there is no getInstance(), you use the constant directly
ConnectionPool.INSTANCE.acquire("OrderService");
```

- **Good:** the least code. It is also the only variant that can't be broken by reflection or
  serialization, two advanced ways of sneaking in a second instance.
- **Cost:** an enum can't extend another class, and `ConnectionPool.INSTANCE` reads
  differently from the `getInstance()` most people expect.

### Which one should I use?

| Variant | Lazy? | Locking cost | Use it when |
|---|---|---|---|
| **Eager** (`static final` field, the [good example](#-good-example)) | No | None | The default. Setup is cheap, or the object is always needed |
| **Synchronized method** | Yes | On every call | `getInstance()` is called rarely and you want the simplest lazy code |
| **Double-checked locking** | Yes | Only at startup | You must read or maintain existing code that uses it |
| **Holder class** | Yes | None | You want lazy. This is the best general-purpose lazy version |
| **Enum** | No | None | You want the shortest, most robust form |

In short: start with **eager**. If you need lazy, use the **holder class**. Learn
**double-checked locking** well enough to read it and to explain why `volatile` is there.

"Eager" needs one clarification. Java initializes a class the first time it is *used*, so an
eager Singleton is created when anything in the class is first touched, not when the program
starts. The difference from lazy shows up only when the class has other static members:
calling `ConnectionPool.someOtherStaticMethod()` creates an eager instance, but not a
holder-based one.

All of this protects only the **creation** of the instance. The instance's own methods (like
`acquire()`) are called by many threads too, and need their own protection, such as
`synchronized`.

Run [`examples/03-thread-safe-variants`](./examples/03-thread-safe-variants/Main.java) to see
it for yourself: 10 threads call `getInstance()` at the same moment, the naive version creates
several instances, and each fix creates exactly one.

## ⚠️ The price of a Singleton

Singleton is the easiest pattern to write and the most misused one. It is global state, and
global state has costs:

- **Hidden dependencies.** `OrderService` uses the pool, but its constructor doesn't say so.
  You only find out by reading the method bodies.
- **Hard to test.** You can't swap the real pool for a fake one, and state left in the
  Singleton by one test leaks into the next.
- **Tight coupling.** Every class that calls `ConnectionPool.getInstance()` is welded to that
  concrete class (a DIP violation).
- **Two jobs in one class.** The class does its real work *and* manages its own lifecycle (an
  SRP smell).

You can keep "exactly one instance" and avoid most of these costs: call `getInstance()` in
**one** place, at application startup, and **pass the instance in** to whoever needs it.

```java
class OrderService {
    private final ConnectionPool pool;

    public OrderService(ConnectionPool pool) {   // the dependency is now visible
        this.pool = pool;
    }
}

// at application startup
ConnectionPool pool = ConnectionPool.getInstance();
OrderService orderService = new OrderService(pool);
ReportService reportService = new ReportService(pool);
```

This is what dependency injection frameworks such as Spring do for you.

## When to use it, and when not to

**Use Singleton when:**

- A second instance would cause wrong behavior (broken limits, duplicate IDs, conflicting
  writes to one resource)
- The object is expensive to build and is truly shared by the whole application

**Don't use Singleton when:**

- You only want an easy way to reach an object from anywhere. That is a global variable
- The class has no state. A class with only static methods (like `Math`) is simpler
- "We only need one right now." Needing one today is not the same as *more than one being a
  bug*
- The object holds data that belongs to a user, a request, or a session

## How to spot that you need it

- The same heavy object (`new Config()`, `new ConnectionPool()`) is created in several classes
- A bug comes from two objects that each believe they are "the" manager of something
- An object is passed through 10 layers only because everybody needs the same one
- A counter, cache, or limit gives wrong results because its state is split across instances

## Real-world mapping

- `Runtime.getRuntime()`, `Desktop.getDesktop()`: classic Singletons in the JDK
- Loggers, application configuration, connection pools (HikariCP's pool is created once and
  shared)
- **Spring beans** are singleton-scoped by default: one instance per container, created by the
  framework and injected, which avoids the costs listed above

A Singleton is "one per JVM class loader". In a system running several servers there is one
instance **per server**, so it can't be used to coordinate between machines.

## Examples

| # | Focus |
|---|-------|
| 01 | Each service creates its own `ConnectionPool`, so the connection limit is broken (problem) |
| 02 | `ConnectionPool` as a Singleton: private constructor, one shared instance, limit enforced (fixed) |
| 03 | Naive lazy Singleton vs. the four thread-safe variants, under 10 simultaneous threads |

## Build & run

```sh
cd examples/01-bad-example
javac Main.java && java Main
```

```sh
cd examples/02-good-example
javac Main.java && java Main
```

```sh
cd examples/03-thread-safe-variants
javac Main.java && java Main
```

## 🏋️ Practice exercise

Once the examples above make sense, apply Singleton yourself on a fresh scenario:

### [Exercise: Invoice Number Generator →](./exercise/)

Two sales channels each create their own number generator and issue the same invoice number
twice. Fix it with a lazy, thread-safe Singleton.

## Next

### 2. Factory Method *(coming soon)*

Back to [Creational Patterns](../README.md).
