package com.fragenabhishek.designpatterns.behavioral;

/*
 * =====================================================
 *  TEMPLATE METHOD PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Define the skeleton of an algorithm in a base class,
 *            deferring some steps to subclasses.
 *
 *  Problem:  Making Tea and Coffee follows the same structure:
 *            boil water → add main ingredient → pour → add extras.
 *            But the ingredients and extras differ. Without Template Method,
 *            you'd duplicate the skeleton in every subclass.
 *
 *  Solution: Abstract class defines the algorithm skeleton as a final method (can't be overridden).
 *            Common steps are implemented in the base. Variable steps are abstract.
 *            Optional steps use hook methods (default empty, subclass overrides if needed).
 *
 *  Structure:
 *    Drink                →  Abstract class with template method makeDrink() [final]
 *    addMainIngredient()  →  Abstract step (subclass MUST implement)
 *    addExtras()          →  Abstract step (subclass MUST implement)
 *    addSpecialTouch()    →  Hook method (optional override, default = do nothing)
 *    Tea, Coffee          →  Concrete classes filling in the variable steps
 *
 *  Key: The "final" keyword on makeDrink() prevents subclasses from altering the algorithm order.
 *
 *  Real-world: HttpServlet.service() calls doGet/doPost, Spring JdbcTemplate, AbstractList
 * =====================================================
 */

// --- Abstract class with template method ---
abstract class Drink {

    // Template method — defines the fixed algorithm skeleton
    // final = subclasses cannot change the order of steps
    public final void makeDrink() {
        boilWater();
        addMainIngredient();
        pourInCup();
        addExtras();
        addSpecialTouch();     // hook — optional override
    }

    // Common steps — same for all drinks
    private void boilWater() {
        System.out.println("  Boiling water");
    }

    private void pourInCup() {
        System.out.println("  Pouring into cup");
    }

    // Variable steps — subclass MUST implement
    protected abstract void addMainIngredient();
    protected abstract void addExtras();

    // Hook method — default does nothing; override if needed
    protected void addSpecialTouch() { }
}

// --- Concrete implementations ---

class Tea extends Drink {
    @Override
    protected void addMainIngredient() {
        System.out.println("  Adding tea leaves");
    }

    @Override
    protected void addExtras() {
        System.out.println("  Adding sugar");
    }
}

class Coffee extends Drink {
    @Override
    protected void addMainIngredient() {
        System.out.println("  Adding coffee powder");
    }

    @Override
    protected void addExtras() {
        System.out.println("  Adding sugar and milk");
    }

    @Override
    protected void addSpecialTouch() {
        System.out.println("  Adding whipped cream");  // Coffee-specific hook
    }
}

// --- Demo ---
class TemplateMethodDemo {
    public static void main(String[] args) {
        System.out.println("Making Tea:");
        Drink tea = new Tea();
        tea.makeDrink();
        // Boiling water → Adding tea leaves → Pouring → Adding sugar

        System.out.println("\nMaking Coffee:");
        Drink coffee = new Coffee();
        coffee.makeDrink();
        // Boiling water → Adding coffee powder → Pouring → Adding sugar and milk → Adding whipped cream
    }
}
