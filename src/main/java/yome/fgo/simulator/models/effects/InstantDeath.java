package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.DeathChanceBuff;
import yome.fgo.simulator.models.effects.buffs.DeathResist;
import yome.fgo.simulator.utils.TargetUtils;

@SuperBuilder
public class InstantDeath extends Effect {
    private final Target target;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final double deathSuccessChance = simulation.getActivator().applyBuff(simulation, DeathChanceBuff.class);
                final double deathResist = combatant.applyBuff(simulation, DeathResist.class);

                final double deathProbability = probabilities.get(level) * combatant.getDeathRate() * (1 + deathSuccessChance - deathResist);

                if (deathProbability < simulation.getProbabilityThreshold()) {
                    break;
                }
                combatant.instantDeath();
            }
            simulation.setEffectTarget(null);
        }
    }
}
