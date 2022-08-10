package yome.fgo.simulator.gui.components;

import com.google.common.collect.ImmutableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationTarget;
import yome.fgo.simulator.gui.components.ListContainerVBox.Mode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.ResourceManager.getSkillIcon;
import static yome.fgo.simulator.gui.components.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.LIST_ITEM_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.SPECIAL_ACTIVATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ActiveSkillUpgrade extends HBox {
    private final ImageView skillIcon;
    private final TextField iconFileNameText;
    private final TextField coolDownText;
    private final CheckBox conditionCheckBox;
    private final Label builtConditionLabel;

    private ConditionData activationCondition;

    private final CheckBox specialCheckBox;
    private final List<CheckBox> cardTypeCheckboxes;
    private final ChoiceBox<SpecialActivationTarget> specialActivationTargetChoiceBox;
    private final ListContainerVBox randomEffects;
    private final ListContainerVBox skillEffects;

    public ActiveSkillUpgrade(final Label errorLabel) {
        super(10);
        setFillHeight(false);
        setMinHeight(300);
        setMaxHeight(700);
        setStyle(SPECIAL_INFO_BOX_STYLE);

        final File iconFile = getSkillIcon("default");
        Image icon = null;
        try {
            icon = new Image(new FileInputStream(iconFile));
        } catch (final FileNotFoundException ignored) {
        }
        skillIcon = new ImageView();
        if (icon != null) {
            skillIcon.setImage(icon);
        }
        skillIcon.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        skillIcon.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        final AnchorPane imgAnchor = wrapInAnchor(skillIcon);
        imgAnchor.setStyle(UNIT_THUMBNAIL_STYLE);
        setMargin(imgAnchor, new Insets(10));
        getChildren().add(imgAnchor);

        final ScrollPane otherScroll = new ScrollPane();
        otherScroll.setMinHeight(300);
        otherScroll.setPadding(new Insets(10));
        otherScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        otherScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        otherScroll.setFitToWidth(true);
        HBox.setHgrow(otherScroll, Priority.ALWAYS);
        final VBox dataVBox = new VBox(10);
        otherScroll.setContent(dataVBox);
        getChildren().add(otherScroll);

        final HBox coolDownHBox = new HBox(10);
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

        conditionCheckBox = new CheckBox(getTranslation(APPLICATION_SECTION, "Activate Condition"));
        final Button editConditionButton = new Button();
        editConditionButton.setGraphic(createInfoImageView("edit"));
        editConditionButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Edit")));

        coolDownHBox.getChildren().addAll(coolDownLabel, coolDownText, iconFileNameLabel, iconFileNameText, conditionCheckBox, editConditionButton);

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

        final HBox specialTargetHBox = new HBox(10);
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

        final HBox cardTypeHBox = new HBox(10);
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

        randomEffects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Random Effects"), errorLabel, Mode.EFFECT);

        cardTypeHBox.setVisible(false);
        cardTypeHBox.setManaged(false);
        randomEffects.setVisible(false);
        randomEffects.setManaged(false);
        specialCheckBox.setOnAction(e -> specialActivationTargetChoiceBox.setDisable(!specialCheckBox.isSelected()));
        specialActivationTargetChoiceBox.setOnAction(e -> {
            cardTypeHBox.setVisible(false);
            cardTypeHBox.setManaged(false);
            randomEffects.setVisible(false);
            randomEffects.setManaged(false);
            switch (specialActivationTargetChoiceBox.getValue()) {
                case CARD_TYPE -> {
                    cardTypeHBox.setVisible(true);
                    cardTypeHBox.setManaged(true);
                }
                case RANDOM_EFFECT -> {
                    randomEffects.setVisible(true);
                    randomEffects.setManaged(true);
                }
            }
        });

        skillEffects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Effects"), errorLabel, Mode.EFFECT);

        dataVBox.getChildren().addAll(
                coolDownHBox,
                builtConditionLabel,
                specialTargetHBox,
                cardTypeHBox,
                randomEffects,
                skillEffects
        );
    }

    public ActiveSkillUpgrade(final ActiveSkillUpgrade source, final Label errorLabel) {
        this(errorLabel);
        setFrom(source);
    }

    public ActiveSkillUpgrade(final ActiveSkillData activeSkillData, final Label errorLabel) {
        this(errorLabel);
        setFrom(activeSkillData);
    }

    public void setFrom(final ActiveSkillUpgrade source) {
        this.iconFileNameText.setText(source.iconFileNameText.getText());
        this.coolDownText.setText(source.coolDownText.getText());
        this.skillEffects.loadEffect(source.skillEffects.buildEffect());
        this.activationCondition = source.activationCondition;
        if (this.activationCondition != null) {
            this.builtConditionLabel.setText(printConditionData(this.activationCondition));
        }
        this.conditionCheckBox.setSelected(source.conditionCheckBox.isSelected());
        this.conditionCheckBox.fireEvent(new ActionEvent());
        this.randomEffects.loadEffect(source.randomEffects.buildEffect());
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
                .addAllEffects(skillEffects.buildEffect())
                .setIconName(iconFileNameText.getText());
        if (conditionCheckBox.isSelected()) {
            builder.setActivationCondition(activationCondition);
        }
        if (specialCheckBox.isSelected()) {
            final SpecialActivationParams.Builder specialTargetBuilder = SpecialActivationParams.newBuilder()
                            .setSpecialTarget(specialActivationTargetChoiceBox.getValue())
                            .addAllRandomEffectSelections(randomEffects.buildEffect());
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
        skillEffects.loadEffect(activeSkillData.getEffectsList());
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
            randomEffects.loadEffect(specialActivationParams.getRandomEffectSelectionsList());

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
