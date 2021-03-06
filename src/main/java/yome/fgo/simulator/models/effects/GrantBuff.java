package yome.fgo.simulator.models.effects;

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
import java.util.stream.Collectors;

import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class GrantBuff extends Effect {
    protected final boolean isBuffOvercharged;
    protected final List<BuffData> buffData; // here stores each overcharged effect
    protected final Target target;
    protected final int buffLevel;

    protected Buff buildBuff(final int level) {
        final BuffData buffDataToUse = isBuffOvercharged ?
                buffData.get(level - 1) :
                buffData.get(0);
        return BuffFactory.buildBuff(buffDataToUse, buffLevel);
    }

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final double probability = getProbability(level);

        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);

            if (shouldApply(simulation) && combatant.getCurrentHp() > 0) {
                final Buff buff = buildBuff(level);

                if (simulation.isActivatingCePassiveEffects() || simulation.isActivatingServantPassiveEffects()) {
                    buff.setIrremovable(true);
                    buff.setIsPassive(true);

                    if (simulation.isActivatingServantPassiveEffects()) {
                        buff.setActivator(simulation.getActivator());
                    }
                }

                simulation.setCurrentBuff(buff);

                grantBuff(simulation, buff, combatant, probability);

                simulation.unsetCurrentBuff();
            }

            simulation.unsetEffectTarget();
        }
    }

    private void grantBuff(
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

        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logProbability(combatant.getId(), activationProbability, simulation.getProbabilityThreshold());
        }
        if (activationProbability < simulation.getProbabilityThreshold()) {
            return;
        }

        boolean canActivate = true;
        if (!buff.isStackable()) {
            for (final Buff existingBuff : combatant.getBuffs()) {
                if (existingBuff.getClass().isInstance(buff) && !existingBuff.isStackable()) {
                    canActivate = false;
                    break;
                }
            }
        }

        if (canActivate) {
            combatant.addBuff(buff);
            afterBuffAdditionalChange(simulation);
        }
    }

    protected void afterBuffAdditionalChange(final Simulation simulation) {
        // for subClass to override
    }

    @Override
    public String toString() {
        final String buffString;
        if (isBuffOvercharged) {
            final List<Buff> buffs = buffData.stream()
                    .map(data -> BuffFactory.buildBuff(data, buffLevel))
                    .collect(Collectors.toList());
            buffString = "(OC)" + buffs;
        } else {
            buffString = BuffFactory.buildBuff(buffData.get(0), buffLevel).toString();
        }
        return String.format(
                getTranslation(EFFECT_SECTION, "Grant %s %s %s"),
                getTranslation(TARGET_SECTION, target.name()),
                miscString(),
                buffString
        );
    }
}
