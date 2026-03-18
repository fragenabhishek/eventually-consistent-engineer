package com.fragenabhishek.designpatterns.structural;

public class RealImage {
    private String filename;

    public RealImage(String filename){
        this.filename = filename;
        loadImageFromDisk();
    }

    private void loadImageFromDisk() {
        System.out.println("Loading Images " + filename + " from disk ....");
    }

    public void display(){
        System.out.println("Displaying " + filename);
    }
}


class ProxyImage{
    private RealImage realImage;
    private String filename;

    public ProxyImage(String filename){
        this.filename = filename;
    }

    public void display(){

        if(realImage == null){
            realImage = new RealImage(filename);
        }
        realImage.display();
    }
}

class Main4{
    public static void main(String[] args) {
        ProxyImage img1 = new ProxyImage("image1.jpf");
        ProxyImage img2 = new ProxyImage("image2.jpg");

        System.out.println("Images create.");

        img1.display();
        img1.display();
        img2.display();
    }
}
