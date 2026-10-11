// ❌ BAD: the database allows only 3 connections in total, and ConnectionPool
// is supposed to enforce that. But nothing stops each service from creating
// its OWN pool, so every pool counts to 3 separately and the limit is broken.
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
        System.out.println(who + " acquired a connection (" + inUse + "/" + MAX_CONNECTIONS + " in use)");
    }

    public int getInUse() {
        return inUse;
    }
}

class OrderService {
    // each service builds its own pool
    private final ConnectionPool pool = new ConnectionPool();

    public void placeOrder() {
        pool.acquire("OrderService");
    }

    public ConnectionPool getPool() {
        return pool;
    }
}

class ReportService {
    // ...and so does this one
    private final ConnectionPool pool = new ConnectionPool();

    public void generateReport() {
        pool.acquire("ReportService");
    }

    public ConnectionPool getPool() {
        return pool;
    }
}

public class Main {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        ReportService reportService = new ReportService();

        orderService.placeOrder();
        orderService.placeOrder();
        reportService.generateReport();
        reportService.generateReport();

        int totalOpen = orderService.getPool().getInUse() + reportService.getPool().getInUse();
        System.out.println("Same pool? " + (orderService.getPool() == reportService.getPool()));
        System.out.println("Connections really open: " + totalOpen + " (the database allows 3!)");
    }
}

/*
 * Problems with this design:
 *  - Two pools exist, so the "max 3 connections" rule is enforced twice, separately
 *  - Each pool thinks only 2 connections are in use, while 4 are really open
 *  - The expensive pool setup runs once per service instead of once per application
 *  - Nothing in ConnectionPool prevents the next developer from writing `new ConnectionPool()` again
 */
