class InvoiceNumberGenerator {
    private int lastNumber = 1000;

    private InvoiceNumberGenerator() {
        System.out.println("Starting a new invoice sequence after " + lastNumber);
    }

    private static class Holder {
        private static final InvoiceNumberGenerator INSTANCE = new InvoiceNumberGenerator();
    }

    public static InvoiceNumberGenerator getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized String next() {
        lastNumber++;
        return "INV-" + lastNumber;
    }
}

class WebCheckout {
    private final InvoiceNumberGenerator generator = InvoiceNumberGenerator.getInstance();

    public void sell(String customer) {
        System.out.println("Web sale to " + customer + " -> " + generator.next());
    }
}

class PhoneSalesDesk {
    private final InvoiceNumberGenerator generator = InvoiceNumberGenerator.getInstance();

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
