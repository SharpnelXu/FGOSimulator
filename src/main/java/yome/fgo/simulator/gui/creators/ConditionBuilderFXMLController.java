package yome.fgo.simulator.gui.creators;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.BuffTraits;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.gui.components.ListContainerVBox;
import yome.fgo.simulator.gui.components.ListContainerVBox.Mode;
import yome.fgo.simulator.gui.components.TranslationConverter;
import yome.fgo.simulator.models.conditions.Condition.ConditionFields;
import yome.fgo.simulator.models.conditions.Condition.ConditionType;
import yome.fgo.simulator.models.effects.buffs.BuffType;
import yome.fgo.simulator.utils.RoundUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static yome.fgo.simulator.gui.components.DataPrinter.doubleToString;
import static yome.fgo.simulator.gui.components.DataPrinter.intToString;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.addSplitTraitListener;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillFateClass;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillTargets;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_BUFF_TRAIT_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_BUFF_TYPE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_CLASS_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_LIMITED_SUB_CONDITION;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_NAMES;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_TARGET;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_TRAIT_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_UNLIMITED_SUB_CONDITION;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getKeyForTrait;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasKeyForTrait;
import static yome.fgo.simulator.utils.BuffUtils.REGULAR_BUFF_TRAITS;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.REGULAR_CARD_TYPES;

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
    private VBox subConditionPane;

    @FXML
    private ChoiceBox<Target> targetChoices;

    @FXML
    private Label targetLabel;

    @FXML
    private HBox targetPane;

    @FXML
    private Label valueLabel;

    @FXML
    private HBox valuePane;

    @FXML
    private TextField valueText;

    @FXML
    private ChoiceBox<String> buffChoices;

    @FXML
    private Label buffLabel;

    @FXML
    private HBox buffPane;

    @FXML
    private ChoiceBox<FateClass> classChoices;

    @FXML
    private Label classLabel;

    @FXML
    private HBox classPane;

    @FXML
    private Label cardLabel;

    @FXML
    private HBox cardPane;

    @FXML
    private Label errorLabel;

    @FXML
    private VBox buffTraitPane;

    @FXML
    private HBox regularBuffTraitsHBox;

    @FXML
    private RadioButton buffTraitRadioButton;

    @FXML
    private TextField buffTraitText;

    private ConditionData.Builder conditionDataBuilder;

    private Set<ConditionFields> requiredFields;

    private ChangeListener<String> valueTextListener;
    private ListContainerVBox conditions;
    private Map<BuffTraits, RadioButton> buffTraitsMap;
    private ToggleGroup buffTraitToggle;
    private Map<CommandCardType, RadioButton> cardTypeMap;
    private ToggleGroup cardTypeToggle;

    public void setParentBuilder(final ConditionData.Builder builder) {
        this.conditionDataBuilder = builder;

        if (!builder.getType().isEmpty()) {
            requiredFields = ConditionType.ofType(builder.getType()).getRequiredFields();
            conditionChoices.getSelectionModel().select(builder.getType());
            
            if (requiredFields.contains(CONDITION_FIELD_INT_VALUE)) {
                valueText.setText(intToString(builder.getDoubleValue()));
            } else if (requiredFields.contains(CONDITION_FIELD_DOUBLE_VALUE)) {
                valueText.setText(doubleToString(builder.getDoubleValue()));
            } else if (requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE)) {
                valueText.setText(getTranslation(TRAIT_SECTION, builder.getValue()));
            } else if (requiredFields.contains(CONDITION_FIELD_NAMES)) {
                valueText.setText(builder.getValue());
            }

            if (requiredFields.contains(CONDITION_FIELD_BUFF_TYPE)) {
                buffChoices.getSelectionModel().select(builder.getValue());
            }
            if (requiredFields.contains(CONDITION_FIELD_CARD_TYPE)) {
                cardTypeMap.get(CommandCardType.valueOf(builder.getValue())).setSelected(true);
            }
            if (requiredFields.contains(CONDITION_FIELD_CLASS_VALUE)) {
                classChoices.getSelectionModel().select(FateClass.valueOf(builder.getValue()));
            }

            if (requiredFields.contains(CONDITION_FIELD_TARGET)) {
                targetChoices.getSelectionModel().select(builder.getTarget());
            }
            if (requiredFields.contains(CONDITION_FIELD_UNLIMITED_SUB_CONDITION)
                    || requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION)) {
                conditions.loadCondition(builder.getSubConditionDataList());
            }
            if (requiredFields.contains(CONDITION_FIELD_BUFF_TRAIT_VALUE)) {
                try {
                    final BuffTraits buffTraits = BuffTraits.valueOf(builder.getValue());
                    buffTraitsMap.get(buffTraits).setSelected(true);
                } catch (final Exception ignored) {
                    buffTraitRadioButton.setSelected(true);
                    buffTraitRadioButton.fireEvent(new ActionEvent());
                    buffTraitText.setText(builder.getValue());
                }
            }
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        conditionLabel.setText(getTranslation(APPLICATION_SECTION, "Condition Type"));

        conditionChoices.setConverter(new TranslationConverter(CONDITION_SECTION));
        conditionChoices.setItems(FXCollections.observableArrayList(ConditionType.getOrder()));

        conditionChoices.getSelectionModel().select( "Always");
        requiredFields = ConditionType.ofType("Always").getRequiredFields();

        conditionChoices.setOnAction(e -> onConditionChoicesChange());

        buffLabel.setText(getTranslation(APPLICATION_SECTION, "Buff Type"));
        buffChoices.setConverter(new TranslationConverter(BUFF_SECTION));
        buffChoices.setItems(FXCollections.observableArrayList(BuffType.getOrder()));
        buffChoices.getSelectionModel().selectFirst();

        cardLabel.setText(getTranslation(APPLICATION_SECTION, "Card Type"));
        cardTypeToggle = new ToggleGroup();
        cardTypeMap = new HashMap<>();
        for (final CommandCardType commandCardType : REGULAR_CARD_TYPES) {
            final RadioButton radioButton = new RadioButton(getTranslation(COMMAND_CARD_TYPE_SECTION, commandCardType.name()));
            radioButton.setToggleGroup(cardTypeToggle);
            cardTypeMap.put(commandCardType, radioButton);
            cardPane.getChildren().add(radioButton);
        }

        classLabel.setText(getTranslation(APPLICATION_SECTION, "Class"));
        fillFateClass(classChoices);

        targetLabel.setText(getTranslation(APPLICATION_SECTION, "Target"));

        fillTargets(targetChoices);

        cancelButton.setText(getTranslation(APPLICATION_SECTION, "Cancel"));

        buildButton.setText(getTranslation(APPLICATION_SECTION, "Build"));

        buffTraitToggle = new ToggleGroup();
        final Label regularBuffTraitLabel = new Label(getTranslation(APPLICATION_SECTION, "Buff Trait"));
        regularBuffTraitsHBox.getChildren().add(regularBuffTraitLabel);
        buffTraitsMap = new HashMap<>();
        for (final BuffTraits buffTrait : REGULAR_BUFF_TRAITS) {
            final RadioButton radioButton = new RadioButton(getTranslation(TRAIT_SECTION, buffTrait.name()));
            radioButton.setToggleGroup(buffTraitToggle);
            buffTraitsMap.put(buffTrait, radioButton);
            regularBuffTraitsHBox.getChildren().add(radioButton);
        }

        buffTraitRadioButton.setText(getTranslation(APPLICATION_SECTION, "Custom Buff Trait"));
        buffTraitText.setDisable(true);
        buffTraitRadioButton.setOnAction(e -> buffTraitText.setDisable(!buffTraitRadioButton.isSelected()));
        buffTraitRadioButton.setToggleGroup(buffTraitToggle);

        buffTraitToggle.selectedToggleProperty().addListener(e -> buffTraitRadioButton.fireEvent(new ActionEvent()));
        addSplitTraitListener(buffTraitText, errorLabel);

        resetPane();

        conditions = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Sub-conditions"), errorLabel, Mode.CONDITION);
        subConditionPane.getChildren().add(conditions);

        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Select a condition type to start"));
        errorLabel.setVisible(true);

        valueTextListener = (observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !hasKeyForTrait(newValue)) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Warning: unmapped traits:") + newValue);
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        };
    }

    private void resetPane() {
        valuePane.setVisible(false);
        valuePane.setManaged(false);

        buffPane.setVisible(false);
        buffPane.setManaged(false);

        cardPane.setVisible(false);
        cardPane.setManaged(false);

        classPane.setVisible(false);
        classPane.setManaged(false);

        targetPane.setVisible(false);
        targetPane.setManaged(false);

        subConditionPane.setVisible(false);
        subConditionPane.setManaged(false);

        buffTraitPane.setVisible(false);
        buffTraitPane.setManaged(false);
    }

    @FXML
    public void onConditionChoicesChange() {
        errorLabel.setVisible(false);

        resetPane();

        requiredFields = ConditionType.ofType(conditionChoices.getValue()).getRequiredFields();

        if (requiredFields.contains(CONDITION_FIELD_INT_VALUE) ||
                requiredFields.contains(CONDITION_FIELD_DOUBLE_VALUE) ||
                requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE) ||
                requiredFields.contains(CONDITION_FIELD_NAMES)
        ) {
            valuePane.setVisible(true);
            valuePane.setManaged(true);
            valueText.clear();
            if (requiredFields.contains(CONDITION_FIELD_INT_VALUE)) {
                valueLabel.setText(getTranslation(APPLICATION_SECTION, "Value"));
                valueText.textProperty().removeListener(valueTextListener);
            } else if (requiredFields.contains(CONDITION_FIELD_DOUBLE_VALUE)) {
                valueLabel.setText(getTranslation(APPLICATION_SECTION, "Value (%)"));
                valueText.textProperty().removeListener(valueTextListener);
            } else if (requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE)) {
                valueLabel.setText(getTranslation(APPLICATION_SECTION, "Traits"));
                valueText.textProperty().addListener(valueTextListener);
            } else {
                valueLabel.setText(getTranslation(APPLICATION_SECTION, "ID"));
                valueText.textProperty().removeListener(valueTextListener);
            }
        }

        if (requiredFields.contains(CONDITION_FIELD_BUFF_TYPE)) {
            buffPane.setVisible(true);
            buffPane.setManaged(true);
        }
        if (requiredFields.contains(CONDITION_FIELD_CARD_TYPE)) {
            cardPane.setVisible(true);
            cardPane.setManaged(true);
        }
        if (requiredFields.contains(CONDITION_FIELD_CLASS_VALUE)) {
            classPane.setVisible(true);
            classPane.setManaged(true);
        }

        if (requiredFields.contains(CONDITION_FIELD_TARGET)) {
            targetPane.setVisible(true);
            targetPane.setManaged(true);
        }
        if (requiredFields.contains(CONDITION_FIELD_UNLIMITED_SUB_CONDITION) || requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION)) {
            subConditionPane.setVisible(true);
            subConditionPane.setManaged(true);
        }
        if (requiredFields.contains(CONDITION_FIELD_BUFF_TRAIT_VALUE)) {
            buffTraitPane.setVisible(true);
            buffTraitPane.setManaged(true);
        }
    }

    @FXML
    public void onCancelButtonClick() {
        if (conditionDataBuilder != null) {
            conditionDataBuilder.clearType();
        }
        final Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onBuildButtonClick() {
        if (conditionDataBuilder != null) {
            conditionDataBuilder.clear();
            if (requiredFields.contains(CONDITION_FIELD_INT_VALUE)) {
                try {
                    conditionDataBuilder.setDoubleValue(Integer.parseInt(valueText.getText()));
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Integer"));
                    return;
                }
            } else if (requiredFields.contains(CONDITION_FIELD_DOUBLE_VALUE)) {
                try {
                    conditionDataBuilder.setDoubleValue(RoundUtils.roundNearest(Double.parseDouble(valueText.getText()) / 100.0));
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Double"));
                    return;
                }
            } else if (requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE) || requiredFields.contains(
                    CONDITION_FIELD_NAMES)) {
                if (valueText.getText().isEmpty()) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "String is empty"));
                    return;
                }
                if (requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE)) {
                    conditionDataBuilder.setValue(getKeyForTrait(valueText.getText().trim()));
                } else {
                    conditionDataBuilder.setValue(valueText.getText().trim());
                }
            }

            if (requiredFields.contains(CONDITION_FIELD_BUFF_TYPE)) {
                conditionDataBuilder.setValue(buffChoices.getValue());
            } else if (requiredFields.contains(CONDITION_FIELD_CARD_TYPE)) {
                if (cardTypeToggle.getSelectedToggle() == null) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "No card type selected"));
                    return;
                }

                for (final CommandCardType cardType : cardTypeMap.keySet()) {
                    if (cardTypeMap.get(cardType).isSelected()) {
                        conditionDataBuilder.setValue(cardType.name());
                        break;
                    }
                }
            } else if (requiredFields.contains(CONDITION_FIELD_CLASS_VALUE)) {
                conditionDataBuilder.setValue(classChoices.getValue().name());
            }
            
            if (requiredFields.contains(CONDITION_FIELD_TARGET)) {
                conditionDataBuilder.setTarget(targetChoices.getValue());
            }
            if (requiredFields.contains(CONDITION_FIELD_UNLIMITED_SUB_CONDITION) ||
                    requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION)) {
                final List<ConditionData> builtCondition = conditions.buildCondition();
                if (requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION) && builtCondition.size() != 1) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Only one sub-condition allowed"));
                    return;
                }
                conditionDataBuilder.addAllSubConditionData(builtCondition);
            }
            if (requiredFields.contains(CONDITION_FIELD_BUFF_TRAIT_VALUE)) {
                if (buffTraitRadioButton.isSelected()) {

                    if (buffTraitText.getText().isEmpty()) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "No custom buff trait provided"));
                        return;
                    }
                    conditionDataBuilder.setValue(getKeyForTrait(buffTraitText.getText().trim()));
                } else {
                    if (buffTraitToggle.getSelectedToggle() == null) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "No custom buff trait provided"));
                        return;
                    }

                    for (final BuffTraits buffTrait : buffTraitsMap.keySet()) {
                        if (buffTraitsMap.get(buffTrait).isSelected()) {
                            conditionDataBuilder.setValue(buffTrait.name());
                            break;
                        }
                    }
                }
            }

            conditionDataBuilder.setType(conditionChoices.getValue());
        }

        final Stage stage = (Stage) buildButton.getScene().getWindow();
        stage.close();
    }
}
