package yome.fgo.simulator.gui.creators;

import com.google.protobuf.util.JsonFormat;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.gui.components.CraftEssenceDataWrapper;
import yome.fgo.simulator.gui.components.EffectsCellFactory;
import yome.fgo.simulator.gui.components.FormationSelector;
import yome.fgo.simulator.gui.components.MysticCodeDataWrapper;
import yome.fgo.simulator.gui.components.ServantDataWrapper;
import yome.fgo.simulator.gui.components.StageNode;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.gui.creators.EntitySelector.selectMysticCode;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.LEVEL_DIRECTORY_PATH;

public class LevelCreatorFMXLController implements Initializable {

    @FXML
    private Button addLevelEffectsButton;

    @FXML
    private Button addStageButton;

    @FXML
    private Label idLabel;

    @FXML
    private TextField idText;

    @FXML
    private Label levelEffectsLabel;

    @FXML
    private ListView<EffectData> levelEffectsList;

    @FXML
    private Button loadLevelButton;

    @FXML
    private Button removeStageButton;

    @FXML
    private Button saveLevelButton;

    @FXML
    private VBox stagesVBox;

    @FXML
    private Label errorLabel;

    @FXML
    private HBox simulationPrepHBox;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Level Name"));

        stagesVBox.getChildren().add(new StageNode(1));
        addStageButton.setText(getTranslation(APPLICATION_SECTION, "Add Stage"));
        addStageButton.setOnAction(e -> stagesVBox.getChildren().add(new StageNode(stagesVBox.getChildren().size() + 1)));
        removeStageButton.setText(getTranslation(APPLICATION_SECTION, "Remove Stage"));
        removeStageButton.setOnAction(e -> {
            if (stagesVBox.getChildren().size() > 1) {
                stagesVBox.getChildren().remove(stagesVBox.getChildren().size() - 1);
            }
        });
        levelEffectsLabel.setText(getTranslation(APPLICATION_SECTION, "Level Effects"));
        levelEffectsList.setCellFactory(new EffectsCellFactory(errorLabel));
        addLevelEffectsButton.setText(getTranslation(APPLICATION_SECTION, "Add Effect"));
        addLevelEffectsButton.setOnAction(e -> {
            try {
                final EffectData.Builder builder = EffectData.newBuilder();
                createEffect(addLevelEffectsButton.getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    levelEffectsList.getItems().add(builder.build());
                }
            } catch (final IOException exception) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + exception);
                errorLabel.setVisible(true);
            }
        });

        loadLevelButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        loadLevelButton.setOnAction(e -> loadLevel());
        saveLevelButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
        saveLevelButton.setOnAction(e -> saveLevel());

        errorLabel.setVisible(false);

        simulationPrepHBox.setVisible(false);
        simulationPrepHBox.setManaged(false);
    }

    private void loadLevel() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(LEVEL_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load Level Data"));
        final File levelDataFile = fileChooser.showOpenDialog(null);
        if (levelDataFile == null) {
            return;
        }

        final JsonFormat.Parser parser = JsonFormat.parser();
        final LevelData.Builder levelDataBuilder = LevelData.newBuilder();
        try {
            parser.merge(new FileReader(levelDataFile), levelDataBuilder);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + " " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        idText.setText(levelDataBuilder.getId());

        stagesVBox.getChildren().clear();
        for (int i = 1; i <= levelDataBuilder.getStageDataCount(); i += 1) {
            final StageData stageData = levelDataBuilder.getStageData(i - 1);
            final StageNode stageNode = new StageNode(i);
            stageNode.loadStageData(stageData);
            stagesVBox.getChildren().add(stageNode);
        }

        levelEffectsList.getItems().clear();
        levelEffectsList.getItems().addAll(levelDataBuilder.getEffectsList());

        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Load success!"));
        errorLabel.setVisible(true);
    }

    private void saveLevel() {
        if (idText.getText().isBlank()) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "ID is null or empty!"));
            idText.requestFocus();
            return;
        }

        final List<StageData> stages = new ArrayList<>();
        for (final Node node : stagesVBox.getChildren()) {
            final StageNode stageNode = (StageNode) node;

            final StageData stageData = stageNode.buildStageData();
            if (stageData == null) {
                return;
            }
            stages.add(stageData);
        }
        final LevelData levelData = LevelData.newBuilder()
                .setId(idText.getText())
                .addAllStageData(stages)
                .addAllEffects(levelEffectsList.getItems())
                .build();

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(LEVEL_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Save Level Data"));
        fileChooser.setInitialFileName(idText.getText() + ".json");

        final File saveFile = fileChooser.showSaveDialog(null);
        if (saveFile == null) {
            return;
        }

        try {
            DataWriter.writeMessage(levelData, saveFile.getAbsolutePath());
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
            errorLabel.setVisible(true);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error while saving enemy!") + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    public void setPreviewMode() {
        final Map<Integer, ServantDataWrapper> servantDataMap = ResourceManager.buildServantSortMap();
        final Map<Integer, CraftEssenceDataWrapper> ceDataMap = ResourceManager.buildCESortMap();
        final Map<Integer, MysticCodeDataWrapper> mcDataMap = ResourceManager.buildMCSortMap();
        simulationPrepHBox.setVisible(true);
        simulationPrepHBox.setManaged(true);
        simulationPrepHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        simulationPrepHBox.setSpacing(10);

        final ToggleGroup supportToggle = new ToggleGroup();
        final List<Node> nodes = simulationPrepHBox.getChildren();
        nodes.add(new FormationSelector(supportToggle, errorLabel, servantDataMap, ceDataMap));
        nodes.add(new FormationSelector(supportToggle, errorLabel, servantDataMap, ceDataMap));
        nodes.add(new FormationSelector(supportToggle, errorLabel, servantDataMap, ceDataMap));
        nodes.add(new Separator(Orientation.VERTICAL));
        nodes.add(new FormationSelector(supportToggle, errorLabel, servantDataMap, ceDataMap));
        nodes.add(new FormationSelector(supportToggle, errorLabel, servantDataMap, ceDataMap));
        nodes.add(new FormationSelector(supportToggle, errorLabel, servantDataMap, ceDataMap));
        nodes.add(new Separator(Orientation.VERTICAL));

        final VBox miscVBox = new VBox();
        HBox.setHgrow(miscVBox, Priority.ALWAYS);
        miscVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        miscVBox.setSpacing(10);
        miscVBox.setAlignment(Pos.TOP_CENTER);
        nodes.add(miscVBox);

        final Button selectMCButton = new Button();
        selectMCButton.setGraphic(mcDataMap.get(2));

        final Label mcNameLabel = new Label(getTranslation(ENTITY_NAME_SECTION, "mysticCode2"));
        mcNameLabel.setWrapText(true);
        mcNameLabel.setAlignment(Pos.CENTER);

        selectMCButton.setOnAction(e -> {
            try {
                final MysticCodeDataWrapper wrapper = selectMysticCode(simulationPrepHBox.getScene().getWindow(), mcDataMap);
                if (wrapper != null) {
                    selectMCButton.setGraphic(wrapper);
                    mcNameLabel.setText(getTranslation(ENTITY_NAME_SECTION, wrapper.getMysticCodeData().getId()));
                    mcNameLabel.setMaxWidth(miscVBox.getWidth());
                }
            } catch (final IOException ignored) {
            }
        });

        final Label mcLevelLabel = new Label(getTranslation(APPLICATION_SECTION, "MC Level"));
        final Label mcLevelValueLabel = new Label("1");
        final HBox mcLevelLabelHBox = new HBox();
        mcLevelLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        mcLevelLabelHBox.setSpacing(10);
        mcLevelLabelHBox.getChildren().addAll(mcLevelLabel, mcLevelValueLabel);
        final Slider mcLevelSlider = new Slider();
        mcLevelSlider.setMin(1);
        mcLevelSlider.setMax(10);
        mcLevelSlider.setBlockIncrement(1);
        mcLevelSlider.setMajorTickUnit(1);
        mcLevelSlider.setMinorTickCount(0);
        mcLevelSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final int intValue = newValue.intValue();
                    mcLevelSlider.setValue(intValue);
                    mcLevelValueLabel.setText(Integer.toString(intValue));
                }
        );
        mcLevelSlider.setShowTickMarks(true);
        mcLevelSlider.setValue(10);

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
                }
        );
        randomSlider.setShowTickMarks(true);
        randomSlider.setValue(0.9);

        final Button startSimulationButton = new Button(getTranslation(APPLICATION_SECTION, "Start Simulation"));

        miscVBox.getChildren().addAll(
                selectMCButton,
                mcNameLabel,
                mcLevelLabelHBox,
                mcLevelSlider,
                new Separator(),
                probabilityLabelHBox,
                probabilityThresholdSlider,
                randomLabelHBox,
                randomSlider,
                startSimulationButton
        );
    }
}
