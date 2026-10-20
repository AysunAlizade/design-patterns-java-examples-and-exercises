interface FileStorage {
    String store(String fileName);
}

interface MessageQueue {
    void publish(String message);
}

interface AuditLog {
    void record(String entry);
}

class S3Storage implements FileStorage {
    public String store(String fileName) {
        String location = "s3://uploads/" + fileName;
        System.out.println("AWS S3: stored " + location);
        return location;
    }
}

class SqsQueue implements MessageQueue {
    public void publish(String message) {
        System.out.println("AWS SQS: published \"" + message + "\"");
    }
}

class CloudTrail implements AuditLog {
    public void record(String entry) {
        System.out.println("AWS CloudTrail: " + entry);
    }
  }

class AzureBlobStorage implements FileStorage {
    public String store(String fileName) {
        String location = "https://uploads.blob.core.windows.net/" + fileName;
        System.out.println("Azure Blob Storage: stored " + location);
        return location;
    }
}

class AzureQueue implements MessageQueue {
    public void publish(String message) {
        System.out.println("Azure Queue: published \"" + message + "\"");
    }
}

class AzureMonitor implements AuditLog {
    public void record(String entry) {
        System.out.println("Azure Monitor: " + entry);
    }
}

class GcsStorage implements FileStorage {
    public String store(String fileName) {
        String location = "gs://uploads/" + fileName;
        System.out.println("Google Cloud Storage: stored " + location);
        return location;
    }
}

class PubSubQueue implements MessageQueue {
    public void publish(String message) {
        System.out.println("Google Pub/Sub: published \"" + message + "\"");
    }
}

class CloudAuditLogs implements AuditLog {
    public void record(String entry) {
        System.out.println("Google Cloud Audit Logs: " + entry);
    }
}

interface CloudFactory {
    FileStorage createStorage();
    MessageQueue createQueue();
    AuditLog createAuditLog();
}

class AwsFactory implements CloudFactory {
    public FileStorage createStorage() {
        return new S3Storage();
    }

    public MessageQueue createQueue() {
        return new SqsQueue();
    }

    public AuditLog createAuditLog() {
        return new CloudTrail();
    }
    }

class AzureFactory implements CloudFactory {
    public FileStorage createStorage() {
        return new AzureBlobStorage();
    }

    public MessageQueue createQueue() {
        return new AzureQueue();
    }

    public AuditLog createAuditLog() {
        return new AzureMonitor();
    }
    }

class GcpFactory implements CloudFactory {
    public FileStorage createStorage() {
        return new GcsStorage();
    }

    public MessageQueue createQueue() {
        return new PubSubQueue();
    }

    public AuditLog createAuditLog() {
        return new CloudAuditLogs();
    }
}

class UploadService {
    private final FileStorage storage;
    private final MessageQueue queue;
    private final AuditLog auditLog;

    public UploadService(CloudFactory factory) {
        storage = factory.createStorage();
        queue = factory.createQueue();
        auditLog = factory.createAuditLog();
    }

    public void upload(String fileName) {
        String location = storage.store(fileName);
        String message = "File uploaded: " + location;

        queue.publish(message);
        auditLog.record(message);
    }
    }

public class Starter {
    public static void main(String[] args) {
        String cloudProvider = "AZURE";
        CloudFactory factory;

        switch (cloudProvider) {
            case "AWS": factory = new AwsFactory();
                break;
            case "AZURE": factory = new AzureFactory();
                break;
            case "GCP": factory = new GcpFactory();
                break;
            default:
                throw new IllegalArgumentException( "Unsupported cloud provider: " + cloudProvider);
        }

        UploadService uploads = new UploadService(factory);
        uploads.upload("invoice-1001.pdf");
        uploads.upload("contract-77.pdf");
    }
}
