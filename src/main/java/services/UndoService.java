package services;

import java.util.Stack;

public class UndoService {
    private final Stack<Runnable> undoStack = new Stack<>();

    public void push(Runnable operation) {
        undoStack.push(operation);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            undoStack.pop().run();
        }
    }

}
