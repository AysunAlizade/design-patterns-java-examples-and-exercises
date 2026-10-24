// Builder Practice Exercise — starter code. DatabaseConfig has ten settings and a
// stack of telescoping constructors. Two booleans were swapped in production, and an
// invalid configuration was accepted without complaint. Replace the constructors
// with a validating Builder, then add presets with a Director.
// See README.md in this folder for the full task and requirements.

class DatabaseConfig {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private boolean readOnly;
    private boolean ssl;
    private String sslCertificatePath;
    private int poolSize;
    private int timeoutSeconds;

    public DatabaseConfig(String host, String database) {
        this(host, 5432, database, null, null, false, false, null, 10, 30);
    }

    public DatabaseConfig(String host, int port, String database, String username, String password) {
        this(host, port, database, username, password, false, false, null, 10, 30);
    }

    public DatabaseConfig(String host, int port, String database, String username, String password,
                          boolean readOnly, boolean ssl) {
        this(host, port, database, username, password, readOnly, ssl, null, 10, 30);
    }

    public DatabaseConfig(String host, int port, String database, String username, String password,
                          boolean readOnly, boolean ssl, String sslCertificatePath,
                          int poolSize, int timeoutSeconds) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.readOnly = readOnly;
        this.ssl = ssl;
        this.sslCertificatePath = sslCertificatePath;
        this.poolSize = poolSize;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void connect() {
        System.out.println("Connecting to jdbc:postgresql://" + host + ":" + port + "/" + database
                + " as " + username);
        System.out.println("  ssl=" + ssl + " (cert: " + sslCertificatePath + "), readOnly=" + readOnly
                + ", pool=" + poolSize + ", timeout=" + timeoutSeconds + "s");
    }
}

public class Starter {
    public static void main(String[] args) {
        // Production: the intent was "SSL on, read-only off". Look at the output.
        DatabaseConfig production = new DatabaseConfig(
                "db.prod.acme.com", 5432, "orders", "app", "s3cret", true, false);
        production.connect();

        // Reporting replica: a port typo, SSL without a certificate, and a pool of 0.
        // All of it is accepted.
        DatabaseConfig reporting = new DatabaseConfig(
                "db.replica.acme.com", 99999, "orders", "reports", null, true, true, null, 0, 30);
        reporting.connect();

        // Local development: fine, but someone later "tunes" the shared object.
        DatabaseConfig local = new DatabaseConfig("localhost", "orders_dev");
        local.setPoolSize(-5);
        local.connect();
    }
}
