package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.setWindowSize;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class VariationBuilder extends Application {
    @Override
    public void start(final Stage stage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("variationBuilder.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "VariationBuilder"));
        stage.setScene(scene);
        stage.show();
    }

    public static void createVariation(final Window window, final VariationData.Builder builder) throws IOException {
        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);

        final FXMLLoader fxmlLoader = new FXMLLoader(VariationBuilder.class.getResource("variationBuilder.fxml"));
        final Parent root = fxmlLoader.load();
        final VariationBuilderFXMLController controller = fxmlLoader.getController();
        controller.setVariationBuilder(builder);

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(VariationBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "VariationBuilder"));
        newStage.setScene(scene);

        setWindowSize(root);
        newStage.showAndWait();
    }

    public static void main(final String[] args) {
        launch();
    }
}
