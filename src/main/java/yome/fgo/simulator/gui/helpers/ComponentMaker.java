package yome.fgo.simulator.gui.helpers;

import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
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
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasKeyForTrait;

public class ComponentMaker {
    public static final String COMMA_SPLIT_REGEX = "\s*[，,]\s*";

    public static void fillFateClass(final ChoiceBox<FateClass> classChoiceBox) {
        final List<FateClass> fateClasses = Lists.newArrayList(FateClass.values());
        fateClasses.remove(FateClass.NO_CLASS);
        fateClasses.remove(FateClass.UNRECOGNIZED);
        classChoiceBox.setConverter(new EnumConverter<>(CLASS_SECTION));
        classChoiceBox.setItems(FXCollections.observableArrayList(fateClasses));
        classChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillTargets(final ChoiceBox<Target> targetChoiceBox) {
        final List<Target> targets = Lists.newArrayList(Target.values());
        targets.remove(Target.UNRECOGNIZED);
        targets.remove(Target.SERVANT_EXCHANGE);
        targetChoiceBox.setConverter(new EnumConverter<>(TARGET_SECTION));
        targetChoiceBox.setItems(FXCollections.observableArrayList(targets));
        targetChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillCommandCardType(final ChoiceBox<CommandCardType> targetChoiceBox) {
        final List<CommandCardType> cardTypes = Lists.newArrayList(CommandCardType.values());
        cardTypes.remove(CommandCardType.UNRECOGNIZED);
        targetChoiceBox.setConverter(new EnumConverter<>(COMMAND_CARD_TYPE_SECTION));
        targetChoiceBox.setItems(FXCollections.observableArrayList(cardTypes));
        targetChoiceBox.getSelectionModel().selectFirst();
    }

    public static void fillClassAdvMode(final ChoiceBox<ClassAdvantageChangeMode> targetChoiceBox) {
        final List<ClassAdvantageChangeMode> values = Lists.newArrayList(ClassAdvantageChangeMode.values());
        values.remove(ClassAdvantageChangeMode.UNRECOGNIZED);
        targetChoiceBox.setConverter(new EnumConverter<>(CLASS_ADV_SECTION));
        targetChoiceBox.setItems(FXCollections.observableArrayList(values));
        targetChoiceBox.getSelectionModel().selectFirst();
    }

    public static void addSplitTraitListener(final TextField targetTextField, final Label errorLabel) {
        targetTextField.textProperty().addListener(
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
