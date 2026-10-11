// Factory Practice Exercise — starter code. Two services export documents, and each
// one decides for itself how to handle every format. Refactor it so the format
// decision lives in one place (Simple Factory), then remove the switch (Factory Method).
// See README.md in this folder for the full task and requirements.

class SalesReportService {
    public void exportReport(String format, String title, String content) {
        if (format.equals("PDF")) {
            System.out.println("[PDF] " + title);
            System.out.println("  " + content + " (laid out on A4 pages)");
        } else if (format.equals("CSV")) {
            System.out.println("[CSV] " + title);
            System.out.println("  " + content.replace(" ", ","));
        } else if (format.equals("JSON")) {
            System.out.println("[JSON] " + title);
            System.out.println("  {\"title\": \"" + title + "\", \"content\": \"" + content + "\"}");
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}

class InvoiceService {
    public void downloadInvoice(String format, String title, String content) {
        if (format.equals("PDF")) {
            System.out.println("[PDF] " + title);
            System.out.println("  " + content + " (laid out on A4 pages)");
        } else if (format.equals("CSV")) {
            System.out.println("[CSV] " + title);
            System.out.println("  " + content.replace(" ", ";"));
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}

public class Starter {
    public static void main(String[] args) {
        SalesReportService reports = new SalesReportService();
        InvoiceService invoices = new InvoiceService();

        reports.exportReport("PDF", "Sales Q3", "north 120 south 95");
        reports.exportReport("CSV", "Sales Q3", "north 120 south 95");
        reports.exportReport("JSON", "Sales Q3", "north 120 south 95");

        invoices.downloadInvoice("CSV", "Invoice 1001", "laptop 1 mouse 2");

        try {
            invoices.downloadInvoice("JSON", "Invoice 1001", "laptop 1 mouse 2");
        } catch (IllegalArgumentException e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }
}
