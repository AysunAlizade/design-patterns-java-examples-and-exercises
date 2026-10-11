// ✅ GOOD (Factory Method): there is no switch at all. A base class defines the
// workflow and leaves ONE step, "create the notification", to its subclasses.
interface Notification {
    void send(String recipient, String message);
}

class EmailNotification implements Notification {
    public void send(String recipient, String message) {
        System.out.println("EMAIL to " + recipient + ": " + message);
    }
}

class SMSNotification implements Notification {
    public void send(String recipient, String message) {
        System.out.println("SMS to " + recipient + ": " + message);
    }
}

class PushNotification implements Notification {
    public void send(String recipient, String message) {
        System.out.println("PUSH to " + recipient + ": " + message);
    }
}

// The "creator". It knows HOW to notify a user, but not WHICH notification it uses.
abstract class Notifier {

    // the factory method: subclasses decide which class to instantiate
    protected abstract Notification createNotification();

    // shared workflow, written once, working with whatever the subclass creates
    public void notifyUser(String recipient, String message) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        Notification notification = createNotification();
        notification.send(recipient, message);
        System.out.println("  (logged: " + notification.getClass().getSimpleName() + " sent)");
    }
}

class EmailNotifier extends Notifier {
    @Override
    protected Notification createNotification() {
        return new EmailNotification();
    }
}

class SMSNotifier extends Notifier {
    @Override
    protected Notification createNotification() {
        return new SMSNotification();
    }
}

class PushNotifier extends Notifier {
    @Override
    protected Notification createNotification() {
        return new PushNotification();
    }
}

// --- Added later, for the WhatsApp feature. No class above this line was edited. ---
class WhatsAppNotification implements Notification {
    public void send(String recipient, String message) {
        System.out.println("WHATSAPP to " + recipient + ": " + message);
    }
}

class WhatsAppNotifier extends Notifier {
    @Override
    protected Notification createNotification() {
        return new WhatsAppNotification();
    }
}
// -----------------------------------------------------------------------------------

// Business code depends only on the abstract creator.
class OrderService {
    private final Notifier notifier;

    public OrderService(Notifier notifier) {
        this.notifier = notifier;
    }

    public void confirmOrder(String recipient) {
        notifier.notifyUser(recipient, "Your order is confirmed");
    }
}

public class Main {
    public static void main(String[] args) {
        // The choice of channel is made once, here, at the edge of the application.
        OrderService emailOrders = new OrderService(new EmailNotifier());
        OrderService pushOrders = new OrderService(new PushNotifier());
        OrderService whatsAppOrders = new OrderService(new WhatsAppNotifier());

        emailOrders.confirmOrder("jane@example.com");
        pushOrders.confirmOrder("jane-phone");
        whatsAppOrders.confirmOrder("+1-555-0100");
    }
}

/*
 * Why this is better:
 *  - OCP is fully satisfied: WhatsApp was added with two new classes and zero edits
 *  - The validation + send + log workflow is written once, in Notifier
 *  - OrderService depends on one abstraction and knows no concrete class
 *
 * The price:
 *  - One extra creator class per notification type
 *  - Somebody still chooses `new EmailNotifier()`, but only once, at startup
 */
