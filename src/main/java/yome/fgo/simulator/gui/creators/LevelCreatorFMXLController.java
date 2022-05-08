package yome.fgo.simulator.gui.creators;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.gui.components.EffectsCellFactory;
import yome.fgo.simulator.gui.components.StageNode;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

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
        // TODO: implement
    }

    private void saveLevel() {
        // TODO: implement
    }
}
