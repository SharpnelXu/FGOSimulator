package yome.fgo.simulator.gui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.AppendSkillData;
import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;
import yome.fgo.simulator.gui.components.ListContainerVBox.Mode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static yome.fgo.simulator.ResourceManager.getSkillIcon;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class NonActiveSkill extends HBox {
    private final ImageView skillIcon;
    private final TextField iconFileNameText;
    private final ListContainerVBox skillEffects;

    public NonActiveSkill(final VBox skillVBox, final Label errorLabel) {
        super(10);

        setPadding(new Insets(10));
        setFillHeight(false);
        setStyle(SPECIAL_INFO_BOX_STYLE);

        final List<Node> nodes = getChildren();

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

        final VBox imageVBox = new VBox(10);
        imageVBox.setAlignment(Pos.TOP_CENTER);
        imageVBox.getChildren().add(imgAnchor);
        imageVBox.setFillWidth(false);

        final Button upButton = new Button();
        upButton.setGraphic(createInfoImageView("up"));
        upButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move Skill Up")));
        upButton.setOnAction(e -> {
            final ObservableList<Node> children = skillVBox.getChildren();
            if (children.isEmpty() || children.size() == 1) {
                return;
            }

            final int index = children.indexOf(this);
            if (index > 0) {
                final ObservableList<Node> workingCollection = FXCollections.observableArrayList(children);
                Collections.swap(workingCollection, index - 1, index);
                children.setAll(workingCollection);
            }
        });

        final Button downButton = new Button();
        downButton.setGraphic(createInfoImageView("down"));
        downButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move Skill Down")));
        downButton.setOnAction(e -> {
            final ObservableList<Node> children = skillVBox.getChildren();
            if (children.isEmpty() || children.size() == 1) {
                return;
            }

            final int index = children.indexOf(this);
            if (index < children.size() - 1 && index >= 0) {
                final ObservableList<Node> workingCollection = FXCollections.observableArrayList(children);
                Collections.swap(workingCollection, index + 1, index);
                children.setAll(workingCollection);
            }
        });

        final Button removeButton = new Button();
        removeButton.setGraphic(createInfoImageView("remove"));
        removeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Remove Skill")));
        removeButton.setOnAction(e -> {
            final ObservableList<Node> children = skillVBox.getChildren();
            final List<Node> remainingNodes = new ArrayList<>(children);
            remainingNodes.remove(this);
            children.setAll(remainingNodes);
        });

        final HBox buttonHBox = new HBox(5);
        buttonHBox.getChildren().addAll(upButton, downButton, removeButton);

        imageVBox.getChildren().add(buttonHBox);

        nodes.add(imageVBox);

        final VBox dataVBox = new VBox(10);
        HBox.setHgrow(dataVBox, Priority.ALWAYS);

        final HBox iconNameHBox = new HBox(10);
        iconNameHBox.setAlignment(Pos.CENTER_LEFT);
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
        iconNameHBox.getChildren().addAll(iconFileNameLabel, iconFileNameText);

        skillEffects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Effects"), errorLabel, Mode.EFFECT);

        dataVBox.getChildren().addAll(iconNameHBox, skillEffects);
        nodes.add(dataVBox);
    }

    public NonActiveSkill(final NonActiveSkill source, final VBox passiveSkillVBox, final Label errorLabel) {
        this(passiveSkillVBox, errorLabel);

        this.iconFileNameText.setText(source.iconFileNameText.getText());
        this.skillEffects.clear();
        this.skillEffects.loadEffect(source.skillEffects.buildEffect());
    }

    public NonActiveSkill(final PassiveSkillData passiveSkillData, final VBox passiveSkillVBox, final Label errorLabel) {
        this(passiveSkillVBox, errorLabel);

        this.iconFileNameText.setText(passiveSkillData.getIconName());
        this.skillEffects.clear();
        this.skillEffects.loadEffect(passiveSkillData.getEffectsList());
    }

    public NonActiveSkill(final AppendSkillData appendSkillData, final VBox passiveSkillVBox, final Label errorLabel) {
        this(passiveSkillVBox, errorLabel);

        this.iconFileNameText.setText(appendSkillData.getIconName());
        this.skillEffects.clear();
        this.skillEffects.loadEffect(appendSkillData.getEffectsList());
    }

    public PassiveSkillData buildPassive() {
        return PassiveSkillData.newBuilder()
                .addAllEffects(skillEffects.buildEffect())
                .setIconName(iconFileNameText.getText())
                .build();
    }

    public AppendSkillData buildAppend() {
        return AppendSkillData.newBuilder()
                .addAllEffects(skillEffects.buildEffect())
                .setIconName(iconFileNameText.getText())
                .build();
    }
}
