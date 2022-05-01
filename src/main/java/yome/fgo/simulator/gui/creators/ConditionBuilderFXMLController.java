package yome.fgo.simulator.gui.creators;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ConditionBuilderFXMLController implements Initializable {
    @FXML
    private Button buildButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ChoiceBox<?> conditionChoices;

    @FXML
    private Label conditionLabel;

    @FXML
    private Label subConditionLabel;

    @FXML
    private AnchorPane subConditionPane;

    @FXML
    private TreeView<?> subConditionTree;

    @FXML
    private ChoiceBox<?> targetChoices;

    @FXML
    private Label targetLabel;

    @FXML
    private AnchorPane targetPane;

    @FXML
    private Label valueLabel;

    @FXML
    private AnchorPane valuePane;

    @FXML
    private TextField valueText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        valuePane.setVisible(false);
        valuePane.setManaged(false);
    }
}
