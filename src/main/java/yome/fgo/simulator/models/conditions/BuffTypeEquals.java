package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.BuffType;

import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class BuffTypeEquals extends Condition {
    private final BuffType buffType;

    @Override
    public boolean evaluate(final Simulation simulation) {
        return buffType == simulation.getCurrentBuff().getBuffType();
    }

    @Override
    public String toString() {
        return getTranslation(BUFF_SECTION, buffType.getType());
    }
}
