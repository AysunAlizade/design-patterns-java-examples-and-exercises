// ✅ GOOD: ConnectionPool controls its own creation, so exactly one exists.
class ConnectionPool {
    private static final int MAX_CONNECTIONS = 3;

    // 1. the single instance, created once when the class is loaded
    private static final ConnectionPool INSTANCE = new ConnectionPool();

    private int inUse = 0;

    // 2. private constructor: `new ConnectionPool()` no longer compiles outside this class
    private ConnectionPool() {
        System.out.println("Creating the ConnectionPool (expensive setup, runs once)");
    }

    // 3. the one public way to reach the instance
    public static ConnectionPool getInstance() {
        return INSTANCE;
    }

    // synchronized: one shared instance means many threads may call this at once
    public synchronized void acquire(String who) {
        if (inUse >= MAX_CONNECTIONS) {
            throw new IllegalStateException("Pool exhausted, " + who + " must wait");
        }
        inUse++;
        System.out.println(who + " acquired a connection (" + inUse + "/" + MAX_CONNECTIONS + " in use)");
    }

    public synchronized int getInUse() {
        return inUse;
    }
}

class OrderService {
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public void placeOrder() {
        pool.acquire("OrderService");
    }

    public ConnectionPool getPool() {
        return pool;
    }
}

class ReportService {
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public void generateReport() {
        pool.acquire("ReportService");
    }

    public ConnectionPool getPool() {
        return pool;
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        OrderService orderService = new OrderService();
        ReportService reportService = new ReportService();

        orderService.placeOrder();
        orderService.placeOrder();
        reportService.generateReport();

        try {
            reportService.generateReport(); // the 4th connection: now correctly refused
        } catch (IllegalStateException e) {
            System.out.println("Refused: " + e.getMessage());
        }

        System.out.println("Same pool? " + (orderService.getPool() == reportService.getPool()));
        System.out.println("Connections really open: " + ConnectionPool.getInstance().getInUse());

        // Even when many threads ask at the same time, they all get the same object.
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> System.out.println(
                    Thread.currentThread().getName() + " got pool #"
                            + System.identityHashCode(ConnectionPool.getInstance())));
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }
}

/*
 * Why this is better:
 *  - The private constructor makes a second pool impossible, not just "discouraged"
 *  - Both services share one counter, so the 3-connection limit is really enforced
 *  - The expensive setup runs exactly once for the whole application
 *  - Creating the instance in a static final field is thread-safe: the JVM guarantees
 *    a class is initialized only once, no matter how many threads touch it
 */
