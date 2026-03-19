package com.fragenabhishek.designpatterns.behavioral;

import java.util.Stack;

public interface Command {
    void execute();
    void undo();
}

class TextEditor{
    StringBuilder holdText = new StringBuilder();

    void addText(String text){
        holdText.append(text);
    }

    void removeLast(int length){
        int start = holdText.length() - length;
        if(start >= 0){
            holdText.delete(start, holdText.length());
        }
    }

    String getText(){
        return holdText.toString();
    }
}

class AddTextCommand implements Command{
    private TextEditor textEditor ;
    private String text;
    public AddTextCommand(TextEditor editor, String text){
        this.textEditor = editor;
        this.text = text;
    }
    @Override
    public void execute() {
        textEditor.addText(text);
    }

    @Override
    public void undo() {
    textEditor.removeLast(text.length());
    }
}

class Invoker{
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void executeCommand(Command cmd){
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void undo(){
        if(!undoStack.isEmpty()){
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    public void redo(){
        if(!redoStack.isEmpty()){
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
    }
}

class Main5{
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        Invoker invoker = new Invoker();

        AddTextCommand cmd1 = new AddTextCommand(editor, "hello");
        AddTextCommand cmd2 = new AddTextCommand(editor, "World");

        invoker.executeCommand(cmd1);
        invoker.executeCommand(cmd2);

        System.out.println("After executing commands: " + editor.getText());

        invoker.undo();
        System.out.println("After undo: " + editor.getText());

        invoker.redo();
        System.out.println("After redo: " + editor.getText());

    }
}
