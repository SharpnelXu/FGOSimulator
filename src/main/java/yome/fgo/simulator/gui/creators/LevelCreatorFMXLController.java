package yome.fgo.simulator.gui.creators;

import com.google.protobuf.util.JsonFormat;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.data.proto.FgoStorageData.CraftEssencePreference;
import yome.fgo.data.proto.FgoStorageData.Formation;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.data.proto.FgoStorageData.MysticCodePreference;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.data.proto.FgoStorageData.ServantPreference;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.data.proto.FgoStorageData.UserPreference;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.gui.components.FormationSelector;
import yome.fgo.simulator.gui.components.ListContainerVBox;
import yome.fgo.simulator.gui.components.ListContainerVBox.Mode;
import yome.fgo.simulator.gui.components.MysticCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.SimulationWindow;
import yome.fgo.simulator.gui.components.StageNode;
import yome.fgo.simulator.gui.helpers.LaunchUtils;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static yome.fgo.simulator.ResourceManager.MYSTIC_CODE_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.readFile;
import static yome.fgo.simulator.gui.creators.EntitySelector.selectMysticCode;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.DEFAULT_18;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createTooltip;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.LEVEL_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.USER_PREFERENCE_FILE_PATH;

public class LevelCreatorFMXLController implements Initializable {

    @FXML
    private Button addStageButton;

    @FXML
    private Label idLabel;

    @FXML
    private TextField idText;

    @FXML
    private Button loadLevelButton;

    @FXML
    private Button saveLevelButton;

    @FXML
    private VBox stagesVBox;

    @FXML
    private Label errorLabel;

    @FXML
    private HBox simulationPrepHBox;

    @FXML
    private Button startSimulationButton;

    @FXML
    private VBox levelEffectVBox;

    @FXML
    private HBox formationNameHBox;

    private MysticCodeDataAnchorPane mysticCodeDataAnchorPane;
    private List<FormationSelector> formationSelectors;
    private Label costValueLabel;
    private ListContainerVBox levelEffects;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Level Name"));

        stagesVBox.getChildren().add(new StageNode(1, errorLabel, stagesVBox));
        addStageButton.setText(null);

        addStageButton.setGraphic(createInfoImageView("add"));
        addStageButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Add Stage")));
        addStageButton.setOnAction(e -> stagesVBox.getChildren().add(new StageNode(stagesVBox.getChildren().size() + 1, errorLabel, stagesVBox)));

        levelEffects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Level Effects"), errorLabel, Mode.EFFECT);
        levelEffectVBox.getChildren().add(levelEffects);

        loadLevelButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        loadLevelButton.setOnAction(e -> loadLevel());
        saveLevelButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
        saveLevelButton.setOnAction(e -> saveLevel());

        errorLabel.setVisible(false);

        formationNameHBox.setVisible(false);
        formationNameHBox.setManaged(false);
        simulationPrepHBox.setVisible(false);
        simulationPrepHBox.setManaged(false);
        startSimulationButton.setVisible(false);
        startSimulationButton.setManaged(false);
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
            parser.merge(readFile(levelDataFile), levelDataBuilder);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + " " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        idText.setText(levelDataBuilder.getId());

        stagesVBox.getChildren().clear();
        for (int i = 1; i <= levelDataBuilder.getStageDataCount(); i += 1) {
            final StageData stageData = levelDataBuilder.getStageData(i - 1);
            final StageNode stageNode = new StageNode(i, errorLabel, stagesVBox);
            stageNode.loadStageData(stageData);
            stagesVBox.getChildren().add(stageNode);
        }

        levelEffects.loadEffect(levelDataBuilder.getEffectsList());

        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Load success!"));
        errorLabel.setVisible(true);
    }

    private LevelData buildLevelData() {
        if (idText.getText().isBlank()) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Level ID is null or empty!"));
            return null;
        }

        final List<StageData> stages = new ArrayList<>();
        for (final Node node : stagesVBox.getChildren()) {
            final StageNode stageNode = (StageNode) node;

            final StageData stageData = stageNode.buildStageData();
            if (stageData == null) {
                return null;
            }
            stages.add(stageData);
        }
        if (stages.isEmpty()) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "No added stages"));
            return null;
        }
        return LevelData.newBuilder()
                .setId(idText.getText())
                .addAllStageData(stages)
                .addAllEffects(levelEffects.buildEffect())
                .build();
    }

    private void saveLevel() {
        final LevelData levelData = buildLevelData();

        if (levelData == null) {
            return;
        }

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
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error while saving!") + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    public void calculateCost() {
        int cost = 0;
        for (final FormationSelector formationSelector : formationSelectors) {
            cost += formationSelector.getCost();
        }
        costValueLabel.setText(Integer.toString(cost));
    }

    public void setPreviewMode() {
        ResourceManager.rebuildDataMap();

        final UserPreference userPreference = ResourceManager.getUserPreference();
        final Map<Integer, ServantOption> servantOptions = new HashMap<>();
        final Map<Integer, CraftEssenceOption> ceOptions = new HashMap<>();
        final Map<Integer, MysticCodeOption> mcOptions = new HashMap<>();
        for (final ServantPreference preference : userPreference.getServantPrefsList()) {
            servantOptions.put(preference.getServantNo(), preference.getOption());
        }
        for (final CraftEssencePreference preference : userPreference.getCePrefsList()) {
            ceOptions.put(preference.getCraftEssenceNo(), preference.getOption());
        }
        for (final MysticCodePreference preference : userPreference.getMcPrefsList()) {
            mcOptions.put(preference.getMysticCodeNo(), preference.getOption());
        }

        final List<Formation> formationList = new ArrayList<>(userPreference.getFormationsList());
        final Map<String, Integer> formationIndex = new HashMap<>();
        for (int i = 0; i < formationList.size(); i += 1) {
            formationIndex.put(formationList.get(i).getName(), i);
        }

        formationNameHBox.setVisible(true);
        formationNameHBox.setManaged(true);
        final Button loadFormationButton = new Button(getTranslation(APPLICATION_SECTION, "Load Formation"));
        loadFormationButton.setFont(DEFAULT_18);
        final Button saveFormationButton = new Button(getTranslation(APPLICATION_SECTION, "Save Formation"));
        saveFormationButton.setFont(DEFAULT_18);
        final TextField formationNameText = new TextField();
        formationNameText.setFont(DEFAULT_18);
        final AnchorPane nameAnchor = wrapInAnchor(formationNameText);
        HBox.setHgrow(nameAnchor, Priority.ALWAYS);

        formationNameHBox.getChildren().addAll(loadFormationButton, saveFormationButton, nameAnchor);

        simulationPrepHBox.setVisible(true);
        simulationPrepHBox.setManaged(true);
        simulationPrepHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        simulationPrepHBox.setSpacing(10);

        final List<Node> nodes = simulationPrepHBox.getChildren();
        formationSelectors = new ArrayList<>();
        for (int i = 0; i < 3; i += 1) {
            final FormationSelector formationSelector = new FormationSelector(
                    errorLabel,
                    servantOptions,
                    ceOptions,
                    this
            );
            formationSelectors.add(formationSelector);
            nodes.add(formationSelector);
        }
        nodes.add(new Separator(Orientation.VERTICAL));
        for (int i = 0; i < 3; i += 1) {
            final FormationSelector formationSelector = new FormationSelector(
                    errorLabel,
                    servantOptions,
                    ceOptions,
                    this
            );
            formationSelectors.add(formationSelector);
            nodes.add(formationSelector);
        }
        nodes.add(new Separator(Orientation.VERTICAL));

        final VBox miscVBox = new VBox(10);
        HBox.setHgrow(miscVBox, Priority.ALWAYS);
        miscVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        miscVBox.setAlignment(Pos.TOP_CENTER);
        nodes.add(miscVBox);

        final Button selectMCButton = new Button();
        final MysticCodeDataAnchorPane defaultMC = MYSTIC_CODE_DATA_ANCHOR_MAP.get(18);
        mysticCodeDataAnchorPane = new MysticCodeDataAnchorPane(defaultMC.getMysticCodeData(), defaultMC.getImages());
        selectMCButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Select mystic code")));
        selectMCButton.setGraphic(mysticCodeDataAnchorPane);

        final Label mcNameLabel = new Label(getTranslation(ENTITY_NAME_SECTION, "mysticCode18"));
        mcNameLabel.setWrapText(true);
        mcNameLabel.setAlignment(Pos.CENTER);

        selectMCButton.setOnAction(e -> {
            try {
                final MysticCodeDataAnchorPane wrapper = selectMysticCode(
                        simulationPrepHBox.getScene().getWindow(),
                        mysticCodeDataAnchorPane.getGender()
                );
                if (wrapper != null) {
                    selectMCButton.setGraphic(wrapper);
                    mysticCodeDataAnchorPane = wrapper;
                    mcNameLabel.setText(getTranslation(ENTITY_NAME_SECTION, wrapper.getMysticCodeData().getId()));
                    mcNameLabel.setMaxWidth(miscVBox.getWidth());
                }
            } catch (final IOException ignored) {
            }
        });

        final Button viewMCButton = new Button();
        viewMCButton.setGraphic(createInfoImageView("info2"));
        viewMCButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Details")));
        viewMCButton.setOnAction(e -> {
            try {
                MysticCodeCreator.preview(simulationPrepHBox.getScene().getWindow(), mysticCodeDataAnchorPane.getMysticCodeData());
            } catch (IOException ignored) {
            }
        });

        final Label mcLevelLabel = new Label(getTranslation(APPLICATION_SECTION, "MC Level"));
        final Label mcLevelValueLabel = new Label("1");
        final HBox mcLevelLabelHBox = new HBox(10);
        mcLevelLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
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
        if (mcOptions.containsKey(2)) {
            final MysticCodeOption mysticCodeOption = mcOptions.get(2);
            mcLevelSlider.setValue(mysticCodeOption.getMysticCodeLevel());
            mysticCodeDataAnchorPane.setFromGender(mysticCodeOption.getGender());
        }

        final Label probabilityLabel = new Label(getTranslation(APPLICATION_SECTION, "Probability Threshold (%)"));
        final Label probabilityValueLabel = new Label("100");
        final HBox probabilityLabelHBox = new HBox(10);
        probabilityLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
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
        final HBox randomLabelHBox = new HBox(10);
        randomLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
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

        final HBox costHBox = new HBox(10);
        final Label costLabel = new Label(getTranslation(APPLICATION_SECTION, "COST"));
        costValueLabel = new Label("0");
        costHBox.getChildren().addAll(costLabel, costValueLabel);

        miscVBox.getChildren().addAll(
                selectMCButton,
                mcNameLabel,
                viewMCButton,
                mcLevelLabelHBox,
                mcLevelSlider,
                new Separator(),
                probabilityLabelHBox,
                probabilityThresholdSlider,
                randomLabelHBox,
                randomSlider,
                costHBox
        );

        loadFormationButton.setOnAction(e -> {
            try {
                final List<Formation> newFormationList = new ArrayList<>(formationList);
                final Formation selection = EntitySelector.selectFormation(
                        formationNameHBox.getScene().getWindow(),
                        newFormationList

                );
                if (selection != null) {
                    formationNameText.setText(selection.getName());
                    final List<ServantPreference> servantPreferences = selection.getServantsList();
                    final List<CraftEssencePreference> craftEssencePreferences = selection.getCraftEssencesList();
                    for (int i = 0; i < selection.getServantsCount(); i += 1) {
                        formationSelectors.get(i).setFromPreferences(servantPreferences.get(i), craftEssencePreferences.get(i));
                    }

                    final MysticCodePreference mysticCodePreference = selection.getMysticCode();
                    mcLevelSlider.setValue(mysticCodePreference.getOption().getMysticCodeLevel());

                    final MysticCodeDataAnchorPane reference = MYSTIC_CODE_DATA_ANCHOR_MAP.get(mysticCodePreference.getMysticCodeNo());
                    mysticCodeDataAnchorPane = new MysticCodeDataAnchorPane();
                    mysticCodeDataAnchorPane.setFrom(reference.getMysticCodeData(), reference.getImages(), mysticCodePreference.getOption().getGender());
                    selectMCButton.setGraphic(mysticCodeDataAnchorPane);
                    mcNameLabel.setText(getTranslation(ENTITY_NAME_SECTION, mysticCodeDataAnchorPane.getMysticCodeData().getId()));
                    mcNameLabel.setMaxWidth(miscVBox.getWidth());

                    formationList.clear();
                    formationList.addAll(newFormationList);
                    formationIndex.clear();
                    for (int i = 0; i < formationList.size(); i += 1) {
                        formationIndex.put(formationList.get(i).getName(), i);
                    }
                    writeUserPreference(servantOptions, ceOptions, mcOptions, formationList);
                }
            } catch (final IOException ex) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
            }
        });

        saveFormationButton.setOnAction(e -> {
            final Formation.Builder formation = Formation.newBuilder();
            if (formationNameText.getText().isBlank()) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Formation name is empty"));
                return;
            }

            formation.setName(formationNameText.getText());
            for (final FormationSelector formationSelector : formationSelectors) {
                if (formationSelector.getServantData() != null) {
                    formation.addServants(
                            ServantPreference.newBuilder()
                                    .setServantNo(formationSelector.getServantData().getServantNum())
                                    .setOption(formationSelector.getServantOption())
                    );
                } else {
                    formation.addServants(ServantPreference.getDefaultInstance());
                }
                if (formationSelector.getCraftEssenceData() != null) {
                    formation.addCraftEssences(
                            CraftEssencePreference.newBuilder()
                                    .setCraftEssenceNo(formationSelector.getCraftEssenceData().getCeNum())
                                    .setOption(formationSelector.getCraftEssenceOption())
                    );
                } else {
                    formation.addCraftEssences(CraftEssencePreference.getDefaultInstance());
                }
            }
            if (formation.getServantsList().isEmpty()) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Formation is empty"));
                return;
            }

            formation.setMysticCode(
                    MysticCodePreference.newBuilder()
                            .setMysticCodeNo(mysticCodeDataAnchorPane.getMysticCodeData().getMcNum())
                            .setOption(
                                    MysticCodeOption.newBuilder()
                                            .setMysticCodeLevel((int) mcLevelSlider.getValue())
                                            .setGender(mysticCodeDataAnchorPane.getGender())
                            )
            );

            if (formationIndex.containsKey(formation.getName())) {
                formationList.set(formationIndex.get(formation.getName()), formation.build());
            } else {
                formationList.add(formation.build());
            }

            writeUserPreference(servantOptions, ceOptions, mcOptions, formationList);
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
        });

        startSimulationButton.setVisible(true);
        startSimulationButton.setManaged(true);
        startSimulationButton.setText(getTranslation(APPLICATION_SECTION, "Start Simulation"));
        startSimulationButton.setOnAction(e -> {
            startSimulation(
                    (int) mcLevelSlider.getValue(),
                    RoundUtils.roundNearest(probabilityThresholdSlider.getValue() / 10.0),
                    randomSlider.getValue(),
                    servantOptions,
                    ceOptions,
                    mcOptions
            );
            writeUserPreference(servantOptions, ceOptions, mcOptions, formationList);
        });
    }

    private void startSimulation(
            final int mcLevel,
            final double probabilityThreshold,
            final double random,
            final Map<Integer, ServantOption> storedServantOptions,
            final Map<Integer, CraftEssenceOption> storedCEOptions,
            final Map<Integer, MysticCodeOption> storedMCOptions
    ) {
        final LevelData levelData = buildLevelData();

        if (levelData == null) {
            return;
        }

        final List<ServantData> servantData = new ArrayList<>();
        final List<ServantOption> servantOptions = new ArrayList<>();
        final List<CraftEssenceData> craftEssenceData = new ArrayList<>();
        final List<CraftEssenceOption> craftEssenceOptions = new ArrayList<>();

        for (final FormationSelector formationSelector : formationSelectors) {
            servantData.add(formationSelector.getServantData());
            servantOptions.add(formationSelector.getServantOption());
            craftEssenceData.add(formationSelector.getCraftEssenceData());
            craftEssenceOptions.add(formationSelector.getCraftEssenceOption());
        }

        if (servantData.stream().noneMatch(Objects::nonNull)) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "No servant selected"));
            errorLabel.setVisible(true);
            return;
        }

        final MysticCodeData mysticCodeData = mysticCodeDataAnchorPane.getMysticCodeData();

        final MysticCodeOption mysticCodeOption = MysticCodeOption.newBuilder()
                .setMysticCodeLevel(mcLevel)
                .setGender(mysticCodeDataAnchorPane.getGender())
                .build();

        for (int i = 0; i < servantData.size(); i += 1) {
            final ServantData selectedServant = servantData.get(i);
            if (selectedServant == null) {
                continue;
            }
            storedServantOptions.put(selectedServant.getServantNum(), servantOptions.get(i));
        }

        for (int i = 0; i < craftEssenceData.size(); i += 1) {
            final CraftEssenceData selectedCE = craftEssenceData.get(i);
            if (selectedCE == null) {
                continue;
            }
            storedCEOptions.put(selectedCE.getCeNum(), craftEssenceOptions.get(i));
        }

        storedMCOptions.put(mysticCodeData.getMcNum(), mysticCodeOption);

        final SimulationWindow simulationWindow = new SimulationWindow(
                levelData,
                servantData,
                servantOptions,
                craftEssenceData,
                craftEssenceOptions,
                mysticCodeData,
                mysticCodeOption,
                probabilityThreshold,
                random
        );

        LaunchUtils.launch("Simulation Window", simulationWindow.getRoot(), true);
        simulationWindow.init();
    }

    private void writeUserPreference(
            final Map<Integer, ServantOption> storedServantOptions,
            final Map<Integer, CraftEssenceOption> storedCEOptions,
            final Map<Integer, MysticCodeOption> storedMCOptions,
            final List<Formation> formationList
    ) {
        final UserPreference.Builder builder = UserPreference.newBuilder();
        for (final int servantNumber : storedServantOptions.keySet()) {
            builder.addServantPrefs(
                    ServantPreference.newBuilder()
                            .setServantNo(servantNumber)
                            .setOption(storedServantOptions.get(servantNumber))
            );
        }
        for (final int ceNumber : storedCEOptions.keySet()) {
            builder.addCePrefs(
                    CraftEssencePreference.newBuilder()
                            .setCraftEssenceNo(ceNumber)
                            .setOption(storedCEOptions.get(ceNumber))
            );
        }
        for (final int mcNumber : storedMCOptions.keySet()) {
            builder.addMcPrefs(
                    MysticCodePreference.newBuilder()
                            .setMysticCodeNo(mcNumber)
                            .setOption(storedMCOptions.get(mcNumber))
            );
        }
        builder.addAllFormations(formationList);
        DataWriter.writeMessage(builder.build(), USER_PREFERENCE_FILE_PATH);
    }
}
