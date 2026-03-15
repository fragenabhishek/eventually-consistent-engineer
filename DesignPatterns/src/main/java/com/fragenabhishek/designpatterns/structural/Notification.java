package com.fragenabhishek.designpatterns.structural;

public interface Notification {
    void send(String message);
}

class SMSNotification implements Notification{

    @Override
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
}

class EmailNotification implements Notification{

    @Override
    public void send(String message) {
        System.out.println("Sending Email: " + message);
    }
}

class NotificationDecorator implements Notification{
    protected Notification wrapperNotification;
    public NotificationDecorator(Notification notification){
        this.wrapperNotification = notification;
    }
    @Override
    public void send(String message) {
        wrapperNotification.send(message);
    }
}

class LoggingDecorator extends NotificationDecorator{

    public LoggingDecorator(Notification notification) {
        super(notification);
    }
    @Override
    public void send(String message) {
        System.out.println("Logging notification..."); // print log
        super.send(message); // send the actual message

    }

}

class EncryptionDecorator extends NotificationDecorator {

    public EncryptionDecorator(Notification notification) {
        super(notification);
    }

    @Override
    public void send(String message) {
        // modify the message
        String encryptedMessage = "Encrypted(" + message + ")";
        super.send(encryptedMessage); // send the encrypted message
    }
}


class Main{
    public static void main(String[] args) {
        Notification sms = new SMSNotification();
        Notification logger = new LoggingDecorator(sms);
        Notification encrypt = new EncryptionDecorator(logger);
        encrypt.send("order created");

        Notification email = new EncryptionDecorator(new LoggingDecorator(new EmailNotification()));
        email.send("email check order created");

    }
}
