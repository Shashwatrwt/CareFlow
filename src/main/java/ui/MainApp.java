package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MainApp extends Application {

    static {
        Logger.getLogger("com.gluonhq").setLevel(Level.SEVERE);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainController controller = new MainController();
        primaryStage.setTitle("CareFlow: Smart Health Response Platform");
        primaryStage.setScene(new Scene(controller.getRoot(), 1200, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
