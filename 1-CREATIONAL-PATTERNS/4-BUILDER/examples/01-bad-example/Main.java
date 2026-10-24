import java.util.ArrayList;
import java.util.List;

// ❌ BAD: an Email with many optional parts, created in the two "classic" ways:
// a stack of telescoping constructors, and JavaBeans-style setters. Both compile
// fine and both let broken emails through.
class Email {
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String body;
    private boolean html;
    private boolean highPriority;
    private List<String> attachments = new ArrayList<>();

    // --- way 1: telescoping constructors, one more parameter each time ---
    public Email() { }

    public Email(String from, String to, String subject) {
        this(from, to, null, null, subject, "", false, false);
    }

    public Email(String from, String to, String subject, String body) {
        this(from, to, null, null, subject, body, false, false);
    }

    public Email(String from, String to, String cc, String bcc, String subject,
                 String body, boolean html, boolean highPriority) {
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.body = body;
        this.html = html;
        this.highPriority = highPriority;
    }

    // --- way 2: JavaBeans setters, set whatever you like, in any order ---
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
    public void setCc(String cc) { this.cc = cc; }
    public void setBcc(String bcc) { this.bcc = bcc; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setBody(String body) { this.body = body; }
    public void setHtml(boolean html) { this.html = html; }
    public void setHighPriority(boolean highPriority) { this.highPriority = highPriority; }
    public List<String> getAttachments() { return attachments; }

    public void send() {
        System.out.println("Sending email");
        System.out.println("  from: " + from + " | to: " + to + " | cc: " + cc + " | bcc: " + bcc);
        System.out.println("  subject: " + subject + " | html: " + html + " | high priority: " + highPriority);
        System.out.println("  attachments: " + attachments);
    }
}

public class Main {
    public static void main(String[] args) {
        // Problem 1: which argument is which? cc and bcc are both Strings, so swapping
        // them compiles. The secret auditor is now visible to every recipient.
        // And is `true, false` "html, not urgent" or "urgent, not html"?
        Email quarterly = new Email("ceo@acme.com", "staff@acme.com",
                "auditor@external.com", null,          // meant as BCC, passed as CC!
                "Q3 results", "<h1>Great quarter</h1>", false, true);   // html and priority swapped
        quarterly.send();

        // Problem 2: a half-built object. Nothing forces `to` to be set before send().
        Email reminder = new Email();
        reminder.setFrom("noreply@acme.com");
        reminder.setSubject("Your invoice is due");
        reminder.send();                                  // sent to: null

        // Problem 3: the object is mutable, even after it was "finished" and validated.
        Email welcome = new Email("hello@acme.com", "new.user@example.com", "Welcome!");
        welcome.getAttachments().add("terms.pdf");
        Email copyForAudit = welcome;                     // shared reference
        copyForAudit.setTo("everyone@acme.com");          // changes the original too
        welcome.send();                                   // the welcome mail goes to everyone@
    }
}

/*
 * Problems with this design:
 *  - Long parameter lists: call sites are unreadable, and arguments of the same type
 *    can be swapped without any compiler error
 *  - Every new optional field means yet another constructor, or one more argument
 *    added to every existing call
 *  - Setters allow half-built objects: no rule checks the email before send()
 *  - The object stays mutable, so a "finished" email can be changed by anyone holding it
 *
 * SOLID check: no SOLID principle is clearly violated here. These are problems of
 * readability, correctness, and mutability, and those are what Builder fixes.
 */
