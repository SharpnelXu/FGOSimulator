package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.effects.buffs.BuffType.BUFF_CHANCE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEBUFF_CHANCE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEBUFF_RESIST;
import static yome.fgo.simulator.models.effects.buffs.BuffType.MAX_HP_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.RECEIVED_BUFF_CHANCE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SKILL_EFFECTIVENESS_UP;
import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class GrantBuff extends Effect {
    protected final boolean isBuffOvercharged;
    protected final List<BuffData> buffData; // here stores each overcharged effect
    protected final Target target;
    protected final int buffLevel;

    protected BuffData getBuffData(final int level) {
        return isBuffOvercharged ?
                buffData.get(level - 1) :
                buffData.get(0);
    }

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final double probability = getProbability(level);

        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);

            if (shouldApply(simulation) && combatant.isAlive(simulation)) {
                final boolean isPassive = simulation.isActivatingCePassiveEffects() ||
                        simulation.isActivatingServantPassiveEffects();
                final Combatant activator = simulation.getActivator();
                final Buff buff = BuffFactory.buildBuff(
                        getBuffData(level),
                        buffLevel,
                        isPassive,
                        activator.getActivatorHash()
                );

                simulation.setCurrentBuff(buff);

                final double skillEffectiveness = activator.applyBuff(simulation, SKILL_EFFECTIVENESS_UP);
                buff.addEffectiveness(skillEffectiveness);

                grantBuff(simulation, buff, combatant, probability);

                simulation.unsetCurrentBuff();
            }

            simulation.unsetEffectTarget();
        }
    }

    protected boolean isProbabilityBelowThreshold(
            final Simulation simulation,
            final Buff buff,
            final Combatant combatant,
            final double probability
    ) {
        final double activationProbability;
        if (buff.isDebuff()) {
            final double debuffChance = simulation.getActivator().applyBuff(simulation, DEBUFF_CHANCE_BUFF);
            final double debuffResist = combatant.applyBuff(simulation, DEBUFF_RESIST);

            activationProbability = RoundUtils.roundNearest(probability + debuffChance - debuffResist);
        } else if (buff.isBuff()) {
            final double buffChance = simulation.getActivator().applyBuff(simulation, BUFF_CHANCE_BUFF);
            final double receivedBuffChance = combatant.applyBuff(simulation, RECEIVED_BUFF_CHANCE_BUFF);

            activationProbability = RoundUtils.roundNearest(probability + buffChance + receivedBuffChance);
        } else {
            activationProbability = probability;
        }

        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logProbability(combatant.getId(), activationProbability, simulation.getProbabilityThreshold());
        }
        return activationProbability < simulation.getProbabilityThreshold();
    }

    private void grantBuff(
            final Simulation simulation,
            final Buff buff,
            final Combatant combatant,
            final double probability
    ) {
        if (isProbabilityBelowThreshold(simulation, buff, combatant, probability)) {
            return;
        }

        boolean canActivate = true;
        if (!buff.isStackable()) {
            for (final Buff existingBuff : combatant.getBuffs()) {
                if (existingBuff.getBuffType() == buff.getBuffType() && !existingBuff.isStackable()) {
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
        if (simulation.getCurrentBuff().getBuffType() == MAX_HP_BUFF) {
            final int change = (int) simulation.getCurrentBuff().getValue(simulation);
            simulation.getEffectTarget().changeHpAfterMaxHpChange(change);
        }
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
