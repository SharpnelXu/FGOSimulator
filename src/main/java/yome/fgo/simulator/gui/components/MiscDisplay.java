package yome.fgo.simulator.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.gui.components.StatsLogger.LogLevel;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.mysticcodes.MysticCode;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.SpecialActivationTarget.NO_SPECIAL_TARGET;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class MiscDisplay extends VBox {
    private final SimulationWindow simulationWindow;
    private final ImageView mysticCodeImage;
    private final List<Button> skillButtons;
    private final List<ImageView> skillThumbnails;
    private final List<AnchorPane> skillCoolDownHides;
    private final Label critStarValueLabel;
    private final Label currentTurnCountLabel;
    private final Label currentStageCountLabel;
    private final Label enemyCountLabel;

    public MiscDisplay(final SimulationWindow simulationWindow) {
        super();

        this.simulationWindow = simulationWindow;

        HBox.setHgrow(this, Priority.ALWAYS);
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
            skillImgView.setFitWidth(60);
            skillImgView.setFitHeight(60);
            skillThumbnails.add(skillImgView);
            final AnchorPane skillImgAnchor = new AnchorPane();
            skillImgView.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 1");
            AnchorPane.setBottomAnchor(skillImgView, 0.0);
            AnchorPane.setTopAnchor(skillImgView, 0.0);
            AnchorPane.setLeftAnchor(skillImgView, 0.0);
            AnchorPane.setRightAnchor(skillImgView, 0.0);
            skillImgAnchor.getChildren().add(skillImgView);
            final int iI = i;
            skillButton.setOnAction(e -> activateSkill(iI));
            skillButton.setGraphic(skillImgView);

            final AnchorPane cdAnchor = new AnchorPane();
            cdAnchor.setStyle("-fx-background-color: rgba(0,0,0,0.78); -fx-border-radius: 3; -fx-border-width: 1");
            final Label cdLabel = new Label();
            cdLabel.setFont(new Font(30));
            cdLabel.setStyle("-fx-text-fill: white");
            cdLabel.setAlignment(Pos.CENTER);
            cdLabel.setWrapText(true);
            cdLabel.setMaxWidth(60);
            AnchorPane.setBottomAnchor(cdLabel, 0.0);
            AnchorPane.setTopAnchor(cdLabel, 0.0);
            AnchorPane.setLeftAnchor(cdLabel, 0.0);
            AnchorPane.setRightAnchor(cdLabel, 0.0);
            cdAnchor.getChildren().add(cdLabel);
            skillCoolDownHides.add(cdAnchor);

            stackPane.getChildren().addAll(skillButton, cdAnchor);
            skillHBoxes.getChildren().add(stackPane);
        }

        final Label starLabel = new Label(getTranslation(APPLICATION_SECTION, "critStar"));
        critStarValueLabel = new Label();
        final HBox enemyCountHBox = new HBox();
        enemyCountHBox.setSpacing(10);
        final Label enemyLabel = new Label(getTranslation(APPLICATION_SECTION, "Enemy remaining"));
        enemyCountLabel = new Label();
        enemyCountHBox.getChildren().addAll(starLabel, critStarValueLabel, enemyLabel, enemyCountLabel);

        final Label currentTurnLabel = new Label(getTranslation(APPLICATION_SECTION, "Current Turn"));
        currentTurnCountLabel = new Label();
        final HBox currentStageHBox = new HBox();
        currentStageHBox.setSpacing(10);
        final Label currentStageLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage"));
        currentStageCountLabel = new Label();
        currentStageHBox.getChildren().addAll(currentTurnLabel, currentTurnCountLabel, currentStageLabel, currentStageCountLabel);


        final Label probabilityLabel = new Label(getTranslation(APPLICATION_SECTION, "Probability Threshold (%)"));
        final Label probabilityValueLabel = new Label("100");
        final HBox probabilityLabelHBox = new HBox();
        probabilityLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        probabilityLabelHBox.setSpacing(10);
        probabilityLabelHBox.getChildren().addAll(probabilityLabel, probabilityValueLabel);
        final Slider probabilityThresholdSlider = new Slider();
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

        final Label randomLabel = new Label(getTranslation(APPLICATION_SECTION, "Random Value"));
        final Label randomValueLabel = new Label(String.format("%.3f", 0.9));
        final HBox randomLabelHBox = new HBox();
        randomLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        randomLabelHBox.setSpacing(10);
        randomLabelHBox.getChildren().addAll(randomLabel, randomValueLabel);
        final Slider randomSlider = new Slider();
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

        final Button executeButton = new Button(getTranslation(APPLICATION_SECTION, "Attack"));
        executeButton.setOnAction(e -> this.simulationWindow.executeCombatActions());

        final Button activateEffectButton = new Button();
        final Image commandSealImage = this.simulationWindow.getSkillImage("like");
        final ImageView activateEffectImgView = new ImageView(commandSealImage);
        activateEffectImgView.setFitHeight(40);
        activateEffectImgView.setFitWidth(40);
        activateEffectImgView.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        AnchorPane.setBottomAnchor(activateEffectImgView, 0.0);
        AnchorPane.setTopAnchor(activateEffectImgView, 0.0);
        AnchorPane.setLeftAnchor(activateEffectImgView, 0.0);
        AnchorPane.setRightAnchor(activateEffectImgView, 0.0);
        final AnchorPane commandSealAnchor = new AnchorPane();
        commandSealAnchor.getChildren().add(activateEffectImgView);

        activateEffectButton.setGraphic(commandSealAnchor);
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

        final Label logLevelChangeLabel = new Label(getTranslation(APPLICATION_SECTION, "Log Level"));
        final ChoiceBox<LogLevel> logLevelChoiceBox = new ChoiceBox<>();
        logLevelChoiceBox.getItems().addAll(LogLevel.DEBUG, LogLevel.EFFECT, LogLevel.ACTION);
        logLevelChoiceBox.getSelectionModel().selectFirst();
        logLevelChoiceBox.setOnAction(e -> this.simulationWindow.setLogLevel(logLevelChoiceBox.getValue()));
        final HBox logLevelHBox = new HBox();
        logLevelHBox.setSpacing(10);
        logLevelHBox.setAlignment(Pos.CENTER_LEFT);
        logLevelHBox.getChildren().addAll(logLevelChangeLabel, logLevelChoiceBox);

        getChildren().addAll(
                mysticCodeImage,
                skillHBoxes,
                enemyCountHBox,
                currentStageHBox,
                new Separator(),
                probabilityLabelHBox,
                probabilityThresholdSlider,
                randomLabelHBox,
                randomSlider,
                executeButton,
                activateEffectButton,
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
        mysticCodeImage.setImage(simulationWindow.getMysticCodeImage(mysticCode.getId(), mysticCode.gender));

        for (int i = 0; i < 3; i += 1) {
            skillThumbnails.get(i).setImage(simulationWindow.getSkillImage(mysticCode.getSkillIconName(i)));

            final boolean canActivate = simulation.canActivateMysticCodeSkill(i);
            skillButtons.get(i).setDisable(!canActivate);
            final AnchorPane skillHide = skillCoolDownHides.get(i);
            skillHide.setVisible(!canActivate);
            if (!canActivate) {
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
        enemyCountLabel.setText(Integer.toString(simulation.getBackupEnemies().size()));
    }

    private void activateSkill(final int skillIndex) {
        final SpecialActivationParams specialActivationParams =
                simulationWindow.getSimulation().getMysticCode().getActiveSkillSpecialTarget(skillIndex);
        if (specialActivationParams == null || specialActivationParams.getSpecialTarget() == NO_SPECIAL_TARGET) {
            simulationWindow.getSimulation().activateMysticCodeSkill(skillIndex);
            simulationWindow.render();
        } else {
            simulationWindow.showSpecialTargetSelectionWindow(specialActivationParams, -1, skillIndex);
        }
    }
}
