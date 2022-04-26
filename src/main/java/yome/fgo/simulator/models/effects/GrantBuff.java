package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
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
    protected final boolean isBuffOvercharged;
    protected final List<BuffData> buffData; // here stores each overcharged effect
    protected final Target target;
    protected final int buffLevel;

    protected final boolean isProbabilityOvercharged;
    protected final List<Double> probabilities;
    @Builder.Default
    private final int repeatTimes = 1;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            for (int i = 0; i < repeatTimes; i++) {
                if (shouldApply(simulation)) {
                    final BuffData buffDataToUse = isBuffOvercharged ?
                            buffData.get(level - 1) :
                            buffData.get(0);

                    final Buff buff = BuffFactory.buildBuff(buffDataToUse, buffLevel);
                    simulation.setCurrentBuff(buff);

                    final double probability;
                    if (!probabilities.isEmpty()) {
                        probability = isProbabilityOvercharged ?
                                probabilities.get(level - 1) :
                                probabilities.get(0);
                    } else {
                        probability = 1;
                    }

                    final boolean success = grantBuff(simulation, buff, combatant, probability);
                    if (!success) {
                        break;
                    }

                    simulation.setCurrentBuff(null);
                }
            }

            simulation.setEffectTarget(null);
        }
    }

    public boolean grantBuff(
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
            boolean canActivate = true;
            if (!buff.isStackable()) {
                for (final Buff existingBuff : combatant.getBuffs()) {
                    if (existingBuff.getClass().isInstance(buff)) {
                        canActivate = false;
                        break;
                    }
                }
            }

            if (canActivate) {
                combatant.addBuff(buff);
                afterBuffAdditionalChange(simulation);
                return true;
            }
        }
        return false;
    }

    protected void afterBuffAdditionalChange(final Simulation simulation) {
        // for subClass to override
    }
}
