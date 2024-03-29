package yome.fgo.simulator.gui.creators;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.BuffTraits;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeAdditionalParams;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.OnFieldBuffParams;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.gui.components.ListContainerVBox;
import yome.fgo.simulator.gui.components.ListContainerVBox.Mode;
import yome.fgo.simulator.gui.components.TranslationConverter;
import yome.fgo.simulator.models.effects.buffs.BuffFields;
import yome.fgo.simulator.models.effects.buffs.BuffType;
import yome.fgo.simulator.translation.TranslationManager;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_NO_CHANGE;
import static yome.fgo.data.writer.DataWriter.generateSkillValues;
import static yome.fgo.simulator.ResourceManager.getBuffIcon;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.creators.VariationBuilder.createVariation;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.addSplitTraitListener;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createTooltip;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillClassAdvMode;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillCommandCardType;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillOnFieldEffectTargets;
import static yome.fgo.simulator.gui.helpers.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.helpers.DataPrinter.printVariationData;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_BUFF_TYPE;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_CLASS_ADV;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_EFFECTS;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_NO_VARIATION;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_ON_FIELD;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_PERCENT_OPTION;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_STRING_VALUE;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getKeyForTrait;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasKeyForTrait;
import static yome.fgo.simulator.utils.BuffUtils.REGULAR_BUFF_TRAITS;

public class BuffBuilderFXMLController implements Initializable {
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
    private VBox effectsPane;

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
    private HBox gutsPane;

    @FXML
    private CheckBox gutsPercentCheckbox;

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

    @FXML
    private Label buffIconLabel;

    @FXML
    private TextField buffIconText;

    @FXML
    private ImageView buffImage;

    @FXML
    private HBox regularBuffTraitsHBox;

    private Set<BuffFields> requiredFields;

    private BuffData.Builder buffDataBuilder;
    private TextField generateTargetTextField;
    private ConditionData applyCondition;
    private VariationData variationData;

    private ListContainerVBox effects;
    private Map<BuffTraits, CheckBox> buffTraitsMap;

    @FXML
    private VBox scrollPaneVBox;
    private VBox onFieldPane;
    private ChoiceBox<Target> targetChoiceBox;
    private ListContainerVBox buffs;

    private VBox buffTypePane;
    private ChoiceBox<String> buffTypePaneChoices;
    private TextField convertIconPath;

    public void setParentBuilder(final BuffData.Builder buffDataBuilder) {
        this.buffDataBuilder = buffDataBuilder;

        if (!buffDataBuilder.getType().isEmpty()) {
            requiredFields = BuffType.ofType(buffDataBuilder.getType()).getRequiredFields();
            buffTypeChoices.getSelectionModel().select(buffDataBuilder.getType());

            if (buffDataBuilder.getNumTurnsActive() > 0) {
                numTurnCheckbox.setSelected(true);
                numTurnCheckbox.fireEvent(new ActionEvent());
                numTurnText.setText(Integer.toString(buffDataBuilder.getNumTurnsActive()));
            }
            if (buffDataBuilder.getNumTimesActive() > 0) {
                numTimesCheckbox.setSelected(true);
                numTimesCheckbox.fireEvent(new ActionEvent());
                numTimesText.setText(Integer.toString(buffDataBuilder.getNumTimesActive()));
            }
            irremovableCheckbox.setSelected(buffDataBuilder.getIrremovable());
            forceStackCheckbox.setSelected(buffDataBuilder.getForceStackable());
            buffIconText.setText(buffDataBuilder.getBuffIcon());
            if (buffDataBuilder.getProbabilitiesCount() > 0) {
                probabilityCheckbox.setSelected(true);
                probabilityCheckbox.fireEvent(new ActionEvent());
                probabilityText.setText(doublesToString(buffDataBuilder.getProbabilitiesList()));
            }
            if (buffDataBuilder.getHasCustomTraits()) {
                final Set<String> customTraits = new HashSet<>(buffDataBuilder.getCustomTraitsList());

                for (final String customTrait : buffDataBuilder.getCustomTraitsList()) {
                    try {
                        final BuffTraits buffTraits = BuffTraits.valueOf(customTrait);
                        buffTraitsMap.get(buffTraits).setSelected(true);
                        customTraits.remove(customTrait);
                    } catch (final Exception ignored) {
                    }
                }

                if (!customTraits.isEmpty()) {
                    traitCheckbox.setSelected(true);
                    traitCheckbox.fireEvent(new ActionEvent());
                    traitText.setText(String.join(", ", customTraits));
                }
            }
            if (buffDataBuilder.hasApplyCondition()) {
                conditionCheckbox.setSelected(true);
                conditionCheckbox.fireEvent(new ActionEvent());
                applyCondition = buffDataBuilder.getApplyCondition();
                builtConditionLabel.setText(printConditionData(applyCondition));
            }

            useVariationCheckbox.setDisable(requiredFields.contains(BUFF_FIELD_NO_VARIATION));
            if (requiredFields.contains(BUFF_FIELD_DOUBLE_VALUE) ||
                    (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION) && buffDataBuilder.getIsGutsPercentBased())) {
                valuesText.setText(doublesToString(buffDataBuilder.getValuesList()));
                if (buffDataBuilder.hasVariationData() && !requiredFields.contains(BUFF_FIELD_NO_VARIATION)) {
                    useVariationCheckbox.setSelected(true);
                    useVariationCheckbox.fireEvent(new ActionEvent());
                    variationData = buffDataBuilder.getVariationData();
                    builtVariationLabel.setText(printVariationData(variationData));
                    variationAdditionText.setText(doublesToString(buffDataBuilder.getAdditionsList()));
                }
            }
            if (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION)) {
                gutsPercentCheckbox.setSelected(buffDataBuilder.getIsGutsPercentBased());
            }
            if (requiredFields.contains(BUFF_FIELD_INT_VALUE) ||
                    (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION) && !buffDataBuilder.getIsGutsPercentBased())) {
                valuesText.setText(intsToString(buffDataBuilder.getValuesList()));
                if (buffDataBuilder.hasVariationData() && !requiredFields.contains(BUFF_FIELD_NO_VARIATION)) {
                    useVariationCheckbox.setSelected(true);
                    useVariationCheckbox.fireEvent(new ActionEvent());
                    variationData = buffDataBuilder.getVariationData();
                    builtVariationLabel.setText(printVariationData(variationData));
                    variationAdditionText.setText(intsToString(buffDataBuilder.getAdditionsList()));
                }
            }

            if (requiredFields.contains(BUFF_FIELD_STRING_VALUE)) {
                stringValueText.setText(getTranslation(TRAIT_SECTION, buffDataBuilder.getStringValue()));
            }
            if (requiredFields.contains(BUFF_FIELD_EFFECTS)) {
                effects.loadEffect(buffDataBuilder.getSubEffectsList());
            }
            if (requiredFields.contains(BUFF_FIELD_CLASS_ADV) && buffDataBuilder.hasClassAdvChangeAdditionalParams()) {
                final ClassAdvantageChangeAdditionalParams additionalParams = buffDataBuilder.getClassAdvChangeAdditionalParams();
                classAdvChoicesAtk.getSelectionModel().select(additionalParams.getAttackMode());
                classAdvAtkTargetClassText.setText(
                        additionalParams.getAttackModeAffectedClassesList()
                                .stream()
                                .map(cl -> getTranslation(CLASS_SECTION, cl.name()))
                                .collect(Collectors.joining(", "))
                );
                if (additionalParams.getCustomizeAttackModifier()) {
                    classAdvCustomAtkCheckbox.setSelected(true);
                    classAdvCustomAtkCheckbox.fireEvent(new ActionEvent());
                    classAdvCustomAtkText.setText(Double.toString(additionalParams.getAttackAdv()));
                }
                classAdvChoicesDef.getSelectionModel().select(additionalParams.getDefenseMode());
                classAdvDefTargetClassText.setText(
                        additionalParams.getDefenseModeAffectedClassesList()
                                .stream()
                                .map(cl -> getTranslation(CLASS_SECTION, cl.name()))
                                .collect(Collectors.joining(", "))
                );
                if (additionalParams.getCustomizeDefenseModifier()) {
                    classAdvCustomDefCheckbox.setSelected(true);
                    classAdvCustomDefCheckbox.fireEvent(new ActionEvent());
                    classAdvCustomDefText.setText(Double.toString(additionalParams.getDefenseAdv()));
                }
            }
            if (requiredFields.contains(BUFF_FIELD_CARD_TYPE)) {
                cardTypeChoices.getSelectionModel().select(CommandCardType.valueOf(buffDataBuilder.getStringValue()));
            }
            if (requiredFields.contains(BUFF_FIELD_ON_FIELD)) {
                targetChoiceBox.getSelectionModel().select(buffDataBuilder.getOnFieldBuffParams().getTarget());
                buffs.loadBuff(List.of(buffDataBuilder.getOnFieldBuffParamsBuilder().getBuffData()));
            }
            if (requiredFields.contains(BUFF_FIELD_BUFF_TYPE)) {
                buffTypePaneChoices.getSelectionModel().select(buffDataBuilder.getStringValue());
                convertIconPath.setText(buffDataBuilder.getConvertIconPath());
            }
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        onFieldPane = new VBox(10);
        buffTypePane = new VBox(10);
        resetPane();

        buffTypeLabel.setText(getTranslation(APPLICATION_SECTION, "Buff Type"));
        buffTypeChoices.setConverter(new TranslationConverter(BUFF_SECTION));
        buffTypeChoices.setItems(FXCollections.observableArrayList(BuffType.getOrder()));
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
            generateValueBaseText.requestFocus();
        });

        final Label regularBuffTraitLabel = new Label(getTranslation(APPLICATION_SECTION, "Add custom buff traits"));
        regularBuffTraitsHBox.getChildren().add(regularBuffTraitLabel);
        buffTraitsMap = new HashMap<>();
        for (final BuffTraits buffTrait : REGULAR_BUFF_TRAITS) {
            final CheckBox checkBox = new CheckBox(getTranslation(TRAIT_SECTION, buffTrait.name()));
            buffTraitsMap.put(buffTrait, checkBox);
            regularBuffTraitsHBox.getChildren().add(checkBox);
        }

        traitCheckbox.setText(getTranslation(APPLICATION_SECTION, "Custom Buff Trait"));
        traitText.setDisable(true);
        traitCheckbox.setOnAction(e -> traitText.setDisable(!traitCheckbox.isSelected()));
        addSplitTraitListener(traitText, errorLabel);

        conditionCheckbox.setText(getTranslation(APPLICATION_SECTION, "Apply Condition"));
        conditionEditButton.setDisable(true);
        builtConditionLabel.setDisable(true);
        conditionCheckbox.setOnAction(e -> {
            conditionEditButton.setDisable(!conditionCheckbox.isSelected());
            builtConditionLabel.setDisable(!conditionCheckbox.isSelected());
        });
        conditionEditButton.setGraphic(createInfoImageView("edit"));
        conditionEditButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Edit")));
        conditionEditButton.setOnAction(e -> editCondition());
        conditionEditButton.setText(null);
        builtConditionLabel.setPadding(new Insets(5));
        builtConditionLabel.setMaxWidth(Double.MAX_VALUE);
        builtConditionLabel.setStyle(SPECIAL_INFO_BOX_STYLE);
        builtConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Leave unchecked to always apply"));

        generateValuesButton.setText(getTranslation(APPLICATION_SECTION, "Autofill"));
        generateValuesButton.setOnAction(e -> {
            generateTargetTextField = valuesText;
            generateValuePane.setVisible(true);
            generateValueBaseText.requestFocus();
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
            generateValueBaseText.requestFocus();
        });
        editVariationButton.setGraphic(createInfoImageView("edit"));
        editVariationButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "Edit")));
        editVariationButton.setOnAction(e -> editVariation());
        editVariationButton.setText(null);
        builtVariationLabel.setPadding(new Insets(5));
        builtVariationLabel.setMaxWidth(Double.MAX_VALUE);
        builtVariationLabel.setStyle(SPECIAL_INFO_BOX_STYLE);
        builtVariationLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));

        stringValueLabel.setText(getTranslation(APPLICATION_SECTION, "Traits"));
        stringValueText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !hasKeyForTrait(newValue)) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Warning: unmapped traits:") + newValue);
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        });

        effects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Effects"), errorLabel, Mode.EFFECT);
        effectsPane.getChildren().addAll(effects);

        final Label targetLabel = new Label(getTranslation(APPLICATION_SECTION, "Target"));
        targetChoiceBox = new ChoiceBox<>();
        fillOnFieldEffectTargets(targetChoiceBox);
        final HBox targetHBox = new HBox(10);
        targetHBox.setAlignment(Pos.CENTER_LEFT);
        targetHBox.getChildren().addAll(targetLabel, targetChoiceBox);
        buffs = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Buffs"), errorLabel, Mode.BUFF);
        onFieldPane.getChildren().addAll(targetHBox, buffs);
        scrollPaneVBox.getChildren().add(onFieldPane);

        final Label buffTypePaneLabel = new Label(getTranslation(APPLICATION_SECTION, "Convert Type"));
        buffTypePaneChoices = new ChoiceBox<>();
        buffTypePaneChoices.setConverter(new TranslationConverter(BUFF_SECTION));
        buffTypePaneChoices.setItems(FXCollections.observableArrayList(BuffType.getOrder()));
        buffTypePaneChoices.setOnAction(e -> onBuffTypeChoiceChange());
        buffTypePaneChoices.getSelectionModel().selectFirst();
        final Label convertIconLabel = new Label(getTranslation(APPLICATION_SECTION, "Convert Icon"));
        convertIconPath = new TextField();
        final File iconFile = getBuffIcon("default");
        Image convertIcon = null;
        try {
            convertIcon = new Image(new FileInputStream(iconFile));
        } catch (FileNotFoundException ignored) {
        }
        final ImageView convertIconView = new ImageView();
        convertIconView.setFitWidth(30);
        convertIconView.setFitHeight(30);
        convertIconView.setImage(convertIcon);
        convertIconPath.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        convertIconView.setImage(new Image(new FileInputStream(getBuffIcon(newValue))));
                    } catch (final FileNotFoundException ignored) {
                    }
                }
        );
        buffTypePane.getChildren().addAll(buffTypePaneLabel, buffTypePaneChoices, convertIconLabel, convertIconPath, convertIconView);
        scrollPaneVBox.getChildren().add(buffTypePane);

        gutsPercentCheckbox.setText(getTranslation(APPLICATION_SECTION, "Set as percent"));
        gutsPercentCheckbox.setOnAction(e ->
            valueLabel.setText(
                    gutsPercentCheckbox.isSelected() ?
                            getTranslation(APPLICATION_SECTION, "Value (%)") :
                            getTranslation(APPLICATION_SECTION, "Value")
                    )
        );

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
                if (generateTargetTextField == probabilityText || requiredFields.contains(BUFF_FIELD_DOUBLE_VALUE) ||
                        (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION) && gutsPercentCheckbox.isSelected())) {
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

        buffIconLabel.setText(getTranslation(APPLICATION_SECTION, "Buff Icon"));
        Image icon = null;
        try {
            icon = new Image(new FileInputStream(iconFile));
        } catch (FileNotFoundException ignored) {
        }
        buffImage.setImage(icon);
        buffIconText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        buffImage.setImage(new Image(new FileInputStream(getBuffIcon(newValue))));
                    } catch (final FileNotFoundException ignored) {
                    }
                }
        );
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

        onFieldPane.setVisible(false);
        onFieldPane.setManaged(false);

        buffTypePane.setVisible(false);
        buffTypePane.setManaged(false);
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
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
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
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
            errorLabel.setVisible(true);
        }
    }

    public void onBuffTypeChoiceChange() {
        resetPane();
        requiredFields = BuffType.ofType(buffTypeChoices.getValue()).getRequiredFields();

        useVariationCheckbox.setDisable(requiredFields.contains(BUFF_FIELD_NO_VARIATION));
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
            valuePane.setVisible(true);
            valuePane.setManaged(true);
            gutsPane.setVisible(true);
            gutsPane.setManaged(true);
            valueLabel.setText(
                    gutsPercentCheckbox.isSelected() ?
                            getTranslation(APPLICATION_SECTION, "Value (%)") :
                            getTranslation(APPLICATION_SECTION, "Value")
            );
        }
        if (requiredFields.contains(BUFF_FIELD_CLASS_ADV)) {
            classAdvPane.setVisible(true);
            classAdvPane.setManaged(true);
        }
        if (requiredFields.contains(BUFF_FIELD_CARD_TYPE)) {
            cardTypePane.setVisible(true);
            cardTypePane.setManaged(true);
        }
        if (requiredFields.contains(BUFF_FIELD_ON_FIELD)) {
            onFieldPane.setVisible(true);
            onFieldPane.setManaged(true);
        }
        if (requiredFields.contains(BUFF_FIELD_BUFF_TYPE)) {
            buffTypePane.setVisible(true);
            buffTypePane.setManaged(true);
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
            buffDataBuilder.clear();
            if (numTurnCheckbox.isSelected()) {
                try {
                    final int numTurnsActive = Integer.parseInt(numTurnText.getText());
                    buffDataBuilder.setNumTurnsActive(numTurnsActive);
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Num turns active not Integer"));
                    return;
                }
            }
            if (numTimesCheckbox.isSelected()) {
                try {
                    final int numTimesActive = Integer.parseInt(numTimesText.getText());
                    buffDataBuilder.setNumTimesActive(numTimesActive);
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Num times active not Integer"));
                    return;
                }
            }
            buffDataBuilder.setIrremovable(irremovableCheckbox.isSelected());
            buffDataBuilder.setForceStackable(forceStackCheckbox.isSelected());
            buffDataBuilder.setBuffIcon(buffIconText.getText().trim());
            if (probabilityCheckbox.isSelected()) {
                try {
                    final List<Double> probabilities = parseDoubles(probabilityText.getText());
                    buffDataBuilder.addAllProbabilities(probabilities);
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Probabilities not double"));
                    return;
                }
            }

            for (final BuffTraits buffTrait : buffTraitsMap.keySet()) {
                if (buffTraitsMap.get(buffTrait).isSelected()) {
                    buffDataBuilder.setHasCustomTraits(true);
                    buffDataBuilder.addCustomTraits(buffTrait.name());
                }
            }

            if (traitCheckbox.isSelected()) {
                buffDataBuilder.setHasCustomTraits(true);
                buffDataBuilder.addAllCustomTraits(Arrays.stream(traitText.getText().trim().split(COMMA_SPLIT_REGEX)).sequential()
                                                           .filter(s -> !s.isEmpty())
                                                           .map(TranslationManager::getKeyForTrait)
                                                           .collect(Collectors.toList()));
            }
            if (conditionCheckbox.isSelected()) {
                if (applyCondition == null || applyCondition.getType().isEmpty()) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Apply condition not set"));
                    return;
                }
                buffDataBuilder.setApplyCondition(applyCondition);
            }
            
            if (requiredFields.contains(BUFF_FIELD_DOUBLE_VALUE) ||
                    (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION) && gutsPercentCheckbox.isSelected())) {
                try {
                    final List<Double> values = parseDoubles(valuesText.getText());
                    buffDataBuilder.addAllValues(values);
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Double"));
                    return;
                }
                if (useVariationCheckbox.isSelected() && !requiredFields.contains(BUFF_FIELD_NO_VARIATION)) {
                    try {
                        final List<Double> additions = parseDoubles(variationAdditionText.getText());
                        buffDataBuilder.addAllAdditions(additions);
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
                    buffDataBuilder.setVariationData(variationData);
                }
            }
            if (requiredFields.contains(BUFF_FIELD_INT_VALUE) ||
                    (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION) && !gutsPercentCheckbox.isSelected())) {
                try {
                    final List<Double> values = parseInts(valuesText.getText());
                    buffDataBuilder.addAllValues(values);
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Integer"));
                    return;
                }
                if (useVariationCheckbox.isSelected() && !requiredFields.contains(BUFF_FIELD_NO_VARIATION)) {
                    try {
                        final List<Double> additions = parseInts(variationAdditionText.getText());
                        buffDataBuilder.addAllAdditions(additions);
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
                    buffDataBuilder.setVariationData(variationData);
                }
            }
            if (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION)) {
                buffDataBuilder.setIsGutsPercentBased(gutsPercentCheckbox.isSelected());
            }
            if (requiredFields.contains(BUFF_FIELD_STRING_VALUE)) {
                buffDataBuilder.setStringValue(getKeyForTrait(stringValueText.getText()));
            }
            if (requiredFields.contains(BUFF_FIELD_EFFECTS)) {
                buffDataBuilder.addAllSubEffects(effects.buildEffect());
            }
            if (requiredFields.contains(BUFF_FIELD_CLASS_ADV)) {
                final ClassAdvantageChangeAdditionalParams.Builder additionalParams = ClassAdvantageChangeAdditionalParams.newBuilder();
                additionalParams.setAttackMode(classAdvChoicesAtk.getValue());
                if (classAdvChoicesAtk.getValue() != CLASS_ADV_NO_CHANGE) {
                    if (classAdvCustomAtkCheckbox.isSelected()) {
                        additionalParams.setCustomizeAttackModifier(true);
                        try {
                            final double rate = Double.parseDouble(classAdvCustomAtkText.getText());
                            additionalParams.setAttackAdv(rate);
                        } catch (final Exception e) {
                            errorLabel.setVisible(true);
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Double"));
                            return;
                        }
                    }

                    if (classAdvAtkTargetClassText.getText().isEmpty()) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Affected class not set"));
                        return;
                    }
                }

                additionalParams.addAllAttackModeAffectedClasses(
                        Arrays.stream(classAdvAtkTargetClassText.getText().trim().split(COMMA_SPLIT_REGEX)).sequential()
                                .filter(s -> !s.isEmpty())
                                .map(s -> FateClass.valueOf(getKeyForTrait(s)))
                                .collect(Collectors.toList())
                );
                additionalParams.setDefenseMode(classAdvChoicesDef.getValue());
                if (classAdvChoicesDef.getValue() != CLASS_ADV_NO_CHANGE) {
                    if (classAdvCustomDefCheckbox.isSelected()) {
                        additionalParams.setCustomizeDefenseModifier(true);
                        try {
                            final double rate = Double.parseDouble(classAdvCustomDefText.getText());
                            additionalParams.setDefenseAdv(rate);
                        } catch (final Exception e) {
                            errorLabel.setVisible(true);
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Double"));
                            return;
                        }
                    }

                    if (classAdvDefTargetClassText.getText().isEmpty()) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Affected class not set"));
                        return;
                    }
                }

                additionalParams.addAllDefenseModeAffectedClasses(
                        Arrays.stream(classAdvDefTargetClassText.getText().trim().split(COMMA_SPLIT_REGEX)).sequential()
                                .filter(s -> !s.isEmpty())
                                .map(s -> FateClass.valueOf(getKeyForTrait(s)))
                                .collect(Collectors.toList())
                );

                buffDataBuilder.setClassAdvChangeAdditionalParams(additionalParams);
            }

            if (requiredFields.contains(BUFF_FIELD_CARD_TYPE)) {
                buffDataBuilder.setStringValue(cardTypeChoices.getValue().name());
            }

            if (requiredFields.contains(BUFF_FIELD_ON_FIELD)) {
                final List<BuffData> builtBuff = buffs.buildBuff();
                if (builtBuff.size() != 1) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Should only contain one buff"));
                    return;
                }

                final OnFieldBuffParams onFieldBuffParamsBuilder = OnFieldBuffParams.newBuilder()
                        .setTarget(targetChoiceBox.getValue())
                        .setBuffData(builtBuff.get(0))
                        .build();

                buffDataBuilder.setOnFieldBuffParams(onFieldBuffParamsBuilder);
            }
            if (requiredFields.contains(BUFF_FIELD_BUFF_TYPE)) {
                buffDataBuilder.setStringValue(buffTypePaneChoices.getValue());
                buffDataBuilder.setConvertIconPath(convertIconPath.getText());
            }

            buffDataBuilder.setType(buffTypeChoices.getValue());
        }

        final Stage stage = (Stage) buildButton.getScene().getWindow();
        stage.close();
    }

    public static List<Double> parseDoubles(final String values) {
        return Arrays.stream(values.trim().split(COMMA_SPLIT_REGEX))
            .sequential()
            .map(val -> RoundUtils.roundNearest(Double.parseDouble(val) / 100))
            .collect(Collectors.toList());
    }

    public static String doublesToString(final List<Double> list) {
        return list.stream()
                .map(d -> Double.toString(RoundUtils.roundNearest(d * 100)))
                .collect(Collectors.joining(", "));
    }

    public static List<Double> parseInts(final String values) {
        return Arrays.stream(values.trim().split(COMMA_SPLIT_REGEX))
                .sequential()
                .map(val -> (double) Integer.parseInt(val))
                .collect(Collectors.toList());
    }

    public static String intsToString(final List<Double> list) {
        return list.stream()
                .map(d -> Integer.toString((int) d.doubleValue()))
                .collect(Collectors.joining(", "));
    }
}
