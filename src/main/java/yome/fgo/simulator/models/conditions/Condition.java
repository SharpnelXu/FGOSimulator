package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public abstract class Condition {
    public abstract boolean evaluate(final Simulation simulation);

    @Override
    public String toString() {
        return getTranslation(CONDITION_SECTION, this.getClass().getSimpleName());
    }
}
