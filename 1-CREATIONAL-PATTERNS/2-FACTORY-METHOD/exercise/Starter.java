interface Exporter {
    void export(String title, String content);
}

class PdfExporter implements Exporter {
    @Override
    public void export(String title, String content) {
        System.out.println("[PDF] " + title);
        System.out.println("  " + content + " (laid out on A4 pages)");
    }
}

class CsvExporter implements Exporter {
    @Override
    public void export(String title, String content) {
        System.out.println("[CSV] " + title);
        System.out.println("  " + content.replace(" ", ","));
    }
}

class JsonExporter implements Exporter {
    @Override
    public void export(String title, String content) {
        System.out.println("[JSON] " + title);
        System.out.println("  {\"title\": \"" + title
                + "\", \"content\": \"" + content + "\"}");
    }
}

// Added 1 new class and edited 1 existing line (one new case in ExporterFactory).
class XmlExporter implements Exporter {
    @Override
    public void export(String title, String content) {
        System.out.println("[XML] " + title);
        System.out.println("  <report title=\"" + title + "\">"
                + content + "</report>");
    }
}

class ExporterFactory {
    public static Exporter createExporter(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return new PdfExporter();
            case "csv":
                return new CsvExporter();
            case "json":
                return new JsonExporter();
            case "xml":
                return new XmlExporter();
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}

class SalesReportService {
    public void exportReport(String format, String title, String content) {
        Exporter exporter = ExporterFactory.createExporter(format);
        exporter.export(title, content);
    }
}

class InvoiceService {
    public void downloadInvoice(String format, String title, String content) {
        Exporter exporter = ExporterFactory.createExporter(format);
        exporter.export(title, content);
    }
}

public class Starter {
        public static void main(String[] args) {
        SalesReportService reports = new SalesReportService();
        InvoiceService invoices = new InvoiceService();

        reports.exportReport("PDF", "Sales Q3", "north 120 south 95");
        reports.exportReport("CSV", "Sales Q3", "north 120 south 95");
        reports.exportReport("JSON", "Sales Q3", "north 120 south 95");
        reports.exportReport("XML", "Sales Q3", "north 120 south 95");

        invoices.downloadInvoice("CSV", "Invoice 1001", "laptop 1 mouse 2");

        try {invoices.downloadInvoice("JSON", "Invoice 1001",
                    "laptop 1 mouse 2");
        } catch (IllegalArgumentException e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }
}
