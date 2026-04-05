/*
 * =====================================================
 *  MEDIATOR PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Define an object (Mediator) that encapsulates how a set of
 *            objects interact. It promotes loose coupling by preventing
 *            objects from referring to each other explicitly.
 *
 *  Problem:  UI components (Button, TextBox, CheckBox) need to communicate
 *            with each other. If each component directly interacts with others,
 *            it leads to tight coupling and messy dependencies.
 *
 *  Solution: Introduce a Mediator that handles all communication between
 *            components. Components notify the mediator instead of directly
 *            calling each other.
 *
 *  Structure:
 *    Mediator (interface)        → Defines communication contract
 *    FormMediator                → Concrete mediator (controls interactions)
 *    Component (abstract)        → Base class for UI elements
 *    Button, TextBox, CheckBox   → Concrete components
 *
 *  Flow:
 *    Button click → Mediator notified → Mediator updates TextBox & CheckBox
 *    TextBox change → Mediator notified → Logs change
 *    CheckBox check → Mediator notified → Logs change
 *
 *  Key: Components are decoupled — they only know the mediator,
 *       not each other.
 *
 *  Real-world: Dialog controllers in UI frameworks, event buses,
 *              chat rooms, air traffic control systems
 * =====================================================
 */
package com.fragenabhishek.designpatterns.behavioral;

interface Mediator{
    void notify(Component sender, String event);
}

abstract class Component{
    protected Mediator mediator;
    public Component(Mediator mediator){
        this.mediator = mediator;
    }
}

class Button extends Component{

    public Button(Mediator mediator) {
        super(mediator);
    }
    public void click(){
        System.out.println("Button clicked");
        mediator.notify(this, "click");
    }
}

class TextBox extends Component{

    public TextBox(Mediator mediator) {
        super(mediator);
    }

    public void setText(String text){
        System.out.println("TextBox set to : " + text);
        mediator.notify(this, "textChange");
    }
}

class CheckBox extends Component{

    public CheckBox(Mediator mediator) {
        super(mediator);
    }
    public void check(){
        System.out.println("Checkbox checked");
        mediator.notify(this, "check");
    }
}

class FormMediator implements Mediator{
    private Button button;
    private TextBox textBox;
    private CheckBox checkBox;

    public void setComponents(Button b, TextBox t, CheckBox c){
        button = b;
        textBox = t;
        checkBox = c;
    }

    @Override
    public void notify(Component sender, String event) {
        if(sender == button && event.equals("click")){
            textBox.setText("Button was clicked");
            checkBox.check();
        }else if(sender == textBox && event.equals("textChange")){
            System.out.println("Mediator noticed TextBox change");
        }else if(sender == checkBox && event.equals("check")){
            System.out.println("Mediator noticed Checkbox checked");
        }
    }
}

public class MediatorDemo {
    public static void main(String[] args) {
        FormMediator mediator = new FormMediator();
        Button button = new Button(mediator);
        TextBox textBox = new TextBox(mediator);
        CheckBox checkBox = new CheckBox(mediator);

        mediator.setComponents(button, textBox, checkBox);
        button.click();
    }
}