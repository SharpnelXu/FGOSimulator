package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

import java.util.List;

import static yome.fgo.simulator.utils.HpVariedValuedBuffUtils.calculateValue;

@SuperBuilder
public class HpVariedNpDamage extends NoblePhantasmDamage {
    private final boolean isHpVariedDamageOverchargeEffect;
    private final List<Double> additionDamageRates;

    @Override
    protected double getDamageRate(final Simulation simulation, final int level) {
        final double baseDamageRate = isNpDamageOverchargedEffect ? damageRates.get(level - 1) : damageRates.get(0);
        final double additionDamageRate = isHpVariedDamageOverchargeEffect ? additionDamageRates.get(level - 1) : additionDamageRates.get(0);

        return calculateValue(simulation.getAttacker(), baseDamageRate, additionDamageRate, 1, 0);
    }
}
