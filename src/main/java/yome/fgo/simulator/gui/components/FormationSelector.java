package yome.fgo.simulator.gui.components;

import com.google.common.collect.Lists;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillUpgrades;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.data.proto.FgoStorageData.CraftEssencePreference;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.data.proto.FgoStorageData.ServantPreference;
import yome.fgo.simulator.gui.creators.CraftEssenceCreator;
import yome.fgo.simulator.gui.creators.LevelCreatorFMXLController;
import yome.fgo.simulator.gui.creators.ServantCreator;
import yome.fgo.simulator.gui.helpers.LaunchUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static yome.fgo.simulator.ResourceManager.CRAFT_ESSENCE_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.SERVANT_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.getUnknownServantThumbnail;
import static yome.fgo.simulator.gui.creators.EntitySelector.selectCraftEssence;
import static yome.fgo.simulator.gui.creators.EntitySelector.selectServant;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createTooltip;
import static yome.fgo.simulator.gui.helpers.DataPrinter.printServantOption;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class FormationSelector extends VBox {
    private final CheckBox support;
    private ServantDataAnchorPane selectedServant;
    private ServantOption servantOption;
    private final Label servantOptionLabel;
    private final ServantDataAnchorPane defaultServantSelection;

    private CraftEssenceDataAnchorPane selectedCE;
    private final Slider ceLevelSlider;
    private final CheckBox ceLimitBreakCheck;
    private final CraftEssenceDataAnchorPane defaultCESelection;
    private final LevelCreatorFMXLController controller;

    private final Button servantSelectButton;
    private final Label servantNameLabel;
    private final Button editServantOptionButton;
    private final Button viewServantButton;
    private final Button removeServantButton;
    private final Button ceSelectButton;
    private final Label ceNameLabel;
    private final HBox ceLevelLabelHBox;
    private final Button viewCEButton;
    private final Button removeCEButton;


    public FormationSelector(
            final Label errorLabel,
            final Map<Integer, ServantOption> servantOptions,
            final Map<Integer, CraftEssenceOption> ceOptions,
            final LevelCreatorFMXLController controller
    ) {
        super(10);

        this.controller = controller;

        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(this, Priority.ALWAYS);

        support = new CheckBox(getTranslation(APPLICATION_SECTION, "In Formation"));
        support.setOnAction(e -> {
            if (support.isSelected()) {
                support.setText(getTranslation(APPLICATION_SECTION, "Support"));
            } else {
                support.setText(getTranslation(APPLICATION_SECTION, "In Formation"));
            }
            controller.calculateCost();
        });

        servantSelectButton = new Button();
        Image unknown = null;
        try {
            unknown = new Image(new FileInputStream(getUnknownServantThumbnail()));
        } catch (final FileNotFoundException ignored) {
        }
        defaultServantSelection = new ServantDataAnchorPane(null, Lists.newArrayList(unknown));
        selectedServant = defaultServantSelection;
        servantNameLabel = new Label(getTranslation(APPLICATION_SECTION, "No servant selected"));
        servantNameLabel.setWrapText(true);
        servantNameLabel.setAlignment(Pos.CENTER);
        servantOptionLabel = new Label();
        servantOptionLabel.setWrapText(true);

        viewServantButton = new Button();
        viewServantButton.setGraphic(createInfoImageView("info2"));
        viewServantButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Details")));
        viewServantButton.setDisable(true);
        viewServantButton.setOnAction(e -> {
            try {
                ServantCreator.preview(this.getScene().getWindow(), selectedServant.getServantData());
            } catch (final IOException ignored) {
            }
        });

        editServantOptionButton = new Button();
        editServantOptionButton.setGraphic(createInfoImageView("edit"));
        editServantOptionButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Edit Servant Option")));
        editServantOptionButton.setDisable(true);
        editServantOptionButton.setOnAction(e -> editServantOption());

        removeServantButton = new Button();
        removeServantButton.setGraphic(createInfoImageView("remove"));
        removeServantButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Remove selection")));
        removeServantButton.setDisable(true);
        removeServantButton.setOnAction(e -> removeServant());

        servantSelectButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Select servant")));
        servantSelectButton.setGraphic(selectedServant);
        servantSelectButton.setOnAction(e -> {
            try {
                final ServantDataAnchorPane selection = selectServant(this.getScene().getWindow());
                if (selection != null) {
                    final int servantNo = selection.getServantData().getServantNum();
                    servantOption = servantOptions.containsKey(servantNo) ?
                            servantOptions.get(servantNo) :
                            createDefaultServantOption(selection.getServantData());
                    setServant(selection, servantOption);
                }
            } catch (final IOException ex) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
            }
        });

        ceSelectButton = new Button();
        defaultCESelection = new CraftEssenceDataAnchorPane(null, unknown);
        selectedCE = defaultCESelection;
        ceNameLabel = new Label(getTranslation(APPLICATION_SECTION, "No CE selected"));
        ceNameLabel.setWrapText(true);
        ceNameLabel.setAlignment(Pos.CENTER);

        final Label ceLevelLabel = new Label(getTranslation(APPLICATION_SECTION, "CE Level"));
        final Label ceLevelValueLabel = new Label("0");
        ceLevelLabelHBox = new HBox(10);
        ceLevelLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
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

        viewCEButton = new Button();
        viewCEButton.setGraphic(createInfoImageView("info2"));
        viewCEButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Details")));
        viewCEButton.setDisable(true);
        viewCEButton.setOnAction(e -> {
            try {
                CraftEssenceCreator.preview(this.getScene().getWindow(), selectedCE.getCraftEssenceData());
            } catch (IOException ignored) {
            }
        });

        removeCEButton = new Button();
        removeCEButton.setGraphic(createInfoImageView("remove"));
        removeCEButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Remove selection")));
        removeCEButton.setDisable(true);
        removeCEButton.setOnAction(e -> removeCraftEssence());

        ceSelectButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Select craft essence")));
        ceSelectButton.setGraphic(selectedCE);
        ceSelectButton.setOnAction(e -> {
            try {
                final CraftEssenceDataAnchorPane selection = selectCraftEssence(this.getScene().getWindow());
                if (selection != null) {
                    final int maxLevel = Math.min(
                            defaultCEMaxLevel(selection.getCraftEssenceData().getRarity()),
                            selection.getCraftEssenceData().getStatusDataCount()
                    );
                    final int ceNo = selection.getCraftEssenceData().getCeNum();
                    final CraftEssenceOption ceOption = ceOptions.containsKey(ceNo) ?
                            ceOptions.get(ceNo) :
                            CraftEssenceOption.newBuilder()
                                    .setCraftEssenceLevel(maxLevel)
                                    .setIsLimitBreak(true)
                                    .build();
                    setCraftEssence(selection, ceOption, maxLevel);
                }
            } catch (final IOException ex) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
            }
        });

        final HBox servantButtonHBox = new HBox(5);
        servantButtonHBox.setAlignment(Pos.CENTER);
        servantButtonHBox.getChildren().addAll(
                viewServantButton, editServantOptionButton, removeServantButton
        );

        final HBox ceButtonHBox = new HBox(5);
        ceButtonHBox.setAlignment(Pos.CENTER);
        ceButtonHBox.getChildren().addAll(viewCEButton, removeCEButton);

        getChildren().addAll(
                support,
                servantSelectButton,
                servantNameLabel,
                servantButtonHBox,
                ceSelectButton,
                ceNameLabel,
                ceButtonHBox,
                ceLevelLabelHBox,
                ceLevelSlider,
                ceLimitBreakCheck,
                servantOptionLabel
        );
    }

    private static ServantOption createDefaultServantOption(final ServantData servantData) {
        final ServantAscensionData servantAscensionData = servantData.getServantAscensionData(0);
        final int rarity = servantAscensionData.getCombatantData().getRarity();
        final int servantLevel = Math.min(servantAscensionData.getServantStatusDataCount(), defaultServantLevel(rarity));

        final ServantOption.Builder builder = ServantOption.newBuilder()
                .setAscension(1)
                .setServantLevel(servantLevel)
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

    public static int defaultServantLevel(final int rarity) {
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
        final ServantOption.Builder builder = servantOption.toBuilder();
        final ServantOptionEditor editor = new ServantOptionEditor(
                new ServantDataAnchorPane(selectedServant.getServantData(), selectedServant.getAscensionImages()),
                builder
        );

        LaunchUtils.launchBlocking("BuffBuilder", this.getScene().getWindow(), editor.getRoot(), false);

        if (builder.getAscension() != -1) {
            servantOption = builder.build();
            servantOptionLabel.setText(printServantOption(servantOption));
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

    public void setFromPreferences(
            final ServantPreference servantPreference,
            final CraftEssencePreference craftEssencePreference
    ) {
        final int servantNo = servantPreference.getServantNo();
        if (SERVANT_DATA_ANCHOR_MAP.containsKey(servantNo)) {
            final ServantDataAnchorPane reference = SERVANT_DATA_ANCHOR_MAP.get(servantNo);
            final ServantDataAnchorPane servantSelection = new ServantDataAnchorPane(reference.getServantData(), reference.getAscensionImages());
            servantOption = servantPreference.getOption();
            servantSelection.getImageView().setImage(servantSelection.getAscensionImages().get(servantOption.getAscension() - 1));
            setServant(servantSelection, servantOption);
        } else {
            removeServant();
        }

        final int ceNo = craftEssencePreference.getCraftEssenceNo();
        if (CRAFT_ESSENCE_DATA_ANCHOR_MAP.containsKey(ceNo)) {
            final CraftEssenceDataAnchorPane reference = CRAFT_ESSENCE_DATA_ANCHOR_MAP.get(ceNo);
            final CraftEssenceDataAnchorPane ceSelection = new CraftEssenceDataAnchorPane(reference.getCraftEssenceData(), reference.getImage());
            final CraftEssenceOption ceOption = craftEssencePreference.getOption();
            final int maxLevel = Math.min(
                    defaultCEMaxLevel(ceSelection.getCraftEssenceData().getRarity()),
                    ceSelection.getCraftEssenceData().getStatusDataCount()
            );
            setCraftEssence(ceSelection, ceOption, maxLevel);
        } else {
            removeCraftEssence();
        }
    }

    public void setServant(
            final ServantDataAnchorPane selection,
            final ServantOption servantOption
    ) {
        selectedServant = selection;
        servantSelectButton.setGraphic(selectedServant);
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
        editServantOptionButton.setDisable(false);
        viewServantButton.setDisable(false);
        removeServantButton.setDisable(false);
        controller.calculateCost();
    }

    public void removeServant() {
        selectedServant = defaultServantSelection;
        servantSelectButton.setGraphic(selectedServant);
        servantOption = null;
        servantNameLabel.setText(getTranslation(APPLICATION_SECTION, "No servant selected"));
        servantOptionLabel.setText(null);
        editServantOptionButton.setDisable(true);
        viewServantButton.setDisable(true);
        removeServantButton.setDisable(true);
        controller.calculateCost();
    }

    public void setCraftEssence(
            final CraftEssenceDataAnchorPane selection,
            final CraftEssenceOption option,
            final int maxLevel
    ) {
        selectedCE = selection;
        ceSelectButton.setGraphic(selectedCE);
        ceNameLabel.setText(getTranslation(ENTITY_NAME_SECTION, selectedCE.getCraftEssenceData().getId()));
        ceLevelSlider.setMax(maxLevel);
        ceLevelSlider.setValue(option.getCraftEssenceLevel());
        if (maxLevel == 1) {
            ceLevelSlider.setDisable(true);
        }
        ceLimitBreakCheck.setSelected(option.getIsLimitBreak());
        ceLevelLabelHBox.setDisable(false);
        ceLevelSlider.setDisable(false);
        ceLimitBreakCheck.setDisable(false);
        viewCEButton.setDisable(false);
        removeCEButton.setDisable(false);
        controller.calculateCost();
    }

    public void removeCraftEssence() {
        selectedCE = defaultCESelection;
        ceSelectButton.setGraphic(selectedCE);
        ceNameLabel.setText(getTranslation(APPLICATION_SECTION, "No CE selected"));
        ceLevelLabelHBox.setDisable(true);
        ceLevelSlider.setDisable(true);
        ceLimitBreakCheck.setDisable(true);
        viewCEButton.setDisable(true);
        removeCEButton.setDisable(true);
        controller.calculateCost();
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
