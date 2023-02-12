package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.effects.CommandCardExecution.CriticalStarParameters;
import yome.fgo.simulator.models.effects.CommandCardExecution.DefNpParameters;
import yome.fgo.simulator.models.effects.CommandCardExecution.NpParameters;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.variations.Variation;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

import static yome.fgo.simulator.models.conditions.Never.NEVER;
import static yome.fgo.simulator.models.effects.CommandCardExecution.getHitsPercentages;
import static yome.fgo.simulator.models.effects.CommandCardExecution.hitExecution;
import static yome.fgo.simulator.models.effects.CommandCardExecution.shouldSkipDamage;
import static yome.fgo.simulator.models.effects.buffs.BuffType.ATTACK_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.COMMAND_CARD_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.COMMAND_CARD_RESIST;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CRITICAL_STAR_GENERATION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DAMAGE_ADDITION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DAMAGE_REDUCTION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEFENSE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEF_NP_GENERATION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.IGNORE_DEFENSE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.NP_DAMAGE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.NP_DAMAGE_BUFF_EFFECTIVENESS_UP;
import static yome.fgo.simulator.models.effects.buffs.BuffType.NP_GENERATION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PERCENT_ATTACK_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PERCENT_DEFENSE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.POST_ATTACK_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.POST_DEFENSE_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PRE_ATTACK_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PRE_DEFENSE_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SLEEP;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SPECIFIC_ATTACK_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SPECIFIC_DEFENSE_BUFF;
import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;
import static yome.fgo.simulator.utils.AttributeUtils.getAttributeAdvantage;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.convertDamageRate;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardDamageCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.modifierCap;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAdvantage;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAttackCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassNpCorrection;

@SuperBuilder
public class NoblePhantasmDamage extends Effect {
    public static final double NP_DAMAGE_MULTIPLIER = 0.23;

    protected final Target target;
    protected final boolean isNpDamageOverchargedEffect;
    protected final List<Double> damageRates;
    protected final List<Double> npSpecificDamageRates;
    @Builder.Default
    protected final Condition npSpecificDamageCondition = NEVER;
    protected final boolean isNpSpecificDamageOverchargedEffect;
    private final boolean isNpIgnoreDefense;

    @Builder.Default
    private final Variation damageRateVariation = NO_VARIATION;
    private final boolean isNpDamageAdditionOvercharged;
    @Builder.Default
    protected final List<Double> damageRateAdditions = Lists.newArrayList(0.0);
    @Builder.Default
    private final Variation specificDamageRateVariation = NO_VARIATION;
    private final boolean isNpSpecificDamageAdditionOvercharged;
    @Builder.Default
    protected final List<Double> npSpecificDamageRateAdditions = Lists.newArrayList(0.0);

    protected double getDamageRate(final Simulation simulation, final int level) {
        final double baseDamageRate = isNpDamageOverchargedEffect ?
                damageRates.get(level - 1) :
                damageRates.get(0);
        final double addition = isNpDamageAdditionOvercharged ?
                damageRateAdditions.get(level - 1) :
                damageRateAdditions.get(0);

        return damageRateVariation.evaluate(simulation, baseDamageRate, addition);
    }

    protected double getNpSpecificDamageRate(final Simulation simulation, final int level) {
        if (npSpecificDamageCondition.evaluate(simulation)) {
            final double baseDamageRate = isNpSpecificDamageOverchargedEffect ?
                    npSpecificDamageRates.get(level - 1) :
                    npSpecificDamageRates.get(0);
            final double addition = isNpSpecificDamageAdditionOvercharged ?
                    npSpecificDamageRateAdditions.get(level - 1) :
                    npSpecificDamageRateAdditions.get(0);

            return specificDamageRateVariation.evaluate(simulation, baseDamageRate, addition);
        } else {
            return 1.0;
        }
    }

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        // Here only three targets makes sense: attacker (for Bazett's NP), targeted enemy, and all enemies
        final List<Combatant> targets = TargetUtils.getTargets(simulation, target);
        final Combatant activator = simulation.getActivator();
        simulation.setAttacker(activator);

        final CommandCard currentCard = activator.getNoblePhantasm();
        simulation.setCurrentCommandCard(currentCard);

        final CommandCardType currentCardType = currentCard.getCommandCardType();
        final List<Double> hitsPercentages = getHitsPercentages(simulation, activator, currentCard.getHitPercentages());
        final CommandCardType originalCardType = activator.getOriginalNoblePhantasmCardType();

        for (final Combatant defender : targets) {
            simulation.setDefender(defender);
            final FateClass defenderClass = defender.getFateClass();

            activator.activateEffectActivatingBuff(simulation, PRE_ATTACK_EFFECT);
            defender.activateEffectActivatingBuff(simulation, PRE_DEFENSE_EFFECT);

            final double originalDamageRate = getDamageRate(simulation, level);
            final double damageRate = originalCardType == currentCardType ?
                    originalDamageRate :
                    convertDamageRate(originalDamageRate, originalCardType, currentCardType);

            final double commandCardBuff = activator.applyValuedBuff(simulation, COMMAND_CARD_BUFF);
            final double attackBuff = activator.applyValuedBuff(simulation, ATTACK_BUFF);
            final double specificAttackBuff = activator.applyValuedBuff(simulation, SPECIFIC_ATTACK_BUFF);
            final double npDamageUpBuff = activator.applyPositiveBuff(simulation, NP_DAMAGE_BUFF);
            final double npDamageDownBuff = activator.applyNegativeBuff(simulation, NP_DAMAGE_BUFF);; // value is negative
            final double npEffectivenessUpBuff = activator.applyValuedBuff(simulation, NP_DAMAGE_BUFF_EFFECTIVENESS_UP);
            final double npDamageBuff = RoundUtils.roundNearest(npDamageUpBuff * (1 + npEffectivenessUpBuff) + npDamageDownBuff);

            final double percentAttackBuff = activator.applyValuedBuff(simulation, PERCENT_ATTACK_BUFF);
            final double damageAdditionBuff = activator.applyValuedBuff(simulation, DAMAGE_ADDITION_BUFF);
            final boolean ignoreDefense = activator.consumeFirstBuff(simulation, IGNORE_DEFENSE_BUFF) || isNpIgnoreDefense;

            final double npGenerationBuff = activator.applyValuedBuff(simulation, NP_GENERATION_BUFF);
            final double classNpCorrection = defender.getCombatantData().getUseCustomNpMod()
                    ? defender.getCombatantData().getCustomNpMod()
                    : getClassNpCorrection(defenderClass);

            final double critStarGenerationBuff = activator.applyValuedBuff(simulation, CRITICAL_STAR_GENERATION_BUFF);

            final double npSpecificDamageRate = getNpSpecificDamageRate(simulation, level);

            final double classAdvantage = getClassAdvantage(simulation, activator, defender);

            final NpDamageParameters.NpDamageParametersBuilder npDamageParams = NpDamageParameters.builder()
                    .attack(activator.getAttack())
                    .totalHits(currentCard.getTotalHits())
                    .damageRate(damageRate)
                    .npSpecificAttackRate(npSpecificDamageRate)
                    .attackerClass(activator.getFateClass())
                    .defenderClass(defenderClass)
                    .classAdvantage(classAdvantage)
                    .attackerAttribute(activator.getAttribute())
                    .defenderAttribute(defender.getAttribute())
                    .currentCardType(currentCardType)
                    .commandCardBuff(commandCardBuff)
                    .attackBuff(attackBuff)
                    .specificAttackBuff(specificAttackBuff)
                    .npDamageBuff(npDamageBuff)
                    .percentAttackBuff(percentAttackBuff)
                    .damageAdditionBuff(damageAdditionBuff)
                    .fixedRandom(simulation.getFixedRandom());

            final NpParameters.NpParametersBuilder npParameters = NpParameters.builder();
            final CriticalStarParameters.CriticalStarParametersBuilder critStarParams = CriticalStarParameters.builder();
            if (activator.isAlly()) {
                npParameters.npCharge(currentCard.getNpCharge())
                        .defenderClass(defenderClass)
                        .useUndeadNpCorrection(defender.getUndeadNpCorrection())
                        .currentCardType(currentCardType)
                        .chainIndex(0)
                        .isCriticalStrike(false)
                        .useFirstCardBoost(false)
                        .commandCardBuff(commandCardBuff)
                        .npGenerationBuff(npGenerationBuff)
                        .classNpCorrection(classNpCorrection);

                critStarParams.servantCriticalStarGeneration(currentCard.getCriticalStarGeneration())
                        .defenderClass(defenderClass)
                        .currentCardType(currentCardType)
                        .chainIndex(0)
                        .isCriticalStrike(false)
                        .useFirstCardBoost(false)
                        .commandCardBuff(commandCardBuff)
                        .critStarGenerationBuff(critStarGenerationBuff);
            }

            final DefNpParameters.DefNpParametersBuilder defNpParameters = DefNpParameters.builder();
            if (defender.isAlly()) {
                final Servant defendServant = (Servant) defender;
                final double attackerClassNpCorrection = activator.getCombatantData().getUseCustomNpMod()
                        ? activator.getCombatantData().getCustomNpMod()
                        : getClassNpCorrection(activator.getFateClass());
                defNpParameters.defNpCharge(defendServant.getDefNpCharge())
                        .attackerClass(activator.getFateClass())
                        .classNpCorrection(attackerClassNpCorrection)
                        .useUndeadNpCorrection(activator.getUndeadNpCorrection())
                        .npGenerationBuff(defendServant.applyValuedBuff(simulation, NP_GENERATION_BUFF))
                        .defNpGenerationBuff(defendServant.applyValuedBuff(simulation, DEF_NP_GENERATION_BUFF));
            }

            final boolean skipDamage = shouldSkipDamage(simulation, activator, defender, currentCard);

            if (!skipDamage) {
                final double commandCardResist = defender.applyValuedBuff(simulation, COMMAND_CARD_RESIST);
                final double defenseUpBuff = defender.applyPositiveBuff(simulation, DEFENSE_BUFF);
                final double defenseDownBuff = defender.applyNegativeBuff(simulation, DEFENSE_BUFF); // value is negative
                final double defenseBuff = ignoreDefense ? defenseDownBuff : defenseUpBuff + defenseDownBuff;
                final double specificDefenseBuff = defender.applyValuedBuff(simulation, SPECIFIC_DEFENSE_BUFF);
                final double percentDefenseBuff = defender.applyValuedBuff(simulation, PERCENT_DEFENSE_BUFF);
                final double damageReductionBuff = defender.applyValuedBuff(simulation, DAMAGE_REDUCTION_BUFF);

                npDamageParams.commandCardResist(commandCardResist)
                        .defenseBuff(defenseBuff)
                        .specificDefenseBuff(specificDefenseBuff)
                        .percentDefenseBuff(percentDefenseBuff)
                        .damageReductionBuff(damageReductionBuff);

                npParameters.commandCardResist(commandCardResist);

                critStarParams.commandCardResist(commandCardResist);
            }

            if (simulation.getStatsLogger() != null) {
                simulation.getStatsLogger().logDamageParameter(npDamageParams.toString());
                simulation.getStatsLogger().logDamageParameter(npParameters.toString());
                simulation.getStatsLogger().logDamageParameter(critStarParams.toString());
            }

            if (defender.isReceivedInstantDeath()) {
                simulation.unsetDefender();
                continue; // for NP I remembered it skips damage calculation
            }

            final int totalDamage = calculateTotalNpDamage(npDamageParams.build());
            final int damageDealt = hitExecution(
                    simulation,
                    activator,
                    defender,
                    currentCard,
                    hitsPercentages,
                    skipDamage,
                    totalDamage,
                    npParameters.build(),
                    critStarParams.build(),
                    defNpParameters.build(),
                    Combatant::isAlreadyDead
            );

            activator.activateEffectActivatingBuff(simulation, POST_ATTACK_EFFECT);
            defender.activateEffectActivatingBuff(simulation, POST_DEFENSE_EFFECT);
            final List<Buff> buffs = defender.getBuffs();
            for (int j = buffs.size() - 1; j >= 0; j -= 1) {
                final Buff buff = buffs.get(j);
                if (buff.getBuffType() == SLEEP) {
                    buffs.remove(j);
                }
            }

            // overkill bug
            defender.addCumulativeTurnDamage(damageDealt);

            simulation.unsetDefender();
        }

        simulation.unsetCurrentCommandCard();
        simulation.unsetAttacker();
    }

    public static int calculateTotalNpDamage(final NpDamageParameters npDamageParams) {
        // fixed values
        final double classAttackCorrection = getClassAttackCorrection(npDamageParams.attackerClass);
        final double attributeAdvantage = getAttributeAdvantage(
                npDamageParams.attackerAttribute,
                npDamageParams.defenderAttribute
        );
        final double commandCardDamageCorrection = getCommandCardDamageCorrection(npDamageParams.currentCardType, 0);

        // capped buffs
        final double commandCardBuff = modifierCap(npDamageParams.commandCardBuff, 4, -1);

        final double attackBuff = modifierCap(npDamageParams.attackBuff, 4, -1);
        final double defenseBuff = npDamageParams.defenseBuff < -1 ? -1 : npDamageParams.defenseBuff;

        final double specificAttackBuff = modifierCap(npDamageParams.specificAttackBuff, 10, -1);
        final double npDamageBuff = modifierCap(npDamageParams.npDamageBuff, 5, -1);

        final double percentAttackBuff = modifierCap(npDamageParams.percentAttackBuff, 10, -1);
        final double percentDefenseBuff = modifierCap(npDamageParams.percentDefenseBuff, 1, -1);

        final int totalDamage = (int) (npDamageParams.attack * NP_DAMAGE_MULTIPLIER * npDamageParams.damageRate *
                commandCardDamageCorrection *
                (1 + commandCardBuff - npDamageParams.commandCardResist) *
                classAttackCorrection * npDamageParams.classAdvantage * attributeAdvantage *
                (1 + attackBuff - defenseBuff - npDamageParams.specificDefenseBuff) *
                (1 + specificAttackBuff + npDamageBuff) * npDamageParams.npSpecificAttackRate *
                (1 - percentDefenseBuff) *
                (1 + percentAttackBuff) * npDamageParams.totalHits / 100.0 * npDamageParams.fixedRandom +
                npDamageParams.damageAdditionBuff - npDamageParams.damageReductionBuff);

        return Math.max(0, totalDamage);
    }

    @Builder
    @ToString
    public static class NpDamageParameters {
        private final int attack;
        private final double damageRate;
        private final int totalHits;
        private final double npSpecificAttackRate;
        private final FateClass attackerClass;
        private final FateClass defenderClass;
        private final double classAdvantage;
        private final Attribute attackerAttribute;
        private final Attribute defenderAttribute;
        private final CommandCardType currentCardType;
        private final double commandCardBuff;
        private final double commandCardResist;
        private final double attackBuff;
        private final double defenseBuff;
        private final double specificAttackBuff;
        private final double specificDefenseBuff;
        private final double npDamageBuff;
        private final double percentAttackBuff;
        private final double percentDefenseBuff;
        private final double damageAdditionBuff;
        private final double damageReductionBuff;
        private final double fixedRandom;
    }
}
