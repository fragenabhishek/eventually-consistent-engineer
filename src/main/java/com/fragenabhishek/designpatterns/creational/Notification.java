package com.fragenabhishek.designpatterns.creational;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface Notification {

    void notifyUser();
}



class EmailNotification implements Notification{
    @Override
    public void notifyUser() {
        System.out.println("Sending Email Notification");
    }
}


class SMSNotification implements Notification{

    @Override
    public void notifyUser() {
        System.out.println("Sending SMS Notification");
    }
}
class PushNotification implements Notification{

    @Override
    public void notifyUser() {
        System.out.println("Sending PUSH Notification");
    }
}

class NotificationFactory{

    private static Map<String, Supplier<Notification>> notificationMap = new HashMap<>();
    static {
        notificationMap.put("EMAIL", EmailNotification :: new);
        notificationMap.put("PUSH", PushNotification :: new);
        notificationMap.put("SMS", SMSNotification :: new);

    }

    public static Notification createNotification(String type){
        Supplier<Notification> supplier = notificationMap.get(type);
        return supplier != null ? supplier.get() : null;
    }
}

class NotificationMail{
    public static void main(String[] args) {
        Notification notification = NotificationFactory.createNotification("SMS");
        notification.notifyUser();

        Notification notification1 = NotificationFactory.createNotification("PUSH");
        notification1.notifyUser();
    }
}