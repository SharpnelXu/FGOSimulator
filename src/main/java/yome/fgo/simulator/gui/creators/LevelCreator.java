package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import yome.fgo.simulator.gui.helpers.LaunchUtils;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class LevelCreator extends Application {
    @Override
    public void start(final Stage stage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("levelCreator.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "LevelCreator"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(final String[] args) {
        launch();
    }

    public static void simulationPreviewMode() throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("levelCreator.fxml"));
        final Parent root = fxmlLoader.load();
        final LevelCreatorFMXLController controller = fxmlLoader.getController();
        controller.setPreviewMode();

        LaunchUtils.launch("LevelCreator", root, true);
    }
}
