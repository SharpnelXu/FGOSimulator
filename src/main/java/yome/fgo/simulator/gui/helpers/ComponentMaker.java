package yome.fgo.simulator.gui.helpers;

import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.gui.components.EnumConverter;

import java.util.List;

import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;

public class ComponentMaker {
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
}
