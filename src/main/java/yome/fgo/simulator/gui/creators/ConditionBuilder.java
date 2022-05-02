package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class ConditionBuilder extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws IOException {
        final Parent root = FXMLLoader.load(getClass().getResource("conditionBuilder.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "ConditionBuilder"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
