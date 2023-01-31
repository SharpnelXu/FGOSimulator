package yome.fgo.simulator.gui.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.gui.components.StatsLogger.LogLevel;
import yome.fgo.simulator.gui.creators.MysticCodeCreator;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.NpChange;
import yome.fgo.simulator.models.mysticcodes.MysticCode;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static yome.fgo.data.proto.FgoStorageData.Gender.MALE;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.BUFF_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.INFO_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SKILL_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createSkillCdAnchor;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.getFieldIcon;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class MiscDisplay extends VBox {
    private static final EffectData CHARGE_100_NP_FOR_ALL_ALLIES = EffectData.newBuilder()
            .setType(NpChange.class.getSimpleName())
            .setTarget(ALL_ALLIES)
            .addValues(1)
            .build();

    private final SimulationWindow simulationWindow;
    private final ImageView mysticCodeImage;
    private final List<Button> skillButtons;
    private final List<ImageView> skillThumbnails;
    private final List<AnchorPane> skillCoolDownHides;
    private final Label critStarValueLabel;
    private final Label currentTurnCountLabel;
    private final Label currentStageCountLabel;
    private final Label enemyCountLabel;
    private final HBox fieldTraitsHBox;
    private final Label fieldTraitsLabel;
    private final Slider probabilityThresholdSlider;
    private final Slider randomSlider;
    private final ImageView customEffectImage;

    public MiscDisplay(final SimulationWindow simulationWindow) {
        super();

        this.simulationWindow = simulationWindow;

        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);

        mysticCodeImage = new ImageView();
        mysticCodeImage.setFitWidth(100);
        mysticCodeImage.setFitHeight(100);

        final HBox skillHBoxes = new HBox();
        skillHBoxes.setSpacing(10);
        skillHBoxes.setAlignment(Pos.CENTER);

        skillButtons = new ArrayList<>();
        skillThumbnails = new ArrayList<>();
        skillCoolDownHides = new ArrayList<>();

        for (int i = 0; i < 3; i += 1) {
            final StackPane stackPane = new StackPane();

            final Button skillButton = new Button();
            skillButtons.add(skillButton);

            final ImageView skillImgView = new ImageView();
            skillImgView.setFitWidth(SKILL_THUMBNAIL_SIZE);
            skillImgView.setFitHeight(SKILL_THUMBNAIL_SIZE);
            skillThumbnails.add(skillImgView);
            final int iI = i;
            skillButton.setOnAction(e -> activateSkill(iI));
            skillButton.setGraphic(skillImgView);

            final AnchorPane cdAnchor = createSkillCdAnchor();
            skillCoolDownHides.add(cdAnchor);

            stackPane.getChildren().addAll(skillButton, cdAnchor);
            skillHBoxes.getChildren().add(stackPane);
        }

        final Button mysticCodeInfoButton = new Button();
        mysticCodeInfoButton.setOnAction(e -> {
            try {
                MysticCodeCreator.preview(getScene().getWindow(), this.simulationWindow.getSimulation().getMysticCode().mysticCodeData);
            } catch (IOException ignored) {
            }
        });
        mysticCodeInfoButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Details")));
        final ImageView infoImg = new ImageView(this.simulationWindow.getSimulationImage("info"));
        infoImg.setFitHeight(INFO_THUMBNAIL_SIZE);
        infoImg.setFitWidth(INFO_THUMBNAIL_SIZE);
        mysticCodeInfoButton.setGraphic(infoImg);

        final HBox mysticCodeHBox = new HBox();
        mysticCodeHBox.setSpacing(10);
        mysticCodeHBox.setAlignment(Pos.CENTER_LEFT);
        mysticCodeHBox.getChildren().addAll(skillHBoxes, mysticCodeImage, mysticCodeInfoButton);
        mysticCodeHBox.setFillHeight(false);

        final Label starLabel = new Label(getTranslation(APPLICATION_SECTION, "critStar:"));
        starLabel.setStyle("-fx-font-weight: bold");
        critStarValueLabel = new Label();
        final Label enemyLabel = new Label(getTranslation(APPLICATION_SECTION, "Enemy remaining:"));
        enemyLabel.setStyle("-fx-font-weight: bold");
        enemyCountLabel = new Label();
        final Label currentTurnLabel = new Label(getTranslation(APPLICATION_SECTION, "Current Turn:"));
        currentTurnLabel.setStyle("-fx-font-weight: bold");
        currentTurnCountLabel = new Label();
        final Label currentStageLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage:"));
        currentStageLabel.setStyle("-fx-font-weight: bold");
        currentStageCountLabel = new Label();

        final HBox generalInfoHBox = new HBox();
        generalInfoHBox.setSpacing(10);
        generalInfoHBox.getChildren()
                .addAll(
                        starLabel, critStarValueLabel,
                        currentStageLabel, currentStageCountLabel,
                        enemyLabel, enemyCountLabel,
                        currentTurnLabel, currentTurnCountLabel
                );

        fieldTraitsHBox = new HBox(5);
        fieldTraitsHBox.setAlignment(Pos.CENTER_LEFT);
        fieldTraitsLabel = new Label(getTranslation(APPLICATION_SECTION, "Field traits:"));
        fieldTraitsLabel.setStyle("-fx-font-weight: bold");
        fieldTraitsHBox.getChildren().add(fieldTraitsLabel);

        final Label probabilityLabel = new Label(getTranslation(APPLICATION_SECTION, "Probability Threshold (%):"));
        probabilityLabel.setStyle("-fx-font-weight: bold");
        final Label probabilityValueLabel = new Label("100");
        final HBox probabilityLabelHBox = new HBox();
        probabilityLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        probabilityLabelHBox.setSpacing(10);
        probabilityLabelHBox.getChildren().addAll(probabilityLabel, probabilityValueLabel);
        probabilityThresholdSlider = new Slider();
        probabilityThresholdSlider.setMin(0);
        probabilityThresholdSlider.setMax(10);
        probabilityThresholdSlider.setBlockIncrement(1);
        probabilityThresholdSlider.setMajorTickUnit(1);
        probabilityThresholdSlider.setMinorTickCount(0);
        probabilityThresholdSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final int intValue = newValue.intValue();
                    probabilityThresholdSlider.setValue(intValue);
                    probabilityValueLabel.setText(Integer.toString(intValue * 10));
                    this.simulationWindow.getSimulation()
                            .setProbabilityThreshold(
                                    RoundUtils.roundNearest(intValue / 10.0)
                            );
                }
        );
        probabilityThresholdSlider.setShowTickMarks(true);
        probabilityThresholdSlider.setValue(10);

        final Label randomLabel = new Label(getTranslation(APPLICATION_SECTION, "Random Value:"));
        randomLabel.setStyle("-fx-font-weight: bold");
        final Label randomValueLabel = new Label(String.format("%.3f", 0.9));
        final HBox randomLabelHBox = new HBox();
        randomLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        randomLabelHBox.setSpacing(10);
        randomLabelHBox.getChildren().addAll(randomLabel, randomValueLabel);
        randomSlider = new Slider();
        randomSlider.setMin(0.9);
        randomSlider.setMax(1.1);
        randomSlider.setBlockIncrement(0.001);
        randomSlider.setMajorTickUnit(0.1);
        randomSlider.setMinorTickCount(0);
        randomSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final double random = RoundUtils.roundNearest(newValue.doubleValue());
                    randomSlider.setValue(random);
                    randomValueLabel.setText(String.format("%.3f", random));
                    this.simulationWindow.getSimulation().setFixedRandom(random);
                }
        );
        randomSlider.setShowTickMarks(true);
        randomSlider.setValue(0.9);

        final HBox buttonsHBox = new HBox();
        buttonsHBox.setSpacing(10);
        buttonsHBox.setAlignment(Pos.CENTER);

        final Button executeButton = new Button();
        executeButton.setOnAction(e -> this.simulationWindow.executeCombatActions());
        executeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Attack")));
        final ImageView executeImg = new ImageView(this.simulationWindow.getSimulationImage("attack"));
        executeImg.setFitHeight(SKILL_THUMBNAIL_SIZE);
        executeImg.setFitWidth(SKILL_THUMBNAIL_SIZE);
        executeButton.setGraphic(executeImg);

        final Button chargeNpButton = new Button();
        chargeNpButton.setOnAction(e -> {
            this.simulationWindow.getSimulation().activateCustomEffect(CHARGE_100_NP_FOR_ALL_ALLIES);
            this.simulationWindow.render();
        });
        chargeNpButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Charge 100% NP for all allies")));
        final ImageView chargeNpImg = new ImageView(this.simulationWindow.getSimulationImage("npCharge"));
        chargeNpImg.setFitHeight(SKILL_THUMBNAIL_SIZE);
        chargeNpImg.setFitWidth(SKILL_THUMBNAIL_SIZE);
        chargeNpButton.setGraphic(chargeNpImg);

        final Button activateEffectButton = new Button();
        activateEffectButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Activate custom effect")));
        customEffectImage = new ImageView();
        customEffectImage.setFitHeight(SKILL_THUMBNAIL_SIZE);
        customEffectImage.setFitWidth(SKILL_THUMBNAIL_SIZE);
        activateEffectButton.setGraphic(customEffectImage);
        activateEffectButton.setOnAction(e -> {
            try {
                final EffectData.Builder builder = EffectData.newBuilder();
                createEffect(activateEffectButton.getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    this.simulationWindow.getSimulation().activateCustomEffect(builder.build());
                    this.simulationWindow.render();
                }
            } catch (final IOException ignored) {
            }
        });

        final Button revertActionButton = new Button();
        revertActionButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Revert")));
        final ImageView revertImg = new ImageView(this.simulationWindow.getSimulationImage("revert"));
        revertImg.setFitHeight(SKILL_THUMBNAIL_SIZE);
        revertImg.setFitWidth(SKILL_THUMBNAIL_SIZE);
        revertActionButton.setGraphic(revertImg);
        revertActionButton.setOnAction(e -> {
            this.simulationWindow.getSimulation().fromSnapshot();
            this.simulationWindow.render();
        });

        buttonsHBox.getChildren().addAll(executeButton, chargeNpButton, activateEffectButton, revertActionButton);

        final HBox logLevelHBox = new HBox();
        logLevelHBox.setSpacing(10);
        logLevelHBox.setAlignment(Pos.CENTER);
        final Label logLevelChangeLabel = new Label(getTranslation(APPLICATION_SECTION, "Log Level:"));
        logLevelChangeLabel.setStyle("-fx-font-weight: bold");
        logLevelHBox.getChildren().add(logLevelChangeLabel);
        final ToggleGroup logLevelToggleGroup = new ToggleGroup();
        for (final LogLevel logLevel : LogLevel.values()) {
            final RadioButton logRadio = new RadioButton(logLevel.name());
            logRadio.setToggleGroup(logLevelToggleGroup);
            logRadio.setOnAction(e -> this.simulationWindow.setLogLevel(logLevel));
            logLevelHBox.getChildren().add(logRadio);
        }

        getChildren().addAll(
                mysticCodeHBox,
                new Separator(),
                generalInfoHBox,
                fieldTraitsHBox,
                new Separator(),
                probabilityLabelHBox,
                probabilityThresholdSlider,
                randomLabelHBox,
                randomSlider,
                buttonsHBox,
                logLevelHBox
        );
    }

    public void renderMisc() {
        final Simulation simulation = simulationWindow.getSimulation();

        final MysticCode mysticCode = simulation.getMysticCode();

        if (mysticCode == null) {
            return;
        }

        setVisible(true);

        final String genderString = mysticCode.gender == MALE ? "male" : "female";
        mysticCodeImage.setImage(simulationWindow.getMysticCodeImage(mysticCode.getId(), genderString));
        customEffectImage.setImage(simulationWindow.getSimulationImage(String.format("commandSeal_%s", genderString)));

        for (int i = 0; i < 3; i += 1) {
            skillThumbnails.get(i).setImage(simulationWindow.getSkillImage(mysticCode.getSkillIconName(i)));

            final boolean cannotActivate = !simulation.canActivateMysticCodeSkill(i);
            skillButtons.get(i).setDisable(cannotActivate);
            final AnchorPane skillHide = skillCoolDownHides.get(i);
            skillHide.setVisible(cannotActivate);
            if (cannotActivate) {
                final Label label = (Label) skillHide.getChildren().get(0);
                final int currentCoolDown = mysticCode.getCurrentCoolDown(i);

                if (currentCoolDown == 0) {
                    label.setText(getTranslation(APPLICATION_SECTION, "Skill Inaccessible"));
                } else {
                    label.setText(Integer.toString(currentCoolDown));
                }
            }
        }

        critStarValueLabel.setText(String.format("%.2f", simulation.getCurrentStars()));
        currentTurnCountLabel.setText(Integer.toString(simulation.getCurrentTurn()));
        currentStageCountLabel.setText(String.format("%d/%d", simulation.getCurrentStage(), simulation.getLevel().getStages().size()));
        enemyCountLabel.setText(Integer.toString(simulation.getBackupEnemies().size() + simulation.getAliveEnemies().size()));

        final Set<String> fieldTraits = simulation.getStageTraits();
        final List<Node> fieldChildren = fieldTraitsHBox.getChildren();
        fieldChildren.clear();
        fieldChildren.add(fieldTraitsLabel);
        for (final String fieldTrait : fieldTraits) {
            final ImageView fieldImage = new ImageView();
            fieldImage.setFitHeight(BUFF_SIZE);
            fieldImage.setFitWidth(BUFF_SIZE);
            fieldImage.setImage(simulationWindow.getBuffImage(getFieldIcon(fieldTrait)));
            final Tooltip tooltip = new Tooltip(getTranslation(TRAIT_SECTION, fieldTrait));
            Tooltip.install(fieldImage, tooltip);
            fieldChildren.add(fieldImage);
        }

        probabilityThresholdSlider.setValue(simulation.getProbabilityThreshold() * 10);
        randomSlider.setValue(simulation.getFixedRandom());
    }


    private void activateSkill(final int skillIndex) {
        simulationWindow.getSimulation().activateMysticCodeSkill(skillIndex);
        simulationWindow.render();
    }
}
