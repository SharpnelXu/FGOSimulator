package yome.fgo.simulator.gui.creators;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode;
import yome.fgo.data.proto.FgoStorageData.EffectData;

import java.net.URL;
import java.util.ResourceBundle;

public class BuffBuilderFXMLController implements Initializable {
    @FXML
    private Button addEffectsButton;

    @FXML
    private ChoiceBox<String> buffTypeChoices;

    @FXML
    private Label buffTypeLabel;

    @FXML
    private Button buildButton;

    @FXML
    private Label builtConditionLabel;

    @FXML
    private Label builtVariationLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Label classAdvAtkLabel;

    @FXML
    private Label classAdvAtkTargetClassLabel;

    @FXML
    private TextField classAdvAtkTargetClassText;

    @FXML
    private ChoiceBox<ClassAdvantageChangeMode> classAdvChoicesAtk;

    @FXML
    private ChoiceBox<ClassAdvantageChangeMode> classAdvChoicesDef;

    @FXML
    private CheckBox classAdvCustomAtkCheckbox;

    @FXML
    private TextField classAdvCustomAtkText;

    @FXML
    private CheckBox classAdvCustomDefCheckbox;

    @FXML
    private TextField classAdvCustomDefText;

    @FXML
    private Label classAdvDefLabel;

    @FXML
    private Label classAdvDefTargetClassLabel;

    @FXML
    private TextField classAdvDefTargetClassText;

    @FXML
    private VBox classAdvPane;

    @FXML
    private CheckBox conditionCheckbox;

    @FXML
    private Button conditionEditButton;

    @FXML
    private Button editVariationButton;

    @FXML
    private Label effectsLabel;

    @FXML
    private ListView<EffectData> effectsList;

    @FXML
    private HBox effectsPane;

    @FXML
    private Label errorLabel;

    @FXML
    private CheckBox forceStackCheckbox;

    @FXML
    private Label generateValueBaseLabel;

    @FXML
    private TextField generateValueBaseText;

    @FXML
    private Button generateValueCancelButton;

    @FXML
    private Button generateValueGenerateButton;

    @FXML
    private StackPane generateValuePane;

    @FXML
    private Label generateValueStepLabel;

    @FXML
    private TextField generateValueStepText;

    @FXML
    private Button generateValuesButton;

    @FXML
    private Button generateVariationsButton;

    @FXML
    private TextField gutsIntText;

    @FXML
    private Label gutsLabel;

    @FXML
    private HBox gutsPane;

    @FXML
    private CheckBox gutsPercentCheckbox;

    @FXML
    private TextField gutsPercentText;

    @FXML
    private CheckBox irremovableCheckbox;

    @FXML
    private CheckBox numTimesCheckbox;

    @FXML
    private TextField numTimesText;

    @FXML
    private CheckBox numTurnCheckbox;

    @FXML
    private TextField numTurnText;

    @FXML
    private CheckBox probabilityCheckbox;

    @FXML
    private Button probabilityGenerateButton;

    @FXML
    private TextField probabilityText;

    @FXML
    private Label stringValueLabel;

    @FXML
    private HBox stringValuePane;

    @FXML
    private TextField stringValueText;

    @FXML
    private CheckBox traitCheckbox;

    @FXML
    private TextField traitText;

    @FXML
    private CheckBox useVariationCheckbox;

    @FXML
    private Label valueLabel;

    @FXML
    private VBox valuePane;

    @FXML
    private TextField valuesText;

    @FXML
    private TextField variationAdditionText;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

    }
}
