package yome.fgo.simulator.gui.creators;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.simulator.gui.components.CraftEssenceDataWrapper;
import yome.fgo.simulator.gui.components.MysticCodeDataWrapper;
import yome.fgo.simulator.gui.components.ServantDataWrapper;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;
import java.util.Map;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class EntitySelector {

    public static ServantDataWrapper selectServant(
            final Window window,
            final Map<Integer, ServantDataWrapper> servantDataMap
    ) throws IOException {
        final ServantDataWrapper selection = new ServantDataWrapper();

        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);

        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("entityFilter.fxml"));
        final Parent root = fxmlLoader.load();
        final EntityFilterFXMLController controller = fxmlLoader.getController();
        controller.fillServants(servantDataMap, selection);

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(BuffBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "ServantSelector"));
        newStage.setScene(scene);

        newStage.showAndWait();

        return selection.getServantData() == null ? null : selection;
    }

    public static CraftEssenceDataWrapper selectCraftEssence(
            final Window window,
            final Map<Integer, CraftEssenceDataWrapper> ceDataMap
    ) throws IOException {
        final CraftEssenceDataWrapper selection = new CraftEssenceDataWrapper();

        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);

        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("entityFilter.fxml"));
        final Parent root = fxmlLoader.load();
        final EntityFilterFXMLController controller = fxmlLoader.getController();
        controller.fillCraftEssence(ceDataMap, selection);

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(BuffBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "Craft Essence Selector"));
        newStage.setScene(scene);

        newStage.showAndWait();

        return selection.getCraftEssenceData() == null ? null : selection;
    }

    public static MysticCodeDataWrapper selectMysticCode(
            final Window window,
            final Map<Integer, MysticCodeDataWrapper> mcDataMap
    ) throws IOException {
        final MysticCodeDataWrapper selection = new MysticCodeDataWrapper();

        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);

        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("entityFilter.fxml"));
        final Parent root = fxmlLoader.load();
        final EntityFilterFXMLController controller = fxmlLoader.getController();
        controller.fillMysticCode(mcDataMap, selection);

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(BuffBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "Mystic Code Selector"));
        newStage.setScene(scene);

        newStage.showAndWait();

        return selection.getMysticCodeData() == null ? null : selection;
    }
}
