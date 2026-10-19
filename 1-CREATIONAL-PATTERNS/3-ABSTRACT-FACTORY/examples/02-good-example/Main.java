// ✅ GOOD: one factory per platform creates the WHOLE family of components.
// The application receives one factory, so it can only ever get a matching set.

// --- Abstract products: what the application knows ---
interface Button {
    void render();
}

interface Checkbox {
    void render();
}

// --- Concrete products: the Windows family ---
class WindowsButton implements Button {
    public void render() {
        System.out.println("Rendering Windows button");
    }
}

class WindowsCheckbox implements Checkbox {
    public void render() {
        System.out.println("Rendering Windows checkbox");
    }
}

// --- Concrete products: the macOS family ---
class MacButton implements Button {
    public void render() {
        System.out.println("Rendering macOS button");
    }
}

class MacCheckbox implements Checkbox {
    public void render() {
        System.out.println("Rendering macOS checkbox");
    }
}

// --- Abstract factory: one creation method per product type ---
interface GUIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// --- Concrete factories: one per family ---
class WindowsFactory implements GUIFactory {
    public Button createButton() {
        return new WindowsButton();
    }

    public Checkbox createCheckbox() {
        return new WindowsCheckbox();
    }
}

class MacFactory implements GUIFactory {
    public Button createButton() {
        return new MacButton();
    }

    public Checkbox createCheckbox() {
        return new MacCheckbox();
    }
}

// --- Client: knows only the abstract factory and the abstract products ---
class Application {
    private final Button button;
    private final Checkbox checkbox;

    public Application(GUIFactory factory) {
        button = factory.createButton();
        checkbox = factory.createCheckbox();
    }

    public void renderUI() {
        button.render();
        checkbox.render();
    }
}

// --- Added later, for Linux support. No class above this line was edited. ---
class LinuxButton implements Button {
    public void render() {
        System.out.println("Rendering Linux button");
    }
}

class LinuxCheckbox implements Checkbox {
    public void render() {
        System.out.println("Rendering Linux checkbox");
    }
}

class LinuxFactory implements GUIFactory {
    public Button createButton() {
        return new LinuxButton();
    }

    public Checkbox createCheckbox() {
        return new LinuxCheckbox();
    }
}
// --------------------------------------------------------------------------------

public class Main {

    // The ONE place that turns configuration into a family. This is the only
    // line that changes when a platform is added.
    static GUIFactory factoryFor(String os) {
        switch (os) {
            case "WINDOWS":
                return new WindowsFactory();
            case "MAC":
                return new MacFactory();
            case "LINUX":
                return new LinuxFactory();
            default:
                throw new IllegalArgumentException("Unsupported OS: " + os);
        }
    }

    public static void main(String[] args) {
        for (String os : new String[] { "WINDOWS", "MAC", "LINUX" }) {
            System.out.println("--- " + os + " ---");
            Application app = new Application(factoryFor(os));
            app.renderUI();
        }
    }
}

/*
 * Why this is better:
 *  - Application gets ONE factory, so a mixed UI (Linux button + Windows checkbox)
 *    is no longer possible: every component comes from the same family
 *  - Linux was added with three new classes; Application and the other factories
 *    were not touched (OCP)
 *  - Application depends only on GUIFactory, Button, and Checkbox (DIP)
 *  - Application only renders; choosing platform classes is the factories' job (SRP)
 *
 * The price:
 *  - Adding a new product TYPE (say, a Slider) means adding createSlider() to
 *    GUIFactory and to every concrete factory
 */
