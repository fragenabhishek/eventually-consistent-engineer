package com.fragenabhishek.designpatterns.behavioral;

abstract class Drink {
    public final void makeDrink(){
        boilWater();
        addMainIngredient();
        pourInCup();
        addExtras();
        addSpecialTouch();
    }

    private void boilWater(){
        System.out.println("Boiling Water");
    }
    private void pourInCup(){
        System.out.println("Pouring into Cup");
    }
    protected abstract void addMainIngredient();
    protected abstract void addExtras();

    protected void addSpecialTouch(){

    }
}

class Tea extends Drink{

    @Override
    protected void addMainIngredient() {
        System.out.println("Adding tea leaves");
    }

    @Override
    protected void addExtras() {
        System.out.println("Adding sugar");
    }
}

class Coffee extends Drink{

    @Override
    protected void addMainIngredient() {
        System.out.println("Adding coffee powder");
    }

    @Override
    protected void addExtras() {
        System.out.println("Adding Sugar");
    }

    protected void addSpecialTouch(){
        System.out.println("Adding whipped cream");
    }
}

class Main6{
    public static void main(String[] args) {
        Drink tea = new Tea();
        Drink coffee = new Coffee();

        System.out.println("Making Tea : ");
        tea.makeDrink();

        System.out.println(" Making Coffee : ");
        coffee.makeDrink();
    }
}
