package yome.fgo.simulator.gui.creators;

import com.google.protobuf.util.JsonFormat;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.gui.components.EffectsCellFactory;
import yome.fgo.simulator.gui.components.StageNode;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
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

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Level Name"));

        stagesVBox.getChildren().clear();
        stagesVBox.getChildren().add(new StageNode(1));
        addStageButton.setText(getTranslation(APPLICATION_SECTION, "Add Stage"));
        addStageButton.setOnAction(e -> {
            stagesVBox.getChildren().add(new StageNode(stagesVBox.getChildren().size() + 1));
        });
        removeStageButton.setText(getTranslation(APPLICATION_SECTION, "Remove Stage"));
        removeStageButton.setOnAction(e -> {
            if (stagesVBox.getChildren().size() > 0) {
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
}
