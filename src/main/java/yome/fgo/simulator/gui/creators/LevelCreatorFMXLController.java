package yome.fgo.simulator.gui.creators;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.gui.components.StageNode;

import java.net.URL;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        stagesVBox.getChildren().addAll(new StageNode(2));
    }
}
