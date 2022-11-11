package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.setWindowSize;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class EffectBuilder extends Application {
    @Override
    public void start(final Stage stage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("effectBuilder.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "EffectBuilder"));
        stage.setScene(scene);
        stage.show();
    }

    public static void createEffect(final Window window, final EffectData.Builder builder) throws IOException {
        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);

        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("effectBuilder.fxml"));
        final Parent root = fxmlLoader.load();
        final EffectBuilderFXMLController controller = fxmlLoader.getController();
        controller.setParentBuilder(builder);

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(BuffBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "EffectBuilder"));
        newStage.setScene(scene);

        setWindowSize(root);
        newStage.showAndWait();
    }

    public static void main(final String[] args) {
        launch();
    }
}
