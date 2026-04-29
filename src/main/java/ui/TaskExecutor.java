package ui;

import javafx.concurrent.Task;

public class TaskExecutor {
    private Runnable onSuccess;
    private Runnable onFailure;

    public TaskExecutor(Runnable successCallback) {
        this(successCallback, () -> {});
    }

    public TaskExecutor(Runnable successCallback, Runnable failureCallback) {
        this.onSuccess = successCallback;
        this.onFailure = failureCallback;
    }

    public void execute(SimpleTask task) {
        Task<Void> backgroundTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                task.run();
                return null;
            }
        };

        backgroundTask.setOnSucceeded(e -> onSuccess.run());
        backgroundTask.setOnFailed(e -> onFailure.run());

        new Thread(backgroundTask).start();
    }

    public <T> void executeWithResult(ResultTask<T> task, ResultCallback<T> callback) {
        Task<T> backgroundTask = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return task.run();
            }
        };

        backgroundTask.setOnSucceeded(e -> {
            try {
                T result = backgroundTask.getValue();
                callback.onResult(result);
                onSuccess.run();
            } catch (Exception ex) {
                onFailure.run();
            }
        });
        backgroundTask.setOnFailed(e -> onFailure.run());

        new Thread(backgroundTask).start();
    }

    @FunctionalInterface
    public interface SimpleTask {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ResultTask<T> {
        T run() throws Exception;
    }

    @FunctionalInterface
    public interface ResultCallback<T> {
        void onResult(T result) throws Exception;
    }
}
