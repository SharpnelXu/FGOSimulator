package yome.fgo.simulator.gui.creators;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmType;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.simulator.gui.components.CommandCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.CraftEssenceDataAnchorPane;
import yome.fgo.simulator.gui.components.EnumConverter;
import yome.fgo.simulator.gui.components.MysticCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.ServantDataAnchorPane;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static yome.fgo.simulator.ResourceManager.COMMAND_CODE_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.CRAFT_ESSENCE_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.MYSTIC_CODE_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.SERVANT_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createClassImageView;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FateClassUtils.ALL_CLASSES;

public class EntityFilterFXMLController implements Initializable {
    @FXML
    private FlowPane entityFlowPane;

    @FXML
    private HBox filterHBox;

    private ServantDataAnchorPane servantReturnWrapper;
    private CraftEssenceDataAnchorPane ceReturnWrapper;
    private MysticCodeDataAnchorPane mcReturnWrapper;
    private CommandCodeDataAnchorPane ccReturnWrapper;
    private ChoiceBox<Gender> genderChoiceBox;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {}

    public void fillServants(final ServantDataAnchorPane returnWrapper) {
        this.servantReturnWrapper = returnWrapper;

        for (final ServantDataAnchorPane servantDataAnchorPane : SERVANT_DATA_ANCHOR_MAP.values()) {
            final Button servantSelectButton = new Button();
            servantSelectButton.setGraphic(servantDataAnchorPane);
            servantSelectButton.setOnAction(e -> selectServant(servantDataAnchorPane));
            entityFlowPane.getChildren().add(servantSelectButton);
        }

        final Label classLabel = new Label(getTranslation(APPLICATION_SECTION, "Class"));
        final Label npCardTypeLabel = new Label(getTranslation(APPLICATION_SECTION, "NP Card Type"));
        final Label npTypeLabel = new Label(getTranslation(APPLICATION_SECTION, "NP Type"));

        final Map<CheckBox, FateClass> allowedClasses = new HashMap<>();
        final CheckBox allClasses = new CheckBox();
        allowedClasses.put(allClasses, FateClass.ANY_CLASS);

        final HBox classFilterHBox = new HBox(5);
        classFilterHBox.setAlignment(Pos.CENTER_LEFT);

        final VBox classFilterRowVBox = new VBox(5);
        classFilterHBox.getChildren().addAll(classLabel, classFilterRowVBox);

        final HBox classFilterRow1 = new HBox(5);
        classFilterRow1.setAlignment(Pos.CENTER_LEFT);
        final HBox classFilterRow2 = new HBox(5);
        classFilterRow2.setAlignment(Pos.CENTER_LEFT);
        classFilterRowVBox.getChildren().addAll(classFilterRow1, classFilterRow2);

        classFilterRow1.getChildren().add(allClasses);

        for (int i = 0; i < 14; i += 1) {
            final FateClass fateClass = ALL_CLASSES[i];
            final CheckBox classCheck = new CheckBox();
            allowedClasses.put(classCheck, fateClass);
            if (i < 7) {
                classFilterRow1.getChildren().add(classCheck);
            } else {
                classFilterRow2.getChildren().add(classCheck);
            }
        }

        final CheckBox beast = new CheckBox();
        allowedClasses.put(beast, FateClass.ANY_BEAST);
        classFilterRow2.getChildren().add(beast);

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

        final HBox otherFilterHBox = new HBox(10);
        otherFilterHBox.setAlignment(Pos.CENTER_LEFT);
        otherFilterHBox.getChildren().addAll(npCardTypeLabel, npCardTypeChoiceBox, npTypeLabel, npTypeChoiceBox);

        final VBox wrapperVBox = new VBox(10);
        wrapperVBox.getChildren().addAll(classFilterHBox, otherFilterHBox);

        filterHBox.getChildren().add(wrapperVBox);

        for (final CheckBox checkBox : allowedClasses.keySet()) {
            final FateClass fateClass = allowedClasses.get(checkBox);
            checkBox.setGraphic(createClassImageView(fateClass));
            checkBox.setTooltip(new Tooltip(getTranslation(CLASS_SECTION, fateClass.name())));
            checkBox.setText(" ");

            checkBox.setOnAction(e -> filterServant(
                    getSelectedClasses(allowedClasses, allClasses, beast),
                    npCardTypeChoiceBox.getValue(),
                    npTypeChoiceBox.getValue()
            ));
        }
        npCardTypeChoiceBox.setOnAction(e -> filterServant(
                getSelectedClasses(allowedClasses, allClasses, beast),
                npCardTypeChoiceBox.getValue(),
                npTypeChoiceBox.getValue()
        ));
        npTypeChoiceBox.setOnAction(e -> filterServant(
                getSelectedClasses(allowedClasses, allClasses, beast),
                npCardTypeChoiceBox.getValue(),
                npTypeChoiceBox.getValue()
        ));
    }

    private Set<FateClass> getSelectedClasses(
            final Map<CheckBox, FateClass> allowedClasses,
            final CheckBox allClasses,
            final CheckBox beast
    ) {
        final Set<FateClass> selectedClasses = new HashSet<>();
        if (allClasses.isSelected()) {
            selectedClasses.add(FateClass.ANY_CLASS);
        } else {
            if (beast.isSelected()) {
                selectedClasses.add(FateClass.BEAST_I);
                selectedClasses.add(FateClass.BEAST_II);
                selectedClasses.add(FateClass.BEAST_III_R);
                selectedClasses.add(FateClass.BEAST_III_L);
                selectedClasses.add(FateClass.BEAST_IV);
            }
            for (final CheckBox other : allowedClasses.keySet()) {
                if (other.isSelected()) {
                    selectedClasses.add(allowedClasses.get(other));
                }
            }
        }
        return selectedClasses;
    }

    private void filterServant(final Set<FateClass> selectedClasses, final CommandCardType filterCardType, final NoblePhantasmType filterNpType) {
        final boolean selectAllClass = selectedClasses.contains(FateClass.ANY_CLASS) || selectedClasses.isEmpty();
        for (final Node node : entityFlowPane.getChildren()) {
            final Button button = (Button) node;
            final ServantDataAnchorPane servantDataAnchorPane = (ServantDataAnchorPane) button.getGraphic();

            boolean classMatch = false;
            boolean npMatch = false;

            for (final ServantAscensionData servantAscensionData :
                    servantDataAnchorPane.getServantData().getServantAscensionDataList()) {

                classMatch = selectAllClass || selectedClasses.contains(servantAscensionData.getCombatantData().getFateClass());

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

    private void selectServant(final ServantDataAnchorPane servantDataAnchorPane) {
        if (servantReturnWrapper != null) {
            servantReturnWrapper.setFrom(servantDataAnchorPane.getServantData(), servantDataAnchorPane.getAscensionImages());
        }
        final Stage stage = (Stage) filterHBox.getScene().getWindow();
        stage.close();
    }

    public void fillCraftEssence(final CraftEssenceDataAnchorPane returnWrapper) {
        this.ceReturnWrapper = returnWrapper;

        for (final CraftEssenceDataAnchorPane ceDataWrapper : CRAFT_ESSENCE_DATA_ANCHOR_MAP.values()) {
            final Button selectButton = new Button();
            selectButton.setGraphic(ceDataWrapper);
            selectButton.setOnAction(e -> selectCraftEssence(ceDataWrapper));
            entityFlowPane.getChildren().add(selectButton);
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
            final CraftEssenceDataAnchorPane craftEssenceDataAnchorPane = (CraftEssenceDataAnchorPane) button.getGraphic();

            final boolean rarityMatch = rarity == -1 || craftEssenceDataAnchorPane.getCraftEssenceData().getRarity() == rarity;
            button.setVisible(rarityMatch);
            button.setManaged(rarityMatch);
        }
    }

    private void selectCraftEssence(final CraftEssenceDataAnchorPane craftEssenceDataAnchorPane) {
        if (ceReturnWrapper != null) {
            ceReturnWrapper.setFrom(craftEssenceDataAnchorPane.getCraftEssenceData(), craftEssenceDataAnchorPane.getImage());
        }
        final Stage stage = (Stage) filterHBox.getScene().getWindow();
        stage.close();
    }

    public void fillMysticCode(
            final MysticCodeDataAnchorPane returnWrapper,
            final Gender gender
    ) {
        this.mcReturnWrapper = returnWrapper;

        for (final MysticCodeDataAnchorPane dataWrapper : MYSTIC_CODE_DATA_ANCHOR_MAP.values()) {
            final Button selectButton = new Button();
            dataWrapper.setFromGender(gender);
            selectButton.setGraphic(dataWrapper);
            selectButton.setOnAction(e -> selectMysticCode(dataWrapper));
            entityFlowPane.getChildren().add(selectButton);
        }

        final Label genderLabel = new Label(getTranslation(APPLICATION_SECTION, "Gender"));

        genderChoiceBox = new ChoiceBox<>();
        genderChoiceBox.setConverter(new EnumConverter<>(TRAIT_SECTION));
        genderChoiceBox.getItems().addAll(Gender.MALE, Gender.FEMALE);
        genderChoiceBox.getSelectionModel().select(gender);

        filterHBox.getChildren().addAll(genderLabel, genderChoiceBox);

        genderChoiceBox.setOnAction(e -> changeMysticCodeGender(genderChoiceBox.getValue()));
    }

    private void selectMysticCode(final MysticCodeDataAnchorPane dataWrapper) {
        if (mcReturnWrapper != null) {
            mcReturnWrapper.setFrom(dataWrapper.getMysticCodeData(), dataWrapper.getImages(), genderChoiceBox.getValue());
        }
        final Stage stage = (Stage) filterHBox.getScene().getWindow();
        stage.close();
    }

    private void changeMysticCodeGender(final Gender gender) {
        final int imgIndex = gender == Gender.MALE ? 0 : 1;

        for (final Node node : entityFlowPane.getChildren()) {
            final Button button = (Button) node;
            final MysticCodeDataAnchorPane dataWrapper = (MysticCodeDataAnchorPane) button.getGraphic();
            dataWrapper.getImageView().setImage(dataWrapper.getImages().get(imgIndex));
        }
    }

    public void fillCommandCode(final CommandCodeDataAnchorPane returnWrapper) {
        this.ccReturnWrapper = returnWrapper;

        for (final CommandCodeDataAnchorPane ccDataWrapper : COMMAND_CODE_DATA_ANCHOR_MAP.values()) {
            final Button selectButton = new Button();
            selectButton.setGraphic(ccDataWrapper);
            selectButton.setOnAction(e -> selectCommandCode(ccDataWrapper));
            entityFlowPane.getChildren().add(selectButton);
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

        rarityChoices.setOnAction(e -> filterCommandCode(rarityChoices.getValue()));
    }

    private void filterCommandCode(final int rarity) {
        for (final Node node : entityFlowPane.getChildren()) {
            final Button button = (Button) node;
            final CommandCodeDataAnchorPane commandCodeDataAnchorPane = (CommandCodeDataAnchorPane) button.getGraphic();

            final boolean rarityMatch = rarity == -1 || commandCodeDataAnchorPane.getCommandCodeData().getRarity() == rarity;
            button.setVisible(rarityMatch);
            button.setManaged(rarityMatch);
        }
    }

    private void selectCommandCode(final CommandCodeDataAnchorPane commandCodeDataAnchorPane) {
        if (ccReturnWrapper != null) {
            ccReturnWrapper.setFrom(commandCodeDataAnchorPane.getCommandCodeData(), commandCodeDataAnchorPane.getImage());
        }
        final Stage stage = (Stage) filterHBox.getScene().getWindow();
        stage.close();
    }
}
