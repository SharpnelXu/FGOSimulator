package yome.fgo.simulator.gui.creators;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.simulator.gui.helpers.LaunchUtils;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class EnemyCreator extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("enemyCreator.fxml"));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "EnemyCreator"));
        stage.setScene(scene);
        stage.show();
    }

    public static void editCombatantData(final Window window, final CombatantData.Builder builder) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(ConditionBuilder.class.getResource("enemyCreator.fxml"));
        final Parent root = fxmlLoader.load();
        final EnemyCreatorFXMLController controller = fxmlLoader.getController();
        controller.setParentBuilder(builder);

        LaunchUtils.launchBlocking("EnemyCreator", window, root, false);
    }

    public static void main(final String[] args) {
        launch();
    }
}
