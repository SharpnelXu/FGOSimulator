package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.SpecificDamageUtils;
import yome.fgo.simulator.utils.TargetUtils;

@SuperBuilder
public class BuffSpecificAttackBuff extends SpecificAttackBuff {
    private final Class<?> targetBuff;
    private final Target target;

    @Override
    public double getValue(final Simulation simulation) {
        final int buffCount = SpecificDamageUtils.countBuff(simulation, target, targetBuff);

        return RoundUtils.roundNearest(value * buffCount);
    }
}
