package com.fragenabhishek.designpatterns.behavioral;

import java.util.Stack;

// Memento
class TextMemento {
    private final String text;

    public TextMemento(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

// Originator
class TextEditorOriginator {
    private String text = "";

    public void write(String newText) {
        text += newText;
    }

    public TextMemento save() {
        return new TextMemento(text);
    }

    public void restore(TextMemento memento) {
        if (memento != null) {
            text = memento.getText();
        }
    }

    public String read() {
        return text;
    }
}

// Caretaker with proper undo/redo
class History {
    private Stack<TextMemento> undoStack = new Stack<>();
    private Stack<TextMemento> redoStack = new Stack<>();

    // Save the current state BEFORE a change
    public void saveBeforeChange(TextEditorOriginator editor) {
        undoStack.push(editor.save());
        redoStack.clear(); // New change clears redo history
    }

    public TextMemento undo(TextEditorOriginator editor) {
        if (!undoStack.isEmpty()) {
            TextMemento currentState = editor.save(); // save current for redo
            redoStack.push(currentState);
            return undoStack.pop();
        }
        return null;
    }

    public TextMemento redo(TextEditorOriginator editor) {
        if (!redoStack.isEmpty()) {
            TextMemento currentState = editor.save(); // save current for undo
            undoStack.push(currentState);
            return redoStack.pop();
        }
        return null;
    }
}

// Usage
public class Memento {
    public static void main(String[] args) {
        TextEditorOriginator editor = new TextEditorOriginator();
        History history = new History();

        // Save initial empty state
        history.saveBeforeChange(editor);

        // First write
        history.saveBeforeChange(editor); // save BEFORE change
        editor.write("hello this ");

        // Second write
        history.saveBeforeChange(editor); // save BEFORE change
        editor.write("memento design");

        System.out.println("Current Text: " + editor.read()); // hello this memento design

        // Undo #1
        editor.restore(history.undo(editor));
        System.out.println("After Undo: " + editor.read()); // hello this

        // Undo #2
        editor.restore(history.undo(editor));
        System.out.println("After Second Undo: " + editor.read()); // ""

        // Redo #1
        editor.restore(history.redo(editor));
        System.out.println("After Redo: " + editor.read()); // hello this

        // Redo #2
        editor.restore(history.redo(editor));
        System.out.println("After Second Redo: " + editor.read()); // hello this memento design
    }
}