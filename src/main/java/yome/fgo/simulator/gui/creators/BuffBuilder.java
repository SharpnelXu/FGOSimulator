package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.simulator.gui.helpers.LaunchUtils;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class BuffBuilder extends Application {
    @Override
    public void start(final Stage stage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("buffBuilder.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "BuffBuilder"));
        stage.setScene(scene);
        stage.show();
    }

    public static void createBuff(final Window window, final BuffData.Builder builder) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("buffBuilder.fxml"));
        final Parent root = fxmlLoader.load();
        final BuffBuilderFXMLController controller = fxmlLoader.getController();
        controller.setParentBuilder(builder);

        LaunchUtils.launchBlocking("BuffBuilder", window, root, false);
    }

    public static void main(final String[] args) {
        launch();
    }
}
