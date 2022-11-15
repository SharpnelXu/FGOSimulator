package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.simulator.gui.helpers.LaunchUtils;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class ServantCreator extends Application {
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("servantCreator.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "ServantCreator"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void preview(final Window window, final ServantData servantData) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("servantCreator.fxml"));
        final Parent root = fxmlLoader.load();
        final ServantCreatorFXMLController controller = fxmlLoader.getController();
        controller.setPreviewMode(servantData);

        LaunchUtils.launchBlocking("ServantCreator", window, root, false);
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
