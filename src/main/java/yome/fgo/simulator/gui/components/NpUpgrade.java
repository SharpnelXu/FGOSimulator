package yome.fgo.simulator.gui.components;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;

import java.io.IOException;
import java.util.List;

import static yome.fgo.simulator.gui.components.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class NpUpgrade extends HBox {
    private final CommandCardBox cardBox;
    private final ListView<EffectData> npEffects;
    private final Label errorLabel;
    private final CheckBox conditionCheckBox;
    private final Label builtConditionLabel;

    private ConditionData activationCondition;

    public NpUpgrade() {
        super();
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setSpacing(10);
        setPadding(new Insets(5, 5, 5, 5));
        setFillHeight(false);

        final List<Node> nodes = getChildren();
        cardBox = new CommandCardBox(6);

        nodes.add(cardBox);

        final VBox dataVBox = new VBox();
        HBox.setHgrow(dataVBox, Priority.ALWAYS);
        dataVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        dataVBox.setSpacing(10);

        final HBox conditionHBox = new HBox();
        conditionHBox.setAlignment(Pos.CENTER_LEFT);
        conditionHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        conditionHBox.setSpacing(10);
        errorLabel = new Label();
        errorLabel.setVisible(false);
        errorLabel.setStyle("-fx-text-fill: red");
        conditionCheckBox = new CheckBox(getTranslation(APPLICATION_SECTION, "Activate Condition"));
        final Button editConditionButton = new Button(getTranslation(APPLICATION_SECTION, "Edit"));
        builtConditionLabel = new Label(getTranslation(APPLICATION_SECTION, "Empty"));
        builtConditionLabel.setMaxWidth(500);
        editConditionButton.setOnAction(e -> {
            try {
                final ConditionData.Builder builder = activationCondition == null ? ConditionData.newBuilder() : activationCondition.toBuilder();
                createCondition(editConditionButton.getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    activationCondition = builder.build();
                    builtConditionLabel.setText(printConditionData(activationCondition));
                }
            } catch (final IOException ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
                errorLabel.setVisible(true);
            }
        });
        editConditionButton.setDisable(true);
        builtConditionLabel.setDisable(true);
        conditionCheckBox.setOnAction(e -> {
            editConditionButton.setDisable(!conditionCheckBox.isSelected());
            builtConditionLabel.setDisable(!conditionCheckBox.isSelected());
        });

        conditionHBox.getChildren().addAll(conditionCheckBox, editConditionButton, builtConditionLabel);
        dataVBox.getChildren().add(conditionHBox);

        final HBox effectsHBox = new HBox();
        effectsHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        effectsHBox.setSpacing(10);
        final Label effectsLabel = new Label(getTranslation(APPLICATION_SECTION, "Effects"));

        effectsHBox.getChildren().add(effectsLabel);

        final VBox effectsVBox = new VBox();
        effectsVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        effectsVBox.setSpacing(10);
        HBox.setHgrow(effectsVBox, Priority.ALWAYS);
        npEffects = new ListView<>();
        npEffects.setCellFactory(new EffectsCellFactory(errorLabel));
        npEffects.setItems(FXCollections.observableArrayList());
        npEffects.setMaxHeight(235);
        final Button addEffectsButton = new Button(getTranslation(APPLICATION_SECTION, "Add Effect"));
        addEffectsButton.setOnAction(e -> {
            try {
                final EffectData.Builder builder = EffectData.newBuilder();
                createEffect(addEffectsButton.getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    npEffects.getItems().add(builder.build());
                }
            } catch (final IOException ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
                errorLabel.setVisible(true);
            }
        });
        final HBox effectButtonHBox = new HBox();
        effectButtonHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        effectButtonHBox.setSpacing(10);
        effectButtonHBox.setAlignment(Pos.CENTER_RIGHT);
        effectButtonHBox.getChildren().addAll(errorLabel, addEffectsButton);

        effectsVBox.getChildren().addAll(npEffects, effectButtonHBox);
        effectsHBox.getChildren().add(effectsVBox);

        dataVBox.getChildren().add(effectsHBox);

        nodes.add(dataVBox);
    }

    public NpUpgrade(final NpUpgrade source) {
        this();
        setFrom(source);
    }

    public void setFrom(final NpUpgrade source) {
        this.cardBox.setFrom(source.cardBox);
        this.npEffects.getItems().addAll(source.npEffects.getItems());
        this.activationCondition = source.activationCondition;
        if (this.activationCondition != null) {
            this.builtConditionLabel.setText(printConditionData(this.activationCondition));
        }
        this.conditionCheckBox.setSelected(source.conditionCheckBox.isSelected());
        this.conditionCheckBox.fireEvent(new ActionEvent());
    }
}
