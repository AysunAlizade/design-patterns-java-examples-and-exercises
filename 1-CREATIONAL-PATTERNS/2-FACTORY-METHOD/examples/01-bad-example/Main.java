// ❌ BAD: every service that sends a notification decides for itself which
// concrete class to create. The same if/else is copy-pasted into each service,
// and the copies have already drifted apart.
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

class OrderService {
    public void confirmOrder(String type, String recipient) {
        Notification notification;

        // creation logic inside business logic
        if (type.equals("EMAIL")) {
            notification = new EmailNotification();
        } else if (type.equals("SMS")) {
            notification = new SMSNotification();
        } else if (type.equals("PUSH")) {
            notification = new PushNotification();
        } else {
            throw new IllegalArgumentException("Unknown notification type: " + type);
        }

        notification.send(recipient, "Your order is confirmed");
    }
}

class PasswordResetService {
    public void sendResetLink(String type, String recipient) {
        Notification notification;

        // the same decision, copy-pasted... and PUSH was forgotten in this copy
        if (type.equals("EMAIL")) {
            notification = new EmailNotification();
        } else if (type.equals("SMS")) {
            notification = new SMSNotification();
        } else {
            throw new IllegalArgumentException("Unknown notification type: " + type);
        }

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

        try {
            passwordResetService.sendResetLink("PUSH", "jane-phone");
        } catch (IllegalArgumentException e) {
            System.out.println("Failed: " + e.getMessage() + " (but OrderService supports PUSH!)");
        }
    }
}

/*
 * Problems with this design:
 *  - The "which class do I create?" decision is duplicated in every service
 *  - The copies drift apart: PUSH works for orders but not for password resets
 *  - Adding WhatsApp means finding and editing every one of these if/else chains
 *  - Each service knows every concrete notification class by name
 *
 * SOLID violations:
 *  - OCP: a new notification type forces edits to OrderService and PasswordResetService,
 *    classes that already worked
 *  - DIP: business services depend on EmailNotification, SMSNotification, PushNotification
 *    directly, instead of only on the Notification abstraction
 *  - SRP: each service does its own job AND decides which notification class to build,
 *    so it has two reasons to change
 */
