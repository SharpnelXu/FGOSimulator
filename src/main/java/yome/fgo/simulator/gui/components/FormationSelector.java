package yome.fgo.simulator.gui.components;

import com.google.common.collect.Lists;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillUpgrades;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.simulator.gui.creators.CraftEssenceCreator;
import yome.fgo.simulator.gui.creators.LevelCreatorFMXLController;
import yome.fgo.simulator.gui.creators.ServantCreator;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static yome.fgo.simulator.ResourceManager.getUnknownServantThumbnail;
import static yome.fgo.simulator.gui.components.DataPrinter.printServantOption;
import static yome.fgo.simulator.gui.creators.EntitySelector.selectCraftEssence;
import static yome.fgo.simulator.gui.creators.EntitySelector.selectServant;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class FormationSelector extends VBox {
    private final RadioButton support;
    private ServantDataWrapper selectedServant;
    private ServantOption servantOption;
    private final Label servantOptionLabel;
    private final ServantDataWrapper defaultServantSelection;

    private CraftEssenceDataWrapper selectedCE;
    private final Slider ceLevelSlider;
    private final CheckBox ceLimitBreakCheck;
    private final CraftEssenceDataWrapper defaultCESelection;

    public FormationSelector(
            final ToggleGroup toggleGroup,
            final Label errorLabel,
            final Map<Integer, ServantDataWrapper> servantDataMap,
            final Map<Integer, ServantOption> servantOptions,
            final Map<Integer, CraftEssenceDataWrapper> ceDataMap,
            final Map<Integer, CraftEssenceOption> ceOptions,
            final LevelCreatorFMXLController controller
    ) {
        super();

        setSpacing(10);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(this, Priority.ALWAYS);

        support = new RadioButton(getTranslation(APPLICATION_SECTION, "In Formation"));
        support.setToggleGroup(toggleGroup);
        support.setOnAction(e -> {
            if (support.isSelected()) {
                support.setText(getTranslation(APPLICATION_SECTION, "Support"));
            } else {
                support.setText(getTranslation(APPLICATION_SECTION, "In Formation"));
            }
            controller.calculateCost();
        });

        final Button servantSelectButton = new Button();
        Image unknown = null;
        try {
            unknown = new Image(new FileInputStream(getUnknownServantThumbnail()));
        } catch (final FileNotFoundException ignored) {
        }
        defaultServantSelection = new ServantDataWrapper(null, Lists.newArrayList(unknown));
        selectedServant = defaultServantSelection;
        final Label servantNameLabel = new Label(getTranslation(APPLICATION_SECTION, "No servant selected"));
        servantNameLabel.setWrapText(true);
        servantNameLabel.setAlignment(Pos.CENTER);
        servantOptionLabel = new Label();
        servantOptionLabel.setWrapText(true);

        final Button editServantOptionButton = new Button(getTranslation(APPLICATION_SECTION, "Edit Servant Option"));
        editServantOptionButton.setDisable(true);
        editServantOptionButton.setOnAction(e -> editServantOption());

        final Button viewServantButton = new Button(getTranslation(APPLICATION_SECTION, "Details"));
        viewServantButton.setDisable(true);
        viewServantButton.setOnAction(e -> {
            try {
                ServantCreator.preview(this.getScene().getWindow(), selectedServant.getServantData());
            } catch (IOException ignored) {
            }
        });

        final Button removeServantButton = new Button(getTranslation(APPLICATION_SECTION, "Remove selection"));
        removeServantButton.setDisable(true);
        removeServantButton.setOnAction(e -> {
            selectedServant = defaultServantSelection;
            servantSelectButton.setGraphic(selectedServant);
            servantOption = null;
            servantNameLabel.setText(getTranslation(APPLICATION_SECTION, "No servant selected"));
            servantNameLabel.setMaxWidth(getWidth());
            servantOptionLabel.setText(null);
            servantOptionLabel.setMaxWidth(getWidth());
            editServantOptionButton.setDisable(true);
            viewServantButton.setDisable(true);
            removeServantButton.setDisable(true);
            controller.calculateCost();
        });

        servantSelectButton.setGraphic(selectedServant);
        servantSelectButton.setOnAction(e -> {
            try {
                final ServantDataWrapper selection = selectServant(this.getScene().getWindow(), servantDataMap);
                if (selection != null) {
                    selectedServant = selection;
                    servantSelectButton.setGraphic(selectedServant);
                    final int servantNo = selectedServant.getServantData().getServantNum();
                    servantOption = servantOptions.containsKey(servantNo) ?
                            servantOptions.get(servantNo) :
                            createDefaultServantOption(selectedServant.getServantData());
                    selectedServant.getImageView().setImage(selectedServant.getAscensionImages().get(servantOption.getAscension() - 1));
                    servantNameLabel.setText(
                            getTranslation(
                                    ENTITY_NAME_SECTION,
                                    selectedServant.getServantData()
                                            .getServantAscensionData(0)
                                            .getCombatantData()
                                            .getId()
                            )
                    );
                    servantOptionLabel.setText(printServantOption(servantOption));
                    servantNameLabel.setMaxWidth(getWidth());
                    servantOptionLabel.setMaxWidth(getWidth());
                    editServantOptionButton.setDisable(false);
                    viewServantButton.setDisable(false);
                    removeServantButton.setDisable(false);
                    controller.calculateCost();
                }
            } catch (final IOException ex) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
            }
        });

        final Button ceSelectButton = new Button();
        defaultCESelection = new CraftEssenceDataWrapper(null, unknown);
        selectedCE = defaultCESelection;
        final Label ceNameLabel = new Label(getTranslation(APPLICATION_SECTION, "No CE selected"));
        ceNameLabel.setWrapText(true);
        ceNameLabel.setAlignment(Pos.CENTER);

        final Label ceLevelLabel = new Label(getTranslation(APPLICATION_SECTION, "CE Level"));
        final Label ceLevelValueLabel = new Label("0");
        final HBox ceLevelLabelHBox = new HBox();
        ceLevelLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        ceLevelLabelHBox.setSpacing(10);
        ceLevelLabelHBox.getChildren().addAll(ceLevelLabel, ceLevelValueLabel);
        ceLevelSlider = new Slider();
        ceLevelSlider.setMin(0);
        ceLevelSlider.setBlockIncrement(1);
        ceLevelSlider.setMajorTickUnit(20);
        ceLevelSlider.setMinorTickCount(0);
        ceLevelSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    int intValue = newValue.intValue();
                    if (intValue < 1) {
                        intValue = 1;
                    }
                    ceLevelSlider.setValue(intValue);
                    ceLevelValueLabel.setText(Integer.toString(intValue));
                }
        );
        ceLevelSlider.setShowTickMarks(true);

        ceLimitBreakCheck = new CheckBox(getTranslation(APPLICATION_SECTION, "Limit Break"));
        ceLimitBreakCheck.setSelected(true);

        ceLevelLabelHBox.setDisable(true);
        ceLevelSlider.setDisable(true);
        ceLimitBreakCheck.setDisable(true);

        final Button viewCEButton = new Button(getTranslation(APPLICATION_SECTION, "Details"));
        viewCEButton.setDisable(true);
        viewCEButton.setOnAction(e -> {
            try {
                CraftEssenceCreator.preview(this.getScene().getWindow(), selectedCE.getCraftEssenceData());
            } catch (IOException ignored) {
            }
        });

        final Button removeCEButton = new Button(getTranslation(APPLICATION_SECTION, "Remove selection"));
        removeCEButton.setDisable(true);
        removeCEButton.setOnAction(e -> {
            selectedCE = defaultCESelection;
            ceSelectButton.setGraphic(selectedCE);
            ceNameLabel.setText(getTranslation(APPLICATION_SECTION, "No CE selected"));
            ceNameLabel.setMaxWidth(getWidth());
            ceLevelLabelHBox.setDisable(true);
            ceLevelSlider.setDisable(true);
            ceLimitBreakCheck.setDisable(true);
            viewCEButton.setDisable(true);
            removeCEButton.setDisable(true);
            controller.calculateCost();
        });

        ceSelectButton.setGraphic(selectedCE);
        ceSelectButton.setOnAction(e -> {
            try {
                final CraftEssenceDataWrapper selection = selectCraftEssence(this.getScene().getWindow(), ceDataMap);
                if (selection != null) {
                    selectedCE = selection;
                    ceSelectButton.setGraphic(selectedCE);
                    ceNameLabel.setText(getTranslation(ENTITY_NAME_SECTION, selectedCE.getCraftEssenceData().getId()));
                    ceNameLabel.setMaxWidth(getWidth());
                    final int maxLevel = defaultCEMaxLevel(selectedCE.getCraftEssenceData().getRarity());
                    ceLevelSlider.setMax(maxLevel);

                    final int ceNo = selectedCE.getCraftEssenceData().getCeNum();
                    final int ceLevel;
                    final boolean isLimitBreak;
                    if (ceOptions.containsKey(ceNo)) {
                        final CraftEssenceOption ceOption = ceOptions.get(ceNo);
                        ceLevel = ceOption.getCraftEssenceLevel();
                        isLimitBreak = ceOption.getIsLimitBreak();
                    } else {
                        ceLevel = maxLevel;
                        isLimitBreak = true;
                    }
                    ceLevelSlider.setValue(ceLevel);
                    ceLimitBreakCheck.setSelected(isLimitBreak);
                    ceLevelLabelHBox.setDisable(false);
                    ceLevelSlider.setDisable(false);
                    ceLimitBreakCheck.setDisable(false);
                    viewCEButton.setDisable(false);
                    removeCEButton.setDisable(false);
                    controller.calculateCost();
                }
            } catch (final IOException ex) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
            }
        });

        getChildren().addAll(
                support,
                servantSelectButton,
                servantNameLabel,
                removeServantButton,
                viewServantButton,
                editServantOptionButton,
                ceSelectButton,
                ceNameLabel,
                removeCEButton,
                viewCEButton,
                ceLevelLabelHBox,
                ceLevelSlider,
                ceLimitBreakCheck,
                servantOptionLabel
        );
    }

    private static ServantOption createDefaultServantOption(final ServantData servantData) {
        final ServantAscensionData servantAscensionData = servantData.getServantAscensionData(0);
        final int rarity = servantAscensionData.getCombatantData().getRarity();

        final ServantOption.Builder builder = ServantOption.newBuilder()
                .setAscension(1)
                .setServantLevel(defaultServantLevel(rarity))
                .setAttackStatusUp(1000)
                .setHealthStatusUp(1000)
                .setNoblePhantasmRank(servantAscensionData.getNoblePhantasmUpgrades().getNoblePhantasmDataCount())
                .setNoblePhantasmLevel(5);

        for (final ActiveSkillUpgrades activeSkillUpgrades : servantAscensionData.getActiveSkillUpgradesList()) {
            builder.addActiveSkillRanks(activeSkillUpgrades.getActiveSkillDataCount());
            builder.addActiveSkillLevels(10);
        }
        for (int i = 0; i < servantAscensionData.getAppendSkillDataCount(); i += 1) {
            builder.addAppendSkillLevels(0);
        }
        builder.setBond(10);

        return builder.build();
    }

    private static int defaultServantLevel(final int rarity) {
        return switch (rarity) {
            case 5 -> 90;
            case 4 -> 80;
            case 3 -> 70;
            case 2, 0 -> 65;
            case 1 -> 60;
            default -> 1;
        };
    }

    private void editServantOption() {
        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(this.getScene().getWindow());

        final ServantOption.Builder builder = servantOption.toBuilder();
        final ServantOptionEditor editor = new ServantOptionEditor(
                new ServantDataWrapper(selectedServant.getServantData(), selectedServant.getAscensionImages()),
                builder
        );
        final Parent root = editor.getRoot();
        final Scene scene = new Scene(root);

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "ServantOptionEditor"));
        newStage.setScene(scene);

        newStage.showAndWait();

        if (builder.getAscension() != -1) {
            servantOption = builder.build();
            servantOptionLabel.setText(printServantOption(servantOption));
            servantOptionLabel.setMaxWidth(getWidth());
            selectedServant.getImageView().setImage(selectedServant.getAscensionImages().get(servantOption.getAscension() - 1));
        }
    }

    private static int defaultCEMaxLevel(final int rarity) {
        return switch (rarity) {
            case 5 -> 100;
            case 4 -> 80;
            case 3 -> 60;
            case 2 -> 55;
            case 1 -> 50;
            default -> 1;
        };
    }

    public int getCost() {
        if (support.isSelected()) {
            return 0;
        }
        int servantCost = 0;
        if (selectedServant.getServantData() != null) {
            servantCost = selectedServant.getServantData().getServantAscensionData(servantOption.getAscension() - 1).getCost();
        }
        int ceCost = 0;
        if (selectedCE.getCraftEssenceData() != null) {
            ceCost = selectedCE.getCraftEssenceData().getCost();
        }
        return ceCost + servantCost;
    }

    public ServantData getServantData() {
        return selectedServant.getServantData();
    }

    public ServantOption getServantOption() {
        return servantOption;
    }

    public CraftEssenceData getCraftEssenceData() {
        return selectedCE.getCraftEssenceData();
    }

    public CraftEssenceOption getCraftEssenceOption() {
        return CraftEssenceOption.newBuilder()
                .setCraftEssenceLevel((int) ceLevelSlider.getValue())
                .setIsLimitBreak(ceLimitBreakCheck.isSelected())
                .build();
    }
}
