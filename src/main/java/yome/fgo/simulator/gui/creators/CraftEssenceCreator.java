package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class CraftEssenceCreator extends Application {

    public static void main(final String[] args) {
        launch();
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("craftEssenceCreator.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "CraftEssenceCreator"));
        stage.setScene(scene);
        stage.show();
    }

    public static void preview(final Window window, final CraftEssenceData ceData) throws IOException {
        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);

        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("craftEssenceCreator.fxml"));
        final Parent root = fxmlLoader.load();
        final CraftEssenceCreatorFXMLController controller = fxmlLoader.getController();
        controller.setPreviewMode(ceData);

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(BuffBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "CraftEssenceCreator"));
        newStage.setScene(scene);

        newStage.showAndWait();
    }
}
