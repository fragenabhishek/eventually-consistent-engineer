package com.fragenabhishek.designpatterns.creational;

/*
 * =====================================================
 *  SINGLETON PATTERN (Creational)
 * =====================================================
 *
 *  Intent:   Ensure a class has only ONE instance and provide a global access point to it.
 *
 *  Problem:  Multiple parts of the app create their own DB connections → resource waste,
 *            inconsistent state, connection pool exhaustion.
 *
 *  Solution: Private constructor + static getInstance() that returns the same object every time.
 *
 *  Structure:
 *    DatabaseConnection  →  the Singleton class (private constructor, static instance)
 *
 *  Thread Safety:
 *    - volatile ensures the instance is visible across threads immediately
 *    - Double-checked locking avoids synchronizing every call (only the first creation locks)
 *
 *  Real-world: Spring beans (default scope), Runtime.getRuntime(), Logger instances
 *
 *  Variants:
 *    1. Eager init       → private static final INSTANCE = new Singleton()
 *    2. Synchronized     → synchronized on getInstance() (simple but slow)
 *    3. Double-checked   → volatile + sync block (used below)
 *    4. Bill Pugh holder → static inner class (JVM guarantees thread-safe class loading)
 *    5. Enum singleton   → enum with single constant (safest, prevents reflection attacks)
 * =====================================================
 */
public class DatabaseConnection {

    // volatile prevents instruction reordering — ensures partially constructed
    // object is never visible to another thread
    private static volatile DatabaseConnection instance;

    private final String url;
    private final String username;
    private final String password;

    // Private constructor — no one outside can call new DatabaseConnection()
    private DatabaseConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // Double-checked locking: first call synchronizes and creates; subsequent calls skip the lock
    public static DatabaseConnection getInstance(String url, String username, String password) {
        if (instance == null) {                             // 1st check (no lock — fast path)
            synchronized (DatabaseConnection.class) {
                if (instance == null) {                     // 2nd check (with lock — safe path)
                    instance = new DatabaseConnection(url, username, password);
                }
            }
        }
        return instance;
    }

    // Convenience overload for subsequent calls after initialization
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "DatabaseConnection not initialized. Call getInstance(url, username, password) first.");
        }
        return instance;
    }

    public void showMessage() {
        System.out.println("Singleton database connection!");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
    }

    // --- Demo ---
    public static void main(String[] args) {
        // First call: creates the singleton with credentials
        DatabaseConnection db = DatabaseConnection.getInstance(
                "jdbc:mysql://localhost:3306/mydb", "admin", "password123"
        );
        db.showMessage();

        // Subsequent calls: no need to provide credentials — same instance returned
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        DatabaseConnection db2 = DatabaseConnection.getInstance();

        System.out.println("Same instance? " + (db1 == db2));  // true
    }
}
