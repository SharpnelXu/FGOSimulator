package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.SpecificDamageUtils;

@SuperBuilder
public class TraitSpecificAttackBuff extends SpecificAttackBuff {
    private final String targetTrait;
    private final Target target;

    @Override
    public double getValue(final Simulation simulation) {
        final int buffCount = SpecificDamageUtils.countTrait(simulation, target, targetTrait);

        return RoundUtils.roundNearest(value * buffCount);
    }
}
