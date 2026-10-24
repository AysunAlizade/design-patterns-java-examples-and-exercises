# Builder Practice Exercise — Database Connection Config

## 🎯 Goal

Replace a stack of telescoping constructors with a Builder that names every option, applies
sensible defaults, rejects invalid combinations, and produces an object that can't be changed
afterwards. Then capture the team's standard setups as presets with a Director.

## 📦 Scenario

Every service in the company connects to PostgreSQL through a `DatabaseConfig` object with ten
settings:

| Setting | Default | Rule |
|---|---|---|
| `host` | (required) | must not be blank |
| `database` | (required) | must not be blank |
| `port` | `5432` | between 1 and 65535 |
| `username` | none | required if a password is set |
| `password` | none | |
| `readOnly` | `false` | |
| `ssl` | `false` | if on, `sslCertificatePath` is required |
| `sslCertificatePath` | none | |
| `poolSize` | `10` | between 1 and 100 |
| `timeoutSeconds` | `30` | greater than 0 |

Last night's incident report:

- Production has been running with **SSL off** and **read-only on**, so traffic was
  unencrypted and every write failed. The code *looks* right
- The reporting replica's config has a port of `99999`, SSL without a certificate, and a pool
  size of `0`. Nobody noticed until it was deployed
- Someone set the local config's pool size to `-5` after it was created

See [`Starter.java`](Starter.java). The constructors:

```java
public DatabaseConfig(String host, String database) { ... }

public DatabaseConfig(String host, int port, String database, String username, String password) { ... }

public DatabaseConfig(String host, int port, String database, String username, String password,
                      boolean readOnly, boolean ssl) { ... }

public DatabaseConfig(String host, int port, String database, String username, String password,
                      boolean readOnly, boolean ssl, String sslCertificatePath,
                      int poolSize, int timeoutSeconds) { ... }
```

And the production call site:

```java
// the intent was "SSL on, read-only off"
DatabaseConfig production = new DatabaseConfig(
        "db.prod.acme.com", 5432, "orders", "app", "s3cret", true, false);
```

Run it first and look at the output:

```sh
javac Starter.java && java Starter
```

## 🔥 Spot the problem

Before writing any code, answer these out loud:

- Why did production end up with SSL off? Why didn't the compiler catch it?
- Name three different reasons why the reporting config should never have been created.
- What made the `-5` pool size possible after the object was created?
- To add a new `applicationName` setting, how many constructors and call sites would you have
  to touch?

## 🛠️ Your task

### Part 1 — The Builder

1. Make `DatabaseConfig` immutable: all fields `final`, no setters, a `private` constructor
2. Add an entry point `DatabaseConfig.builder(String host, String database)`, so the two
   required values can't be forgotten
3. Add a nested `Builder` with one method per optional setting: `port(int)`,
   `credentials(String username, String password)`, `readOnly()`, `ssl(String certificatePath)`,
   `poolSize(int)`, `timeoutSeconds(int)`. Defaults come from the table above
4. Put every rule from the table into `build()`. A broken rule throws an
   `IllegalStateException` whose message names the setting and the bad value
5. Rewrite the three configs in `main()` with the builder. Production must have SSL on and
   read-only off. For the reporting config, catch the exception and print its message

### Part 2 — Presets with a Director

The team keeps copying the same settings between services. Capture the two standard setups:

6. Create a `DatabaseConfigDirector` with two methods that return a **pre-filled builder**, so
   the caller can still add credentials before calling `build()`:
   - `production(String host, String database)`: SSL on with certificate
     `/etc/ssl/certs/db.pem`, pool size 50, timeout 10 seconds
   - `localDevelopment(String database)`: host `localhost`, SSL off, pool size 2
7. Use the presets in `main()` for the production and local configs

## ✅ Requirements / acceptance criteria

**Part 1**

- [ ] `DatabaseConfig` has no public constructor and no setters, and every field is `final`
- [ ] Every value at every call site is named: you can tell what `true` means without opening
      `DatabaseConfig`
- [ ] Production prints `ssl=true` and `readOnly=false`
- [ ] `DatabaseConfig.builder("localhost", "orders_dev").build()` works and uses every default
      from the table
- [ ] Each of these throws an `IllegalStateException` with a clear message: port `99999`,
      `ssl` without a certificate path, pool size `0` or `101`, timeout `0`, a blank host, a
      password without a username
- [ ] The reporting config prints its error instead of connecting
- [ ] Changing a config after `build()` is impossible: there's no method to call

**Part 2**

- [ ] `DatabaseConfigDirector.production("db.prod.acme.com", "orders").credentials("app", "s3cret").build()`
      gives SSL on, pool 50, timeout 10, and port 5432
- [ ] The production SSL settings are written in exactly one place
- [ ] You can explain in one sentence what the Director knows that the Builder doesn't

### ⭐ Bonus

- [ ] Add a new optional setting, `applicationName` (default `"unknown"`). Count how many
      existing call sites you had to change, and compare with the telescoping version
- [ ] The operations team wants the same presets as JDBC URL strings, such as
      `jdbc:postgresql://db.prod.acme.com:5432/orders?ssl=true`. Introduce a builder
      *interface*, make `DatabaseConfig.Builder` one implementation and a new
      `JdbcUrlBuilder` another, and change the Director to work with the interface. Now one
      preset produces two different representations: that is the classic Builder from
      [step 2 of the lesson](../README.md#-good-example-step-2-the-classic-builder-with-a-director)

## 💡 Hint

Decide for every setting: *is it required, or does it have a sensible default?* Required
values go into `builder(...)`. Everything else becomes a builder method with its default set
in the builder's field.

Rules that involve **two** settings (SSL needs a certificate, a password needs a username)
can only be checked once all values are known. That's why the checks belong in `build()`,
not in the individual builder methods.

## Submitting your solution

See [Submitting your solutions](../../../README.md#submitting-your-solutions) in the root
README for the branch/commit convention. Suggested commit message:

```
exercise: Builder - database connection config
```
