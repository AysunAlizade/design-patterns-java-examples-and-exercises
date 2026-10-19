# Abstract Factory Practice Exercise — Multi-Cloud File Upload

## 🎯 Goal

Take objects that must always come from the same provider, but are chosen separately, and
make them a single family that can only be switched as a unit.

## 📦 Scenario

A document platform stores uploaded files in the cloud. After every upload it publishes a
message on a queue, and background workers in the **same cloud** pick the message up and
process the file (virus scan, thumbnails, search indexing).

The company is migrating from AWS to Azure. Last week someone changed `storage.provider` to
`AZURE` and forgot `queue.provider`. Since then, files are stored in Azure but announced on
the AWS queue. The Azure workers never hear about them, and nothing has been processed for a
week. No error was ever thrown.

See [`Starter.java`](Starter.java):

```java
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
```

Run it first and look at the output:

```sh
javac Starter.java && java Starter
```

## 🔥 Spot the problem

Before writing any code, answer these out loud:

- Which line of the output proves the bug? Why did the code accept it without any error?
- What are the **families** here, and what are the **product types**? Draw the grid from the
  [lesson](../README.md) (section **The trade-off**).
- Which SOLID principles does `UploadService` violate? (You should find the same three as in
  the lesson's bad example.)

## 🛠️ Your task

### Part 1 — Abstract Factory

1. Create two product interfaces:
   - `FileStorage` with `String store(String fileName)`, which returns the file's location
   - `MessageQueue` with `void publish(String message)`
2. Move each provider's logic into concrete products: `S3Storage`, `SqsQueue`,
   `AzureBlobStorage`, `AzureQueue`
3. Create the abstract factory `CloudFactory` with `createStorage()` and `createQueue()`, and
   the concrete factories `AwsFactory` and `AzureFactory`
4. Change `UploadService` so its constructor takes **only** a `CloudFactory`
5. Replace the two settings in `main()` with **one** setting, `cloud.provider`, and turn it
   into a factory in exactly one place

### Part 2 — Add a new family

6. Add Google Cloud: `GcsStorage` (location `gs://uploads/<file>`), `PubSubQueue`, and
   `GcpFactory`. Write in a comment above `GcpFactory` which existing classes you had to edit

### Part 3 — Feel the cost

7. Every provider now also needs to log an audit entry after each upload. Add a third product
   type, `AuditLog` with `void record(String entry)`, implement it for all three providers
   (CloudTrail, Azure Monitor, Cloud Audit Logs), and call it from `UploadService`. Write in a
   comment above `CloudFactory` which classes you had to edit this time, and why that list is
   longer than in Part 2

## ✅ Requirements / acceptance criteria

**Part 1 and 2**

- [ ] `UploadService` contains no `if`/`switch` on the provider and never names a concrete
      storage, queue, or factory class
- [ ] It is impossible to build an `UploadService` whose storage and queue come from different
      providers. The constructor takes nothing but a `CloudFactory`
- [ ] The provider is chosen from one setting, in exactly one place
- [ ] With `cloud.provider = AZURE`, both lines of every upload say Azure
- [ ] An unknown provider throws `IllegalArgumentException` with the provider in the message
- [ ] Adding Google Cloud needed 3 new classes and one new `case` where the factory is chosen.
      `UploadService`, `AwsFactory`, and `AzureFactory` were not edited

**Part 3**

- [ ] `AuditLog` exists for all three providers, and every upload records one audit entry in
      the same cloud as the file
- [ ] Your comment above `CloudFactory` lists every class you edited, and explains the
      difference from Part 2 in one or two sentences

### ⭐ Bonus

- [ ] Create an `InMemoryCloudFactory` whose storage, queue, and audit log just remember what
      they received in a `List`. In `main()`, upload a file through it and print `PASS` or
      `FAIL` for: the file was stored, exactly one message was published, and that message
      contains the stored location. No cloud is involved (see
      [`examples/03-production-vs-test`](../examples/03-production-vs-test/Main.java))
- [ ] Which SOLID principle would Part 3 start to strain if you kept adding product types,
      like `SecretStore`, `Cache`, and `EmailSender`, to `CloudFactory`? What would you do
      about it?

## 💡 Hint

Look at the two `if`/`else` blocks in `UploadService`. They check **different** settings, but
they should always give the **same** answer. That is exactly the case Abstract Factory exists
for: make one decision, and get every product from it.

If you're unsure whether something belongs in a factory, ask: *"does this differ between AWS
and Azure?"* If yes, it belongs in a concrete product. If no, it stays in `UploadService`.

## Submitting your solution

See [Submitting your solutions](../../../README.md#submitting-your-solutions) in the root
README for the branch/commit convention. Suggested commit message:

```
exercise: Abstract Factory - multi-cloud file upload
```
