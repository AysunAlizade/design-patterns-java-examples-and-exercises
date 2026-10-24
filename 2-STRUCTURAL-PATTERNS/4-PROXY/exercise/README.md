# Proxy Practice Exercise — Exchange Rate Service

## 🎯 Goal

Take a class that mixes its real job with access rules, caching, and expensive setup, and
split it into one real object plus a chain of proxies, each with one job, in the right order.

## 📦 Scenario

A finance app converts amounts between currencies. The exchange rates come from **RatesAPI**,
an external service that is:

- **Slow to connect to:** authenticating takes a few seconds
- **Paid:** every rate request costs $0.01
- **Plan-restricted by the app:** FREE users may only convert between USD, EUR, and GBP. PRO
  users may convert anything

Today one class, `ExchangeRateService`, does all of it. The team now needs to:

- run the converter in tests **without** connecting to RatesAPI
- stop connecting at startup, because most users open the app and never convert anything
- add a rate limit next month, without touching the API code again

See [`Starter.java`](Starter.java). The heart of it:

```java
class ExchangeRateService {
    private final Map<String, Double> cache = new HashMap<>();
    private int apiCalls = 0;

    public ExchangeRateService() {
        // expensive: runs at application startup, even if nobody ever converts anything
        System.out.println("Connecting and authenticating with RatesAPI (slow)...");
    }

    public double getRate(String plan, String from, String to) {
        // concern 1: plan restriction. FREE users may only convert between USD, EUR, GBP
        if (plan.equals("FREE") && !(isMajor(from) && isMajor(to))) {
            throw new IllegalStateException("Upgrade to PRO to convert " + from + " -> " + to);
        }

        // concern 2: caching
        String pair = from + "->" + to;
        if (cache.containsKey(pair)) {
            return cache.get(pair);
        }

        // concern 3: the real work, a slow API call that costs money
        apiCalls++;
        Double rate = API_RATES.get(pair);
        // ...
        System.out.println("RatesAPI call #" + apiCalls + ": " + pair + " = " + rate + " ($0.01 charged)");
        cache.put(pair, rate);
        return rate;
    }
}
```

Run it first and look at the output:

```sh
javac Starter.java && java Starter
```

## 🔥 Spot the problem

Before writing any code, answer these out loud:

- List every job `ExchangeRateService` does. Which one is the *real* job?
- Why can't you test `CurrencyConverter` without connecting to RatesAPI?
- Which SOLID principles does `ExchangeRateService` violate? (You should find the same three as
  in the [lesson's bad example](../README.md#-bad-example).)
- Which kinds of proxy from the lesson's table would you need here?

## 🛠️ Your task

### Part 1 — The subject and the real subject

1. Create the subject interface `ExchangeRates` with one method:
   `double getRate(String from, String to)`. The plan is **not** a parameter
2. Create the real subject `RatesApiClient`: the connection message in its constructor, the
   API call, the `$0.01` message, and the call counter. Nothing else
3. Change `CurrencyConverter` to depend only on `ExchangeRates`, and remove its `plan` field

### Part 2 — Three proxies, one job each

4. `CachingExchangeRates`: returns a rate it has already fetched without calling its target
5. `PlanRestrictedExchangeRates`: receives the plan in its constructor and refuses non-major
   pairs for FREE users with an `IllegalStateException`
6. `LazyExchangeRates` (a virtual proxy): creates the `RatesApiClient` only on the first
   `getRate()` call

### Part 3 — Wire them in the right order

7. In `main()`, build **one** shared chain for the whole app (cache + lazy + real client), and
   put **one plan proxy per user** in front of it, like the one-per-user access proxies in
   the lesson
8. In a comment in `main()`, explain why the plan proxy must sit in front of the cache, and
   what would go wrong the other way round

## ✅ Requirements / acceptance criteria

- [ ] `RatesApiClient` contains no cache, no plan check, and no lazy-loading logic
- [ ] Each proxy implements `ExchangeRates` and does exactly one job
- [ ] `CurrencyConverter` depends only on `ExchangeRates`, and you didn't have to change it again
      after Part 1, however you combined the proxies
- [ ] `App started` is printed **before** `Connecting and authenticating...`, and the connection
      message appears **only once**, even with two users
- [ ] If `main()` never converts anything, `Connecting and authenticating...` is never printed
      (comment out the `convert` calls to check)
- [ ] Converting USD → EUR for both the PRO and the FREE user costs **one** API call in total
- [ ] The FREE user is refused USD → JPY **even right after the PRO user fetched it**, when it is
      already in the shared cache
- [ ] A refused request never reaches the API, so it is never charged

### ⭐ Bonus

- [ ] Rates change during the day. Give `CachingExchangeRates` an expiry time: after it
      expires, the next request fetches a fresh rate. Pass the duration in the constructor so
      a test can use a very short one
- [ ] Write a `CountingExchangeRates` proxy that counts how many calls reach it, and put it
      somewhere in the chain to **prove** the cache works, without reading the console output.
      Which SOLID principle let you add it without touching any other class?
- [ ] Write a fake `ExchangeRates` that returns fixed rates, and use it to test
      `CurrencyConverter` with no RatesAPI at all

## 💡 Hint

When you're unsure where a piece of code belongs, ask: *"would this still be needed if RatesAPI
were fast, free, and open to everyone?"* If no, it doesn't belong in `RatesApiClient`. It
belongs in a proxy.

For the order of the chain, follow the rule from the lesson: proxies that may **refuse** a call
go in front of proxies that may **answer** it.

## Submitting your solution

See [Submitting your solutions](../../../README.md#submitting-your-solutions) in the root
README for the branch/commit convention. Suggested commit message:

```
exercise: Proxy - exchange rate service
```
