package com.fragenabhishek.designpatterns.creational;

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

    public static Notification createNotification(String type){
        if(type.equals("EMAIL")){
            return new EmailNotification();
        }else if(type.equals("SMS")){
            return new SMSNotification();
        }else if(type.equals("PUSH")){
            return new PushNotification();
        }
        return null;
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