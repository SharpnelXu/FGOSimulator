package yome.fgo.simulator.gui.components;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.AppendSkillData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.ResourceManager.getSkillIcon;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class NonActiveSkill extends HBox {
    private final ImageView skillIcon;
    private final TextField iconFileNameText;
    private final ListView<DataWrapper<EffectData>> skillEffects;
    private final Label errorLabel;

    public NonActiveSkill() {
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

        final HBox iconNameHBox = new HBox();
        iconNameHBox.setSpacing(10);
        iconNameHBox.setAlignment(Pos.CENTER_LEFT);
        iconNameHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
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

        errorLabel = new Label();
        errorLabel.setVisible(false);
        errorLabel.setStyle("-fx-text-fill: red");

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
        skillEffects.setMaxHeight(125);
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

        dataVBox.getChildren().addAll(iconNameHBox, effectsHBox);
        nodes.add(dataVBox);
    }

    public NonActiveSkill(final NonActiveSkill source) {
        this();

        this.iconFileNameText.setText(source.iconFileNameText.getText());
        this.skillEffects.getItems().addAll(source.skillEffects.getItems());
    }

    public NonActiveSkill(final PassiveSkillData passiveSkillData) {
        this();

        this.iconFileNameText.setText(passiveSkillData.getIconName());
        this.skillEffects.getItems().addAll(passiveSkillData.getEffectsList().stream().map(DataWrapper::new).collect(Collectors.toList()));
    }

    public NonActiveSkill(final AppendSkillData appendSkillData) {
        this();

        this.iconFileNameText.setText(appendSkillData.getIconName());
        this.skillEffects.getItems().addAll(appendSkillData.getEffectsList().stream().map(DataWrapper::new).collect(Collectors.toList()));
    }

    public PassiveSkillData buildPassive() {
        return PassiveSkillData.newBuilder()
                .addAllEffects(skillEffects.getItems().stream().map(e -> e.protoData).collect(Collectors.toList()))
                .setIconName(iconFileNameText.getText())
                .build();
    }

    public AppendSkillData buildAppend() {
        return AppendSkillData.newBuilder()
                .addAllEffects(skillEffects.getItems().stream().map(e -> e.protoData).collect(Collectors.toList()))
                .setIconName(iconFileNameText.getText())
                .build();
    }
}
