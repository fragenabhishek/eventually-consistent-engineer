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
        System.out.println("TestBox set to : + text");
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
            System.out.println("Mediator noticed testBox Change");
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
