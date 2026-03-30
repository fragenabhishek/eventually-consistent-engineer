package com.fragenabhishek.designpatterns.structural;

/*
 * =====================================================
 *  PROXY PATTERN (Structural)
 * =====================================================
 *
 *  Intent:   Provide a substitute/placeholder that controls access to another object.
 *            Same interface as the real object, with added behavior.
 *
 *  Problem:  Loading a high-res image from disk is expensive (takes seconds).
 *            You don't want to load it until someone actually displays it.
 *
 *  Solution: A ProxyImage has the same interface as RealImage. On first display(),
 *            it creates the RealImage (lazy loading). On subsequent calls, it
 *            delegates to the already-loaded image.
 *
 *  Structure:
 *    Image       →  Common interface (both real and proxy implement this)
 *    RealImage   →  Real Subject (expensive to create, does the actual work)
 *    ProxyImage  →  Proxy (controls access, lazy-loads the real subject)
 *
 *  Types of Proxy:
 *    - Virtual Proxy:    lazy-load expensive object (this example)
 *    - Protection Proxy: access control (check permissions before delegating)
 *    - Remote Proxy:     local stand-in for remote object (gRPC stubs)
 *    - Caching Proxy:    cache results from real object
 *
 *  Real-world: Spring AOP (@Transactional, @Cacheable), Hibernate lazy loading, gRPC stubs
 * =====================================================
 */

// --- Common interface (makes proxy and real subject interchangeable) ---
interface Image {
    void display();
}

// --- Real Subject: expensive to create ---
public class RealImage implements Image {
    private final String filename;

    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk();                    // heavy operation happens at construction
    }

    private void loadFromDisk() {
        System.out.println("Loading " + filename + " from disk...");
    }

    @Override
    public void display() {
        System.out.println("Displaying " + filename);
    }
}

// --- Proxy: controls access, lazy-loads the real image ---
class ProxyImage implements Image {
    private RealImage realImage;           // null until first use
    private final String filename;

    public ProxyImage(String filename) {
        this.filename = filename;          // lightweight — no disk load yet
    }

    @Override
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename);  // lazy load on first call
        }
        realImage.display();                       // delegate to real
    }
}

// --- Demo ---
class ProxyDemo {
    public static void main(String[] args) {
        // Create proxy — NO disk load happens here (lightweight)
        Image img1 = new ProxyImage("photo1.jpg");
        Image img2 = new ProxyImage("photo2.jpg");
        System.out.println("Proxies created. No images loaded yet.\n");

        // First display → triggers lazy load
        img1.display();
        // Loading photo1.jpg from disk...
        // Displaying photo1.jpg

        System.out.println();

        // Second display → uses already-loaded image (no reload)
        img1.display();
        // Displaying photo1.jpg

        System.out.println();

        // img2 still not loaded until needed
        img2.display();
        // Loading photo2.jpg from disk...
        // Displaying photo2.jpg
    }
}
