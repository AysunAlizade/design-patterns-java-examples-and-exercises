import java.util.HashMap;
import java.util.Map;

// ❌ BAD: the class that loads documents from slow storage ALSO checks
// permissions, caches results, and writes an audit log. Four jobs in one class,
// and every new concern (rate limiting, metrics, ...) means editing it again.
class DocumentStore {
    private final Map<String, String> cache = new HashMap<>();
    private int storageReads = 0;

    public String read(String user, String documentId) {
        // job 1: access control
        if (documentId.startsWith("hr/") && !user.equals("hr-manager")) {
            System.out.println("AUDIT: " + user + " was DENIED " + documentId);
            throw new SecurityException(user + " may not read " + documentId);
        }

        // job 2: caching
        if (cache.containsKey(documentId)) {
            System.out.println("AUDIT: " + user + " read " + documentId + " (from cache)");
            return cache.get(documentId);
        }

        // job 3: the real work, a slow read from storage
        storageReads++;
        System.out.println("Storage read #" + storageReads + ": loading " + documentId + " (slow)...");
        String content = "<contents of " + documentId + ">";
        cache.put(documentId, content);

        // job 4: audit logging
        System.out.println("AUDIT: " + user + " read " + documentId);
        return content;
    }
}

public class Main {
    public static void main(String[] args) {
        DocumentStore store = new DocumentStore();

        store.read("alice", "handbook.pdf");
        store.read("bob", "handbook.pdf");
        store.read("hr-manager", "hr/salaries.xlsx");

        try {
            store.read("bob", "hr/salaries.xlsx");
        } catch (SecurityException e) {
            System.out.println("Refused: " + e.getMessage());
        }
    }
}

/*
 * Problems with this design:
 *  - The core job (reading from storage) is buried under three unrelated concerns
 *  - You can't read a document WITHOUT the cache or the permission check, for example
 *    in a batch job or a unit test of the storage logic
 *  - The audit log line is written in three places, and is easy to forget in a fourth
 *  - Every new cross-cutting concern means opening and re-testing this class again
 *
 * SOLID violations:
 *  - SRP: one class has four reasons to change: storage, security rules, caching policy,
 *    and audit format
 *  - OCP: adding rate limiting or metrics means editing a class that already worked
 *  - DIP: clients use the concrete DocumentStore class; with no interface in between,
 *    there is nothing to put a cache or a security check in front of
 */
