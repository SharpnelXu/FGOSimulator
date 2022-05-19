package yome.fgo.simulator.gui.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.gui.components.EnumConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_ADV_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasKeyForTrait;

public class ComponentMaker {
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
}
