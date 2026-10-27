class DatabaseConfig {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean readOnly;
    private final boolean ssl;
    private final String certificate;
    private final int poolSize;
    private final int timeout;

    private DatabaseConfig(Builder b) {
        host = b.host;
        port = b.port;
        database = b.database;
        username = b.username;
        password = b.password;
        readOnly = b.readOnly;
        ssl = b.ssl;
        certificate = b.certificate;
        poolSize = b.poolSize;
        timeout = b.timeout;
    }

    public static Builder builder(String host, String database) {
        return new Builder(host, database);
    }

    public void connect() {
        System.out.println("Database: " + host + ":" + port + "/" + database);
        System.out.println("ssl=" + ssl + ", readOnly=" + readOnly + ", pool=" + poolSize + ", timeout=" + timeout);
    }

    static class Builder {
        private final String host;
        private final String database;

        private int port = 5432;
        private String username;
        private String password;
        private boolean readOnly = false;
        private boolean ssl = false;
        private String certificate;
        private int poolSize = 10;
        private int timeout = 30;

        Builder(String host, String database) {
            this.host = host;
            this.database = database;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder readOnly() {
            this.readOnly = true;
            return this;
        }

        public Builder ssl(String certificate) {
            this.ssl = true;
            this.certificate = certificate;
            return this;
        }

        public Builder poolSize(int size) {
            this.poolSize = size;
            return this;
        }

        public Builder timeout(int seconds) {
            this.timeout = seconds;
            return this;
        }

        public DatabaseConfig build() {
            if (host == null || host.isBlank())
                throw new IllegalStateException("Host is empty");

            if (database == null || database.isBlank())
                throw new IllegalStateException("Database is empty");

            if (port < 1 || port > 65535)
                throw new IllegalStateException("Invalid port: " + port);

            if (password != null && (username == null || username.isBlank()))
                throw new IllegalStateException("Username is required");

            if (ssl && (certificate == null || certificate.isBlank()))
                throw new IllegalStateException("SSL certificate is missing");

            if (poolSize < 1 || poolSize > 100)
                throw new IllegalStateException("Invalid pool size: " + poolSize);

            if (timeout <= 0)
                throw new IllegalStateException("Invalid timeout: " + timeout);

            return new DatabaseConfig(this);
        }
    }
}

class DatabaseConfigDirector {
    public static DatabaseConfig.Builder production(String host, String database) {
        return DatabaseConfig.builder(host, database).ssl("/etc/ssl/certs/db.pem").poolSize(50).timeout(10);
    }

    public static DatabaseConfig.Builder localDevelopment(String database) {
        return DatabaseConfig.builder("localhost", database).poolSize(2);
    }
}

public class Starter {
    public static void main(String[] args) {
        DatabaseConfig production = DatabaseConfigDirector.production("db.prod.acme.com", "orders") .credentials("app", "s3cret") .build();

        production.connect();

        try {
            DatabaseConfig reporting = DatabaseConfig.builder("db.replica.acme.com", "orders").port(99999) .ssl(null).poolSize(0).build();

            reporting.connect();
          }  
         catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        DatabaseConfig local = DatabaseConfigDirector.localDevelopment("orders_dev").build();

        local.connect();
    }
}
