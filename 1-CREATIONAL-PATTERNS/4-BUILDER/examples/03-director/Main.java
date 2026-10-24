// ✅ The classic (Gang of Four) Builder: the SAME construction steps produce
// DIFFERENT representations. Real emails carry two versions of their body, HTML
// for modern mail clients and plain text for everything else. The recipe for a
// welcome email is written once, in a Director, and each builder turns the same
// steps into its own format.

// --- Builder interface: the construction steps ---
interface EmailBodyBuilder {
    void addHeading(String text);
    void addParagraph(String text);
    void addButton(String label, String url);
    void addFooter(String text);
}

// --- Concrete builder #1: produces HTML ---
class HtmlBodyBuilder implements EmailBodyBuilder {
    private final StringBuilder html = new StringBuilder();

    public void addHeading(String text) {
        html.append("<h1>").append(text).append("</h1>\n");
    }

    public void addParagraph(String text) {
        html.append("<p>").append(text).append("</p>\n");
    }

    public void addButton(String label, String url) {
        html.append("<a class=\"button\" href=\"").append(url).append("\">").append(label).append("</a>\n");
    }

    public void addFooter(String text) {
        html.append("<footer>").append(text).append("</footer>\n");
    }

    public String getResult() {
        return html.toString();
    }
}

// --- Concrete builder #2: produces plain text ---
class PlainTextBodyBuilder implements EmailBodyBuilder {
    private final StringBuilder text = new StringBuilder();

    public void addHeading(String heading) {
        text.append(heading.toUpperCase()).append("\n").append("=".repeat(heading.length())).append("\n\n");
    }

    public void addParagraph(String paragraph) {
        text.append(paragraph).append("\n\n");
    }

    public void addButton(String label, String url) {
        text.append(label).append(": ").append(url).append("\n\n");
    }

    public void addFooter(String footer) {
        text.append("-- \n").append(footer).append("\n");
    }

    public String getResult() {
        return text.toString();
    }
}

// --- Director: knows the RECIPE (which steps, in which order), not the format ---
class WelcomeEmailDirector {
    public void construct(EmailBodyBuilder builder, String userName) {
        builder.addHeading("Welcome, " + userName + "!");
        builder.addParagraph("Thanks for joining Acme. Your account is ready.");
        builder.addButton("Confirm your email", "https://acme.com/confirm");
        builder.addFooter("Acme Inc. You can unsubscribe at any time.");
    }
}

public class Main {
    public static void main(String[] args) {
        WelcomeEmailDirector director = new WelcomeEmailDirector();

        HtmlBodyBuilder html = new HtmlBodyBuilder();
        director.construct(html, "Jane");
        System.out.println("--- HTML version ---");
        System.out.println(html.getResult());

        PlainTextBodyBuilder text = new PlainTextBodyBuilder();
        director.construct(text, "Jane");
        System.out.println("--- Plain-text version ---");
        System.out.println(text.getResult());
    }
}

/*
 * What to notice:
 *  - The welcome email's content and order are written ONCE, in the Director
 *  - Each builder only knows its format; neither knows what a welcome email says
 *  - A Markdown version would be one new builder; the Director stays unchanged (OCP)
 *  - A password-reset email would be one new Director; the builders stay unchanged
 *  - getResult() is on the concrete builders, not the interface, because the results
 *    are different types of thing (here both Strings, but often they are not)
 */
