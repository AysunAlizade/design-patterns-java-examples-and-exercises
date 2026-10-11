// ✅ GOOD (Simple Factory): the "which class do I create?" decision lives in
// exactly one place. Services ask the factory and only know the interface.
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

// The one class that knows the concrete notification classes.
class NotificationFactory {
    public static Notification createNotification(String type) {
        switch (type) {
            case "EMAIL":
                return new EmailNotification();
            case "SMS":
                return new SMSNotification();
            case "PUSH":
                return new PushNotification();
            default:
                throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }
}

class OrderService {
    public void confirmOrder(String type, String recipient) {
        Notification notification = NotificationFactory.createNotification(type);
        notification.send(recipient, "Your order is confirmed");
    }
}

class PasswordResetService {
    public void sendResetLink(String type, String recipient) {
        Notification notification = NotificationFactory.createNotification(type);
        notification.send(recipient, "Click here to reset your password");
    }
}

public class Main {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        PasswordResetService passwordResetService = new PasswordResetService();

        orderService.confirmOrder("EMAIL", "jane@example.com");
        orderService.confirmOrder("PUSH", "jane-phone");
        passwordResetService.sendResetLink("SMS", "+1-555-0100");
        passwordResetService.sendResetLink("PUSH", "jane-phone"); // works now: one shared decision
    }
}

/*
 * Why this is better:
 *  - One switch instead of one per service, so the services can't drift apart
 *  - Adding WhatsApp = one new class + one new case in the factory; no service is touched
 *  - Services depend only on the Notification interface (DIP) and do only their own job (SRP)
 *
 * What is still not perfect:
 *  - The factory itself must be edited for every new type, so OCP is improved
 *    (one place instead of many) but not fully satisfied. See 03-factory-method.
 */
