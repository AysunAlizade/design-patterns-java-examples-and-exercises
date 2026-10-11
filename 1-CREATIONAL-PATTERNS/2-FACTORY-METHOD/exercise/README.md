# Factory Practice Exercise — Report Exporter

## 🎯 Goal

Take a "which class do I create?" decision that is copy-pasted across services, give it one
home with a Simple Factory, and then remove the `switch` altogether with a Factory Method.

## 📦 Scenario

A back-office tool lets users export documents in different formats. Two services do this
today: `SalesReportService` exports sales reports, and `InvoiceService` lets customers
download invoices.

Support has two open tickets:

- "I can export a sales report as JSON, but not an invoice. Why?"
- "The CSV file from invoices uses `;` and the one from reports uses `,`. Our import tool
  breaks on one of them."

And product has a new request: **add XML export, everywhere**.

See [`Starter.java`](Starter.java):

```java
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
```

Run it first and look at the output:

```sh
javac Starter.java && java Starter
```

## 🔥 Spot the problem

Before writing any code, answer these out loud:

- Both support tickets have the same root cause. What is it?
- To add XML to the starter code, how many places would you have to edit?
- Which SOLID principles do these two services violate, and where exactly? (You should find
  three, the same three as in the [lesson](../README.md#-bad-example).)

## 🛠️ Your task

### Part 1 — Simple Factory

1. Create an `Exporter` interface with one method: `export(String title, String content)`
2. Move each format's logic into its own class: `PdfExporter`, `CsvExporter`, `JsonExporter`.
   CSV uses `,` as its separator, for everyone
3. Create an `ExporterFactory` with a `createExporter(String format)` method. It is the only
   class allowed to call `new` on an exporter
4. Make both services use the factory. They should contain no `if`/`else` on the format

### Part 2 — Feel the difference

5. Add XML export (`<report title="..."> ... </report>` is fine). Count the classes you
   created and the existing lines you edited, and write both numbers in a comment above
   `XmlExporter`

### Part 3 — Factory Method

Every export should follow the same workflow, whatever the format: print
`Preparing "<title>"...`, export it, then print `Done.`

6. Create an abstract class `DocumentExport` with:
   - an abstract factory method `createExporter()`
   - a method `run(String title, String content)` that performs the workflow above, using the
     exporter that `createExporter()` returns
7. Create one concrete creator per format: `PdfExport`, `CsvExport`, `JsonExport`, `XmlExport`
8. In `main()`, run the same document through all four creators

## ✅ Requirements / acceptance criteria

**Part 1 and 2**

- [ ] `SalesReportService` and `InvoiceService` contain no `if`/`switch` on the format and
      never name a concrete exporter class
- [ ] Invoices can now be exported as JSON, and CSV output uses `,` in both services
- [ ] An unknown format still throws `IllegalArgumentException` with the format in the message
- [ ] `"pdf"`, `"Pdf"`, and `"PDF"` all work
- [ ] Adding XML required exactly one new class and one new `case`, and neither service was
      edited

**Part 3**

- [ ] `DocumentExport` contains no `if`/`switch` and no `new ...Exporter()`
- [ ] The `Preparing...` / `Done.` workflow is written in exactly one place
- [ ] Each concrete creator is only a few lines: it overrides `createExporter()` and nothing
      else
- [ ] You can explain what you would have to add, and what you would have to edit, to support
      a fifth format in the Part 3 design

### ⭐ Bonus

- [ ] Replace the `switch` in `ExporterFactory` with a `Map<String, Supplier<Exporter>>` and a
      `register(String format, Supplier<Exporter> creator)` method. A new format can now be
      added without editing the factory class at all. Which SOLID principle did that just
      satisfy?
- [ ] `ExporterFactory` (Part 1) and `DocumentExport` (Part 3) both work. In two sentences,
      when would you choose each one for this back-office tool?

## 💡 Hint

For Part 1, ask of every line in the two services: *"is this line about the business task, or
about choosing and building the right object?"* Everything in the second group belongs in the
factory or in an exporter.

For Part 3, the base class should read like a recipe with one blank in it. `run()` is the
recipe, and `createExporter()` is the blank that each subclass fills in.

## Submitting your solution

See [Submitting your solutions](../../../README.md#submitting-your-solutions) in the root
README for the branch/commit convention. Suggested commit message:

```
exercise: Factory - report exporter
```
