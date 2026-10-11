# Singleton Practice Exercise — Invoice Number Generator

## 🎯 Goal

Take a class that must exist exactly once, and make a second instance impossible, even when
many threads use it at the same time.

## 📦 Scenario

A small shop sells through two channels: a web checkout and a phone sales desk. Every sale
gets an invoice number, and by law **invoice numbers must be unique**: no duplicates, ever.

A teammate wrote an `InvoiceNumberGenerator` and used it in both channels. Accounting has just
reported two different invoices with the same number.

See [`Starter.java`](Starter.java):

```java
class InvoiceNumberGenerator {
    private int lastNumber = 1000;

    public InvoiceNumberGenerator() {
        System.out.println("Starting a new invoice sequence after " + lastNumber);
    }

    public String next() {
        lastNumber++;
        return "INV-" + lastNumber;
    }
}

class WebCheckout {
    private final InvoiceNumberGenerator generator = new InvoiceNumberGenerator();

    public void sell(String customer) {
        System.out.println("Web sale to " + customer + " -> " + generator.next());
    }
}

class PhoneSalesDesk {
    private final InvoiceNumberGenerator generator = new InvoiceNumberGenerator();

    public void sell(String customer) {
        System.out.println("Phone sale to " + customer + " -> " + generator.next());
    }
}
```

Run it first and look at the output:

```sh
javac Starter.java && java Starter
```

## 🔥 Spot the problem

Before writing any code, answer these out loud:

- How many times is "Starting a new invoice sequence" printed? How many should it be?
- Which customers received the same invoice number, and why?
- What in `InvoiceNumberGenerator` allowed this to happen?

## 🛠️ Your task

Refactor `Starter.java` so that exactly one `InvoiceNumberGenerator` exists in the whole
application:

1. Turn `InvoiceNumberGenerator` into a Singleton with the three ingredients from the lesson
2. Make it **lazy**: the generator is created the first time someone asks for it, not before
3. Make its creation **thread-safe**. Pick one of the lazy variants from the
   [lesson](../README.md#making-a-singleton-thread-safe)
4. Make `next()` itself thread-safe, so two threads can never receive the same number
5. Update `WebCheckout` and `PhoneSalesDesk` to use the shared generator
6. Add the multi-threaded check below to `main()` to prove it works

```java
// add at the top of the file:
// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;
// and declare: public static void main(String[] args) throws InterruptedException

Set<String> numbers = ConcurrentHashMap.newKeySet();
Thread[] threads = new Thread[10];
for (int i = 0; i < threads.length; i++) {
    threads[i] = new Thread(() -> {
        for (int n = 0; n < 100; n++) {
            numbers.add(InvoiceNumberGenerator.getInstance().next());
        }
    });
    threads[i].start();
}
for (Thread thread : threads) {
    thread.join();
}
System.out.println("Unique invoice numbers: " + numbers.size() + " (expected 1000)");
```

## ✅ Requirements / acceptance criteria

- [ ] `new InvoiceNumberGenerator()` no longer compiles anywhere outside the class itself
- [ ] "Starting a new invoice sequence" is printed **exactly once**, no matter how many
      channels and threads use the generator
- [ ] The generator is created only when it is first asked for, not at program start: add
      `System.out.println("Shop is open")` as the first line of `main()`, and that line must be
      printed **before** "Starting a new invoice sequence"
- [ ] The four sales in `main()` get four different, consecutive numbers: `INV-1001` to
      `INV-1004`
- [ ] The multi-threaded check prints `Unique invoice numbers: 1000 (expected 1000)`, every
      time you run it (run it at least 5 times)
- [ ] You can explain in one sentence why your `getInstance()` is safe when two threads call
      it at the same moment

### ⭐ Bonus

- [ ] Remove `getInstance()` calls from inside `WebCheckout` and `PhoneSalesDesk`: pass the
      generator in through their constructors, and call `getInstance()` only in `main()`.
      What became easier to see, and to test?
- [ ] Write a second version of the generator as an `enum`. Which parts of your first version
      did you no longer have to write yourself, and who does that work now?

## 💡 Hint

There are **two separate** thread-safety problems in this exercise, and fixing one does not
fix the other:

- Creating the instance: can two threads end up with two generators?
- Using the instance: `lastNumber++` is three steps (read, add, write). Can two threads read
  the same value?

If your check prints a number below 1000, the second problem is still open. Look at
`synchronized` or `java.util.concurrent.atomic.AtomicInteger`.

## Submitting your solution

See [Submitting your solutions](../../../README.md#submitting-your-solutions) in the root
README for the branch/commit convention. Suggested commit message:

```
exercise: Singleton - invoice number generator
```
