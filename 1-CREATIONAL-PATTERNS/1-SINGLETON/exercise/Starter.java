// Singleton Practice Exercise — starter code. Each sales channel creates its own
// InvoiceNumberGenerator, so the same invoice number is issued twice. Refactor it
// so exactly one generator exists for the whole application.
// See README.md in this folder for the full task and requirements.

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

public class Starter {
    public static void main(String[] args) {
        WebCheckout web = new WebCheckout();
        PhoneSalesDesk phone = new PhoneSalesDesk();

        web.sell("Alice");
        phone.sell("Bob");
        web.sell("Carol");
        phone.sell("Dave");
    }
}
