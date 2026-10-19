import java.util.ArrayList;
import java.util.List;

// ✅ A non-UI example: production infrastructure vs. test infrastructure.
// OrderService needs a repository AND an event publisher, and the two must come
// from the same "world": a test must never save to memory while publishing real
// events to production Kafka.

// --- Abstract products ---
interface OrderRepository {
    void save(String orderId);
}

interface EventPublisher {
    void publish(String event);
}

// --- Abstract factory ---
interface InfrastructureFactory {
    OrderRepository createRepository();
    EventPublisher createPublisher();
}

// --- Production family ---
class PostgresOrderRepository implements OrderRepository {
    public void save(String orderId) {
        System.out.println("PostgreSQL: INSERT INTO orders VALUES ('" + orderId + "')");
    }
}

class KafkaEventPublisher implements EventPublisher {
    public void publish(String event) {
        System.out.println("Kafka: published " + event);
    }
}

class ProductionFactory implements InfrastructureFactory {
    public OrderRepository createRepository() {
        return new PostgresOrderRepository();
    }

    public EventPublisher createPublisher() {
        return new KafkaEventPublisher();
    }
}

// --- Test family: everything stays in memory, so tests can inspect it ---
class InMemoryOrderRepository implements OrderRepository {
    final List<String> saved = new ArrayList<>();

    public void save(String orderId) {
        saved.add(orderId);
    }
}

class RecordingEventPublisher implements EventPublisher {
    final List<String> published = new ArrayList<>();

    public void publish(String event) {
        published.add(event);
    }
}

class TestFactory implements InfrastructureFactory {
    // keeps the objects it creates, so the test can look inside them afterwards
    final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    final RecordingEventPublisher publisher = new RecordingEventPublisher();

    public OrderRepository createRepository() {
        return repository;
    }

    public EventPublisher createPublisher() {
        return publisher;
    }
}

// --- Client: the same code runs in production and in tests ---
class OrderService {
    private final OrderRepository repository;
    private final EventPublisher publisher;

    public OrderService(InfrastructureFactory factory) {
        this.repository = factory.createRepository();
        this.publisher = factory.createPublisher();
    }

    public void placeOrder(String orderId) {
        repository.save(orderId);
        publisher.publish("OrderPlaced:" + orderId);
    }
}

public class Main {
    private static void check(boolean condition, String description) {
        System.out.println((condition ? "  PASS " : "  FAIL ") + description);
    }

    public static void main(String[] args) {
        System.out.println("--- production ---");
        new OrderService(new ProductionFactory()).placeOrder("A-1");

        System.out.println("--- test (no database, no Kafka) ---");
        TestFactory test = new TestFactory();
        new OrderService(test).placeOrder("A-1");
        check(test.repository.saved.equals(List.of("A-1")), "order was saved");
        check(test.publisher.published.equals(List.of("OrderPlaced:A-1")), "event was published");
    }
}

/*
 * What to notice:
 *  - OrderService is identical in production and in tests; only the factory changes
 *  - Switching the factory switches BOTH dependencies together, so a half-test,
 *    half-production setup can't be built by accident
 *  - Dependency injection frameworks often do this job for you. Abstract Factory is
 *    the same idea written by hand, and the two work fine together
 */
