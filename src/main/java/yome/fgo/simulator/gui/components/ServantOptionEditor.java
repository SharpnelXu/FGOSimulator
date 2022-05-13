package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ServantOptionEditor {
    private final Parent root;
    private final ServantOption.Builder parentOption;
    private final ChoiceBox<Integer> ascChoices;
    private final ChoiceBox<Integer> bondChoices;
    private final Slider attackStatusSlider;
    private final Slider hpStatusSlider;
    private final Slider servantLevelSlider;
    private final ChoiceBox<Integer> npRanks;
    private final Slider npLevelSlider;
    private final List<ChoiceBox<Integer>> activeSkillRanks;
    private final List<Slider> activeSkillLevels;
    private final List<Slider> appendSkillLevels;
    private final List<CommandCardOptionBox> commandCardOptionBoxes;

    public Parent getRoot() {
        return root;
    }

    public ServantOptionEditor(final ServantDataWrapper selectedServant, final ServantOption.Builder source) {
        final VBox rootVBox = new VBox();
        rootVBox.setSpacing(10);
        rootVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        rootVBox.setPadding(new Insets(10, 10, 10, 10));
        this.root = rootVBox;
        this.parentOption = source;

        final HBox topHBox = new HBox();
        topHBox.setSpacing(10);
        topHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        topHBox.setFillHeight(false);

        final VBox topRightVBox = new VBox();
        topRightVBox.setSpacing(10);
        topRightVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        final HBox basicData = new HBox();
        basicData.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        basicData.setSpacing(10);
        basicData.setAlignment(Pos.CENTER_LEFT);
        final Label servantNameLabel = new Label(
                getTranslation(
                        ENTITY_NAME_SECTION,
                        selectedServant.getServantData()
                                .getServantAscensionData(source.getAscension() - 1)
                                .getCombatantData()
                                .getId()
                )
        );
        final Label servantAscLabel = new Label(getTranslation(APPLICATION_SECTION, "Servant Asc"));
        ascChoices = new ChoiceBox<>();
        for (int i = 1; i <= selectedServant.getServantData().getServantAscensionDataCount(); i += 1) {
            ascChoices.getItems().add(i);
        }

        final Label bondLabel = new Label(getTranslation(APPLICATION_SECTION, "Bond"));
        bondChoices = new ChoiceBox<>();
        bondChoices.getItems().addAll(15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
        bondChoices.getSelectionModel().select(Integer.valueOf(source.getBond()));

        basicData.getChildren().addAll(servantNameLabel, servantAscLabel, ascChoices, bondLabel, bondChoices);

        final HBox buttonHBox = new HBox();
        buttonHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        buttonHBox.setAlignment(Pos.CENTER_RIGHT);
        buttonHBox.setSpacing(10);
        final Button cancelButton = new Button(getTranslation(APPLICATION_SECTION, "Cancel"));
        cancelButton.setMinWidth(100);
        cancelButton.setOnAction(e -> cancel());
        final Button buildButton = new Button(getTranslation(APPLICATION_SECTION, "Build"));
        buildButton.setMinWidth(100);
        buildButton.setOnAction(e -> build());
        buttonHBox.getChildren().addAll(cancelButton, buildButton);

        topRightVBox.getChildren().addAll(basicData, buttonHBox);
        topHBox.getChildren().addAll(selectedServant, topRightVBox);
        rootVBox.getChildren().addAll(topHBox, new Separator());

        final ScrollPane optionsScrolls = new ScrollPane();
        optionsScrolls.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        optionsScrolls.setFitToWidth(true);
        optionsScrolls.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        final VBox scrollsVBox = new VBox();
        scrollsVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        scrollsVBox.setSpacing(10);
        optionsScrolls.setContent(scrollsVBox);
        rootVBox.getChildren().add(scrollsVBox);

        final Label attackStatusLabel = new Label(getTranslation(APPLICATION_SECTION, "Attack Status Up"));
        final Label attackStatusValueLabel = new Label(Integer.toString(source.getAttackStatusUp()));
        attackStatusValueLabel.setMinWidth(50);
        attackStatusSlider = new Slider();
        attackStatusSlider.setMin(0);
        attackStatusSlider.setMax(200);
        attackStatusSlider.setBlockIncrement(1);
        attackStatusSlider.setMajorTickUnit(20);
        attackStatusSlider.setMinorTickCount(0);
        attackStatusSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    int intValue = newValue.intValue();
                    if (intValue > 100 && intValue % 2 == 1) {
                        intValue += 1;
                    }
                    attackStatusSlider.setValue(intValue);
                    attackStatusValueLabel.setText(Integer.toString(intValue * 10));
                }
        );
        attackStatusSlider.setShowTickMarks(true);
        attackStatusSlider.setValue(source.getAttackStatusUp() / 10);
        HBox.setHgrow(attackStatusSlider, Priority.ALWAYS);
        final HBox attackStatusHBox = new HBox();
        attackStatusHBox.setSpacing(10);
        attackStatusHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        attackStatusHBox.setAlignment(Pos.CENTER_LEFT);
        attackStatusHBox.getChildren().addAll(attackStatusLabel, attackStatusValueLabel, attackStatusSlider);

        final Label hpStatusLabel = new Label(getTranslation(APPLICATION_SECTION, "Attack Status Up"));
        final Label hpStatusValueLabel = new Label(Integer.toString(source.getHealthStatusUp()));
        hpStatusValueLabel.setMinWidth(50);
        hpStatusSlider = new Slider();
        hpStatusSlider.setMin(0);
        hpStatusSlider.setMax(200);
        hpStatusSlider.setBlockIncrement(1);
        hpStatusSlider.setMajorTickUnit(20);
        hpStatusSlider.setMinorTickCount(0);
        hpStatusSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    int intValue = newValue.intValue();
                    if (intValue > 100 && intValue % 2 == 1) {
                        intValue += 1;
                    }
                    hpStatusSlider.setValue(intValue);
                    hpStatusValueLabel.setText(Integer.toString(intValue * 10));
                }
        );
        hpStatusSlider.setValue(source.getHealthStatusUp() / 10);
        hpStatusSlider.setShowTickMarks(true);
        HBox.setHgrow(hpStatusSlider, Priority.ALWAYS);
        final HBox hpStatusHBox = new HBox();
        hpStatusHBox.setSpacing(10);
        hpStatusHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        hpStatusHBox.setAlignment(Pos.CENTER_LEFT);
        hpStatusHBox.getChildren().addAll(hpStatusLabel, hpStatusValueLabel, hpStatusSlider);

        scrollsVBox.getChildren().addAll(attackStatusHBox, hpStatusHBox);

        final Label servantLevelLabel = new Label(getTranslation(APPLICATION_SECTION, "Servant Level"));
        final Label servantLevelValueLabel = new Label(Integer.toString(source.getHealthStatusUp()));
        servantLevelValueLabel.setMinWidth(50);
        servantLevelSlider = new Slider();
        servantLevelSlider.setMin(0);
        servantLevelSlider.setBlockIncrement(1);
        servantLevelSlider.setMajorTickUnit(10);
        servantLevelSlider.setMinorTickCount(0);
        servantLevelSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    int intValue = newValue.intValue();
                    if (intValue < 1) {
                        intValue = 1;
                    }
                    servantLevelSlider.setValue(intValue);
                    servantLevelValueLabel.setText(Integer.toString(intValue));
                }
        );
        servantLevelSlider.setShowTickMarks(true);
        HBox.setHgrow(servantLevelSlider, Priority.ALWAYS);
        final HBox servantLevelHBox = new HBox();
        servantLevelHBox.setSpacing(10);
        servantLevelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        servantLevelHBox.setAlignment(Pos.CENTER_LEFT);
        servantLevelHBox.getChildren().addAll(servantLevelLabel, servantLevelValueLabel, servantLevelSlider);

        scrollsVBox.getChildren().add(servantLevelHBox);

        final Label npRankLabel = new Label(getTranslation(APPLICATION_SECTION, "NP Rank"));
        npRanks = new ChoiceBox<>();
        final HBox npRankHBox = new HBox();
        npRankHBox.setSpacing(10);
        npRankHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        npRankHBox.setAlignment(Pos.CENTER_LEFT);
        npRankHBox.getChildren().addAll(npRankLabel, npRanks);

        final Label npLevelLabel = new Label(getTranslation(APPLICATION_SECTION, "NP Level"));
        final Label npLevelValueLabel = new Label(Integer.toString(source.getNoblePhantasmLevel()));
        npLevelValueLabel.setMinWidth(50);
        npLevelSlider = new Slider();
        npLevelSlider.setMin(1);
        npLevelSlider.setMax(5);
        npLevelSlider.setBlockIncrement(1);
        npLevelSlider.setMajorTickUnit(1);
        npLevelSlider.setMinorTickCount(0);
        npLevelSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final int intValue = newValue.intValue();
                    npLevelSlider.setValue(intValue);
                    npLevelValueLabel.setText(Integer.toString(intValue));

                }
        );
        npLevelSlider.setValue(source.getNoblePhantasmLevel());
        npLevelSlider.setShowTickMarks(true);
        HBox.setHgrow(npLevelSlider, Priority.ALWAYS);
        final HBox npLevelHBox = new HBox();
        npLevelHBox.setSpacing(10);
        npLevelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        npLevelHBox.setAlignment(Pos.CENTER_LEFT);
        npLevelHBox.getChildren().addAll(npLevelLabel, npLevelValueLabel, npLevelSlider);

        scrollsVBox.getChildren().addAll(npRankHBox, npLevelHBox);

        activeSkillRanks = new ArrayList<>();
        activeSkillLevels = new ArrayList<>();
        for (int i = 0; i < 3; i += 1) {
            final Label activeSkillRankLabel =
                    new Label(String.format(getTranslation(APPLICATION_SECTION, "Active Skill %d Rank"), i + 1));
            final ChoiceBox<Integer> activeSkillRankChoices = new ChoiceBox<>();
            activeSkillRanks.add(activeSkillRankChoices);
            final HBox activeSkillRankHBox = new HBox();
            activeSkillRankHBox.setSpacing(10);
            activeSkillRankHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
            activeSkillRankHBox.setAlignment(Pos.CENTER_LEFT);
            activeSkillRankHBox.getChildren().addAll(activeSkillRankLabel, activeSkillRankChoices);

            final Label activeSkillLevelLabel =
                    new Label(String.format(getTranslation(APPLICATION_SECTION, "Active Skill %d Level"), i + 1));
            final Label activeSkillLevelValueLabel = new Label(Integer.toString(source.getActiveSkillLevels(i)));
            activeSkillLevelValueLabel.setMinWidth(50);
            final Slider activeSkillLevelSlider = new Slider();
            activeSkillLevelSlider.setMin(1);
            activeSkillLevelSlider.setMax(10);
            activeSkillLevelSlider.setBlockIncrement(1);
            activeSkillLevelSlider.setMajorTickUnit(1);
            activeSkillLevelSlider.setMinorTickCount(0);
            activeSkillLevelSlider.valueProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        final int intValue = newValue.intValue();
                        activeSkillLevelSlider.setValue(intValue);
                        activeSkillLevelValueLabel.setText(Integer.toString(intValue));
                    }
            );
            activeSkillLevelSlider.setValue(source.getActiveSkillLevels(i));
            activeSkillLevelSlider.setShowTickMarks(true);
            HBox.setHgrow(activeSkillLevelSlider, Priority.ALWAYS);
            activeSkillLevels.add(activeSkillLevelSlider);
            final HBox activeSkillLevelHBox = new HBox();
            activeSkillLevelHBox.setSpacing(10);
            activeSkillLevelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
            activeSkillLevelHBox.setAlignment(Pos.CENTER_LEFT);
            activeSkillLevelHBox.getChildren().addAll(activeSkillLevelLabel, activeSkillLevelValueLabel, activeSkillLevelSlider);

            scrollsVBox.getChildren().addAll(activeSkillRankHBox, activeSkillLevelHBox);
        }

        appendSkillLevels = new ArrayList<>();

        for (int i = 0; i < 3; i += 1) {
            final Label appendSkillLevelLabel =
                    new Label(String.format(getTranslation(APPLICATION_SECTION, "Append Skill %d Level"), i + 1));
            final Label appendSkillLevelValueLabel = new Label(Integer.toString(source.getAppendSkillLevels(i)));
            appendSkillLevelValueLabel.setMinWidth(50);
            final Slider appendSkillLevelSlider = new Slider();
            appendSkillLevelSlider.setMin(0);
            appendSkillLevelSlider.setMax(10);
            appendSkillLevelSlider.setBlockIncrement(1);
            appendSkillLevelSlider.setMajorTickUnit(1);
            appendSkillLevelSlider.setMinorTickCount(0);
            appendSkillLevelSlider.valueProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        final int intValue = newValue.intValue();
                        appendSkillLevelSlider.setValue(intValue);
                        appendSkillLevelValueLabel.setText(Integer.toString(intValue));
                    }
            );
            appendSkillLevelSlider.setValue(source.getAppendSkillLevels(i));
            appendSkillLevelSlider.setShowTickMarks(true);
            HBox.setHgrow(appendSkillLevelSlider, Priority.ALWAYS);
            appendSkillLevels.add(appendSkillLevelSlider);
            final HBox appendSkillLevelHBox = new HBox();
            appendSkillLevelHBox.setSpacing(10);
            appendSkillLevelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
            appendSkillLevelHBox.setAlignment(Pos.CENTER_LEFT);
            appendSkillLevelHBox.getChildren().addAll(appendSkillLevelLabel, appendSkillLevelValueLabel, appendSkillLevelSlider);

            scrollsVBox.getChildren().add(appendSkillLevelHBox);
        }

        commandCardOptionBoxes = new ArrayList<>();
        for (int i = 0; i < 5; i += 1) {
            final CommandCardOptionBox commandCardOptionBox = source.getCommandCardOptionsCount() > i ?
                    new CommandCardOptionBox(source.getCommandCardOptions(i)) :
                    new CommandCardOptionBox();
            commandCardOptionBoxes.add(commandCardOptionBox);
            scrollsVBox.getChildren().add(commandCardOptionBox);
        }

        ascChoices.setOnAction(e -> {
            selectedServant.getImageView().setImage(selectedServant.getAscensionImages().get(ascChoices.getValue() - 1));
            final ServantAscensionData servantAscensionData =
                    selectedServant.getServantData().getServantAscensionData(ascChoices.getValue() - 1);

            final int maxLevel = servantAscensionData.getServantStatusDataCount();
            servantLevelSlider.setMax(maxLevel);
            if (maxLevel == 1) {
                servantLevelSlider.setDisable(true);
            }
            if (maxLevel < servantLevelSlider.getValue()) {
                servantLevelSlider.setValue(maxLevel);
            }

            final List<Integer> npRankItems = npRanks.getItems();
            npRankItems.clear();
            for (int i = 1; i <= servantAscensionData.getNoblePhantasmUpgrades().getNoblePhantasmDataCount(); i += 1) {
                npRankItems.add(i);
            }
            npRanks.getSelectionModel().selectLast();
            for (int i = 0; i < 3; i += 1) {
                final List<Integer> activeSkillRankItems = activeSkillRanks.get(i).getItems();
                activeSkillRankItems.clear();
                for (int j = 1; j <= servantAscensionData.getActiveSkillUpgrades(i).getActiveSkillDataCount(); j += 1) {
                    activeSkillRankItems.add(j);
                }
                activeSkillRanks.get(i).getSelectionModel().selectLast();
            }
            for (int i = 0; i < 5; i += 1) {
                commandCardOptionBoxes.get(i).setType(servantAscensionData.getCommandCardData(i).getCommandCardType());
            }
        });
        ascChoices.getSelectionModel().select(Integer.valueOf(source.getAscension()));
        servantLevelSlider.setValue(source.getServantLevel());
        npRanks.getSelectionModel().select(Integer.valueOf(source.getNoblePhantasmRank()));
        for (int i = 0; i < 3; i += 1) {
            activeSkillRanks.get(i).getSelectionModel().select(Integer.valueOf(source.getActiveSkillRanks(i)));
        }
    }

    private void cancel() {
        parentOption.setAscension(-1);

        final Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void build() {
        parentOption.clear()
                .setAscension(ascChoices.getValue())
                .setAttackStatusUp((int) attackStatusSlider.getValue() * 10)
                .setHealthStatusUp((int) hpStatusSlider.getValue() * 10)
                .setBond(bondChoices.getValue())
                .setServantLevel((int) servantLevelSlider.getValue())
                .setNoblePhantasmRank(npRanks.getValue())
                .setNoblePhantasmLevel((int) npLevelSlider.getValue())
                .addAllActiveSkillRanks(activeSkillRanks.stream().map(ChoiceBox::getValue).collect(Collectors.toList()))
                .addAllActiveSkillLevels(activeSkillLevels.stream().map(slider -> (int) slider.getValue()).collect(Collectors.toList()))
                .addAllAppendSkillLevels(appendSkillLevels.stream().map(slider -> (int) slider.getValue()).collect(Collectors.toList()))
                .addAllCommandCardOptions(commandCardOptionBoxes.stream().map(CommandCardOptionBox::build).collect(Collectors.toList()));

        final Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }
}
