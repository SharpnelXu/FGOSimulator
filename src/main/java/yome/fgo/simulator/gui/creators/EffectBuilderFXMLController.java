package yome.fgo.simulator.gui.creators;

import com.google.common.collect.ImmutableList;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.gui.components.BuffsCellFactory;
import yome.fgo.simulator.gui.components.EffectsCellFactory;
import yome.fgo.simulator.gui.components.TranslationConverter;
import yome.fgo.simulator.models.effects.EffectFactory.EffectFields;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.UNRECOGNIZED;
import static yome.fgo.data.writer.DataWriter.generateSkillValues;
import static yome.fgo.simulator.gui.components.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.components.DataPrinter.printVariationData;
import static yome.fgo.simulator.gui.creators.BuffBuilder.createBuff;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.gui.creators.VariationBuilder.createVariation;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.fillTargets;
import static yome.fgo.simulator.models.effects.EffectFactory.EFFECT_REQUIRED_FIELDS_MAP;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_CARD_TYPE_SELECT;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_GRANT_BUFF;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_HP_CHANGE;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_NP_DAMAGE;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_RANDOM_SELECTION;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_REMOVE_BUFF;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_TARGET;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class EffectBuilderFXMLController implements Initializable {

    @FXML
    private Button addBuffButton;

    @FXML
    private Button addEffectsButton;

    @FXML
    private Label buffsLabel;

    @FXML
    private ListView<BuffData> buffsList;

    @FXML
    private HBox buffsPane;

    @FXML
    private Button buildButton;

    @FXML
    private Label builtConditionLabel;

    @FXML
    private Label builtNpSPDConditionLabel;

    @FXML
    private Label builtNpSPDVariationLabel;

    @FXML
    private Label builtVariationLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Label cardTypeLabel;

    @FXML
    private HBox cardTypePane;

    @FXML
    private CheckBox conditionCheckbox;

    @FXML
    private Button conditionEditButton;

    @FXML
    private Button editNpSPDConditionButton;

    @FXML
    private Button editNpSPDVariationButton;

    @FXML
    private Button editVariationButton;

    @FXML
    private ChoiceBox<String> effectTypeChoices;

    @FXML
    private Label effectTypeLabel;

    @FXML
    private Label effectsLabel;

    @FXML
    private ListView<EffectData> effectsList;

    @FXML
    private HBox effectsPane;

    @FXML
    private Label errorLabel;

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
    private CheckBox hasNpSPD;

    @FXML
    private CheckBox hpDrainLethalCheckbox;

    @FXML
    private HBox hpPane;

    @FXML
    private CheckBox hpPercentCheckbox;

    @FXML
    private CheckBox isAdditionOvercharged;

    @FXML
    private CheckBox isNpDamageOvercharged;

    @FXML
    private CheckBox isNpIgnoreDefense;

    @FXML
    private CheckBox isNpSPDOvercharged;

    @FXML
    private CheckBox isNpSPDVariationOvercharged;

    @FXML
    private CheckBox isOverchargedEffect;

    @FXML
    private CheckBox isProbabilityOvercharged;

    @FXML
    private TextField npOverchargedRatesText;

    @FXML
    private VBox npPane;

    @FXML
    private TextField npSPDText;

    @FXML
    private CheckBox npSPDVariationCheckbox;

    @FXML
    private TextField npSPDVariationText;

    @FXML
    private CheckBox probabilityCheckbox;

    @FXML
    private Button probabilityGenerateButton;

    @FXML
    private TextField probabilityText;

    @FXML
    private CheckBox removeFromStartCheckbox;

    @FXML
    private HBox targetPane;

    @FXML
    private ChoiceBox<Target> targetChoice;

    @FXML
    private Label targetLabel;

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

    private EffectData.Builder effectDataBuilder;
    private Set<EffectFields> requiredFields;
    private TextField generateTargetTextField;
    private ConditionData applyCondition;
    private VariationData variationData;
    private ConditionData npSpdCondition;
    private VariationData npSpdVariation;
    private List<CheckBox> cardTypeCheckboxes;

    private List<Pane> optionalPanes;

    public void setParentBuilder(final EffectData.Builder builder) {
        this.effectDataBuilder = builder;
        // TODO populate fields based on builder
    }

    public static void setPaneVisAndManaged(final List<Pane> panes, final boolean bool) {
        for (final Pane pane : panes) {
            setPaneVisAndManaged(pane, bool);
        }
    }

    public static void setPaneVisAndManaged(final Pane pane, final boolean bool) {
        pane.setVisible(bool);
        pane.setManaged(bool);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        optionalPanes = ImmutableList.of(buffsPane, effectsPane, hpPane, npPane, targetPane, valuePane, cardTypePane, generateValuePane);
        setPaneVisAndManaged(optionalPanes, false);
        removeFromStartCheckbox.setVisible(false);
        removeFromStartCheckbox.setManaged(false);

        effectTypeLabel.setText(getTranslation(APPLICATION_SECTION, "Effect Type"));
        effectTypeChoices.setConverter(new TranslationConverter(EFFECT_SECTION));
        effectTypeChoices.setItems(FXCollections.observableArrayList(EFFECT_REQUIRED_FIELDS_MAP.keySet()));
        effectTypeChoices.setOnAction(e -> onEffectTypeChoiceChange());
        effectTypeChoices.getSelectionModel().selectFirst();

        isOverchargedEffect.setText(getTranslation(APPLICATION_SECTION, "Is OC Effect"));
        probabilityCheckbox.setText(getTranslation(APPLICATION_SECTION, "Custom Probability (%)"));
        isProbabilityOvercharged.setText(getTranslation(APPLICATION_SECTION, "Is Probability OC"));
        isProbabilityOvercharged.setDisable(true);
        probabilityText.setDisable(true);
        probabilityGenerateButton.setDisable(true);
        probabilityCheckbox.setOnAction(e -> {
            probabilityText.setDisable(!probabilityCheckbox.isSelected());
            isProbabilityOvercharged.setDisable(!probabilityCheckbox.isSelected());
            probabilityGenerateButton.setDisable(!probabilityCheckbox.isSelected());
        });
        probabilityGenerateButton.setText(getTranslation(APPLICATION_SECTION, "Autofill"));
        probabilityGenerateButton.setOnAction(e -> {
            generateTargetTextField = probabilityText;
            generateValuePane.setVisible(true);
        });

        conditionCheckbox.setText(getTranslation(APPLICATION_SECTION, "Apply Condition"));
        conditionEditButton.setDisable(true);
        builtConditionLabel.setDisable(true);
        builtConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Leave unchecked to always apply"));
        conditionCheckbox.setOnAction(e -> {
            conditionEditButton.setDisable(!conditionCheckbox.isSelected());
            builtConditionLabel.setDisable(!conditionCheckbox.isSelected());
        });
        conditionEditButton.setText(getTranslation(APPLICATION_SECTION, "Edit"));
        conditionEditButton.setOnAction(e -> applyCondition = editCondition(applyCondition, builtConditionLabel));

        targetLabel.setText(getTranslation(APPLICATION_SECTION, "Target"));
        fillTargets(targetChoice);

        generateValuesButton.setText(getTranslation(APPLICATION_SECTION, "Autofill"));
        generateValuesButton.setOnAction(e -> {
            generateTargetTextField = valuesText;
            generateValuePane.setVisible(true);
        });

        useVariationCheckbox.setText(getTranslation(APPLICATION_SECTION, "Use Variation"));
        isAdditionOvercharged.setText(getTranslation(APPLICATION_SECTION, "Is Addition OC"));
        isAdditionOvercharged.setDisable(true);
        generateVariationsButton.setDisable(true);
        variationAdditionText.setDisable(true);
        editVariationButton.setDisable(true);
        builtVariationLabel.setDisable(true);
        useVariationCheckbox.setOnAction(e -> {
            isAdditionOvercharged.setDisable(!useVariationCheckbox.isSelected());
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
        editVariationButton.setOnAction(e -> variationData = editVariation(variationData, builtVariationLabel));

        isNpIgnoreDefense.setText(getTranslation(APPLICATION_SECTION, "Ignore Defense"));

        isNpDamageOvercharged.setText(getTranslation(APPLICATION_SECTION, "Is NP Damage OC"));
        npOverchargedRatesText.setDisable(true);
        isNpDamageOvercharged.setOnAction(e -> npOverchargedRatesText.setDisable(!isNpDamageOvercharged.isSelected()));

        hasNpSPD.setText(getTranslation(APPLICATION_SECTION, "Np Specific Damage"));
        isNpSPDOvercharged.setDisable(true);
        npSPDText.setDisable(true);
        editNpSPDConditionButton.setDisable(true);
        builtNpSPDConditionLabel.setDisable(true);
        npSPDVariationCheckbox.setDisable(true);
        isNpSPDVariationOvercharged.setDisable(true);
        npSPDVariationText.setDisable(true);
        editNpSPDVariationButton.setDisable(true);
        builtNpSPDVariationLabel.setDisable(true);
        hasNpSPD.setOnAction(e -> {
            isNpSPDOvercharged.setDisable(!hasNpSPD.isSelected());
            npSPDText.setDisable(!hasNpSPD.isSelected());
            editNpSPDConditionButton.setDisable(!hasNpSPD.isSelected());
            builtNpSPDConditionLabel.setDisable(!hasNpSPD.isSelected());
            npSPDVariationCheckbox.setDisable(!hasNpSPD.isSelected());
            isNpSPDVariationOvercharged.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            npSPDVariationText.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            editNpSPDVariationButton.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            builtNpSPDVariationLabel.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
        });
        isNpSPDOvercharged.setText(getTranslation(APPLICATION_SECTION, "Is Np SPD OC"));
        editNpSPDConditionButton.setText(getTranslation(APPLICATION_SECTION, "Edit SPD Condition"));
        editNpSPDConditionButton.setOnAction(e -> npSpdCondition = editCondition(npSpdCondition, builtNpSPDConditionLabel));
        builtNpSPDConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));
        npSPDVariationCheckbox.setText(getTranslation(APPLICATION_SECTION, "Use Variation"));
        npSPDVariationCheckbox.setOnAction(e -> {
            isNpSPDVariationOvercharged.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            npSPDVariationText.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            editNpSPDVariationButton.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            builtNpSPDVariationLabel.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
        });
        isNpSPDVariationOvercharged.setText(getTranslation(APPLICATION_SECTION, "Is NP SPD Addition OC"));
        editNpSPDVariationButton.setText(getTranslation(APPLICATION_SECTION, "Edit SPD Variation"));
        editNpSPDVariationButton.setOnAction(e -> npSpdVariation = editVariation(npSpdVariation, builtNpSPDVariationLabel));
        builtNpSPDVariationLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));

        effectsLabel.setText(getTranslation(APPLICATION_SECTION, "Effects"));
        addEffectsButton.setText(getTranslation(APPLICATION_SECTION, "Add Effect"));
        addEffectsButton.setOnAction(e -> addEffect());
        effectsList.setCellFactory(new EffectsCellFactory());
        effectsList.setItems(FXCollections.observableArrayList());

        buffsLabel.setText(getTranslation(APPLICATION_SECTION, "Buffs"));
        addBuffButton.setText(getTranslation(APPLICATION_SECTION, "Add Buff"));
        addBuffButton.setOnAction(e -> addBuff());
        buffsList.setCellFactory(new BuffsCellFactory());
        buffsList.setItems(FXCollections.observableArrayList());

        hpPercentCheckbox.setText(getTranslation(APPLICATION_SECTION, "Set as percent"));
        hpPercentCheckbox.setOnAction(e -> {
            final String string = hpPercentCheckbox.isSelected() ? "Value (%)" : "Value";
            valueLabel.setText(getTranslation(APPLICATION_SECTION, string));
        });
        hpDrainLethalCheckbox.setText(getTranslation(APPLICATION_SECTION, "Is lethal on HP drain"));

        cardTypeLabel.setText(getTranslation(APPLICATION_SECTION, "Card Types"));
        final List<CommandCardType> cardTypes = ImmutableList.of(QUICK, ARTS, BUSTER);
        cardTypeCheckboxes = new ArrayList<>();
        for (final CommandCardType cardType : cardTypes) {
            final CheckBox checkBox = new CheckBox(getTranslation(COMMAND_CARD_TYPE_SECTION, cardType.name()));
            cardTypeCheckboxes.add(checkBox);
            cardTypePane.getChildren().add(checkBox);
        }

        removeFromStartCheckbox.setText(getTranslation(APPLICATION_SECTION, "Remove from earliest"));

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
                generateTargetTextField.setText(
                        values.stream()
                                .map(d -> Double.toString(d))
                                .collect(Collectors.joining(", "))
                );
            } catch (final Exception ignored) {
            }
            generateValuePane.setVisible(false);
        });
    }

    public void addBuff() {
        try {
            final BuffData.Builder builder = BuffData.newBuilder();
            createBuff(addEffectsButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                buffsList.getItems().add(builder.build());
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
        }
    }

    public void addEffect() {
        try {
            final EffectData.Builder builder = EffectData.newBuilder();
            createEffect(addEffectsButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                effectsList.getItems().add(builder.build());
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
        }
    }

    public ConditionData editCondition(final ConditionData applyCondition, final Label builtLabel) {
        try {
            final ConditionData.Builder builder = applyCondition == null ? ConditionData.newBuilder() : applyCondition.toBuilder();
            createCondition(conditionEditButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                final ConditionData builtCondition = builder.build();
                builtLabel.setText(printConditionData(builtCondition));
                return builtCondition;
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
        }
        return applyCondition;
    }

    public VariationData editVariation(final VariationData variationData, final Label builtLabel) {
        try {
            final VariationData.Builder builder = variationData == null ? VariationData.newBuilder() : variationData.toBuilder();
            createVariation(editVariationButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                final VariationData builtVariation = builder.build();
                builtLabel.setText(printVariationData(builtVariation));
                return builtVariation;
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
        }
        return variationData;
    }

    public void onEffectTypeChoiceChange() {
        setPaneVisAndManaged(optionalPanes, false);
        removeFromStartCheckbox.setVisible(false);
        removeFromStartCheckbox.setManaged(false);

        // TODO show pieces
        requiredFields = EFFECT_REQUIRED_FIELDS_MAP.get(effectTypeChoices.getValue());
        if (requiredFields.contains(EFFECT_FIELD_TARGET)) {
            setPaneVisAndManaged(targetPane, true);
        }
        if (requiredFields.contains(EFFECT_FIELD_INT_VALUE)) {
            valueLabel.setText(getTranslation(APPLICATION_SECTION, "Value"));
            setPaneVisAndManaged(valuePane, true);
        }
        if (requiredFields.contains(EFFECT_FIELD_DOUBLE_VALUE)) {
            valueLabel.setText(getTranslation(APPLICATION_SECTION, "Value (%)"));
            setPaneVisAndManaged(valuePane, true);
        }
        if (requiredFields.contains(EFFECT_FIELD_NP_DAMAGE)) {
            setPaneVisAndManaged(npPane, true);
        }
        if (requiredFields.contains(EFFECT_FIELD_GRANT_BUFF)) {
            setPaneVisAndManaged(buffsPane, true);
        }
        if (requiredFields.contains(EFFECT_FIELD_CARD_TYPE_SELECT)) {
            setPaneVisAndManaged(cardTypePane, true);
        }
        if (requiredFields.contains(EFFECT_FIELD_HP_CHANGE)) {
            final String string = hpPercentCheckbox.isSelected() ? "Value (%)" : "Value";
            valueLabel.setText(getTranslation(APPLICATION_SECTION, string));
            setPaneVisAndManaged(valuePane, true);
            setPaneVisAndManaged(hpPane, true);
        }
        if (requiredFields.contains(EFFECT_FIELD_REMOVE_BUFF)) {
            removeFromStartCheckbox.setVisible(true);
            removeFromStartCheckbox.setManaged(true);
        }
        if (requiredFields.contains(EFFECT_FIELD_RANDOM_SELECTION)) {
            setPaneVisAndManaged(effectsPane, true);
        }
    }


    public void onCancelButtonClick() {
        if (effectDataBuilder != null) {
            effectDataBuilder.clearType();
        }

        final Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void onBuildButtonClick() {
        if (effectDataBuilder != null) {
            // TODO build
        }

        final Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
