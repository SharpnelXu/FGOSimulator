package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.models.effects.buffs.BuffType.DEATH_CHANCE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEATH_RESIST;

@SuperBuilder
public class InstantDeath extends Effect {
    private final Target target;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final double deathSuccessChance = simulation.getActivator().applyBuff(simulation, DEATH_CHANCE_BUFF);
                final double deathResist = combatant.applyBuff(simulation, DEATH_RESIST);

                final double deathProbability = getProbability(level) * combatant.getDeathRate() * (1 + deathSuccessChance - deathResist);

                if (simulation.getStatsLogger() != null) {
                    simulation.getStatsLogger().logProbability(combatant.getId(), deathProbability, simulation.getProbabilityThreshold());
                }
                if (deathProbability >= simulation.getProbabilityThreshold()) {
                    combatant.instantDeath();
                }
            }
            simulation.unsetEffectTarget();
        }
    }
}
