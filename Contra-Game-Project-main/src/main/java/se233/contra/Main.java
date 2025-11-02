package se233.contra;

import javafx.application.Application;
import javafx.stage.Stage;
import se233.contra.view.MenuView;
import se233.contra.util.GameLogger;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            GameLogger.info("=== Contra Boss Fight Starting ===");

            MenuView menuView = new MenuView(primaryStage);
            menuView.show();

        } catch (Exception e) {
            GameLogger.error("Failed to start application", e);
        }
    }

    @Override
    public void stop() {
        GameLogger.info("=== Contra Boss Fight Stopping ===");
    }

    public static void main(String[] args) {
        launch(args);
    }
}