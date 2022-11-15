package yome.fgo.simulator.gui.creators;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.Formation;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.simulator.gui.components.CommandCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.CraftEssenceDataAnchorPane;
import yome.fgo.simulator.gui.components.MysticCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.ServantDataAnchorPane;
import yome.fgo.simulator.gui.helpers.LaunchUtils;

import java.io.IOException;
import java.util.List;

public class EntitySelector {

    public static Formation selectFormation(final Window window, final List<Formation> formationList) throws IOException {
        final Formation.Builder selection = Formation.newBuilder();
        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("formationViewer.fxml"));
        final Parent root = fxmlLoader.load();
        final FormationFXMLController controller = fxmlLoader.getController();
        controller.fillFormations(
                formationList,
                selection
        );

        LaunchUtils.launchBlocking("Formation Viewer", window, root, false);

        return selection.getName().isEmpty() ? null : selection.build();
    }

    public static ServantDataAnchorPane selectServant(final Window window) throws IOException {
        final ServantDataAnchorPane selection = new ServantDataAnchorPane();

        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("entityFilter.fxml"));
        final Parent root = fxmlLoader.load();
        final EntityFilterFXMLController controller = fxmlLoader.getController();
        controller.fillServants(selection);

        LaunchUtils.launchBlocking("ServantSelector", window, root, false);

        return selection.getServantData() == null ? null : selection;
    }

    public static CraftEssenceDataAnchorPane selectCraftEssence(final Window window) throws IOException {
        final CraftEssenceDataAnchorPane selection = new CraftEssenceDataAnchorPane();
        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("entityFilter.fxml"));
        final Parent root = fxmlLoader.load();
        final EntityFilterFXMLController controller = fxmlLoader.getController();
        controller.fillCraftEssence(selection);

        LaunchUtils.launchBlocking("Craft Essence Selector", window, root, false);

        return selection.getCraftEssenceData() == null ? null : selection;
    }

    public static MysticCodeDataAnchorPane selectMysticCode(final Window window, final Gender gender) throws IOException {
        final MysticCodeDataAnchorPane selection = new MysticCodeDataAnchorPane();
        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("entityFilter.fxml"));
        final Parent root = fxmlLoader.load();
        final EntityFilterFXMLController controller = fxmlLoader.getController();
        controller.fillMysticCode(selection, gender);

        LaunchUtils.launchBlocking("Mystic Code Selector", window, root, false);

        return selection.getMysticCodeData() == null ? null : selection;
    }

    public static CommandCodeDataAnchorPane selectCommandCode(final Window window) throws IOException {
        final CommandCodeDataAnchorPane selection = new CommandCodeDataAnchorPane();
        final FXMLLoader fxmlLoader = new FXMLLoader(BuffBuilder.class.getResource("entityFilter.fxml"));
        final Parent root = fxmlLoader.load();
        final EntityFilterFXMLController controller = fxmlLoader.getController();
        controller.fillCommandCode(selection);

        LaunchUtils.launchBlocking("Command Code Selector", window, root, false);

        return selection.getCommandCodeData() == null ? null : selection;
    }
}
