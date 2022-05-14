package yome.fgo.simulator.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import yome.fgo.simulator.gui.components.StatsLogger.LogLevel;
import yome.fgo.simulator.gui.creators.MysticCodeCreator;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.mysticcodes.MysticCode;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class MiscDisplay extends VBox {
    private final SimulationWindow simulationWindow;
    private final ImageView mysticCodeImage;
    private final List<Button> skillButtons;
    private final List<ImageView> skillThumbnails;
    private final List<AnchorPane> skillCoolDownHides;

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

        final Button viewMCButton = new Button(getTranslation(APPLICATION_SECTION, "Details"));
        viewMCButton.setOnAction(e -> {
            try {
                MysticCodeCreator.preview(
                        this.getScene().getWindow(),
                        this.simulationWindow.getSimulation().getMysticCode().mysticCodeData
                );
            } catch (IOException ignored) {
            }
        });

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
                viewMCButton,
                skillHBoxes,
                new Separator(),
                probabilityLabelHBox,
                probabilityThresholdSlider,
                randomLabelHBox,
                randomSlider,
                logLevelHBox
        );
    }

    public void renderMysticCode() {
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
    }

    private void activateSkill(final int skillIndex) {
        simulationWindow.getSimulation().activateMysticCodeSkill(skillIndex);
        simulationWindow.render();
    }
}
