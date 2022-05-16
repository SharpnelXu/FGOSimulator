package yome.fgo.simulator.gui.components;

import com.google.common.collect.ImmutableList;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationTarget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.ResourceManager.getSkillIcon;
import static yome.fgo.simulator.gui.components.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.SPECIAL_ACTIVATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ActiveSkillUpgrade extends HBox {
    private final ImageView skillIcon;
    private final TextField iconFileNameText;
    private final TextField coolDownText;
    private final ListView<DataWrapper<EffectData>> skillEffects;
    private final Label errorLabel;
    private final CheckBox conditionCheckBox;
    private final Label builtConditionLabel;

    private ConditionData activationCondition;

    private final CheckBox specialCheckBox;
    private final List<CheckBox> cardTypeCheckboxes;
    private final ListView<DataWrapper<EffectData>> randomEffects;
    private final ChoiceBox<SpecialActivationTarget> specialActivationTargetChoiceBox;

    public ActiveSkillUpgrade() {
        setPadding(new Insets(10, 10, 10, 10));
        setSpacing(10);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setFillHeight(false);

        final List<Node> nodes = getChildren();

        final File iconFile = getSkillIcon("default");
        Image icon = null;
        try {
            icon = new Image(new FileInputStream(iconFile));
        } catch (FileNotFoundException ignored) {
        }
        skillIcon = new ImageView();
        if (icon != null) {
            skillIcon.setImage(icon);
        }
        skillIcon.setFitWidth(100);
        skillIcon.setFitHeight(100);
        final AnchorPane imgAnchor = new AnchorPane();
        AnchorPane.setTopAnchor(skillIcon, 0.0);
        AnchorPane.setBottomAnchor(skillIcon, 0.0);
        AnchorPane.setLeftAnchor(skillIcon, 0.0);
        AnchorPane.setRightAnchor(skillIcon, 0.0);
        imgAnchor.setStyle("-fx-border-color: rgba(49,82,145,0.8); -fx-border-style: solid; -fx-border-width: 2");
        imgAnchor.getChildren().add(skillIcon);

        nodes.add(imgAnchor);

        final VBox dataVBox = new VBox();
        HBox.setHgrow(dataVBox, Priority.ALWAYS);
        dataVBox.setSpacing(10);
        dataVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        final HBox coolDownHBox = new HBox();
        coolDownHBox.setSpacing(10);
        coolDownHBox.setAlignment(Pos.CENTER_LEFT);
        coolDownHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label coolDownLabel = new Label(getTranslation(APPLICATION_SECTION, "Base Cool Down"));
        coolDownText = new TextField();
        coolDownText.setMaxWidth(65);
        final Label iconFileNameLabel = new Label(getTranslation(APPLICATION_SECTION, "Icon Name"));
        iconFileNameText = new TextField();
        iconFileNameText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        skillIcon.setImage(new Image(new FileInputStream(getSkillIcon(newValue))));
                    } catch (final FileNotFoundException ignored) {
                    }
                }
        );
        coolDownHBox.getChildren().addAll(coolDownLabel, coolDownText, iconFileNameLabel, iconFileNameText);

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
        builtConditionLabel.setMaxWidth(700);
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

        final HBox specialTargetHBox = new HBox();
        specialTargetHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        specialTargetHBox.setSpacing(10);
        specialTargetHBox.setAlignment(Pos.CENTER_LEFT);
        specialCheckBox = new CheckBox(getTranslation(APPLICATION_SECTION, "Special Target"));
        specialActivationTargetChoiceBox = new ChoiceBox<>();
        specialActivationTargetChoiceBox.setDisable(true);
        specialActivationTargetChoiceBox.setConverter(new EnumConverter<>(SPECIAL_ACTIVATION_SECTION));
        specialActivationTargetChoiceBox.getItems().addAll(
                SpecialActivationTarget.NO_SPECIAL_TARGET,
                SpecialActivationTarget.ORDER_CHANGE,
                SpecialActivationTarget.CARD_TYPE,
                SpecialActivationTarget.RANDOM_EFFECT
        );
        specialActivationTargetChoiceBox.getSelectionModel().selectFirst();
        specialTargetHBox.getChildren().addAll(specialCheckBox, specialActivationTargetChoiceBox);

        final HBox cardTypeHBox = new HBox();
        cardTypeHBox.setSpacing(10);
        cardTypeHBox.setAlignment(Pos.CENTER_LEFT);
        final Label cardTypeLabel = new Label(getTranslation(APPLICATION_SECTION, "Card Types"));
        cardTypeHBox.getChildren().add(cardTypeLabel);
        final List<CommandCardType> cardTypes = ImmutableList.of(QUICK, ARTS, BUSTER);
        cardTypeCheckboxes = new ArrayList<>();
        for (final CommandCardType cardType : cardTypes) {
            final CheckBox checkBox = new CheckBox(getTranslation(COMMAND_CARD_TYPE_SECTION, cardType.name()));
            cardTypeCheckboxes.add(checkBox);
            cardTypeHBox.getChildren().add(checkBox);
        }

        final HBox randomEffectHBox = new HBox();
        randomEffectHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        randomEffectHBox.setSpacing(10);
        final Label randomEffectLabel = new Label(getTranslation(APPLICATION_SECTION, "Random Effects"));

        randomEffectHBox.getChildren().add(randomEffectLabel);

        final VBox randomEffectVBox = new VBox();
        randomEffectVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        randomEffectVBox.setSpacing(10);
        HBox.setHgrow(randomEffectVBox, Priority.ALWAYS);
        randomEffects = new ListView<>();
        randomEffects.setCellFactory(new EffectsCellFactory(errorLabel));
        randomEffects.setItems(FXCollections.observableArrayList());
        randomEffects.setMaxHeight(235);
        final Button addRandomEffectButton = new Button(getTranslation(APPLICATION_SECTION, "Add Effect"));
        addRandomEffectButton.setOnAction(e -> {
            try {
                final EffectData.Builder builder = EffectData.newBuilder();
                createEffect(addRandomEffectButton.getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    randomEffects.getItems().add(new DataWrapper<>(builder.build()));
                }
            } catch (final IOException ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
                errorLabel.setVisible(true);
            }
        });
        final HBox randomEffectButtonHBox = new HBox();
        randomEffectButtonHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        randomEffectButtonHBox.setSpacing(10);
        randomEffectButtonHBox.setAlignment(Pos.CENTER_RIGHT);
        randomEffectButtonHBox.getChildren().addAll(addRandomEffectButton);

        randomEffectVBox.getChildren().addAll(randomEffects, randomEffectButtonHBox);
        randomEffectHBox.getChildren().add(randomEffectVBox);

        cardTypeHBox.setVisible(false);
        cardTypeHBox.setManaged(false);
        randomEffectHBox.setVisible(false);
        randomEffectHBox.setManaged(false);
        specialCheckBox.setOnAction(e -> specialActivationTargetChoiceBox.setDisable(!specialCheckBox.isSelected()));
        specialActivationTargetChoiceBox.setOnAction(e -> {
            cardTypeHBox.setVisible(false);
            cardTypeHBox.setManaged(false);
            randomEffectHBox.setVisible(false);
            randomEffectHBox.setManaged(false);
            switch (specialActivationTargetChoiceBox.getValue()) {
                case CARD_TYPE -> {
                    cardTypeHBox.setVisible(true);
                    cardTypeHBox.setManaged(true);
                }
                case RANDOM_EFFECT -> {
                    randomEffectHBox.setVisible(true);
                    randomEffectHBox.setManaged(true);
                }
            }
        });

        final HBox effectsHBox = new HBox();
        effectsHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        effectsHBox.setSpacing(10);
        final Label effectsLabel = new Label(getTranslation(APPLICATION_SECTION, "Effects"));

        effectsHBox.getChildren().add(effectsLabel);

        final VBox effectsVBox = new VBox();
        effectsVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        effectsVBox.setSpacing(10);
        HBox.setHgrow(effectsVBox, Priority.ALWAYS);
        skillEffects = new ListView<>();
        skillEffects.setCellFactory(new EffectsCellFactory(errorLabel));
        skillEffects.setItems(FXCollections.observableArrayList());
        skillEffects.setMaxHeight(235);
        final Button addEffectsButton = new Button(getTranslation(APPLICATION_SECTION, "Add Effect"));
        addEffectsButton.setOnAction(e -> {
            try {
                final EffectData.Builder builder = EffectData.newBuilder();
                createEffect(addEffectsButton.getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    skillEffects.getItems().add(new DataWrapper<>(builder.build()));
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

        effectsVBox.getChildren().addAll(skillEffects, effectButtonHBox);
        effectsHBox.getChildren().add(effectsVBox);

        dataVBox.getChildren().addAll(coolDownHBox, conditionHBox, specialTargetHBox, cardTypeHBox, randomEffectHBox, effectsHBox);
        nodes.add(dataVBox);
    }

    public ActiveSkillUpgrade(final ActiveSkillUpgrade source) {
        this();
        setFrom(source);
    }

    public ActiveSkillUpgrade(final ActiveSkillData activeSkillData) {
        this();
        setFrom(activeSkillData);
    }

    public void setFrom(final ActiveSkillUpgrade source) {
        this.iconFileNameText.setText(source.iconFileNameText.getText());
        this.coolDownText.setText(source.coolDownText.getText());
        this.skillEffects.getItems().addAll(source.skillEffects.getItems());
        this.activationCondition = source.activationCondition;
        if (this.activationCondition != null) {
            this.builtConditionLabel.setText(printConditionData(this.activationCondition));
        }
        this.conditionCheckBox.setSelected(source.conditionCheckBox.isSelected());
        this.conditionCheckBox.fireEvent(new ActionEvent());
        this.randomEffects.getItems().addAll(source.randomEffects.getItems());
        for (int i = 0; i < this.cardTypeCheckboxes.size(); i += 1) {
            this.cardTypeCheckboxes.get(i).setSelected(source.cardTypeCheckboxes.get(i).isSelected());
        }
        this.specialActivationTargetChoiceBox.getSelectionModel().select(source.specialActivationTargetChoiceBox.getValue());
        this.specialCheckBox.setSelected(source.specialCheckBox.isSelected());
        this.specialCheckBox.fireEvent(new ActionEvent());
    }

    public ActiveSkillData build() {
        final ActiveSkillData.Builder builder = ActiveSkillData.newBuilder()
                .setBaseCoolDown(Integer.parseInt(coolDownText.getText().trim()))
                .addAllEffects(skillEffects.getItems().stream().map(e -> e.protoData).collect(Collectors.toList()))
                .setIconName(iconFileNameText.getText());
        if (conditionCheckBox.isSelected()) {
            builder.setActivationCondition(activationCondition);
        }
        if (specialCheckBox.isSelected()) {
            final SpecialActivationParams.Builder specialTargetBuilder = SpecialActivationParams.newBuilder()
                            .setSpecialTarget(specialActivationTargetChoiceBox.getValue())
                            .addAllRandomEffectSelections(
                                    randomEffects.getItems().stream().map(e -> e.protoData).collect(Collectors.toList())
                            );
            final List<CommandCardType> cardTypes = ImmutableList.of(QUICK, ARTS, BUSTER);
            for (int i = 0; i < cardTypeCheckboxes.size(); i++) {
                if (cardTypeCheckboxes.get(i).isSelected()) {
                    specialTargetBuilder.addCardTypeSelections(cardTypes.get(i));
                }
            }
            builder.setSpecialActivationParams(specialTargetBuilder);
        }
        return builder.build();
    }

    public void setFrom(final ActiveSkillData activeSkillData) {
        iconFileNameText.setText(activeSkillData.getIconName());
        coolDownText.setText(Integer.toString(activeSkillData.getBaseCoolDown()));
        skillEffects.getItems().addAll(activeSkillData.getEffectsList().stream().map(DataWrapper::new).collect(Collectors.toList()));
        if (activeSkillData.hasActivationCondition()) {
            conditionCheckBox.setSelected(true);
            conditionCheckBox.fireEvent(new ActionEvent());
            activationCondition = activeSkillData.getActivationCondition();
            builtConditionLabel.setText(printConditionData(activationCondition));
        }
        if (activeSkillData.hasSpecialActivationParams()) {
            specialCheckBox.setSelected(true);
            specialCheckBox.fireEvent(new ActionEvent());
            final SpecialActivationParams specialActivationParams = activeSkillData.getSpecialActivationParams();
            randomEffects.getItems().addAll(
                    specialActivationParams.getRandomEffectSelectionsList().stream().map(DataWrapper::new).collect(Collectors.toList())
            );

            for (final CommandCardType cardType : specialActivationParams.getCardTypeSelectionsList()) {
                if (cardType == QUICK) {
                    cardTypeCheckboxes.get(0).setSelected(true);
                } else if (cardType == ARTS) {
                    cardTypeCheckboxes.get(1).setSelected(true);
                } else if (cardType == BUSTER) {
                    cardTypeCheckboxes.get(2).setSelected(true);
                }
            }
            specialActivationTargetChoiceBox.getSelectionModel().select(specialActivationParams.getSpecialTarget());
        }
    }
}
