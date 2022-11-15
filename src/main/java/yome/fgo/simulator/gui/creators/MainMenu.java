package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.FileInputStream;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class MainMenu extends Application {
    @Override
    public void start(final Stage stage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("mainMenu.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.getIcons().add(new Image(new FileInputStream(ResourceManager.getCCThumbnail("default"))));

        stage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "FGO Simulator") + " v1.05");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(final String[] args) {
        launch();
    }
}
