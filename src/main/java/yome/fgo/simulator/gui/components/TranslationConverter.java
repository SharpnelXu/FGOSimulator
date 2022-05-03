package yome.fgo.simulator.gui.components;

import javafx.util.StringConverter;
import lombok.AllArgsConstructor;

import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class TranslationConverter extends StringConverter<String> {
    private final String section;

    @Override
    public String toString(final String object) {
        return getTranslation(section, object);
    }

    @Override
    public String fromString(final String string) {
        return null;
    }
}
