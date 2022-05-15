package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class BuffTypeEquals extends Condition {
    private final Class<?> targetBuffType;

    @Override
    public boolean evaluate(final Simulation simulation) {
        return targetBuffType.isInstance(simulation.getCurrentBuff());
    }

    @Override
    public String toString() {
        return getTranslation(BUFF_SECTION, targetBuffType.getSimpleName());
    }
}
