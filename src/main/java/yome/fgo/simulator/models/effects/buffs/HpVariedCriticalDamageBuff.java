package yome.fgo.simulator.models.effects.buffs;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.HpVariedValuedBuffUtils;

@SuperBuilder
public class HpVariedCriticalDamageBuff extends CriticalDamageBuff {
    @Builder.Default
    private final double minHpPercent = 0;
    @Builder.Default
    private final double maxHpPercent = 1;
    @Builder.Default
    private final double baseValue = 0;

    @Override
    public double getValue(final Simulation simulation) {
        return HpVariedValuedBuffUtils.calculateValue(simulation.getAttacker(), baseValue, value, maxHpPercent, minHpPercent);
    }
}
