package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.SpecificDamageUtils;

import static yome.fgo.data.proto.FgoStorageData.Target.DEFENDER;

@SuperBuilder
public class BuffSpecificNpDamage extends NoblePhantasmDamage {
    private final Class<?> targetBuff;
    @Builder.Default
    private final Target specificTarget = DEFENDER;

    @Override
    protected double getNpSpecificDamageRate(final Simulation simulation, final int level) {
        if (npSpecificDamageCondition.evaluate(simulation)) {
            final double baseSpecificDamageRate = isNpSpecificDamageOverchargedEffect ?
                    npSpecificDamageRates.get(level - 1) :
                    npSpecificDamageRates.get(0);


            final int buffCount = SpecificDamageUtils.countBuff(simulation, specificTarget, targetBuff);
            return RoundUtils.roundNearest(baseSpecificDamageRate * buffCount);
        } else {
            return 1.0;
        }
    }
}
