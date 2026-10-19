// Abstract Factory Practice Exercise — starter code. UploadService picks its file
// storage and its message queue separately, from two different settings, so it can
// end up storing a file in one cloud and announcing it on another. Refactor it so a
// single cloud provider supplies a consistent family of services.
// See README.md in this folder for the full task and requirements.

class UploadService {
    private final String storageProvider;
    private final String queueProvider;

    public UploadService(String storageProvider, String queueProvider) {
        this.storageProvider = storageProvider;
        this.queueProvider = queueProvider;
    }

    public void upload(String fileName) {
        // step 1: store the file
        String location;
        if (storageProvider.equals("AWS")) {
            location = "s3://uploads/" + fileName;
            System.out.println("AWS S3: stored " + location);
        } else if (storageProvider.equals("AZURE")) {
            location = "https://uploads.blob.core.windows.net/" + fileName;
            System.out.println("Azure Blob Storage: stored " + location);
        } else {
            throw new IllegalArgumentException("Unsupported storage provider: " + storageProvider);
        }

        // step 2: tell the processing workers that a new file is ready
        String message = "File uploaded: " + location;
        if (queueProvider.equals("AWS")) {
            System.out.println("AWS SQS: published \"" + message + "\"");
        } else if (queueProvider.equals("AZURE")) {
            System.out.println("Azure Queue Storage: published \"" + message + "\"");
        } else {
            throw new IllegalArgumentException("Unsupported queue provider: " + queueProvider);
        }
    }
}

public class Starter {
    public static void main(String[] args) {
        // Read from configuration. The team is migrating from AWS to Azure:
        // storage.provider was switched last week, queue.provider was forgotten.
        String storageProvider = "AZURE";
        String queueProvider = "AWS";

        UploadService uploads = new UploadService(storageProvider, queueProvider);
        uploads.upload("invoice-1001.pdf");
        uploads.upload("contract-77.pdf");
    }
}
