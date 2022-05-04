package yome.fgo.simulator.gui.creators;

import com.google.common.collect.Lists;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.gui.components.EnumConverter;
import yome.fgo.simulator.gui.components.SubConditionCellFactory;
import yome.fgo.simulator.gui.components.TranslationConverter;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.fillFateClass;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.fillTargets;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_BUFF_TYPE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_CLASS_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_LIMITED_SUB_CONDITION;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_SERVANT;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_TARGET;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_TRAIT_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_UNLIMITED_SUB_CONDITION;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_REQUIRED_FIELD_MAP;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_REQUIRED_FIELDS_MAP;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getKeyForTrait;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasKeyForTrait;

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
    private ListView<ConditionData> subConditionList;

    @FXML
    private ChoiceBox<Target> targetChoices;

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
    private ChoiceBox<String> buffChoices;

    @FXML
    private Label buffLabel;

    @FXML
    private AnchorPane buffPane;

    @FXML
    private ChoiceBox<FateClass> classChoices;

    @FXML
    private Label classLabel;

    @FXML
    private AnchorPane classPane;

    @FXML
    private ChoiceBox<CommandCardType> cardTypeChoices;

    @FXML
    private Label cardLabel;

    @FXML
    private AnchorPane cardPane;

    @FXML
    private Label errorLabel;

    @FXML
    private Button addListItemButton;

    private ConditionData.Builder conditionDataBuilder;

    private Set<Integer> requiredFields;

    private ChangeListener<String> valueTextListener;

    public void setParentBuilder(final ConditionData.Builder builder) {
        this.conditionDataBuilder = builder;

        if (!builder.getType().isEmpty()) {
            requiredFields = CONDITION_REQUIRED_FIELD_MAP.get(builder.getType());
            conditionChoices.getSelectionModel().select(builder.getType());
            
            if (requiredFields.contains(CONDITION_FIELD_INT_VALUE)) {
                valueText.setText(Double.toString(builder.getDoubleValue()));
            } else if (requiredFields.contains(CONDITION_FIELD_DOUBLE_VALUE)) {
                valueText.setText(Double.toString(builder.getDoubleValue() * 100));
            } else if (requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE)) {
                valueText.setText(builder.getValue());
            }

            if (requiredFields.contains(CONDITION_FIELD_BUFF_TYPE)) {
                buffChoices.getSelectionModel().select(builder.getValue());
            }
            if (requiredFields.contains(CONDITION_FIELD_CARD_TYPE)) {
                cardTypeChoices.getSelectionModel().select(CommandCardType.valueOf(builder.getValue()));
            }
            if (requiredFields.contains(CONDITION_FIELD_CLASS_VALUE)) {
                classChoices.getSelectionModel().select(FateClass.valueOf(builder.getValue()));
            }

            if (requiredFields.contains(CONDITION_FIELD_TARGET)) {
                targetChoices.getSelectionModel().select(Target.valueOf(builder.getValue()));
            }
            if (requiredFields.contains(CONDITION_FIELD_UNLIMITED_SUB_CONDITION)) {
                subConditionList.setItems(FXCollections.observableArrayList(builder.getSubConditionDataList()));
            } else if (requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION)) {
                subConditionList.setItems(FXCollections.observableArrayList(builder.getSubConditionData(0)));
            }
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        conditionLabel.setText(getTranslation(APPLICATION_SECTION, "Condition Type"));

        conditionChoices.setConverter(new TranslationConverter(CONDITION_SECTION));
        conditionChoices.setItems(FXCollections.observableArrayList(CONDITION_REQUIRED_FIELD_MAP.keySet()));

        conditionChoices.getSelectionModel().select( "Always");
        requiredFields = CONDITION_REQUIRED_FIELD_MAP.get("Always");

        conditionChoices.setOnAction(e -> onConditionChoicesChange());

        buffLabel.setText(getTranslation(APPLICATION_SECTION, "Buff Type"));
        buffChoices.setConverter(new TranslationConverter(BUFF_SECTION));
        buffChoices.setItems(FXCollections.observableArrayList(BUFF_REQUIRED_FIELDS_MAP.keySet()));
        buffChoices.getSelectionModel().selectFirst();

        cardLabel.setText(getTranslation(APPLICATION_SECTION, "Card Type"));
        final List<CommandCardType> cardTypes = Lists.newArrayList(CommandCardType.values());
        cardTypes.remove(CommandCardType.UNRECOGNIZED);
        cardTypeChoices.setConverter(new EnumConverter<>(COMMAND_CARD_TYPE_SECTION));
        cardTypeChoices.setItems(FXCollections.observableArrayList(cardTypes));
        cardTypeChoices.getSelectionModel().selectFirst();

        classLabel.setText(getTranslation(APPLICATION_SECTION, "Class"));
        fillFateClass(classChoices);

        targetLabel.setText(getTranslation(APPLICATION_SECTION, "Target"));

        fillTargets(targetChoices);

        subConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Sub-conditions"));

        cancelButton.setText(getTranslation(APPLICATION_SECTION, "Cancel"));

        buildButton.setText(getTranslation(APPLICATION_SECTION, "Build"));

        resetPane();

        subConditionList.setCellFactory(new SubConditionCellFactory(errorLabel));
        subConditionList.setItems(FXCollections.observableArrayList());

        addListItemButton.setText(getTranslation(APPLICATION_SECTION, "Add sub condition"));

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
    }

    @FXML
    public void onConditionChoicesChange() {
        errorLabel.setVisible(false);

        resetPane();

        requiredFields = CONDITION_REQUIRED_FIELD_MAP.get(conditionChoices.getValue());

        if (requiredFields.contains(CONDITION_FIELD_INT_VALUE) ||
                requiredFields.contains(CONDITION_FIELD_DOUBLE_VALUE) ||
                requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE) ||
                requiredFields.contains(CONDITION_FIELD_SERVANT)
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
                valueLabel.setText(getTranslation(APPLICATION_SECTION, "String Value"));
                valueText.textProperty().addListener(valueTextListener);
            } else {
                valueLabel.setText(getTranslation(APPLICATION_SECTION, "Servant ID"));
                valueText.textProperty().addListener(valueTextListener);
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
    }

    @FXML
    public void onAddListItemButtonClick() {
        try {
            final ConditionData.Builder builder = ConditionData.newBuilder();
            createCondition(addListItemButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                subConditionList.getItems().add(builder.build());
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
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
            } else if (requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE) || requiredFields.contains(CONDITION_FIELD_SERVANT)) {
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
                conditionDataBuilder.setValue(cardTypeChoices.getValue().name());
            } else if (requiredFields.contains(CONDITION_FIELD_CLASS_VALUE)) {
                conditionDataBuilder.setValue(classChoices.getValue().name());
            }
            
            if (requiredFields.contains(CONDITION_FIELD_TARGET)) {
                conditionDataBuilder.setTarget(targetChoices.getValue());
            }
            if (requiredFields.contains(CONDITION_FIELD_UNLIMITED_SUB_CONDITION) ||
                    requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION)) {
                if (requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION) && subConditionList.getItems().size() != 1) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Only one sub-condition allowed"));
                    return;
                }
                conditionDataBuilder.addAllSubConditionData(subConditionList.getItems());
            }

            conditionDataBuilder.setType(conditionChoices.getValue());
        }

        final Stage stage = (Stage) buildButton.getScene().getWindow();
        stage.close();
    }
}
