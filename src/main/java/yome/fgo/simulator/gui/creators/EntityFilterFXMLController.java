package yome.fgo.simulator.gui.creators;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmType;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.simulator.gui.components.EnumConverter;
import yome.fgo.simulator.gui.components.ServantDataWrapper;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class EntityFilterFXMLController implements Initializable {
    @FXML
    private FlowPane entityFlowPane;

    @FXML
    private HBox filterHBox;

    private ServantDataWrapper returnWrapper;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {}

    public void fillServants(final Map<Integer, ServantDataWrapper> dataMap, final ServantDataWrapper returnWrapper) {
        this.returnWrapper = returnWrapper;

        for (final ServantDataWrapper servantDataWrapper : dataMap.values()) {
            final Button servantSelectButton = new Button();
            servantSelectButton.setGraphic(servantDataWrapper);
            servantSelectButton.setOnAction(e -> select(servantDataWrapper));
            entityFlowPane.getChildren().add(servantSelectButton);
        }

        final Label classLabel = new Label(getTranslation(APPLICATION_SECTION, "Class"));
        final Label npCardTypeLabel = new Label(getTranslation(APPLICATION_SECTION, "NP Card Type"));
        final Label npTypeLabel = new Label(getTranslation(APPLICATION_SECTION, "NP Type"));

        final ChoiceBox<FateClass> fateClassChoiceBox = new ChoiceBox<>();
        fateClassChoiceBox.setConverter(new EnumConverter<>(CLASS_SECTION));
        final List<FateClass> fateClasses = new ArrayList<>(List.of(FateClass.values()));
        fateClasses.remove(FateClass.UNRECOGNIZED);
        fateClasses.remove(FateClass.NO_CLASS);
        fateClassChoiceBox.setItems(FXCollections.observableArrayList(fateClasses));
        fateClassChoiceBox.getSelectionModel().select(FateClass.ANY_CLASS);

        final ChoiceBox<CommandCardType> npCardTypeChoiceBox = new ChoiceBox<>();
        npCardTypeChoiceBox.setConverter(new EnumConverter<>(COMMAND_CARD_TYPE_SECTION));
        npCardTypeChoiceBox.setItems(FXCollections.observableArrayList(
                CommandCardType.ANY,
                CommandCardType.QUICK,
                CommandCardType.ARTS,
                CommandCardType.BUSTER
        ));
        npCardTypeChoiceBox.getSelectionModel().select(CommandCardType.ANY);

        final ChoiceBox<NoblePhantasmType> npTypeChoiceBox = new ChoiceBox<>();
        npTypeChoiceBox.setConverter(new EnumConverter<>(COMMAND_CARD_TYPE_SECTION));
        npTypeChoiceBox.setItems(FXCollections.observableArrayList(
                NoblePhantasmType.ANY_NP_TYPE,
                NoblePhantasmType.SINGLE_TARGET_NP,
                NoblePhantasmType.ALL_TARGETS_NP,
                NoblePhantasmType.NON_DAMAGE
        ));
        npTypeChoiceBox.getSelectionModel().select(NoblePhantasmType.ANY_NP_TYPE);

        filterHBox.getChildren().addAll(classLabel, fateClassChoiceBox, npCardTypeLabel, npCardTypeChoiceBox, npTypeLabel, npTypeChoiceBox);

        fateClassChoiceBox.setOnAction(e -> filter(fateClassChoiceBox.getValue(), npCardTypeChoiceBox.getValue(), npTypeChoiceBox.getValue()));
        npCardTypeChoiceBox.setOnAction(e -> filter(fateClassChoiceBox.getValue(), npCardTypeChoiceBox.getValue(), npTypeChoiceBox.getValue()));
        npTypeChoiceBox.setOnAction(e -> filter(fateClassChoiceBox.getValue(), npCardTypeChoiceBox.getValue(), npTypeChoiceBox.getValue()));
    }

    private void filter(final FateClass filterClass, final CommandCardType filterCardType, final NoblePhantasmType filterNpType) {
        for (final Node node : entityFlowPane.getChildren()) {
            final Button button = (Button) node;
            final ServantDataWrapper servantDataWrapper = (ServantDataWrapper) button.getGraphic();

            boolean classMatch = false;
            boolean npMatch = false;

            for (final ServantAscensionData servantAscensionData :
                    servantDataWrapper.getServantData().getServantAscensionDataList()) {

                classMatch = filterClass == FateClass.ANY_CLASS ||
                        servantAscensionData.getCombatantData().getFateClass() == filterClass;

                for (final NoblePhantasmData noblePhantasmData :
                        servantAscensionData.getNoblePhantasmUpgrades().getNoblePhantasmDataList()) {

                    final boolean cardTypeMatch = filterCardType == CommandCardType.ANY ||
                            noblePhantasmData.getCommandCardData().getCommandCardType() == filterCardType;
                    final boolean npTypeMatch = filterNpType == NoblePhantasmType.ANY_NP_TYPE ||
                            noblePhantasmData.getNoblePhantasmType() == filterNpType;
                    if (cardTypeMatch && npTypeMatch) {
                        npMatch = true;
                        break;
                    }
                }

                if (classMatch && npMatch) {
                    break;
                }
            }

            button.setVisible(classMatch && npMatch);
            button.setManaged(classMatch && npMatch);
        }
    }

    private void select(final ServantDataWrapper servantDataWrapper) {
        if (returnWrapper != null) {
            returnWrapper.setFrom(servantDataWrapper.getServantData(), servantDataWrapper.getAscensionImages());
        }
        final Stage stage = (Stage) filterHBox.getScene().getWindow();
        stage.close();
    }
}
