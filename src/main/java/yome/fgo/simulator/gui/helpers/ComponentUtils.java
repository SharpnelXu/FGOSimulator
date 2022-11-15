package yome.fgo.simulator.gui.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.data.proto.FgoStorageData.Traits;
import yome.fgo.simulator.gui.components.EnumConverter;
import yome.fgo.simulator.gui.components.SimulationWindow;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_ADV_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasKeyForTrait;
import static yome.fgo.simulator.utils.FilePathUtils.CLASS_ICON_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SIMULATION_ICON_DIRECTORY_PATH;

public class ComponentUtils {
    public static final String PERMANENT_BUFF_STYLE = "-fx-border-color: grey; -fx-border-style: solid; -fx-border-width: 1; -fx-border-radius: 3px";
    public static final String CD_NUMBER_STYLE = "-fx-background-color: rgba(0,0,0,0.78); -fx-border-radius: 3; -fx-border-width: 1";
    public static final String SPECIAL_INFO_BOX_STYLE = "-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-width: 2; -fx-background-color: white";
    public static final String UNIT_DISPLAY_STYLE = "-fx-background-color: white; -fx-border-color: grey; -fx-border-width: 3; -fx-border-radius: 3";
    public static final String UNIT_THUMBNAIL_STYLE = "-fx-border-color: rgba(73,73,73,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2; -fx-background-color: white";
    public static final String CC_THUMBNAIL_STYLE = "-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2; -fx-background-color: rgba(220,245,255,0.73)";
    public static final String LIST_ITEM_STYLE = "-fx-border-color: grey; -fx-border-style: solid; -fx-border-width: 2; -fx-border-radius: 3px; -fx-background-color: white";

    public static final int SERVANT_THUMBNAIL_SIZE = 100;
    public static final int SKILL_THUMBNAIL_SIZE = 50;
    public static final int INFO_THUMBNAIL_SIZE = 30;
    public static final int BUFF_SIZE = 20;

    public static final Map<String, String> FIELD_ICON_MAP = ImmutableMap.of(
            Traits.CITY.name(), "fieldCity",
            Traits.MILLENNIUM_CASTLE.name(), "fieldMillenniumCastle",
            Traits.BURNING.name(), "fieldBurn",
            Traits.FOREST.name(), "fieldForest",
            Traits.SHORE.name(), "fieldShoreline",
            Traits.SUNLIGHT.name(), "fieldSunlight",
            Traits.IMAGINARY_SPACE.name(), "fieldImaginary"
    );

    public static String getFieldIcon(final String fieldTrait) {
        if (FIELD_ICON_MAP.containsKey(fieldTrait)) {
            return FIELD_ICON_MAP.get(fieldTrait);
        }

        return "default";
    }

    public static final Map<FateClass, String> CLASS_ICON_MAP = buildClassIconMap();

    private static Map<FateClass, String> buildClassIconMap() {
        final ImmutableMap.Builder<FateClass, String> builder = ImmutableMap.builder();
        builder.put(FateClass.SABER, "saber");
        builder.put(FateClass.ARCHER, "archer");
        builder.put(FateClass.LANCER, "lancer");
        builder.put(FateClass.RIDER, "rider");
        builder.put(FateClass.CASTER, "caster");
        builder.put(FateClass.ASSASSIN, "assassin");
        builder.put(FateClass.BERSERKER, "berserker");
        builder.put(FateClass.SHIELDER, "shielder");
        builder.put(FateClass.RULER, "ruler");
        builder.put(FateClass.AVENGER, "avenger");
        builder.put(FateClass.MOONCANCER, "mooncancer");
        builder.put(FateClass.ALTEREGO, "alterego");
        builder.put(FateClass.FOREIGNER, "foreigner");
        builder.put(FateClass.PRETENDER, "pretender");
        builder.put(FateClass.BEAST_I, "Beast_I");
        builder.put(FateClass.BEAST_II, "Beast_II");
        builder.put(FateClass.BEAST_III_L, "Beast_III");
        builder.put(FateClass.BEAST_III_R, "Beast_III");
        builder.put(FateClass.BEAST_IV, "Beast_IV");
        builder.put(FateClass.ANY_CLASS, "all");
        builder.put(FateClass.ANY_BEAST, "Beast");

        return builder.build();
    }
    public static final String COMMA_SPLIT_REGEX = "\s*[，,、]\s*";

    private static final List<Target> VALID_TARGETS = ImmutableList.of(
            Target.SELF,

            Target.ATTACKER,
            Target.DEFENDER,
            Target.EFFECT_TARGET,
            Target.ACTIVATOR,

            Target.TARGETED_ALLY,
            Target.TARGETED_ENEMY,
            Target.NON_TARGETED_ALLIES,
            Target.NON_TARGETED_ENEMIES,

            Target.ALL_ALLIES,
            Target.ALL_ALLIES_INCLUDING_BACKUP,
            Target.ALL_ENEMIES,
            Target.ALL_ENEMIES_INCLUDING_BACKUP,

            Target.ALL_ALLIES_EXCLUDING_SELF,
            Target.ALL_ALLIES_EXCLUDING_SELF_INCLUDING_BACKUP,

            Target.FIRST_ALLY_EXCLUDING_SELF,
            Target.LAST_ALLY_EXCLUDING_SELF,
            Target.FIRST_ENEMY,
            Target.LAST_ENEMY,

            Target.ALL_CHARACTERS,
            Target.ALL_CHARACTERS_INCLUDING_BACKUP,
            Target.ALL_CHARACTERS_EXCLUDING_SELF,
            Target.ALL_CHARACTERS_EXCLUDING_SELF_INCLUDING_BACKUP
    );

    public static void fillFateClass(final ChoiceBox<FateClass> classChoiceBox) {
        final List<FateClass> fateClasses = Lists.newArrayList(FateClass.values());
        fateClasses.remove(FateClass.NO_CLASS);
        fateClasses.remove(FateClass.UNRECOGNIZED);
        classChoiceBox.setConverter(new EnumConverter<>(CLASS_SECTION));
        classChoiceBox.setItems(FXCollections.observableArrayList(fateClasses));
        classChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillGender(final ChoiceBox<Gender> genderChoiceBox) {
        final List<Gender> genderClasses = Lists.newArrayList(Gender.values());
        genderClasses.remove(Gender.UNRECOGNIZED);
        genderChoiceBox.setConverter(new EnumConverter<>(TRAIT_SECTION));
        genderChoiceBox.setItems(FXCollections.observableArrayList(genderClasses));
        genderChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillAttribute(final ChoiceBox<Attribute> attributeChoiceBox) {
        final List<Attribute> attributeClasses = Lists.newArrayList(Attribute.values());
        attributeClasses.remove(Attribute.NO_ATTRIBUTE);
        attributeClasses.remove(Attribute.UNRECOGNIZED);
        attributeChoiceBox.setConverter(new EnumConverter<>(TRAIT_SECTION));
        attributeChoiceBox.setItems(FXCollections.observableArrayList(attributeClasses));
        attributeChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillTargets(final ChoiceBox<Target> targetChoiceBox) {
        targetChoiceBox.setConverter(new EnumConverter<>(TARGET_SECTION));
        targetChoiceBox.setItems(FXCollections.observableArrayList(VALID_TARGETS));
        targetChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillCommandCardType(final ChoiceBox<CommandCardType> commandCardTypeChoiceBox) {
        final List<CommandCardType> cardTypes = Lists.newArrayList(CommandCardType.values());
        cardTypes.remove(CommandCardType.UNRECOGNIZED);
        cardTypes.remove(CommandCardType.ANY);
        commandCardTypeChoiceBox.setConverter(new EnumConverter<>(COMMAND_CARD_TYPE_SECTION));
        commandCardTypeChoiceBox.setItems(FXCollections.observableArrayList(cardTypes));
        commandCardTypeChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillClassAdvMode(final ChoiceBox<ClassAdvantageChangeMode> classAdvModeChoiceBox) {
        final List<ClassAdvantageChangeMode> values = Lists.newArrayList(ClassAdvantageChangeMode.values());
        values.remove(ClassAdvantageChangeMode.UNRECOGNIZED);
        classAdvModeChoiceBox.setConverter(new EnumConverter<>(CLASS_ADV_SECTION));
        classAdvModeChoiceBox.setItems(FXCollections.observableArrayList(values));
        classAdvModeChoiceBox.getSelectionModel().selectFirst();
    }

    public static void addSplitTraitListener(final TextField traitTextField, final Label errorLabel) {
        traitTextField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final List<String> unmappedTraits = Arrays.stream(newValue.split(COMMA_SPLIT_REGEX))
                            .sequential()
                            .filter(s -> !s.isEmpty() && !hasKeyForTrait(s))
                            .collect(Collectors.toList());

                    if (!unmappedTraits.isEmpty()) {
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Warning: unmapped traits:") + unmappedTraits);
                        errorLabel.setVisible(true);
                    } else {
                        errorLabel.setVisible(false);
                    }
                }
        );
    }

    public static String getClassIcon(final FateClass fateClass) {
        if (CLASS_ICON_MAP.containsKey(fateClass)) {
            return CLASS_ICON_MAP.get(fateClass);
        }
        return "unknown";
    }

    public static void wrapInAnchor(final AnchorPane anchorPane, final Node node) {
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
        anchorPane.getChildren().add(node);
    }

    public static AnchorPane wrapInAnchor(final Node node) {
        final AnchorPane anchorPane = new AnchorPane();
        wrapInAnchor(anchorPane, node);
        return anchorPane;
    }

    public static AnchorPane createSkillCdAnchor() {
        final Label cdLabel = new Label("X");
        cdLabel.setFont(new Font(30));
        cdLabel.setStyle("-fx-text-fill: white");
        cdLabel.setAlignment(Pos.CENTER);
        cdLabel.setWrapText(true);
        cdLabel.setMaxWidth(60);

        final AnchorPane cdAnchor = wrapInAnchor(cdLabel);
        cdAnchor.setStyle(CD_NUMBER_STYLE);
        return cdAnchor;
    }

    public static void renderBuffPane(final FlowPane buffsPane, final Combatant combatant, final SimulationWindow simulationWindow) {
        buffsPane.getChildren().clear();
        final List<Buff> buffs = combatant.getBuffs();
        for (final Buff buff : buffs) {
            final ImageView buffImage = new ImageView();
            buffImage.setFitHeight(20);
            buffImage.setFitWidth(20);
            buffImage.setImage(simulationWindow.getBuffImage(buff.getIconName()));
            final AnchorPane buffImgAnchor = wrapInAnchor(buffImage);
            if (buff.isIrremovable()) {
                buffImgAnchor.setStyle(PERMANENT_BUFF_STYLE);
            }
            buffsPane.getChildren().add(buffImgAnchor);
        }
    }

    public static Label createBoldLabel(final String text) {
        final Label newLabel = new Label(text);
        newLabel.setStyle("-fx-font-weight: bold");
        return newLabel;
    }

    public static ImageView createInfoImageView(final String iconName) {
        final ImageView imageView = new ImageView();
        imageView.setFitHeight(22);
        imageView.setFitWidth(22);
        final String path = String.format("%s/%s.png", SIMULATION_ICON_DIRECTORY_PATH, iconName);
        imageView.setImage(getImage(path));
        return imageView;
    }

    public static ImageView createClassImageView(final FateClass fateClass) {
        final ImageView imageView = new ImageView();
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        final String path = String.format("%s/%s.png", CLASS_ICON_DIRECTORY_PATH, getClassIcon(fateClass));
        imageView.setImage(getImage(path));
        return imageView;
    }

    public static Image getImage(final String path) {
        Image image = null;
        try {
            image = new Image(new FileInputStream(path));
        } catch (final FileNotFoundException ignored) {
        }
        return image;
    }

    public static void setWindowSize(final Parent root) {
        final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double maxWidth = Math.min(screenBounds.getWidth() * 0.9, root.prefWidth(-1));
        double maxHeight = Math.min(screenBounds.getHeight() * 0.9, root.prefHeight(-1));
        final Window window = root.getScene().getWindow();
        window.setWidth(maxWidth);
        window.setHeight(maxHeight);
    }
}
