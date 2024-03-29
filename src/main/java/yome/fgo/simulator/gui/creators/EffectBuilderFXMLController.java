package yome.fgo.simulator.gui.creators;

import com.google.common.collect.ImmutableList;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.NpDamageAdditionalParams;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.gui.components.ListContainerVBox;
import yome.fgo.simulator.gui.components.ListContainerVBox.Mode;
import yome.fgo.simulator.gui.components.TranslationConverter;
import yome.fgo.simulator.models.effects.Effect.EffectFields;
import yome.fgo.simulator.models.effects.Effect.EffectType;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.data.writer.DataWriter.generateSkillValues;
import static yome.fgo.simulator.gui.creators.BuffBuilderFXMLController.doublesToString;
import static yome.fgo.simulator.gui.creators.BuffBuilderFXMLController.intsToString;
import static yome.fgo.simulator.gui.creators.BuffBuilderFXMLController.parseDoubles;
import static yome.fgo.simulator.gui.creators.BuffBuilderFXMLController.parseInts;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.creators.VariationBuilder.createVariation;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createTooltip;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillTargets;
import static yome.fgo.simulator.gui.helpers.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.helpers.DataPrinter.printVariationData;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_CARD_TYPE_SELECT;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_GRANT_BUFF;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_HP_CHANGE;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_NP_DAMAGE;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_RANDOM_EFFECT;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_REMOVE_BUFF;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_TARGET;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CARD_TYPE_CHANGE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.NP_CARD_TYPE_CHANGE;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.SELECTABLE_CARD_TYPES;

public class EffectBuilderFXMLController implements Initializable {
    @FXML
    private VBox buffsPane;

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
    private VBox effectsPane;

    @FXML
    private Label effectTypeLabel;

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

    private List<Pane> optionalPanes;
    private ListContainerVBox buffs;
    private ListContainerVBox effects;
    private List<CheckBox> cardTypeCheckBoxes;

    public void setParentBuilder(final EffectData.Builder builder) {
        this.effectDataBuilder = builder;
        if (!effectDataBuilder.getType().isEmpty()) {
            requiredFields = EffectType.ofType(effectDataBuilder.getType()).getRequiredFields();
            effectTypeChoices.getSelectionModel().select(effectDataBuilder.getType());

            isOverchargedEffect.setSelected(effectDataBuilder.getIsOverchargedEffect());
            if (effectDataBuilder.getProbabilitiesCount() > 0) {
                probabilityCheckbox.setSelected(true);
                probabilityCheckbox.fireEvent(new ActionEvent());
                isProbabilityOvercharged.setSelected(effectDataBuilder.getIsProbabilityOvercharged());
                probabilityText.setText(doublesToString(effectDataBuilder.getProbabilitiesList()));
            }
            if (effectDataBuilder.hasApplyCondition()) {
                conditionCheckbox.setSelected(true);
                conditionCheckbox.fireEvent(new ActionEvent());
                applyCondition = effectDataBuilder.getApplyCondition();
                builtConditionLabel.setText(printConditionData(applyCondition));
            }

            if (requiredFields.contains(EFFECT_FIELD_TARGET)) {
                targetChoice.getSelectionModel().select(effectDataBuilder.getTarget());
            }
            if (requiredFields.contains(EFFECT_FIELD_INT_VALUE) ||
                    (requiredFields.contains(EFFECT_FIELD_HP_CHANGE) && !effectDataBuilder.getIsHpChangePercentBased())) {
                valuesText.setText(
                        intsToString(
                                effectDataBuilder.getIntValuesList()
                                        .stream()
                                        .map(Integer::doubleValue)
                                        .collect(Collectors.toList())
                        )
                );
                if (effectDataBuilder.hasVariationData()) {
                    useVariationCheckbox.setSelected(true);
                    useVariationCheckbox.fireEvent(new ActionEvent());
                    variationData = effectDataBuilder.getVariationData();
                    builtVariationLabel.setText(printVariationData(variationData));
                    variationAdditionText.setText(intsToString(effectDataBuilder.getAdditionsList()));
                }
            }
            if (requiredFields.contains(EFFECT_FIELD_DOUBLE_VALUE) ||
                    (requiredFields.contains(EFFECT_FIELD_HP_CHANGE) && effectDataBuilder.getIsHpChangePercentBased())) {
                valuesText.setText(doublesToString(effectDataBuilder.getValuesList()));
                if (effectDataBuilder.hasVariationData()) {
                    useVariationCheckbox.setSelected(true);
                    useVariationCheckbox.fireEvent(new ActionEvent());
                    variationData = effectDataBuilder.getVariationData();
                    builtVariationLabel.setText(printVariationData(variationData));
                    variationAdditionText.setText(doublesToString(effectDataBuilder.getAdditionsList()));
                }
            }
            if (requiredFields.contains(EFFECT_FIELD_NP_DAMAGE) && effectDataBuilder.hasNpDamageAdditionalParams()) {
                final NpDamageAdditionalParams additionalParams = effectDataBuilder.getNpDamageAdditionalParams();
                isNpIgnoreDefense.setSelected(additionalParams.getIsNpIgnoreDefense());

                if (additionalParams.getNpOverchargeDamageRateCount() > 0) {
                    isNpDamageOvercharged.setSelected(true);
                    isNpDamageOvercharged.fireEvent(new ActionEvent());
                    npOverchargedRatesText.setText(doublesToString(additionalParams.getNpOverchargeDamageRateList()));
                }

                if (additionalParams.hasNpSpecificDamageCondition()) {
                    hasNpSPD.setSelected(true);
                    hasNpSPD.fireEvent(new ActionEvent());
                    npSpdCondition = additionalParams.getNpSpecificDamageCondition();
                    builtNpSPDConditionLabel.setText(printConditionData(npSpdCondition));
                    isNpSPDOvercharged.setSelected(additionalParams.getIsNpSpecificDamageOverchargedEffect());
                    npSPDText.setText(doublesToString(additionalParams.getNpSpecificDamageRateList()));

                    if (additionalParams.hasNpSpecificDamageVariation()) {
                        npSPDVariationCheckbox.setSelected(true);
                        npSPDVariationCheckbox.fireEvent(new ActionEvent());
                        npSpdVariation = additionalParams.getNpSpecificDamageVariation();
                        builtNpSPDVariationLabel.setText(printVariationData(npSpdVariation));
                        isNpSPDVariationOvercharged.setSelected(additionalParams.getIsNpSpecificDamageAdditionOvercharged());
                        npSPDVariationText.setText(doublesToString(additionalParams.getNpSpecificDamageAdditionsList()));
                    }
                }
            }
            if (requiredFields.contains(EFFECT_FIELD_GRANT_BUFF)) {
                buffs.loadBuff(builder.getBuffDataList());
            }
            if (requiredFields.contains(EFFECT_FIELD_HP_CHANGE)) {
                hpPercentCheckbox.setSelected(effectDataBuilder.getIsHpChangePercentBased());
                hpDrainLethalCheckbox.setSelected(effectDataBuilder.getIsLethal());
            }
            if (requiredFields.contains(EFFECT_FIELD_REMOVE_BUFF)) {
                removeFromStartCheckbox.setSelected(effectDataBuilder.getRemoveFromStart());
            }
            if (requiredFields.contains(EFFECT_FIELD_RANDOM_EFFECT)) {
                effects.loadEffect(builder.getEffectDataList());
            }
            if (requiredFields.contains(EFFECT_FIELD_CARD_TYPE_SELECT)) {
                for (final CommandCardType cardType : builder.getCardTypeSelectionsList()) {
                    cardTypeCheckBoxes.get(SELECTABLE_CARD_TYPES.indexOf(cardType)).setSelected(true);
                }
            }
        }
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
        optionalPanes = ImmutableList.of(buffsPane, hpPane, npPane, targetPane, valuePane, effectsPane, cardTypePane);
        setPaneVisAndManaged(optionalPanes, false);
        removeFromStartCheckbox.setVisible(false);
        removeFromStartCheckbox.setManaged(false);

        effectTypeLabel.setText(getTranslation(APPLICATION_SECTION, "Effect Type"));
        effectTypeChoices.setConverter(new TranslationConverter(EFFECT_SECTION));
        effectTypeChoices.setItems(FXCollections.observableArrayList(EffectType.getOrder()));
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
            generateValueBaseText.requestFocus();
        });

        conditionCheckbox.setText(getTranslation(APPLICATION_SECTION, "Apply Condition"));
        conditionEditButton.setDisable(true);
        builtConditionLabel.setDisable(true);
        conditionCheckbox.setOnAction(e -> {
            conditionEditButton.setDisable(!conditionCheckbox.isSelected());
            builtConditionLabel.setDisable(!conditionCheckbox.isSelected());
        });
        conditionEditButton.setGraphic(createInfoImageView("edit"));
        conditionEditButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Edit")));
        conditionEditButton.setOnAction(e -> applyCondition = editCondition(applyCondition, builtConditionLabel));
        conditionEditButton.setText(null);
        builtConditionLabel.setPadding(new Insets(5));
        builtConditionLabel.setMaxWidth(Double.MAX_VALUE);
        builtConditionLabel.setStyle(SPECIAL_INFO_BOX_STYLE);
        builtConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Leave unchecked to always apply"));

        targetLabel.setText(getTranslation(APPLICATION_SECTION, "Target"));
        fillTargets(targetChoice);

        generateValuesButton.setText(getTranslation(APPLICATION_SECTION, "Autofill"));
        generateValuesButton.setOnAction(e -> {
            generateTargetTextField = valuesText;
            generateValuePane.setVisible(true);
            generateValueBaseText.requestFocus();
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
            generateValueBaseText.requestFocus();
        });
        editVariationButton.setGraphic(createInfoImageView("edit"));
        editVariationButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Edit")));
        editVariationButton.setOnAction(e -> variationData = editVariation(variationData, builtVariationLabel));
        editVariationButton.setText(null);
        builtVariationLabel.setPadding(new Insets(5));
        builtVariationLabel.setMaxWidth(Double.MAX_VALUE);
        builtVariationLabel.setStyle(SPECIAL_INFO_BOX_STYLE);
        builtVariationLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));

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
        editNpSPDConditionButton.setGraphic(createInfoImageView("edit"));
        editNpSPDConditionButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Edit SPD Condition")));
        editNpSPDConditionButton.setOnAction(e -> npSpdCondition = editCondition(npSpdCondition, builtNpSPDConditionLabel));
        editNpSPDConditionButton.setText(null);
        builtNpSPDConditionLabel.setPadding(new Insets(5));
        builtNpSPDConditionLabel.setMaxWidth(Double.MAX_VALUE);
        builtNpSPDConditionLabel.setStyle(SPECIAL_INFO_BOX_STYLE);
        builtNpSPDConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));
        npSPDVariationCheckbox.setText(getTranslation(APPLICATION_SECTION, "Use Variation"));
        npSPDVariationCheckbox.setOnAction(e -> {
            isNpSPDVariationOvercharged.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            npSPDVariationText.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            editNpSPDVariationButton.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
            builtNpSPDVariationLabel.setDisable(!hasNpSPD.isSelected() || !npSPDVariationCheckbox.isSelected());
        });
        isNpSPDVariationOvercharged.setText(getTranslation(APPLICATION_SECTION, "Is NP SPD Addition OC"));
        editNpSPDVariationButton.setGraphic(createInfoImageView("edit"));
        editNpSPDVariationButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Edit SPD Variation")));
        editNpSPDVariationButton.setOnAction(e -> npSpdVariation = editVariation(npSpdVariation, builtNpSPDVariationLabel));
        editNpSPDVariationButton.setText(null);
        builtNpSPDVariationLabel.setPadding(new Insets(5));
        builtNpSPDVariationLabel.setMaxWidth(Double.MAX_VALUE);
        builtNpSPDVariationLabel.setStyle(SPECIAL_INFO_BOX_STYLE);
        builtNpSPDVariationLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));

        buffs = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Buffs"), errorLabel, Mode.BUFF);
        buffsPane.getChildren().add(buffs);

        hpPercentCheckbox.setText(getTranslation(APPLICATION_SECTION, "Set as percent"));
        hpPercentCheckbox.setOnAction(e -> {
            final String string = hpPercentCheckbox.isSelected() ? "Value (%)" : "Value";
            valueLabel.setText(getTranslation(APPLICATION_SECTION, string));
        });
        hpDrainLethalCheckbox.setText(getTranslation(APPLICATION_SECTION, "Is lethal on HP drain"));

        removeFromStartCheckbox.setText(getTranslation(APPLICATION_SECTION, "Remove from earliest"));

        effects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Effects"), errorLabel, Mode.EFFECT);
        effectsPane.getChildren().add(effects);

        final Label cardTypeLabel = new Label(getTranslation(APPLICATION_SECTION, "Available Card Type Choices"));
        cardTypePane.getChildren().add(cardTypeLabel);
        cardTypeCheckBoxes = new ArrayList<>();
        for (final CommandCardType cardType : SELECTABLE_CARD_TYPES) {
            final CheckBox checkBox = new CheckBox(getTranslation(COMMAND_CARD_TYPE_SECTION, cardType.name()));
            cardTypeCheckBoxes.add(checkBox);
            cardTypePane.getChildren().add(checkBox);
        }

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
                if (generateTargetTextField == probabilityText || requiredFields.contains(EFFECT_FIELD_DOUBLE_VALUE) ||
                        (requiredFields.contains(EFFECT_FIELD_HP_CHANGE) && hpPercentCheckbox.isSelected())) {
                    generateTargetTextField.setText(
                            values.stream()
                                    .map(d -> Double.toString(d))
                                    .collect(Collectors.joining(", "))
                    );
                } else {
                    generateTargetTextField.setText(
                            values.stream()
                                    .map(d -> Integer.toString((int) d.doubleValue()))
                                    .collect(Collectors.joining(", "))
                    );
                }
            } catch (final Exception ignored) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Base or Step not valid double"));
                errorLabel.setVisible(true);
            }
            generateValuePane.setVisible(false);
        });
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
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
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
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
            errorLabel.setVisible(true);
        }
        return variationData;
    }

    public void onEffectTypeChoiceChange() {
        setPaneVisAndManaged(optionalPanes, false);
        removeFromStartCheckbox.setVisible(false);
        removeFromStartCheckbox.setManaged(false);

        requiredFields = EffectType.ofType(effectTypeChoices.getValue()).getRequiredFields();
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
        if (requiredFields.contains(EFFECT_FIELD_RANDOM_EFFECT)) {
            setPaneVisAndManaged(effectsPane, true);
        }
        if (requiredFields.contains(EFFECT_FIELD_CARD_TYPE_SELECT)) {
            setPaneVisAndManaged(cardTypePane, true);
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
            effectDataBuilder.clear();
            effectDataBuilder.setIsOverchargedEffect(isOverchargedEffect.isSelected());
            if (probabilityCheckbox.isSelected()) {
                effectDataBuilder.setIsProbabilityOvercharged(isProbabilityOvercharged.isSelected());
                try {
                    effectDataBuilder.addAllProbabilities(parseDoubles(probabilityText.getText()));
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Probabilities not double"));
                    return;
                }
            }
            if (conditionCheckbox.isSelected()) {
                if (applyCondition == null || applyCondition.getType().isEmpty()) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Apply condition not set"));
                    return;
                }
                effectDataBuilder.setApplyCondition(applyCondition);
            }

            if (requiredFields.contains(EFFECT_FIELD_TARGET)) {
                effectDataBuilder.setTarget(targetChoice.getValue());
            }
            if (requiredFields.contains(EFFECT_FIELD_INT_VALUE) ||
                    (requiredFields.contains(EFFECT_FIELD_HP_CHANGE) && !hpPercentCheckbox.isSelected())) {
                try {
                    final List<Double> values = parseInts(valuesText.getText());
                    effectDataBuilder.addAllIntValues(values.stream().map(Double::intValue).collect(Collectors.toList()));
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Integer"));
                    return;
                }
                if (useVariationCheckbox.isSelected()) {
                    try {
                        final List<Double> additions = parseInts(variationAdditionText.getText());
                        effectDataBuilder.addAllAdditions(additions);
                    } catch (final Exception e) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Addition not Integer"));
                        return;
                    }
                    if (variationData == null || variationData.getType().isEmpty()) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Variation not set"));
                        return;
                    }
                    effectDataBuilder.setIsAdditionOvercharged(isAdditionOvercharged.isSelected());
                    effectDataBuilder.setVariationData(variationData);
                }
            }
            if (requiredFields.contains(EFFECT_FIELD_DOUBLE_VALUE) ||
                    (requiredFields.contains(EFFECT_FIELD_HP_CHANGE) && hpPercentCheckbox.isSelected())) {
                try {
                    final List<Double> values = parseDoubles(valuesText.getText());
                    effectDataBuilder.addAllValues(values);
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Double"));
                    return;
                }
                if (useVariationCheckbox.isSelected()) {
                    try {
                        final List<Double> additions = parseDoubles(variationAdditionText.getText());
                        effectDataBuilder.addAllAdditions(additions);
                    } catch (final Exception e) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Addition not Double"));
                        return;
                    }
                    if (variationData == null || variationData.getType().isEmpty()) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Variation not set"));
                        return;
                    }
                    effectDataBuilder.setIsAdditionOvercharged(isAdditionOvercharged.isSelected());
                    effectDataBuilder.setVariationData(variationData);
                }
            }
            if (requiredFields.contains(EFFECT_FIELD_NP_DAMAGE)) {
                if (isNpIgnoreDefense.isSelected() || isNpDamageOvercharged.isSelected() || hasNpSPD.isSelected()) {
                    final NpDamageAdditionalParams.Builder additionalParams = NpDamageAdditionalParams.newBuilder();

                    additionalParams.setIsNpIgnoreDefense(isNpIgnoreDefense.isSelected());

                    if (isNpDamageOvercharged.isSelected()) {
                        additionalParams.setIsNpDamageOverchargedEffect(isNpDamageOvercharged.isSelected());
                        try {
                            final List<Double> additions = parseDoubles(npOverchargedRatesText.getText());
                            additionalParams.addAllNpOverchargeDamageRate(additions);
                        } catch (final Exception e) {
                            errorLabel.setVisible(true);
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Addition not Double"));
                            return;
                        }
                    }

                    if (hasNpSPD.isSelected()) {
                        if (npSpdCondition == null || npSpdCondition.getType().isEmpty()) {
                            errorLabel.setVisible(true);
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "NP SPD condition not set"));
                            return;
                        }
                        additionalParams.setNpSpecificDamageCondition(npSpdCondition);
                        additionalParams.setIsNpSpecificDamageOverchargedEffect(isNpSPDOvercharged.isSelected());
                        try {
                            final List<Double> spdValues = parseDoubles(npSPDText.getText());
                            additionalParams.addAllNpSpecificDamageRate(spdValues);
                        } catch (final Exception e) {
                            errorLabel.setVisible(true);
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Addition not Double"));
                            return;
                        }

                        if (npSPDVariationCheckbox.isSelected()) {
                            if (npSpdVariation == null || npSpdVariation.getType().isEmpty()) {
                                errorLabel.setVisible(true);
                                errorLabel.setText(getTranslation(APPLICATION_SECTION, "NP SPD Variation not set"));
                                return;
                            }
                            additionalParams.setNpSpecificDamageVariation(npSpdVariation);
                            additionalParams.setIsNpSpecificDamageAdditionOvercharged(isNpSPDVariationOvercharged.isSelected());
                            try {
                                final List<Double> additions = parseDoubles(npSPDVariationText.getText());
                                additionalParams.addAllNpSpecificDamageAdditions(additions);
                            } catch (final Exception e) {
                                errorLabel.setVisible(true);
                                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Addition not Double"));
                                return;
                            }
                        }
                    }

                    effectDataBuilder.setNpDamageAdditionalParams(additionalParams);
                }
            }
            if (requiredFields.contains(EFFECT_FIELD_GRANT_BUFF)) {
                final List<BuffData> builtBuffs = buffs.buildBuff();
                if (builtBuffs.isEmpty()) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "No buffs created"));
                    return;
                }
                effectDataBuilder.addAllBuffData(builtBuffs);
            }

            if (requiredFields.contains(EFFECT_FIELD_HP_CHANGE)) {
                effectDataBuilder.setIsHpChangePercentBased(hpPercentCheckbox.isSelected());
                effectDataBuilder.setIsLethal(hpDrainLethalCheckbox.isSelected());
            }
            if (requiredFields.contains(EFFECT_FIELD_REMOVE_BUFF)) {
                effectDataBuilder.setRemoveFromStart(removeFromStartCheckbox.isSelected());
            }
            if (requiredFields.contains(EFFECT_FIELD_RANDOM_EFFECT)) {
                final List<EffectData> builtEffects = effects.buildEffect();
                if (builtEffects.isEmpty()) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "No random effects provided"));
                    return;
                }
                effectDataBuilder.addAllEffectData(builtEffects);
            }
            if (requiredFields.contains(EFFECT_FIELD_CARD_TYPE_SELECT)) {
                final String buffType = effectDataBuilder.getBuffDataList().get(0).getType();
                if (!buffType.equals(CARD_TYPE_CHANGE.getType()) && !buffType.equals(NP_CARD_TYPE_CHANGE.getType())) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Must select card type related buff"));
                    return;
                }
                for (int i = 0; i < cardTypeCheckBoxes.size(); i++) {
                    if (cardTypeCheckBoxes.get(i).isSelected()) {
                        effectDataBuilder.addCardTypeSelections(SELECTABLE_CARD_TYPES.get(i));
                    }
                }
                if (effectDataBuilder.getCardTypeSelectionsList().isEmpty()) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Must provide card type selections"));
                    return;
                }
            }

            effectDataBuilder.setType(effectTypeChoices.getValue());
        }

        final Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
