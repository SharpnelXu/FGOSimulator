package yome.fgo.simulator.gui.creators;

import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.gui.components.TargetWrapper;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.ConditionFactory.getAllAvailableConditionOptions;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ConditionBuilderFXMLController implements Initializable {
    @FXML
    private Button buildButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ChoiceBox<String> conditionChoices;

    @FXML
    private Label conditionLabel;

    @FXML
    private Label subConditionLabel;

    @FXML
    private AnchorPane subConditionPane;

    @FXML
    private ListView<?> subConditionList;

    @FXML
    private ChoiceBox<TargetWrapper> targetChoices;

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

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conditionLabel.setText(getTranslation(APPLICATION_SECTION, "Condition Type"));

        conditionChoices.setItems(FXCollections.observableArrayList(
                getAllAvailableConditionOptions().stream()
                        .map(s -> getTranslation(CONDITION_SECTION, s))
                        .collect(Collectors.toList())
        ));
        conditionChoices.getSelectionModel().select(getTranslation(CONDITION_SECTION, "Always"));

        valueLabel.setText(getTranslation(APPLICATION_SECTION, "Value"));

        targetLabel.setText(getTranslation(APPLICATION_SECTION, "Target"));

        final List<Target> targets = Lists.newArrayList(Target.values());
        targets.remove(Target.UNRECOGNIZED);
        targets.remove(Target.SERVANT_EXCHANGE);
        targetChoices.setItems(FXCollections.observableArrayList(
                targets.stream()
                        .map(target -> new TargetWrapper(target, getTranslation(TARGET_SECTION, target.name())))
                        .collect(Collectors.toList())
        ));
        targetChoices.getSelectionModel().selectFirst();

        subConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Sub-conditions"));

        cancelButton.setText(getTranslation(APPLICATION_SECTION, "Cancel"));

        buildButton.setText(getTranslation(APPLICATION_SECTION, "Build"));

        valuePane.setVisible(false);
        valuePane.setManaged(false);

        targetPane.setVisible(false);
        targetPane.setManaged(false);

        subConditionPane.setVisible(false);
        subConditionPane.setManaged(false);

        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Select a condition type to start"));
        errorLabel.setVisible(true);
    }
}
