package com.fragenabhishek.designpatterns.behavioral;

import java.util.Stack;

/*
 * =====================================================
 *  MEMENTO PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Capture and restore an object's internal state without violating encapsulation.
 *
 *  Problem:  A text editor needs undo/redo at the STATE level (not just the action level
 *            like Command pattern). You want to snapshot the entire editor content at each
 *            point and restore it freely.
 *
 *  Solution: Three roles:
 *            - Originator creates and restores from snapshots (mementos)
 *            - Memento holds the saved state (immutable, opaque to outsiders)
 *            - Caretaker manages the memento stack (doesn't peek inside mementos)
 *
 *  Structure:
 *    TextMemento           →  Memento (immutable snapshot of state)
 *    TextEditorOriginator  →  Originator (creates/restores mementos)
 *    History               →  Caretaker (manages undo/redo stacks of mementos)
 *
 *  Memento vs Command:
 *    - Memento: saves full STATE snapshots → restore to any previous state
 *    - Command: saves ACTIONS → undo by reversing the action
 *    - Use Memento when state is complex or actions aren't easily reversible
 *
 *  Real-world: Browser back/forward, game save/load, DB transaction rollback, Ctrl+Z
 * =====================================================
 */

// --- Memento: immutable state snapshot ---
class TextMemento {
    private final String text;

    public TextMemento(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

// --- Originator: the object whose state we want to save/restore ---
class TextEditorOriginator {
    private String text = "";

    public void write(String newText) {
        text += newText;
    }

    public TextMemento save() {
        return new TextMemento(text);         // create a snapshot of current state
    }

    public void restore(TextMemento memento) {
        if (memento != null) {
            text = memento.getText();          // restore state from snapshot
        }
    }

    public String read() {
        return text;
    }
}

// --- Caretaker: manages undo/redo history without knowing what's inside mementos ---
class History {
    private final Stack<TextMemento> undoStack = new Stack<>();
    private final Stack<TextMemento> redoStack = new Stack<>();

    // Save the current state BEFORE making a change
    public void saveBeforeChange(TextEditorOriginator editor) {
        undoStack.push(editor.save());
        redoStack.clear();                     // new change invalidates redo history
    }

    public TextMemento undo(TextEditorOriginator editor) {
        if (!undoStack.isEmpty()) {
            redoStack.push(editor.save());     // save current state for redo
            return undoStack.pop();            // return previous state
        }
        return null;
    }

    public TextMemento redo(TextEditorOriginator editor) {
        if (!redoStack.isEmpty()) {
            undoStack.push(editor.save());     // save current state for undo
            return redoStack.pop();            // return redo state
        }
        return null;
    }
}

// --- Demo ---
public class Memento {
    public static void main(String[] args) {
        TextEditorOriginator editor = new TextEditorOriginator();
        History history = new History();

        // Write "Hello " — save state before each change
        history.saveBeforeChange(editor);
        editor.write("Hello ");

        // Write "World" — save state before change
        history.saveBeforeChange(editor);
        editor.write("World");

        System.out.println("Current:      " + editor.read());   // Hello World

        // Undo → back to "Hello "
        editor.restore(history.undo(editor));
        System.out.println("After Undo:   " + editor.read());   // Hello

        // Undo → back to ""
        editor.restore(history.undo(editor));
        System.out.println("After Undo 2: " + editor.read());   // (empty)

        // Redo → forward to "Hello "
        editor.restore(history.redo(editor));
        System.out.println("After Redo:   " + editor.read());   // Hello

        // Redo → forward to "Hello World"
        editor.restore(history.redo(editor));
        System.out.println("After Redo 2: " + editor.read());   // Hello World
    }
}
