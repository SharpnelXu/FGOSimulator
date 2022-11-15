package yome.fgo.simulator.gui.helpers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.simulator.gui.creators.BuffBuilder;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.setWindowSize;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class LaunchUtils {
    public static void launch(final String appName, final String fileName) throws IOException {
        launch(appName, fileName, false);
    }

    public static void launch(final String appName, final String fileName, final boolean setMaximized) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource(fileName + ".fxml"));
        final Parent root = fxmlLoader.load();
        launch(appName, root, setMaximized);
    }

    public static void launch(final String appName, final Parent root, final boolean setMaximized) {
        final Stage newStage = createStage(appName, root, setMaximized);
        newStage.show();
    }

    public static void launchBlocking(final String appName, final Window window, final Parent root, final boolean setMaximized) {
        final Stage newStage = createStage(appName, root, setMaximized);
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);
        newStage.showAndWait();
    }

    private static Stage createStage(final String appName, final Parent root, final boolean setMaximized) {
        final Stage newStage = new Stage();

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(BuffBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, appName));
        newStage.setScene(scene);

        setWindowSize(root);
        newStage.setMaximized(setMaximized);
        return newStage;
    }
}
