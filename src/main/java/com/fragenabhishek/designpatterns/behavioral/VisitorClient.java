package com.fragenabhishek.designpatterns.behavioral;


//step 1 : Element interface
interface Animal{
    void accept(AnimalVisitor visitor);
}
//step 2 : Visitor Interface
interface AnimalVisitor{
    void visitLion(Lion lion);
    void visitElephant(Elephant elephant);
}

//Step 3: Concrete Elements
class Lion implements Animal{

    @Override
    public void accept(AnimalVisitor visitor) {
        visitor.visitLion(this);
    }
}

class Elephant implements Animal{

    @Override
    public void accept(AnimalVisitor visitor) {
        visitor.visitElephant(this);
    }
}

//step : 4 Concrete Visitor
class FeedingVisitor implements AnimalVisitor{

    @Override
    public void visitLion(Lion lion) {
        System.out.println("Feeding the lion");
    }

    @Override
    public void visitElephant(Elephant elephant) {
        System.out.println("Feeding the elephant");
    }
}

class HealthCheckVisitor implements AnimalVisitor{

    @Override
    public void visitLion(Lion lion) {
        System.out.println("Checking lion's health");
    }

    @Override
    public void visitElephant(Elephant elephant) {
        System.out.println("Checking elephant's health");
    }
}

public class VisitorClient {

    public static void main(String[] args) {
        Animal[] animals = { new Lion(), new Elephant() };
        AnimalVisitor feeder = new FeedingVisitor();
        AnimalVisitor doctor = new HealthCheckVisitor();

        for(Animal animal : animals){
            animal.accept(feeder);
            animal.accept(doctor);
        }
    }
}
