package com.fragenabhishek.designpatterns.behavioral;

import java.util.Stack;

/*
 * =====================================================
 *  COMMAND PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Encapsulate a request as an object, allowing undo/redo, queuing,
 *            and logging of operations.
 *
 *  Problem:  A text editor needs undo/redo. If actions are just direct method calls,
 *            there's no way to remember what was done or reverse it.
 *
 *  Solution: Wrap each action as a Command object with execute() and undo().
 *            An Invoker maintains a history stack. Undo = pop and call undo().
 *
 *  Structure:
 *    Command           →  Interface (execute + undo)
 *    TextEditor        →  Receiver (the actual object being manipulated)
 *    AddTextCommand    →  Concrete Command (knows how to do and undo one action)
 *    Invoker           →  Manages command history, provides undo/redo
 *
 *  Key Insight: Commands are data (objects), not just function calls.
 *               You can store them, replay them, serialize them, queue them.
 *
 *  Real-world: Runnable (a command), job queues, DB transactions (commit/rollback), UI action history
 * =====================================================
 */

// --- Command interface ---
public interface Command {
    void execute();
    void undo();
}

// --- Receiver: the object commands act upon ---
class TextEditor {
    private final StringBuilder content = new StringBuilder();

    void addText(String text) {
        content.append(text);
    }

    void removeLast(int length) {
        int start = content.length() - length;
        if (start >= 0) {
            content.delete(start, content.length());
        }
    }

    String getText() {
        return content.toString();
    }
}

// --- Concrete Command ---
class AddTextCommand implements Command {
    private final TextEditor editor;
    private final String text;

    public AddTextCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.addText(text);
    }

    @Override
    public void undo() {
        editor.removeLast(text.length());  // reverse: remove exactly what was added
    }
}

// --- Invoker: manages command history ---
class Invoker {
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    public void executeCommand(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();       // new action invalidates redo history
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
    }
}

// --- Demo ---
class CommandDemo {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        Invoker invoker = new Invoker();

        // Execute commands
        invoker.executeCommand(new AddTextCommand(editor, "Hello "));
        invoker.executeCommand(new AddTextCommand(editor, "World"));
        System.out.println("After commands: " + editor.getText());  // Hello World

        // Undo last command
        invoker.undo();
        System.out.println("After undo:     " + editor.getText());  // Hello

        // Redo the undone command
        invoker.redo();
        System.out.println("After redo:     " + editor.getText());  // Hello World
    }
}
