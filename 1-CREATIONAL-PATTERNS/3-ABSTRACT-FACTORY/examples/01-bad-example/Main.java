// ❌ BAD: the application picks every UI component itself, one if/else per
// component. Nothing ties the choices together, so when Linux support was added
// to the button code and forgotten in the checkbox code, the UI became a mix of
// two different platforms, and nobody got an error.
interface Button {
    void render();
}

interface Checkbox {
    void render();
}

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

class Application {
    private final Button button;
    private final Checkbox checkbox;

    public Application(String os) {
        // decision #1: which button?
        if (os.equals("WINDOWS")) {
            button = new WindowsButton();
        } else if (os.equals("MAC")) {
            button = new MacButton();
        } else if (os.equals("LINUX")) {
            button = new LinuxButton();
        } else {
            throw new IllegalArgumentException("Unsupported OS: " + os);
        }

        // decision #2: which checkbox? Made separately... and LINUX was forgotten here,
        // so Linux silently falls through to the Windows default
        if (os.equals("MAC")) {
            checkbox = new MacCheckbox();
        } else {
            checkbox = new WindowsCheckbox();
        }
    }

    public void renderUI() {
        button.render();
        checkbox.render();
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("--- WINDOWS ---");
        new Application("WINDOWS").renderUI();

        System.out.println("--- MAC ---");
        new Application("MAC").renderUI();

        System.out.println("--- LINUX ---");
        new Application("LINUX").renderUI();   // Linux button + Windows checkbox!
    }
}

/*
 * Problems with this design:
 *  - Every component is chosen by its own if/else, so nothing guarantees a matching set
 *  - A forgotten branch doesn't fail: it silently produces a mixed, broken UI
 *  - Adding a platform means editing every if/else in Application
 *  - Application knows all six concrete component classes by name
 *
 * SOLID violations:
 *  - OCP: a new platform forces edits to Application, a class that already worked
 *  - DIP: Application depends on WindowsButton, MacCheckbox, ... instead of only on
 *    the Button and Checkbox abstractions
 *  - SRP: Application renders the UI AND decides which platform classes to build,
 *    so it changes both when the screen changes and when a platform is added
 */
