package yome.fgo.simulator.gui.creators;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.gui.components.TranslationConverter;
import yome.fgo.simulator.models.variations.Variation.VariationFields;
import yome.fgo.simulator.models.variations.Variation.VariationType;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

import static yome.fgo.simulator.gui.components.DataPrinter.doubleToString;
import static yome.fgo.simulator.gui.components.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillTargets;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_BUFF;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_HP;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_MAX_COUNT;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_TARGET;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_TRAIT;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.VARIATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getKeyForTrait;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasKeyForTrait;

public class VariationBuilderFXMLController implements Initializable {

    @FXML
    private Button addConditionButton;

    @FXML
    private Button buildButton;

    @FXML
    private Label builtConditionLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Label conditionLabel;

    @FXML
    private VBox conditionPane;

    @FXML
    private Label errorLabel;

    @FXML
    private HBox hpPane;

    @FXML
    private Label maxCountDescLabel;

    @FXML
    private Label maxCountLabel;

    @FXML
    private HBox maxCountPane;

    @FXML
    private TextField maxCountText;

    @FXML
    private Label maxHpLabel;

    @FXML
    private TextField maxHpText;

    @FXML
    private Label minHpLabel;

    @FXML
    private TextField minHpText;

    @FXML
    private ChoiceBox<Target> targetChoices;

    @FXML
    private Label targetLabel;

    @FXML
    private HBox targetPane;

    @FXML
    private Label traitLabel;

    @FXML
    private HBox traitPane;

    @FXML
    private TextField traitText;

    @FXML
    private ChoiceBox<String> variationChoices;

    @FXML
    private Label variationLabel;

    private VariationData.Builder variationBuilder;

    private ConditionData conditionData;

    private Set<VariationFields> requiredFields;

    public void setVariationBuilder(final VariationData.Builder variationBuilder) {
        this.variationBuilder = variationBuilder;

        if (!variationBuilder.getType().isEmpty()) {
            requiredFields = VariationType.ofType(variationBuilder.getType()).getRequiredFields();
            variationChoices.getSelectionModel().select(variationBuilder.getType());

            if (requiredFields.contains(VARIATION_FIELD_MAX_COUNT)) {
                maxCountText.setText(Integer.toString(variationBuilder.getMaxCount()));
            }
            if (requiredFields.contains(VARIATION_FIELD_TRAIT)) {
                traitText.setText(variationBuilder.getTrait());
            }
            if (requiredFields.contains(VARIATION_FIELD_BUFF)) {
                conditionData = variationBuilder.getConditionData();
                builtConditionLabel.setText(printConditionData(conditionData));
            }
            if (requiredFields.contains(VARIATION_FIELD_HP)) {
                maxHpText.setText(doubleToString(variationBuilder.getMaxHp()));
                minHpText.setText(doubleToString(variationBuilder.getMinHp()));
            }
            if (requiredFields.contains(VARIATION_FIELD_TARGET)) {
                targetChoices.getSelectionModel().select(variationBuilder.getTarget());
            }
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        resetPane();

        variationLabel.setText(getTranslation(APPLICATION_SECTION, "Variation Type"));
        variationChoices.setConverter(new TranslationConverter(VARIATION_SECTION));
        variationChoices.setItems(FXCollections.observableArrayList(VariationType.getOrder()));
        variationChoices.getSelectionModel().select("NoVariation");
        requiredFields = VariationType.ofType("NoVariation").getRequiredFields();

        variationChoices.setOnAction(e -> onVariationTypeChoiceChanges());

        maxCountLabel.setText(getTranslation(APPLICATION_SECTION, "Max Count"));
        maxCountDescLabel.setText(getTranslation(APPLICATION_SECTION, "Leave black for unlimited count"));

        traitLabel.setText(getTranslation(APPLICATION_SECTION, "Traits"));
        traitText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !hasKeyForTrait(newValue)) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Warning: unmapped traits:") + newValue);
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        });

        maxHpLabel.setText(getTranslation(APPLICATION_SECTION, "Max HP (%)"));
        minHpLabel.setText(getTranslation(APPLICATION_SECTION, "Min HP (%)"));

        targetLabel.setText(getTranslation(APPLICATION_SECTION, "Target"));
        fillTargets(targetChoices);

        conditionLabel.setText(getTranslation(APPLICATION_SECTION, "Buff Condition"));

        addConditionButton.setGraphic(createInfoImageView("edit"));
        addConditionButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Edit")));
        addConditionButton.setOnAction(e -> onAddConditionButtonClick());
        addConditionButton.setText(null);
        builtConditionLabel.setPadding(new Insets(5));
        builtConditionLabel.setMaxWidth(Double.MAX_VALUE);
        builtConditionLabel.setStyle(SPECIAL_INFO_BOX_STYLE);
        builtConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));

        buildButton.setText(getTranslation(APPLICATION_SECTION, "Build"));
        cancelButton.setText(getTranslation(APPLICATION_SECTION, "Cancel"));

        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Select a variation type to start"));
    }

    private void resetPane() {
        traitPane.setVisible(false);
        traitPane.setManaged(false);

        maxCountPane.setVisible(false);
        maxCountPane.setManaged(false);

        hpPane.setVisible(false);
        hpPane.setManaged(false);

        targetPane.setVisible(false);
        targetPane.setManaged(false);

        conditionPane.setVisible(false);
        conditionPane.setManaged(false);
    }

    @FXML
    void onVariationTypeChoiceChanges() {
        errorLabel.setVisible(false);

        resetPane();

        requiredFields = VariationType.ofType(variationChoices.getValue()).getRequiredFields();

        if (requiredFields.contains(VARIATION_FIELD_MAX_COUNT)) {
            maxCountPane.setVisible(true);
            maxCountPane.setManaged(true);
        }
        if (requiredFields.contains(VARIATION_FIELD_TRAIT)) {
            traitPane.setVisible(true);
            traitPane.setManaged(true);
        }
        if (requiredFields.contains(VARIATION_FIELD_BUFF)) {
            conditionPane.setVisible(true);
            conditionPane.setManaged(true);
        }
        if (requiredFields.contains(VARIATION_FIELD_HP)) {
            hpPane.setVisible(true);
            hpPane.setManaged(true);
        }
        if (requiredFields.contains(VARIATION_FIELD_TARGET)) {
            targetPane.setVisible(true);
            targetPane.setManaged(true);
        }
    }

    @FXML
    void onAddConditionButtonClick() {
        try {
            final ConditionData.Builder builder = conditionData == null ? ConditionData.newBuilder() : conditionData.toBuilder();
            createCondition(addConditionButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                conditionData = builder.build();
                builtConditionLabel.setText(printConditionData(conditionData));
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
        }
    }

    @FXML
    void onRemoveConditionButtonClick() {
        conditionData = null;
        conditionLabel.setText(null);
    }

    @FXML
    void onBuildButtonClick() {
        if (variationBuilder != null) {
            variationBuilder.clear();
            if (requiredFields.contains(VARIATION_FIELD_MAX_COUNT) && !maxCountText.getText().isEmpty()) {
                try {
                    variationBuilder.setMaxCount(Integer.parseInt(maxCountText.getText()));
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Integer"));
                    return;
                }
            }
            if (requiredFields.contains(VARIATION_FIELD_TRAIT)) {
                variationBuilder.setTrait(getKeyForTrait(traitText.getText().trim()));
            }
            if (requiredFields.contains(VARIATION_FIELD_BUFF)) {
                if (conditionData == null || conditionData.getType().isEmpty()) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Buff condition is not set"));
                    return;
                }
                variationBuilder.setConditionData(conditionData);
            }
            if (requiredFields.contains(VARIATION_FIELD_HP)) {
                try {
                    variationBuilder.setMaxHp(RoundUtils.roundNearest(Double.parseDouble(maxHpText.getText()) / 100));
                    variationBuilder.setMinHp(RoundUtils.roundNearest(Double.parseDouble(minHpText.getText()) / 100));
                } catch (final Exception e) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Double"));
                    return;
                }
            }
            if (requiredFields.contains(VARIATION_FIELD_TARGET)) {
                variationBuilder.setTarget(targetChoices.getValue());
            }

            variationBuilder.setType(variationChoices.getValue());
        }
        final Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onCancelButtonClick() {
        if (variationBuilder != null) {
            variationBuilder.clearType();
        }
        final Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
