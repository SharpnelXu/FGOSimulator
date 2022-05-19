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
import yome.fgo.simulator.models.effects.CommandCardExecution.NpParameters;
import yome.fgo.simulator.models.effects.buffs.AttackBuff;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardResist;
import yome.fgo.simulator.models.effects.buffs.CriticalStarGenerationBuff;
import yome.fgo.simulator.models.effects.buffs.DamageAdditionBuff;
import yome.fgo.simulator.models.effects.buffs.DamageReductionBuff;
import yome.fgo.simulator.models.effects.buffs.Evade;
import yome.fgo.simulator.models.effects.buffs.IgnoreDefenseBuff;
import yome.fgo.simulator.models.effects.buffs.IgnoreInvincible;
import yome.fgo.simulator.models.effects.buffs.Invincible;
import yome.fgo.simulator.models.effects.buffs.NpDamageBuff;
import yome.fgo.simulator.models.effects.buffs.NpDamageBuffEffectivenessUp;
import yome.fgo.simulator.models.effects.buffs.NpGenerationBuff;
import yome.fgo.simulator.models.effects.buffs.PercentAttackBuff;
import yome.fgo.simulator.models.effects.buffs.PercentDefenseBuff;
import yome.fgo.simulator.models.effects.buffs.PostAttackEffect;
import yome.fgo.simulator.models.effects.buffs.PostDefenseEffect;
import yome.fgo.simulator.models.effects.buffs.PreAttackEffect;
import yome.fgo.simulator.models.effects.buffs.PreDefenseEffect;
import yome.fgo.simulator.models.effects.buffs.Sleep;
import yome.fgo.simulator.models.effects.buffs.SpecialInvincible;
import yome.fgo.simulator.models.effects.buffs.SpecificAttackBuff;
import yome.fgo.simulator.models.effects.buffs.SpecificDefenseBuff;
import yome.fgo.simulator.models.effects.buffs.SureHit;
import yome.fgo.simulator.models.variations.Variation;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ANY;
import static yome.fgo.simulator.models.conditions.Never.NEVER;
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateCritStar;
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateNpGain;
import static yome.fgo.simulator.models.effects.CommandCardExecution.getHitsPercentages;
import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;
import static yome.fgo.simulator.utils.AttributeUtils.getAttributeAdvantage;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.convertDamageRate;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardDamageCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.modifierCap;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAdvantage;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAttackCorrection;

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
        if (!(activator instanceof Servant)) {
            simulation.unsetAttacker();
            return;
        }
        final Servant attacker = (Servant) activator;

        final CommandCard currentCard = attacker.getNoblePhantasm();
        simulation.setCurrentCommandCard(currentCard);

        final CommandCardType currentCardType = currentCard.getCommandCardType();
        final List<Double> hitsPercentages = getHitsPercentages(simulation, attacker, currentCard.getHitPercentages());
        final CommandCardType originalCardType = attacker.getOriginalNoblePhantasmCardType();

        for (final Combatant defender : targets) {
            simulation.setDefender(defender);
            final FateClass defenderClass = defender.getFateClass();

            attacker.activateEffectActivatingBuff(simulation, PreAttackEffect.class);
            defender.activateEffectActivatingBuff(simulation, PreDefenseEffect.class);

            final double originalDamageRate = getDamageRate(simulation, level);
            final double damageRate = originalCardType == currentCardType ?
                    originalDamageRate :
                    convertDamageRate(originalDamageRate, originalCardType, currentCardType);

            final double commandCardBuff = attacker.applyBuff(simulation, CommandCardBuff.class);
            final double attackBuff = attacker.applyBuff(simulation, AttackBuff.class);
            final double specificAttackBuff = attacker.applyBuff(simulation, SpecificAttackBuff.class);
            final double npDamageBuff = RoundUtils.roundNearest(attacker.applyBuff(simulation, NpDamageBuff.class) *
                    (1 + attacker.applyBuff(simulation, NpDamageBuffEffectivenessUp.class)));

            final double percentAttackBuff = attacker.applyBuff(simulation, PercentAttackBuff.class);
            final double damageAdditionBuff = attacker.applyBuff(simulation, DamageAdditionBuff.class);
            final boolean ignoreDefense = attacker.consumeBuffIfExist(simulation, IgnoreDefenseBuff.class) || isNpIgnoreDefense;

            final double npGenerationBuff = attacker.applyBuff(simulation, NpGenerationBuff.class);

            final double critStarGenerationBuff = attacker.applyBuff(simulation, CriticalStarGenerationBuff.class);

            final double npSpecificDamageRate = getNpSpecificDamageRate(simulation, level);

            final double commandCardResist = defender.applyBuff(simulation, CommandCardResist.class);

            final double defenseUpBuff = defender.applyDefenseUpBuff(simulation);
            final double defenseDownBuff = Math.max(defender.applyDefenseDownBuff(simulation), -1); // this is negative, capping at -100%
            final double defenseBuff = ignoreDefense ? defenseDownBuff : defenseUpBuff + defenseDownBuff;
            final double specificDefenseBuff = defender.applyBuff(simulation, SpecificDefenseBuff.class);
            final double percentDefenseBuff = defender.applyBuff(simulation, PercentDefenseBuff.class);
            final double damageReductionBuff = defender.applyBuff(simulation, DamageReductionBuff.class);

            final double classAdvantage = getClassAdvantage(attacker, defender);

            final NpDamageParameters npDamageParams = NpDamageParameters.builder()
                    .attack(attacker.getAttack())
                    .totalHits(currentCard.getTotalHits())
                    .damageRate(damageRate)
                    .npSpecificAttackRate(npSpecificDamageRate)
                    .attackerClass(attacker.getFateClass())
                    .defenderClass(defenderClass)
                    .classAdvantage(classAdvantage)
                    .attackerAttribute(attacker.getAttribute())
                    .defenderAttribute(defender.getAttribute())
                    .currentCardType(currentCardType)
                    .commandCardBuff(commandCardBuff)
                    .commandCardResist(commandCardResist)
                    .attackBuff(attackBuff)
                    .defenseBuff(defenseBuff)
                    .specificAttackBuff(specificAttackBuff)
                    .specificDefenseBuff(specificDefenseBuff)
                    .npDamageBuff(npDamageBuff)
                    .percentAttackBuff(percentAttackBuff)
                    .percentDefenseBuff(percentDefenseBuff)
                    .damageAdditionBuff(damageAdditionBuff)
                    .damageReductionBuff(damageReductionBuff)
                    .fixedRandom(simulation.getFixedRandom())
                    .build();

            final NpParameters npParameters = NpParameters.builder()
                    .npCharge(currentCard.getNpCharge())
                    .defenderClass(defenderClass)
                    .useUndeadNpCorrection(defender.getUndeadNpCorrection())
                    .currentCardType(currentCardType)
                    .chainIndex(0)
                    .isCriticalStrike(false)
                    .firstCardType(ANY) // works as long as not ARTS
                    .commandCardBuff(commandCardBuff)
                    .commandCardResist(commandCardResist)
                    .npGenerationBuff(npGenerationBuff)
                    .build();

            final CriticalStarParameters critStarParams = CriticalStarParameters.builder()
                    .servantCriticalStarGeneration(currentCard.getCriticalStarGeneration())
                    .defenderClass(defenderClass)
                    .currentCardType(currentCardType)
                    .chainIndex(0)
                    .isCriticalStrike(false)
                    .firstCardType(ANY) // works as long as not QUICK
                    .commandCardBuff(commandCardBuff)
                    .commandCardResist(commandCardResist)
                    .critStarGenerationBuff(critStarGenerationBuff)
                    .build();

            if (simulation.getStatsLogger() != null) {
                simulation.getStatsLogger().logDamageParameter(npDamageParams.toString());
                simulation.getStatsLogger().logDamageParameter(npParameters.toString());
                simulation.getStatsLogger().logDamageParameter(critStarParams.toString());
            }

            final boolean skipDamage = shouldSkipDamage(simulation, attacker, defender);

            if (defender.isReceivedInstantDeath()) {
                simulation.unsetDefender();
                continue; // for NP I remembered it skips damage calculation
            }

            final int totalDamage = calculateTotalNpDamage(npDamageParams);

            int remainingDamage = totalDamage;

            double totalNp = 0;
            double totalCritStar = 0;
            int overkillCount = 0;
            for (int i = 0; i < hitsPercentages.size(); i += 1) {
                if (!skipDamage) {
                    final double hitsPercentage = hitsPercentages.get(i);
                    final int hitDamage;
                    if (i < hitsPercentages.size() - 1) {
                        hitDamage = (int) (totalDamage * hitsPercentage / 100.0);
                    } else {
                        hitDamage = remainingDamage;
                    }

                    remainingDamage -= hitDamage;

                    defender.receiveDamage(hitDamage);
                }

                final boolean isOverkill = defender.isAlreadyDead();
                if (isOverkill) {
                    overkillCount += 1;
                }

                final double hitNpGain = calculateNpGain(npParameters, isOverkill);
                totalNp = RoundUtils.roundNearest(hitNpGain + totalNp);
                attacker.changeNp(hitNpGain);

                final double hitStars = calculateCritStar(critStarParams, isOverkill);
                if (hitStars > 3) {
                    totalCritStar += 3;
                } else {
                    totalCritStar += hitStars;
                }
            }
            simulation.gainStar(totalCritStar);

            if (simulation.getStatsLogger() != null) {
                simulation.getStatsLogger().logCommandCardAction(
                        attacker.getId(),
                        defender.getId(),
                        currentCard,
                        totalDamage - remainingDamage,
                        totalNp,
                        totalCritStar,
                        overkillCount,
                        hitsPercentages.size()
                );
            }

            attacker.activateEffectActivatingBuff(simulation, PostAttackEffect.class);
            defender.activateEffectActivatingBuff(simulation, PostDefenseEffect.class);
            final List<Buff> buffs = defender.getBuffs();
            for (int j = buffs.size() - 1; j >= 0; j -= 1) {
                final Buff buff = buffs.get(j);
                if (buff instanceof Sleep) {
                    buffs.remove(j);
                }
            }

            simulation.unsetDefender();
        }

        simulation.unsetCurrentCommandCard();
        simulation.unsetAttacker();
    }

    public static boolean shouldSkipDamage(
            final Simulation simulation,
            final Combatant attacker,
            final Combatant defender
    ) {
        final boolean hasSpecialInvincible = defender.consumeBuffIfExist(simulation, SpecialInvincible.class);
        final boolean hasIgnoreInvincible = attacker.consumeBuffIfExist(simulation, IgnoreInvincible.class);
        if (hasSpecialInvincible) {
            return true;
        }
        final boolean hasInvincible = defender.consumeBuffIfExist(simulation, Invincible.class);
        if (hasIgnoreInvincible) {
            return false;
        }
        final boolean hasSureHit = attacker.consumeBuffIfExist(simulation, SureHit.class);
        if (hasInvincible) {
            return true;
        }
        final boolean hasEvade = defender.consumeBuffIfExist(simulation, Evade.class);
        if (hasSureHit) {
            return false;
        }
        return hasEvade;
    }

    public static int calculateTotalNpDamage(final NpDamageParameters npDamageParams) {
        // fixed values
        final double classAttackCorrection = getClassAttackCorrection(npDamageParams.attackerClass);
        final double classAdvantage = getClassAdvantage(npDamageParams.attackerClass, npDamageParams.defenderClass);
        final double attributeAdvantage = getAttributeAdvantage(
                npDamageParams.attackerAttribute,
                npDamageParams.defenderAttribute
        );
        final double commandCardDamageCorrection = getCommandCardDamageCorrection(npDamageParams.currentCardType, 0);

        // capped buffs
        final double commandCardBuff = modifierCap(npDamageParams.commandCardBuff, 4, -1);

        final double attackBuff = modifierCap(npDamageParams.attackBuff, 4, -1);
        final double defenseBuff = modifierCap(npDamageParams.defenseBuff, 4, -1);

        final double specificAttackBuff = modifierCap(npDamageParams.specificAttackBuff, 10, -1);
        final double npDamageBuff = modifierCap(npDamageParams.npDamageBuff, 5, -1);

        final double percentAttackBuff = modifierCap(npDamageParams.percentAttackBuff, 10, -1);
        final double percentDefenseBuff = modifierCap(npDamageParams.percentDefenseBuff, 1, -1);

        final int totalDamage = (int) (npDamageParams.attack * NP_DAMAGE_MULTIPLIER * npDamageParams.damageRate *
                commandCardDamageCorrection *
                (1 + commandCardBuff - npDamageParams.commandCardResist) *
                classAttackCorrection * classAdvantage * attributeAdvantage *
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
