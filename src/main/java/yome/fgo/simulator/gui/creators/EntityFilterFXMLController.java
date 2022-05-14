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
import javafx.util.StringConverter;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmType;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.simulator.gui.components.CraftEssenceDataWrapper;
import yome.fgo.simulator.gui.components.EnumConverter;
import yome.fgo.simulator.gui.components.MysticCodeDataWrapper;
import yome.fgo.simulator.gui.components.ServantDataWrapper;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class EntityFilterFXMLController implements Initializable {
    @FXML
    private FlowPane entityFlowPane;

    @FXML
    private HBox filterHBox;

    private ServantDataWrapper servantReturnWrapper;
    private CraftEssenceDataWrapper ceReturnWrapper;
    private MysticCodeDataWrapper mcReturnWrapper;
    private ChoiceBox<Gender> genderChoiceBox;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {}

    public void fillServants(final Map<Integer, ServantDataWrapper> dataMap, final ServantDataWrapper returnWrapper) {
        this.servantReturnWrapper = returnWrapper;

        for (final ServantDataWrapper servantDataWrapper : dataMap.values()) {
            final Button servantSelectButton = new Button();
            servantSelectButton.setGraphic(servantDataWrapper);
            servantSelectButton.setOnAction(e -> selectServant(servantDataWrapper));
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

        fateClassChoiceBox.setOnAction(e -> filterServant(fateClassChoiceBox.getValue(), npCardTypeChoiceBox.getValue(), npTypeChoiceBox.getValue()));
        npCardTypeChoiceBox.setOnAction(e -> filterServant(fateClassChoiceBox.getValue(), npCardTypeChoiceBox.getValue(), npTypeChoiceBox.getValue()));
        npTypeChoiceBox.setOnAction(e -> filterServant(fateClassChoiceBox.getValue(), npCardTypeChoiceBox.getValue(), npTypeChoiceBox.getValue()));
    }

    private void filterServant(final FateClass filterClass, final CommandCardType filterCardType, final NoblePhantasmType filterNpType) {
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

    private void selectServant(final ServantDataWrapper servantDataWrapper) {
        if (servantReturnWrapper != null) {
            servantReturnWrapper.setFrom(servantDataWrapper.getServantData(), servantDataWrapper.getAscensionImages());
        }
        final Stage stage = (Stage) filterHBox.getScene().getWindow();
        stage.close();
    }

    public void fillCraftEssence(
            final Map<Integer, CraftEssenceDataWrapper> dataMap,
            final CraftEssenceDataWrapper returnWrapper
    ) {
        this.ceReturnWrapper = returnWrapper;

        for (final CraftEssenceDataWrapper ceDataWrapper : dataMap.values()) {
            final Button servantSelectButton = new Button();
            servantSelectButton.setGraphic(ceDataWrapper);
            servantSelectButton.setOnAction(e -> selectCraftEssence(ceDataWrapper));
            entityFlowPane.getChildren().add(servantSelectButton);
        }

        final Label rarityLabel = new Label(getTranslation(APPLICATION_SECTION, "Rarity"));

        final ChoiceBox<Integer> rarityChoices = new ChoiceBox<>();
        rarityChoices.getItems().addAll(-1, 5, 4, 3, 2, 1);
        rarityChoices.getSelectionModel().selectFirst();
        rarityChoices.setConverter(new StringConverter<>() {
            @Override
            public String toString(final Integer object) {
                if (object > 0) {
                    return object.toString();
                } else {
                    return getTranslation(APPLICATION_SECTION, "Any Rarity");
                }
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });

        filterHBox.getChildren().addAll(rarityLabel, rarityChoices);

        rarityChoices.setOnAction(e -> filterCraftEssence(rarityChoices.getValue()));
    }

    private void filterCraftEssence(final int rarity) {
        for (final Node node : entityFlowPane.getChildren()) {
            final Button button = (Button) node;
            final CraftEssenceDataWrapper craftEssenceDataWrapper = (CraftEssenceDataWrapper) button.getGraphic();

            final boolean rarityMatch = craftEssenceDataWrapper.getCraftEssenceData().getRarity() == rarity;
            button.setVisible(rarityMatch);
            button.setManaged(rarityMatch);
        }
    }

    private void selectCraftEssence(final CraftEssenceDataWrapper craftEssenceDataWrapper) {
        if (ceReturnWrapper != null) {
            ceReturnWrapper.setFrom(craftEssenceDataWrapper.getCraftEssenceData(), craftEssenceDataWrapper.getImage());
        }
        final Stage stage = (Stage) filterHBox.getScene().getWindow();
        stage.close();
    }

    public void fillMysticCode(
            final Map<Integer, MysticCodeDataWrapper> mcDataMap,
            final MysticCodeDataWrapper returnWrapper
    ) {
        this.mcReturnWrapper = returnWrapper;

        for (final MysticCodeDataWrapper dataWrapper : mcDataMap.values()) {
            final Button servantSelectButton = new Button();
            servantSelectButton.setGraphic(dataWrapper);
            servantSelectButton.setOnAction(e -> selectMysticCode(dataWrapper));
            entityFlowPane.getChildren().add(servantSelectButton);
        }

        final Label genderLabel = new Label(getTranslation(APPLICATION_SECTION, "Gender"));

        genderChoiceBox = new ChoiceBox<>();
        genderChoiceBox.setConverter(new EnumConverter<>(TRAIT_SECTION));
        genderChoiceBox.getItems().addAll(Gender.MALE, Gender.FEMALE);
        genderChoiceBox.getSelectionModel().selectFirst();

        filterHBox.getChildren().addAll(genderLabel, genderChoiceBox);

        genderChoiceBox.setOnAction(e -> changeMysticCodeGender(genderChoiceBox.getValue()));
    }

    private void selectMysticCode(final MysticCodeDataWrapper dataWrapper) {
        if (mcReturnWrapper != null) {
            mcReturnWrapper.setFrom(dataWrapper.getMysticCodeData(), dataWrapper.getImages());
            final int imgIndex = genderChoiceBox.getValue() == Gender.MALE ? 0 : 1;
            mcReturnWrapper.getImageView().setImage(mcReturnWrapper.getImages().get(imgIndex));
        }
        final Stage stage = (Stage) filterHBox.getScene().getWindow();
        stage.close();
    }

    private void changeMysticCodeGender(final Gender gender) {
        final int imgIndex = gender == Gender.MALE ? 0 : 1;

        for (final Node node : entityFlowPane.getChildren()) {
            final Button button = (Button) node;
            final MysticCodeDataWrapper dataWrapper = (MysticCodeDataWrapper) button.getGraphic();
            dataWrapper.getImageView().setImage(dataWrapper.getImages().get(imgIndex));
        }
    }
}
