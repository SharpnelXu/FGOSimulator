package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffChanceBuff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.models.effects.buffs.DebuffChanceBuff;
import yome.fgo.simulator.models.effects.buffs.DebuffResist;
import yome.fgo.simulator.models.effects.buffs.ReceivedBuffChanceBuff;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class GrantBuff extends Effect {
    protected final List<BuffData> buffData; // here stores each overcharged effect
    protected final Target target;
    protected final int buffLevel;

    @Builder.Default
    protected final double probability = 1;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final Buff buff = BuffFactory.buildBuff(buffData.get(level - 1), buffLevel);
                simulation.setCurrentBuff(buff);
                grantBuff(simulation, buff, combatant, probability);
                simulation.setCurrentBuff(null);
            }
            simulation.setEffectTarget(null);
        }
    }

    public static void grantBuff(
            final Simulation simulation,
            final Buff buff,
            final Combatant combatant,
            final double probability
    ) {
        final double activationProbability;
        if (buff.isDebuff()) {
            final double debuffChance = simulation.getActivator().applyBuff(simulation, DebuffChanceBuff.class);
            final double debuffResist = combatant.applyBuff(simulation, DebuffResist.class);

            activationProbability = RoundUtils.roundNearest(probability + debuffChance - debuffResist);
        } else if (buff.isBuff()) {
            final double buffChance = simulation.getActivator().applyBuff(simulation, BuffChanceBuff.class);
            final double receivedBuffChance = combatant.applyBuff(simulation, ReceivedBuffChanceBuff.class);

            activationProbability = RoundUtils.roundNearest(probability + buffChance + receivedBuffChance);
        } else {
            activationProbability = probability;
        }

        if (activationProbability >= simulation.getProbabilityThreshold()) {
            if (!buff.isStackable()) {
                boolean alreadyPresent = false;
                for (final Buff existingBuff : combatant.getBuffs()) {
                    if (existingBuff.getClass().isInstance(buff)) {
                        alreadyPresent = true;
                        break;
                    }
                }
                if (!alreadyPresent) {
                    combatant.addBuff(buff);
                }
            } else {
                combatant.addBuff(buff);
            }
        }
    }
}
