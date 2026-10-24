import java.util.ArrayList;
import java.util.List;

// ✅ Virtual proxy: delays creating an EXPENSIVE object until it is really used.
// A photo gallery shows 6 thumbnails, but the user opens only 2 photos. Without a
// proxy, all 6 full-resolution images are loaded from disk up front.

// --- Subject ---
interface Image {
    String getFileName();
    void display();
}

// --- Real subject: loading happens in the constructor, and it is slow ---
class HighResolutionImage implements Image {
    static int loaded = 0;
    private final String fileName;

    public HighResolutionImage(String fileName) {
        this.fileName = fileName;
        loaded++;
        System.out.println("  Loading " + fileName + " from disk (40 MB, slow)...");
    }

    public String getFileName() {
        return fileName;
    }

    public void display() {
        System.out.println("  Displaying " + fileName + " in full resolution");
    }
}

// --- Virtual proxy: cheap to create, builds the real image on first display() ---
class LazyImage implements Image {
    private final String fileName;
    private HighResolutionImage realImage;   // null until somebody really needs it

    public LazyImage(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;                     // answered without loading anything
    }

    public void display() {
        if (realImage == null) {
            realImage = new HighResolutionImage(fileName);
        }
        realImage.display();
    }
}

// --- Client: the gallery only knows Image ---
class Gallery {
    private final List<Image> images = new ArrayList<>();

    public void add(Image image) {
        images.add(image);
    }

    public void showThumbnails() {
        for (Image image : images) {
            System.out.println("  [thumbnail] " + image.getFileName());
        }
    }

    public void open(int index) {
        images.get(index).display();
    }
}

public class Main {
    private static final String[] FILES = {
        "beach.jpg", "mountain.jpg", "city.jpg", "forest.jpg", "desert.jpg", "lake.jpg"
    };

    private static void run(String title, boolean useProxy) {
        HighResolutionImage.loaded = 0;
        System.out.println(title);

        Gallery gallery = new Gallery();
        for (String file : FILES) {
            gallery.add(useProxy ? new LazyImage(file) : new HighResolutionImage(file));
        }
        gallery.showThumbnails();
        gallery.open(0);
        gallery.open(3);
        gallery.open(0);   // the same photo again

        System.out.println("  => images loaded from disk: " + HighResolutionImage.loaded + " of " + FILES.length + "\n");
    }

    public static void main(String[] args) {
        run("Without a proxy:", false);
        run("With a virtual proxy:", true);
    }
}

/*
 * What to notice:
 *  - Gallery is identical in both runs; only the objects handed to it differ
 *  - With the proxy, only the 2 photos actually opened are ever loaded, each one once
 *  - getFileName() is answered by the proxy itself, without touching the real image
 *  - This is why Proxy is sometimes mistaken for a creational pattern: a virtual proxy
 *    controls WHEN the real object is created. But the pattern is about the structure,
 *    an object standing in for another behind the same interface, so it is structural
 */
