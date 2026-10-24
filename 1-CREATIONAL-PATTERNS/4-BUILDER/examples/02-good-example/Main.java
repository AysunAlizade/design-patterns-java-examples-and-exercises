import java.util.ArrayList;
import java.util.List;

// ✅ GOOD: Email is built step by step through a Builder. Every option is named,
// the rules are checked once in build(), and the finished Email can't be changed.
final class Email {
    // 1. every field is final: once built, an Email never changes
    private final String from;
    private final List<String> to;
    private final List<String> cc;
    private final List<String> bcc;
    private final String subject;
    private final String body;
    private final boolean html;
    private final boolean highPriority;
    private final List<String> attachments;

    // 2. private constructor: the only way to get an Email is through the Builder
    private Email(Builder builder) {
        this.from = builder.from;
        this.to = List.copyOf(builder.to);              // copies: nobody can change them later
        this.cc = List.copyOf(builder.cc);
        this.bcc = List.copyOf(builder.bcc);
        this.subject = builder.subject;
        this.body = builder.body;
        this.html = builder.html;
        this.highPriority = builder.highPriority;
        this.attachments = List.copyOf(builder.attachments);
    }

    // 3. the entry point; required values go here, so they can't be forgotten
    public static Builder from(String sender) {
        return new Builder(sender);
    }

    public List<String> getTo() {
        return to;                                      // an unmodifiable list
    }

    public void send() {
        System.out.println("Sending email");
        System.out.println("  from: " + from + " | to: " + to + " | cc: " + cc + " | bcc: " + bcc);
        System.out.println("  subject: " + subject + " | html: " + html + " | high priority: " + highPriority);
        System.out.println("  attachments: " + attachments);
    }

    // 4. the Builder: mutable while building, thrown away afterwards
    public static final class Builder {
        private final String from;
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String subject = "";
        private String body = "";
        private boolean html = false;
        private boolean highPriority = false;
        private final List<String> attachments = new ArrayList<>();

        private Builder(String from) {
            this.from = from;
        }

        // 5. one named method per option; each returns `this`, so calls can be chained
        public Builder to(String address) {
            to.add(address);
            return this;
        }

        public Builder cc(String address) {
            cc.add(address);
            return this;
        }

        public Builder bcc(String address) {
            bcc.add(address);
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder textBody(String body) {
            this.body = body;
            this.html = false;
            return this;
        }

        public Builder htmlBody(String body) {
            this.body = body;
            this.html = true;
            return this;
        }

        public Builder highPriority() {
            this.highPriority = true;
            return this;
        }

        public Builder attach(String fileName) {
            attachments.add(fileName);
            return this;
        }

        // 6. build(): checks every rule in ONE place, then creates the finished object
        public Email build() {
            if (from == null || !from.contains("@")) {
                throw new IllegalStateException("A valid sender is required");
            }
            if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
                throw new IllegalStateException("An email needs at least one recipient");
            }
            if (subject.isBlank()) {
                throw new IllegalStateException("An email needs a subject");
            }
            if (attachments.size() > 10) {
                throw new IllegalStateException("At most 10 attachments are allowed");
            }
            return new Email(this);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        // Problem 1 solved: every value is named, nothing can be swapped by accident
        Email quarterly = Email.from("ceo@acme.com")
                .to("staff@acme.com")
                .bcc("auditor@external.com")
                .subject("Q3 results")
                .htmlBody("<h1>Great quarter</h1>")
                .highPriority()
                .build();
        quarterly.send();

        // Problem 2 solved: a half-built email can't even be created
        try {
            Email.from("noreply@acme.com")
                    .subject("Your invoice is due")
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("Not built: " + e.getMessage());
        }

        // Problem 3 solved: the finished email can't be changed
        Email welcome = Email.from("hello@acme.com")
                .to("new.user@example.com")
                .subject("Welcome!")
                .attach("terms.pdf")
                .build();
        try {
            welcome.getTo().add("everyone@acme.com");
        } catch (UnsupportedOperationException e) {
            System.out.println("Can't change a finished email: its recipient list is read-only");
        }
        welcome.send();
    }
}

/*
 * Why this is better:
 *  - Readable call sites: each value says what it is (.bcc(...), .highPriority())
 *  - Optional parts are simply left out; no constructor overloads, no nulls to pass
 *  - All validation lives in build(), so an invalid Email can never exist
 *  - Email is immutable: final fields and read-only copies of every list
 *  - A new option is one new Builder method; existing call sites keep compiling
 */
