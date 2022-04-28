package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.SpecificDamageUtils;

@SuperBuilder
public class TraitSpecificNpDamage extends NoblePhantasmDamage {
    private final String targetTrait;

    @Override
    protected double getNpSpecificDamageRate(final Simulation simulation, final int level) {
        if (npSpecificDamageCondition.evaluate(simulation)) {
            final double baseSpecificDamageRate = isNpSpecificDamageOverchargedEffect ?
                    npSpecificDamageRates.get(level - 1) :
                    npSpecificDamageRates.get(0);


            final int buffCount = SpecificDamageUtils.countTrait(simulation, target, targetTrait);
            return RoundUtils.roundNearest(baseSpecificDamageRate * buffCount);
        } else {
            return 1.0;
        }
    }
}
