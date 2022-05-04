package yome.fgo.simulator.gui.creators;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
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
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.gui.components.TranslationConverter;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.data.writer.DataWriter.generateSkillValues;
import static yome.fgo.simulator.gui.components.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.components.DataPrinter.printVariationData;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.creators.VariationBuilder.createVariation;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.addSplitTraitListener;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.fillClassAdvMode;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.fillCommandCardType;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_FIELD_CLASS_ADV;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_FIELD_EFFECTS;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_FIELD_PERCENT_OPTION;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_FIELD_STRING_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_REQUIRED_FIELDS_MAP;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasKeyForTrait;

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
    private ChoiceBox<CommandCardType> cardTypeChoices;

    @FXML
    private Label cardTypeLabel;

    @FXML
    private HBox cardTypePane;

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

    private Set<Integer> requiredFields;

    private BuffData.Builder buffDataBuilder;
    private TextField generateTargetTextField;
    private ConditionData applyCondition;
    private VariationData variationData;

    public void setParentBuilder(final BuffData.Builder buffDataBuilder) {
        this.buffDataBuilder = buffDataBuilder;

        resetPane();

        // TODO fill from parent
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        resetPane();

        buffTypeLabel.setText(getTranslation(APPLICATION_SECTION, "Buff Type"));
        buffTypeChoices.setConverter(new TranslationConverter(BUFF_SECTION));
        buffTypeChoices.setItems(FXCollections.observableArrayList(BUFF_REQUIRED_FIELDS_MAP.keySet()));
        buffTypeChoices.setOnAction(e -> onBuffTypeChoiceChange());
        buffTypeChoices.getSelectionModel().selectFirst();

        numTurnCheckbox.setText(getTranslation(APPLICATION_SECTION, "Num Turns Active"));
        numTurnText.setDisable(true);
        numTurnCheckbox.setOnAction(e -> numTurnText.setDisable(!numTurnCheckbox.isSelected()));

        numTimesCheckbox.setText(getTranslation(APPLICATION_SECTION, "Num Times Active"));
        numTimesText.setDisable(true);
        numTimesCheckbox.setOnAction(e -> numTimesText.setDisable(!numTimesCheckbox.isSelected()));

        irremovableCheckbox.setText(getTranslation(APPLICATION_SECTION, "Irremovable"));
        forceStackCheckbox.setText(getTranslation(APPLICATION_SECTION, "Force Stackable"));

        probabilityCheckbox.setText(getTranslation(APPLICATION_SECTION, "Custom Probability (%)"));
        probabilityText.setDisable(true);
        probabilityGenerateButton.setDisable(true);
        probabilityCheckbox.setOnAction(e -> {
            probabilityText.setDisable(!probabilityCheckbox.isSelected());
            probabilityGenerateButton.setDisable(!probabilityCheckbox.isSelected());
        });
        probabilityGenerateButton.setText(getTranslation(APPLICATION_SECTION, "Autofill"));
        probabilityGenerateButton.setOnAction(e -> {
            generateTargetTextField = probabilityText;
            generateValuePane.setVisible(true);
        });

        traitCheckbox.setText(getTranslation(APPLICATION_SECTION, "Custom Buff Trait"));
        traitText.setDisable(true);
        traitCheckbox.setOnAction(e -> traitText.setDisable(!traitCheckbox.isSelected()));
        addSplitTraitListener(traitText, errorLabel);

        conditionCheckbox.setText(getTranslation(APPLICATION_SECTION, "Apply Condition"));
        conditionEditButton.setDisable(true);
        builtConditionLabel.setDisable(true);
        builtConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Leave unchecked to always apply"));
        conditionCheckbox.setOnAction(e -> {
            conditionEditButton.setDisable(!conditionCheckbox.isSelected());
            builtConditionLabel.setDisable(!conditionCheckbox.isSelected());
        });
        conditionEditButton.setText(getTranslation(APPLICATION_SECTION, "Edit"));
        conditionEditButton.setOnAction(e -> editCondition());

        generateValuesButton.setText(getTranslation(APPLICATION_SECTION, "Autofill"));
        generateValuesButton.setOnAction(e -> {
            generateTargetTextField = valuesText;
            generateValuePane.setVisible(true);
        });

        useVariationCheckbox.setText(getTranslation(APPLICATION_SECTION, "Use Variation"));
        generateVariationsButton.setDisable(true);
        variationAdditionText.setDisable(true);
        editVariationButton.setDisable(true);
        builtVariationLabel.setDisable(true);
        useVariationCheckbox.setOnAction(e -> {
            generateVariationsButton.setDisable(!useVariationCheckbox.isSelected());
            variationAdditionText.setDisable(!useVariationCheckbox.isSelected());
            editVariationButton.setDisable(!useVariationCheckbox.isSelected());
            builtVariationLabel.setDisable(!useVariationCheckbox.isSelected());
        });
        generateVariationsButton.setText(getTranslation(APPLICATION_SECTION, "Autofill"));
        generateVariationsButton.setOnAction(e -> {
            generateTargetTextField = variationAdditionText;
            generateValuePane.setVisible(true);
        });
        editVariationButton.setText(getTranslation(APPLICATION_SECTION, "Edit"));
        builtVariationLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));
        editVariationButton.setOnAction(e -> editVariation());

        stringValueLabel.setText(getTranslation(APPLICATION_SECTION, "String Value"));
        stringValueText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !hasKeyForTrait(newValue)) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Warning: unmapped traits:") + newValue);
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        });

        effectsLabel.setText(getTranslation(APPLICATION_SECTION, "Effects"));
        addEffectsButton.setText(getTranslation(APPLICATION_SECTION, "Add Effect"));

        gutsLabel.setText(getTranslation(APPLICATION_SECTION, "Value"));
        gutsPercentCheckbox.setText(getTranslation(APPLICATION_SECTION, "Value (%)"));
        gutsPercentText.setDisable(true);
        gutsPercentCheckbox.setOnAction(e -> {
            gutsPercentText.setDisable(!gutsPercentCheckbox.isSelected());
            gutsIntText.setDisable(gutsPercentCheckbox.isSelected());
            gutsLabel.setDisable(gutsPercentCheckbox.isSelected());
        });

        final ChangeListener<String> classListener = (observable, oldValue, newValue) -> {
            final List<String> unmappedTraits = Arrays.stream(newValue.split(COMMA_SPLIT_REGEX))
                    .sequential()
                    .filter(s -> !s.isEmpty() && !hasKeyForTrait(s))
                    .collect(Collectors.toList());

            if (!unmappedTraits.isEmpty()) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Warning: unmapped classes:") + unmappedTraits);
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        };

        classAdvAtkLabel.setText(getTranslation(APPLICATION_SECTION, "Attack Affinity"));
        fillClassAdvMode(classAdvChoicesAtk);
        classAdvCustomAtkCheckbox.setText(getTranslation(APPLICATION_SECTION, "Custom Affinity Value"));
        classAdvCustomAtkText.setDisable(true);
        classAdvCustomAtkCheckbox.setOnAction(e -> classAdvCustomAtkText.setDisable(!classAdvCustomAtkCheckbox.isSelected()));
        classAdvAtkTargetClassLabel.setText(getTranslation(APPLICATION_SECTION, "Target Class"));
        classAdvAtkTargetClassText.textProperty().addListener(classListener);

        classAdvDefLabel.setText(getTranslation(APPLICATION_SECTION, "Defense Affinity"));
        fillClassAdvMode(classAdvChoicesDef);
        classAdvCustomDefCheckbox.setText(getTranslation(APPLICATION_SECTION, "Custom Affinity Value"));
        classAdvCustomDefText.setDisable(true);
        classAdvCustomDefCheckbox.setOnAction(e -> classAdvCustomDefText.setDisable(!classAdvCustomDefCheckbox.isSelected()));
        classAdvDefTargetClassLabel.setText(getTranslation(APPLICATION_SECTION, "Target Class"));
        classAdvDefTargetClassText.textProperty().addListener(classListener);

        cardTypeLabel.setText(getTranslation(APPLICATION_SECTION, "Card Type"));
        fillCommandCardType(cardTypeChoices);

        errorLabel.setVisible(false);
        cancelButton.setText(getTranslation(APPLICATION_SECTION, "Cancel"));
        cancelButton.setOnAction(e -> onCancelButtonClick());
        buildButton.setText(getTranslation(APPLICATION_SECTION, "Build"));
        buildButton.setOnAction(e -> onBuildButtonClick());

        generateValuePane.setVisible(false);
        generateValueBaseLabel.setText(getTranslation(APPLICATION_SECTION, "Base"));
        generateValueStepLabel.setText(getTranslation(APPLICATION_SECTION, "Step"));
        generateValueCancelButton.setText(getTranslation(APPLICATION_SECTION, "Cancel"));
        generateValueCancelButton.setOnAction(e -> generateValuePane.setVisible(false));
        generateValueGenerateButton.setText(getTranslation(APPLICATION_SECTION, "Generate"));
        generateValueGenerateButton.setOnAction(e -> {
            final double base;
            final double step;
            try {
                base = Double.parseDouble(generateValueBaseText.getText());
                step = Double.parseDouble(generateValueStepText.getText());

                final List<Double> values = generateSkillValues(base, step);
                final StringBuilder builder = new StringBuilder();
                builder.append(values.get(0));
                for (int i = 1; i < values.size(); i++) {
                    builder.append(", ").append(values.get(i));
                }
                generateTargetTextField.setText(builder.toString());
            } catch (final Exception ignored) {
            }
            generateValuePane.setVisible(false);
        });
    }

    public void resetPane() {
        valuePane.setVisible(false);
        valuePane.setManaged(false);

        stringValuePane.setVisible(false);
        stringValuePane.setManaged(false);

        effectsPane.setVisible(false);
        effectsPane.setManaged(false);

        gutsPane.setVisible(false);
        gutsPane.setManaged(false);

        classAdvPane.setVisible(false);
        classAdvPane.setManaged(false);

        cardTypePane.setVisible(false);
        cardTypePane.setManaged(false);
    }

    public void editVariation() {
        try {
            final VariationData.Builder builder = variationData == null ? VariationData.newBuilder() : variationData.toBuilder();
            createVariation(editVariationButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                variationData = builder.build();
                builtVariationLabel.setText(printVariationData(variationData));
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
        }
    }

    public void editCondition() {
        try {
            final ConditionData.Builder builder = applyCondition == null ? ConditionData.newBuilder() : applyCondition.toBuilder();
            createCondition(conditionEditButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                applyCondition = builder.build();
                builtConditionLabel.setText(printConditionData(applyCondition));
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
        }
    }

    public void onBuffTypeChoiceChange() {
        resetPane();
        requiredFields = BUFF_REQUIRED_FIELDS_MAP.get(buffTypeChoices.getValue());

        if (requiredFields.contains(BUFF_FIELD_DOUBLE_VALUE)) {
            valuePane.setVisible(true);
            valuePane.setManaged(true);
            valueLabel.setText(getTranslation(APPLICATION_SECTION, "Value (%)"));
        }
        if (requiredFields.contains(BUFF_FIELD_INT_VALUE)) {
            valuePane.setVisible(true);
            valuePane.setManaged(true);
            valueLabel.setText(getTranslation(APPLICATION_SECTION, "Value"));
        }
        if (requiredFields.contains(BUFF_FIELD_STRING_VALUE)) {
            stringValuePane.setVisible(true);
            stringValuePane.setManaged(true);
        }
        if (requiredFields.contains(BUFF_FIELD_EFFECTS)) {
            effectsPane.setVisible(true);
            effectsPane.setManaged(true);
        }
        if (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION)) {
            gutsPane.setVisible(true);
            gutsPane.setManaged(true);
        }
        if (requiredFields.contains(BUFF_FIELD_CLASS_ADV)) {
            classAdvPane.setVisible(true);
            classAdvPane.setManaged(true);
        }
        if (requiredFields.contains(BUFF_FIELD_CARD_TYPE)) {
            cardTypePane.setVisible(true);
            cardTypePane.setManaged(true);
        }
    }

    public void onCancelButtonClick() {
        if (buffDataBuilder != null) {
            buffDataBuilder.clearType();
        }

        final Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void onBuildButtonClick() {
        if (buffDataBuilder != null) {
            // TODO build buff
        }

        final Stage stage = (Stage) buildButton.getScene().getWindow();
        stage.close();
    }
}
