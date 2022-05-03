package yome.fgo.simulator.gui.helpers;

import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.gui.components.EnumConverter;

import java.util.List;

import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;

public class ComponentMaker {
    public static void fillFateClass(final ChoiceBox<FateClass> classChoiceBox) {
        final List<FateClass> fateClasses = Lists.newArrayList(FateClass.values());
        fateClasses.remove(FateClass.NO_CLASS);
        fateClasses.remove(FateClass.UNRECOGNIZED);
        classChoiceBox.setConverter(new EnumConverter<>(CLASS_SECTION));
        classChoiceBox.setItems(FXCollections.observableArrayList(fateClasses));
        classChoiceBox.getSelectionModel().selectFirst();
    }
}
