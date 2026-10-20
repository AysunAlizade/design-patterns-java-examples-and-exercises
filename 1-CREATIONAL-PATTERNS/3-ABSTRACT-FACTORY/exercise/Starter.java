interface FileStorage {
    String store(String fileName);
}

interface MessageQueue {
    void publish(String message);
}

interface CloudFactory {
    FileStorage createStorage();
    MessageQueue createQueue();
}

class AwsStorage implements FileStorage {
    @Override
    public String store(String fileName) {
        String location = "s3://uploads/" + fileName;
        System.out.println("AWS S3: stored " + location);
        return location;
    }
}

class AwsMessageQueue implements MessageQueue {
    @Override
    public void publish(String message) {
        System.out.println("AWS SQS: published \"" + message + "\"");
    }
}

class AzureStorage implements FileStorage {
    @Override
    public String store(String fileName) {
        String location = "https://uploads.blob.core.windows.net/" + fileName;
        System.out.println("Azure Blob Storage: stored " + location);
        return location;
    }
}

class AzureMessageQueue implements MessageQueue {
    @Override
    public void publish(String message) {
        System.out.println("Azure Queue Storage: published \"" + message + "\"");
    }
}

class AwsFactory implements CloudFactory {
    @Override
    public FileStorage createStorage() {
        return new AwsStorage();
    }

    @Override
    public MessageQueue createQueue() {
        return new AwsMessageQueue();
    }
}

class AzureFactory implements CloudFactory {
    @Override
    public FileStorage createStorage() {
        return new AzureStorage();
    }

    @Override
    public MessageQueue createQueue() {
        return new AzureMessageQueue();
    }
}

class UploadService {
    private final FileStorage storage;
    private final MessageQueue queue;

    public UploadService(CloudFactory factory) {
        this.storage = factory.createStorage();
        this.queue = factory.createQueue();
    }

    public void upload(String fileName) {
        String location = storage.store(fileName);
        String message = "File uploaded: " + location;
        queue.publish(message);
    }
}

public class Starter {
    public static void main(String[] args) {
        String provider = "AZURE";

        CloudFactory factory;

        switch (provider) {
            case "AWS": factory = new AwsFactory();
                break;
            case "AZURE": factory = new AzureFactory();
                break;
            default: throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        UploadService uploads = new UploadService(factory);

        uploads.upload("invoice-1001.pdf");
        uploads.upload("contract-77.pdf");
    }
}
