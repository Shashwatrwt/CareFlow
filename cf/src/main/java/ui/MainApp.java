package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainController controller = new MainController();
        primaryStage.setTitle("CareFlow: Smart Health Response Platform");
        primaryStage.setScene(new Scene(controller.getRoot(), 1200, 800));
        primaryStage.setOnCloseRequest(e -> controller.disposeMap());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
