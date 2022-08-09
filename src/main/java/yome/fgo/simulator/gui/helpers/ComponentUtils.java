package yome.fgo.simulator.gui.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import yome.fgo.data.proto.FgoStorageData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
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
import static yome.fgo.simulator.utils.FilePathUtils.SIMULATION_ICON_DIRECTORY_PATH;

public class ComponentUtils {
    public static final String PERMANENT_BUFF_STYLE = "-fx-border-color: grey; -fx-border-style: solid; -fx-border-width: 1; -fx-border-radius: 3px";
    public static final String CD_NUMBER_STYLE = "-fx-background-color: rgba(0,0,0,0.78); -fx-border-radius: 3; -fx-border-width: 1";
    public static final String SPECIAL_INFO_BOX_STYLE = "-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-width: 5; -fx-background-color: white";
    public static final String UNIT_DISPLAY_STYLE = "-fx-background-color: white; -fx-border-color: grey; -fx-border-width: 3; -fx-border-radius: 3";
    public static final String UNIT_THUMBNAIL_STYLE = "-fx-border-color: rgba(73,73,73,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2; -fx-background-color: white";
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

        return builder.build();
    }
    public static final String COMMA_SPLIT_REGEX = "\s*[，,、]\s*";

    private static final List<FgoStorageData.Target> VALID_TARGETS = ImmutableList.of(
            FgoStorageData.Target.SELF,

            FgoStorageData.Target.ATTACKER,
            FgoStorageData.Target.DEFENDER,
            FgoStorageData.Target.EFFECT_TARGET,
            FgoStorageData.Target.ACTIVATOR,

            FgoStorageData.Target.TARGETED_ALLY,
            FgoStorageData.Target.TARGETED_ENEMY,
            FgoStorageData.Target.NON_TARGETED_ALLIES,
            FgoStorageData.Target.NON_TARGETED_ENEMIES,

            FgoStorageData.Target.ALL_ALLIES,
            FgoStorageData.Target.ALL_ALLIES_INCLUDING_BACKUP,
            FgoStorageData.Target.ALL_ENEMIES,
            FgoStorageData.Target.ALL_ENEMIES_INCLUDING_BACKUP,

            FgoStorageData.Target.ALL_ALLIES_EXCLUDING_SELF,
            FgoStorageData.Target.ALL_ALLIES_EXCLUDING_SELF_INCLUDING_BACKUP,

            FgoStorageData.Target.FIRST_ALLY_EXCLUDING_SELF,
            FgoStorageData.Target.LAST_ALLY_EXCLUDING_SELF,
            FgoStorageData.Target.FIRST_ENEMY,
            FgoStorageData.Target.LAST_ENEMY,

            FgoStorageData.Target.ALL_CHARACTERS,
            FgoStorageData.Target.ALL_CHARACTERS_INCLUDING_BACKUP,
            FgoStorageData.Target.ALL_CHARACTERS_EXCLUDING_SELF,
            FgoStorageData.Target.ALL_CHARACTERS_EXCLUDING_SELF_INCLUDING_BACKUP
    );

    public static void fillFateClass(final ChoiceBox<FateClass> classChoiceBox) {
        final List<FateClass> fateClasses = Lists.newArrayList(FateClass.values());
        fateClasses.remove(FateClass.NO_CLASS);
        fateClasses.remove(FateClass.UNRECOGNIZED);
        classChoiceBox.setConverter(new EnumConverter<>(CLASS_SECTION));
        classChoiceBox.setItems(FXCollections.observableArrayList(fateClasses));
        classChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillGender(final ChoiceBox<FgoStorageData.Gender> genderChoiceBox) {
        final List<FgoStorageData.Gender> genderClasses = Lists.newArrayList(FgoStorageData.Gender.values());
        genderClasses.remove(FgoStorageData.Gender.UNRECOGNIZED);
        genderChoiceBox.setConverter(new EnumConverter<>(TRAIT_SECTION));
        genderChoiceBox.setItems(FXCollections.observableArrayList(genderClasses));
        genderChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillAttribute(final ChoiceBox<FgoStorageData.Attribute> attributeChoiceBox) {
        final List<FgoStorageData.Attribute> attributeClasses = Lists.newArrayList(FgoStorageData.Attribute.values());
        attributeClasses.remove(FgoStorageData.Attribute.NO_ATTRIBUTE);
        attributeClasses.remove(FgoStorageData.Attribute.UNRECOGNIZED);
        attributeChoiceBox.setConverter(new EnumConverter<>(TRAIT_SECTION));
        attributeChoiceBox.setItems(FXCollections.observableArrayList(attributeClasses));
        attributeChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillTargets(final ChoiceBox<FgoStorageData.Target> targetChoiceBox) {
        targetChoiceBox.setConverter(new EnumConverter<>(TARGET_SECTION));
        targetChoiceBox.setItems(FXCollections.observableArrayList(VALID_TARGETS));
        targetChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillCommandCardType(final ChoiceBox<FgoStorageData.CommandCardType> commandCardTypeChoiceBox) {
        final List<FgoStorageData.CommandCardType> cardTypes = Lists.newArrayList(FgoStorageData.CommandCardType.values());
        cardTypes.remove(FgoStorageData.CommandCardType.UNRECOGNIZED);
        commandCardTypeChoiceBox.setConverter(new EnumConverter<>(COMMAND_CARD_TYPE_SECTION));
        commandCardTypeChoiceBox.setItems(FXCollections.observableArrayList(cardTypes));
        commandCardTypeChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillClassAdvMode(final ChoiceBox<FgoStorageData.ClassAdvantageChangeMode> classAdvModeChoiceBox) {
        final List<FgoStorageData.ClassAdvantageChangeMode> values = Lists.newArrayList(FgoStorageData.ClassAdvantageChangeMode.values());
        values.remove(FgoStorageData.ClassAdvantageChangeMode.UNRECOGNIZED);
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

    public static AnchorPane wrapInAnchor(final Node node) {
        final AnchorPane imgAnchor = new AnchorPane();
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
        imgAnchor.getChildren().add(node);
        return imgAnchor;
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
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        final String path = String.format("%s/%s.png", SIMULATION_ICON_DIRECTORY_PATH, iconName);
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
}
