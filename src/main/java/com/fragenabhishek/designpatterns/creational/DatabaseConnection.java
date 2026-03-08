package com.fragenabhishek.designpatterns.creational;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;


    private DatabaseConnection() {
        // private constructor
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public void showMessage() {
        System.out.println("Singleton database connectin!");
    }

    public static void main(String[] args) {
        DatabaseConnection singleton = DatabaseConnection.getInstance();
        singleton.showMessage();
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        DatabaseConnection db2 = DatabaseConnection.getInstance();

        if (db1 == db2) {
            System.out.println("Both modules share the same instance!");
        }
    }
}
