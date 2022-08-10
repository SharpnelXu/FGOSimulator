package yome.fgo.simulator.gui.components;

import com.google.common.collect.ImmutableList;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmType;
import yome.fgo.simulator.gui.components.ListContainerVBox.Mode;

import java.io.IOException;
import java.util.List;

import static yome.fgo.simulator.gui.components.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.LIST_ITEM_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class NpUpgrade extends HBox {
    private final CommandCardBox cardBox;
    private final ChoiceBox<NoblePhantasmType> npTypeChoices;
    private final CheckBox conditionCheckBox;
    private final Label builtConditionLabel;
    private final ListContainerVBox npEffects;
    private final Label errorLabel;

    private ConditionData activationCondition;

    public NpUpgrade(final Label errorLabel) {
        super();
        setFillHeight(false);
        setMinHeight(300);
        setMaxHeight(700);

        this.errorLabel = errorLabel;

        cardBox = new CommandCardBox(6);
        setMargin(cardBox, new Insets(5));
        getChildren().add(cardBox);
        cardBox.setMinWidth(280);

        final ScrollPane otherScroll = new ScrollPane();
        otherScroll.setMinHeight(300);
        otherScroll.setPadding(new Insets(10));
        otherScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        otherScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        otherScroll.setFitToWidth(true);
        HBox.setHgrow(otherScroll, Priority.ALWAYS);
        final VBox dataVBox = new VBox(10);
        otherScroll.setContent(dataVBox);
        getChildren().add(otherScroll);

        final HBox npTypeChoicesHBox = new HBox(10);
        npTypeChoicesHBox.setAlignment(Pos.CENTER_LEFT);
        final Label npTypeLabel = new Label(getTranslation(APPLICATION_SECTION, "NP Type"));
        npTypeChoices = new ChoiceBox<>();
        npTypeChoices.setConverter(new EnumConverter<>(COMMAND_CARD_TYPE_SECTION));
        final List<NoblePhantasmType> npTypes = ImmutableList.of(
                NoblePhantasmType.SINGLE_TARGET_NP,
                NoblePhantasmType.ALL_TARGETS_NP,
                NoblePhantasmType.NON_DAMAGE
        );
        npTypeChoices.setItems(FXCollections.observableArrayList(npTypes));
        npTypeChoices.getSelectionModel().selectFirst();

        conditionCheckBox = new CheckBox(getTranslation(APPLICATION_SECTION, "Activate Condition"));
        final Button editConditionButton = new Button();
        editConditionButton.setGraphic(createInfoImageView("edit"));
        editConditionButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Edit")));

        npTypeChoicesHBox.getChildren().addAll(npTypeLabel, npTypeChoices, conditionCheckBox, editConditionButton);
        dataVBox.getChildren().add(npTypeChoicesHBox);
        builtConditionLabel = new Label(getTranslation(APPLICATION_SECTION, "Empty"));
        builtConditionLabel.setPadding(new Insets(5));
        builtConditionLabel.setWrapText(true);
        builtConditionLabel.setMaxWidth(Double.MAX_VALUE);
        builtConditionLabel.setStyle(LIST_ITEM_STYLE);
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

        dataVBox.getChildren().add(builtConditionLabel);

        npEffects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Effects"), errorLabel, Mode.EFFECT);

        dataVBox.getChildren().add(npEffects);
    }

    public NpUpgrade(final NpUpgrade source, final Label errorLabel) {
        this(errorLabel);
        setFrom(source);
    }

    public NpUpgrade(final NoblePhantasmData noblePhantasmData, final Label errorLabel) {
        this(errorLabel);
        setFrom(noblePhantasmData);
    }

    public void setFrom(final NpUpgrade source) {
        this.cardBox.setFrom(source.cardBox);
        this.npEffects.loadEffect(source.npEffects.buildEffect());
        this.npTypeChoices.getSelectionModel().select(source.npTypeChoices.getValue());
        this.activationCondition = source.activationCondition;
        if (this.activationCondition != null) {
            this.builtConditionLabel.setText(printConditionData(this.activationCondition));
        }
        this.conditionCheckBox.setSelected(source.conditionCheckBox.isSelected());
        this.conditionCheckBox.fireEvent(new ActionEvent());
    }

    public NoblePhantasmData build(final int ascension, final int cardId) {
        final NoblePhantasmData.Builder builder =  NoblePhantasmData.newBuilder()
                .setCommandCardData(cardBox.build(errorLabel, ascension, cardId))
                .setNoblePhantasmType(npTypeChoices.getValue())
                .addAllEffects(npEffects.buildEffect());

        if (conditionCheckBox.isSelected()) {
            builder.setActivationCondition(activationCondition);
        }

        return builder.build();
    }

    public void setFrom(final NoblePhantasmData noblePhantasmData) {
        cardBox.setFrom(noblePhantasmData.getCommandCardData());
        npEffects.loadEffect(noblePhantasmData.getEffectsList());
        npTypeChoices.getSelectionModel().select(noblePhantasmData.getNoblePhantasmType());
        if (noblePhantasmData.hasActivationCondition()) {
            conditionCheckBox.setSelected(true);
            conditionCheckBox.fireEvent(new ActionEvent());
            activationCondition = noblePhantasmData.getActivationCondition();
            builtConditionLabel.setText(printConditionData(activationCondition));
        }
    }

    public void quickFillData(final String npRateString, final String critStarRateString) {
        cardBox.quickFillData(npRateString, critStarRateString);
    }
}
