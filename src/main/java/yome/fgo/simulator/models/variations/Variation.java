package yome.fgo.simulator.models.variations;

import yome.fgo.simulator.models.Simulation;

import static yome.fgo.simulator.translation.TranslationManager.VARIATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public abstract class Variation {
    public abstract double evaluate(final Simulation simulation, final double baseValue, final double additionValue);

    @Override
    public String toString() {
        return getTranslation(VARIATION_SECTION, getClass().getSimpleName());
    }
}
