import java.util.HashMap;
import java.util.Map;

// ✅ GOOD: the real store only reads documents. Caching and access control are
// separate PROXIES: objects with the same interface that stand in front of the
// real store and decide whether, and how, a call reaches it.

// --- Subject: the interface shared by the real object and its proxies ---
interface DocumentStore {
    /** @throws SecurityException if the caller may not read this document */
    String read(String documentId);
}

// --- Real subject: one job, reading from slow storage ---
class StorageDocumentStore implements DocumentStore {
    private int storageReads = 0;

    public String read(String documentId) {
        storageReads++;
        System.out.println("Storage read #" + storageReads + ": loading " + documentId + " (slow)...");
        return "<contents of " + documentId + ">";
    }
}

// --- Caching proxy: answers repeated requests without touching the real store ---
class CachingDocumentStore implements DocumentStore {
    private final DocumentStore target;
    private final Map<String, String> cache = new HashMap<>();

    public CachingDocumentStore(DocumentStore target) {
        this.target = target;
    }

    public String read(String documentId) {
        if (!cache.containsKey(documentId)) {
            cache.put(documentId, target.read(documentId));   // only on a cache miss
        }
        return cache.get(documentId);
    }
}

// --- Protection proxy: decides WHETHER the call may reach the target at all ---
class AccessControlledDocumentStore implements DocumentStore {
    private final DocumentStore target;
    private final String user;

    public AccessControlledDocumentStore(DocumentStore target, String user) {
        this.target = target;
        this.user = user;
    }

    public String read(String documentId) {
        if (documentId.startsWith("hr/") && !user.equals("hr-manager")) {
            System.out.println("AUDIT: " + user + " was DENIED " + documentId);
            throw new SecurityException(user + " may not read " + documentId);
        }
        String content = target.read(documentId);
        System.out.println("AUDIT: " + user + " read " + documentId);
        return content;
    }
}

// --- Client: works with ANY DocumentStore and can't tell a proxy from the real thing ---
class DocumentViewer {
    private final DocumentStore store;

    public DocumentViewer(DocumentStore store) {
        this.store = store;
    }

    public void open(String documentId) {
        try {
            System.out.println("Showing " + store.read(documentId));
        } catch (SecurityException e) {
            System.out.println("Refused: " + e.getMessage());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        // One real store and one shared cache for the whole application...
        DocumentStore cached = new CachingDocumentStore(new StorageDocumentStore());

        // ...and one access-control proxy per user, in FRONT of the cache, so the
        // permission check runs on every call, even when the answer is cached.
        DocumentViewer alice = new DocumentViewer(new AccessControlledDocumentStore(cached, "alice"));
        DocumentViewer bob = new DocumentViewer(new AccessControlledDocumentStore(cached, "bob"));
        DocumentViewer hr = new DocumentViewer(new AccessControlledDocumentStore(cached, "hr-manager"));

        alice.open("handbook.pdf");     // storage read #1
        bob.open("handbook.pdf");       // from the cache: no storage read
        hr.open("hr/salaries.xlsx");    // storage read #2
        bob.open("hr/salaries.xlsx");   // denied, although it is already in the cache
    }
}

/*
 * Why this is better:
 *  - StorageDocumentStore does one thing; caching and security each live in their own class (SRP)
 *  - A new concern (rate limiting, metrics) is a new proxy; no existing class is edited (OCP)
 *  - DocumentViewer depends only on the DocumentStore interface, so any proxy fits (DIP)
 *  - Each proxy can be used, left out, or tested on its own
 *
 * Order matters:
 *  - Access control sits in front of the cache. With the cache in front, Bob's request for
 *    hr/salaries.xlsx would be answered from the cache and never reach the permission check.
 */
