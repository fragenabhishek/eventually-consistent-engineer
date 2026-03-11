package com.fragenabhishek.designpatterns.creational;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;


    private DatabaseConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static synchronized DatabaseConnection getInstance(String url, String username, String password) {
        if (instance == null) {
            instance = new DatabaseConnection(url,username,password);
        }
        return instance;
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseConnection is not initialized. Call getInstance(url, username, password) first.");
        }
        return instance;
    }

    public void showMessage() {
        System.out.println("Singleton database connection!");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
    }

    public static void main(String[] args) {
        DatabaseConnection singleton = DatabaseConnection.getInstance(
                "jdbc:mysql://localhost:3306/mydb", "admin", "password123"
        );
        singleton.showMessage();

        // Subsequent calls: no need to provide credentials
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        DatabaseConnection db2 = DatabaseConnection.getInstance();

        if (db1 == db2) {
            System.out.println("Both modules share the same instance!");
        }
    }
}
