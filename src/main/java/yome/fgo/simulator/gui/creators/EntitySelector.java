package yome.fgo.simulator.gui.creators;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.Formation;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.simulator.gui.components.CraftEssenceDataAnchorPane;
import yome.fgo.simulator.gui.components.MysticCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.ServantDataAnchorPane;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;

public class EntitySelector {

    public static Formation selectFormation(
            final Window window,
            final List<Formation> formationList,
            final Map<Integer, ServantDataAnchorPane> servantDataMap,
            final Map<Integer, CraftEssenceDataAnchorPane> ceDataMap,
            final Map<Integer, MysticCodeDataAnchorPane> mcDataMap
    ) throws IOException {
        final Formation.Builder selection = Formation.newBuilder();

        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);

        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("formationViewer.fxml"));
        final Parent root = fxmlLoader.load();
        final FormationFXMLController controller = fxmlLoader.getController();
        controller.fillFormations(
                formationList,
                servantDataMap,
                ceDataMap,
                mcDataMap,
                selection
        );

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(BuffBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "Formation Viewer"));
        newStage.setScene(scene);

        newStage.showAndWait();

        return selection.getName().isEmpty() ? null : selection.build();
    }

    public static ServantDataAnchorPane selectServant(
            final Window window,
            final Map<Integer, ServantDataAnchorPane> servantDataMap
    ) throws IOException {
        final ServantDataAnchorPane selection = new ServantDataAnchorPane();

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

    public static CraftEssenceDataAnchorPane selectCraftEssence(
            final Window window,
            final Map<Integer, CraftEssenceDataAnchorPane> ceDataMap
    ) throws IOException {
        final CraftEssenceDataAnchorPane selection = new CraftEssenceDataAnchorPane();

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

    public static MysticCodeDataAnchorPane selectMysticCode(
            final Window window,
            final Map<Integer, MysticCodeDataAnchorPane> mcDataMap,
            final Gender gender
    ) throws IOException {
        final MysticCodeDataAnchorPane selection = new MysticCodeDataAnchorPane();

        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(window);

        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("entityFilter.fxml"));
        final Parent root = fxmlLoader.load();
        final EntityFilterFXMLController controller = fxmlLoader.getController();
        controller.fillMysticCode(mcDataMap, selection, gender);

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(BuffBuilder.class.getResource("style.css").toExternalForm());

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "Mystic Code Selector"));
        newStage.setScene(scene);

        newStage.showAndWait();

        return selection.getMysticCodeData() == null ? null : selection;
    }
}
