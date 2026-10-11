import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

// Five ways to write a lazy/shared Singleton, attacked by 10 threads at the same moment.
// Every constructor counts how many times it ran and sleeps a little to simulate slow
// setup, which makes the race in the naive version show up on every run.

// ❌ BROKEN: "check, then create" is two steps, and nothing stops two threads
// from both passing the check before either one has created the instance.
class NaiveLazyPool {
    static final AtomicInteger created = new AtomicInteger();
    private static NaiveLazyPool instance;

    private NaiveLazyPool() {
        created.incrementAndGet();
        Setup.simulateSlowSetup();
    }

    public static NaiveLazyPool getInstance() {
        if (instance == null) {
            instance = new NaiveLazyPool();
        }
        return instance;
    }
}

// ✅ FIX 1: synchronized method. Only one thread at a time can be inside getInstance().
class SynchronizedPool {
    static final AtomicInteger created = new AtomicInteger();
    private static SynchronizedPool instance;

    private SynchronizedPool() {
        created.incrementAndGet();
        Setup.simulateSlowSetup();
    }

    public static synchronized SynchronizedPool getInstance() {
        if (instance == null) {
            instance = new SynchronizedPool();
        }
        return instance;
    }
}

// ✅ FIX 2: double-checked locking. Lock only while the instance doesn't exist yet.
class DoubleCheckedPool {
    static final AtomicInteger created = new AtomicInteger();
    private static volatile DoubleCheckedPool instance;

    private DoubleCheckedPool() {
        created.incrementAndGet();
        Setup.simulateSlowSetup();
    }

    public static DoubleCheckedPool getInstance() {
        if (instance == null) {                          // 1st check, no lock
            synchronized (DoubleCheckedPool.class) {
                if (instance == null) {                  // 2nd check, inside the lock
                    instance = new DoubleCheckedPool();
                }
            }
        }
        return instance;
    }
}

// ✅ FIX 3: holder class. The JVM loads Holder on the first getInstance() call,
// and the JVM itself guarantees a class is initialized exactly once.
class HolderPool {
    static final AtomicInteger created = new AtomicInteger();

    private HolderPool() {
        created.incrementAndGet();
        Setup.simulateSlowSetup();
    }

    private static class Holder {
        static final HolderPool INSTANCE = new HolderPool();
    }

    public static HolderPool getInstance() {
        return Holder.INSTANCE;
    }
}

// ✅ FIX 4: enum. The JVM creates each enum constant exactly once.
enum EnumPool {
    INSTANCE;

    // kept in a separate class because an enum constructor can't touch the enum's own static fields
    static class Counter {
        static final AtomicInteger created = new AtomicInteger();
    }

    EnumPool() {
        Counter.created.incrementAndGet();
        Setup.simulateSlowSetup();
    }
}

class Setup {
    static void simulateSlowSetup() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Main {
    private static final int THREADS = 10;

    // Starts THREADS threads that all call `getInstance` at the same moment.
    private static void attack(Runnable getInstance) throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    startSignal.await();   // everybody waits here...
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                getInstance.run();
            });
            threads[i].start();
        }
        startSignal.countDown();           // ...and is released together
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private static void report(String name, int created) {
        String verdict = created == 1 ? "✅" : "❌ not a singleton!";
        System.out.println(name + created + " instance(s) created " + verdict);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(THREADS + " threads call getInstance() at the same moment:\n");

        attack(NaiveLazyPool::getInstance);
        report("Naive lazy:             ", NaiveLazyPool.created.get());

        attack(SynchronizedPool::getInstance);
        report("Synchronized method:    ", SynchronizedPool.created.get());

        attack(DoubleCheckedPool::getInstance);
        report("Double-checked locking: ", DoubleCheckedPool.created.get());

        attack(HolderPool::getInstance);
        report("Holder class:           ", HolderPool.created.get());

        attack(() -> EnumPool.INSTANCE.name());
        report("Enum:                   ", EnumPool.Counter.created.get());
    }
}

/*
 * What to notice:
 *  - The naive version creates several instances: every thread that passed the
 *    `instance == null` check before the first assignment built its own object
 *  - All four fixes create exactly one, however many threads arrive together
 *  - They differ only in HOW they make "check and create" safe: a lock on every call,
 *    a lock only at the start, or the JVM's own class-initialization guarantee
 */
