package yome.fgo.simulator.gui.components;

import javafx.util.StringConverter;
import lombok.AllArgsConstructor;

import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class EnumConverter<E extends Enum<E>> extends StringConverter<E> {
    private final String section;

    @Override
    public String toString(final E object) {
        return getTranslation(section, object.name());
    }

    @Override
    public E fromString(final String string) {
        return null;
    }
}
