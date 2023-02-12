package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.ToString;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffType;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.models.effects.buffs.BuffType.ATTACK_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.COMMAND_CARD_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.COMMAND_CARD_RESIST;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CRITICAL_DAMAGE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CRITICAL_STAR_GENERATION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DAMAGE_ADDITION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DAMAGE_REDUCTION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEFENSE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEF_NP_GENERATION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.EVADE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.HITS_DOUBLED_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.IGNORE_DEFENSE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.IGNORE_INVINCIBLE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.INVINCIBLE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.NP_GENERATION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PERCENT_ATTACK_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PERCENT_DEFENSE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.POST_ATTACK_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.POST_DEFENSE_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PRE_ATTACK_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PRE_DEFENSE_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SLEEP;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SPECIAL_INVINCIBLE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SPECIFIC_ATTACK_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SPECIFIC_DEFENSE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SURE_HIT;
import static yome.fgo.simulator.utils.AttributeUtils.getAttributeAdvantage;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.busterChainDamageAddition;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.extraCardBuff;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardCritStarCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardDamageCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardNpCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.isBusterChain;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.modifierCap;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAdvantage;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAttackCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassCritStarCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassNpCorrection;

public class CommandCardExecution {
    public static final double COMMAND_CARD_DAMAGE_MULTIPLIER = 0.23;

    public static List<Double> getHitsPercentages(
            final Simulation simulation,
            final Combatant attacker,
            final List<Integer> baseHitsPercentages
    ) {
        for (final Buff buff : attacker.fetchBuffs(HITS_DOUBLED_BUFF)) {
            if (buff.shouldApply(simulation)) {
                buff.setApplied();
                final List<Double> doubledHits = new ArrayList<>();
                for (final int hit : baseHitsPercentages) {
                    for (int i = 0; i < 2; i += 1) {
                        doubledHits.add(hit / 2.0);
                    }
                }
                return doubledHits;
            }
        }
        return baseHitsPercentages.stream().map(Integer::doubleValue).collect(Collectors.toList());
    }

    public static void executeCommandCard(
            final Simulation simulation,
            final int chainIndex,
            final boolean isCriticalStrike,
            final CommandCardType firstCardType,
            final boolean isTypeChain,
            final boolean isTriColorChain
    ) {
        final Combatant attacker = simulation.getAttacker();
        final Combatant defender = simulation.getDefender();
        final FateClass defenderClass = defender.getFateClass();
        final CommandCard currentCard = simulation.getCurrentCommandCard();
        final CommandCardType currentCardType = currentCard.getCommandCardType();

        final List<Double> hitsPercentages = getHitsPercentages(simulation, attacker, currentCard.getHitPercentages());

        activateEffectActivatingBuff(simulation, attacker, currentCard, PRE_ATTACK_EFFECT);
        defender.activateEffectActivatingBuff(simulation, PRE_DEFENSE_EFFECT);

        final double commandCardBuff = applyBuff(simulation, attacker, currentCard, COMMAND_CARD_BUFF);

        final double attackBuff = applyBuff(simulation, attacker, currentCard, ATTACK_BUFF);

        final boolean ignoreDefense = consumeBuffIfExists(simulation, attacker, currentCard, IGNORE_DEFENSE_BUFF);

        final double specificAttackBuff = applyBuff(simulation, attacker, currentCard, SPECIFIC_ATTACK_BUFF);
        final double criticalDamageBuff = isCriticalStrike ?
                applyBuff(simulation, attacker, currentCard, CRITICAL_DAMAGE_BUFF) :
                0;

        final double percentAttackBuff = applyBuff(simulation, attacker, currentCard, PERCENT_ATTACK_BUFF);

        final double damageAdditionBuff = applyBuff(simulation, attacker, currentCard, DAMAGE_ADDITION_BUFF);

        final double npGenerationBuff = applyBuff(simulation, attacker, currentCard, NP_GENERATION_BUFF);

        final double classNpCorrection = defender.getCombatantData().getUseCustomNpMod()
                ? defender.getCombatantData().getCustomNpMod()
                : getClassNpCorrection(defenderClass);

        final double critStarGenerationBuff = applyBuff(simulation, attacker, currentCard, CRITICAL_STAR_GENERATION_BUFF);

        final double classAdvantage = getClassAdvantage(simulation, attacker, defender);

        final DamageParameters.DamageParametersBuilder damageParameters = DamageParameters.builder()
                .attack(attacker.getAttack() + currentCard.getCommandCardStrengthen())
                .totalHits(currentCard.getTotalHits())
                .attackerClass(attacker.getFateClass())
                .defenderClass(defenderClass)
                .classAdvantage(classAdvantage)
                .attackerAttribute(attacker.getAttribute())
                .defenderAttribute(defender.getAttribute())
                .currentCardType(currentCardType)
                .chainIndex(chainIndex)
                .isCriticalStrike(isCriticalStrike)
                .isTypeChain(isTypeChain)
                .useFirstCardBoost(firstCardType == BUSTER || isTriColorChain)
                .isBusterChain(isBusterChain(isTypeChain, firstCardType))
                .commandCardBuff(commandCardBuff)
                .attackBuff(attackBuff)
                .specificAttackBuff(specificAttackBuff)
                .percentAttackBuff(percentAttackBuff)
                .damageAdditionBuff(damageAdditionBuff)
                .criticalDamageBuff(criticalDamageBuff)
                .fixedRandom(simulation.getFixedRandom());

        final NpParameters.NpParametersBuilder npParameters = NpParameters.builder();
        final CriticalStarParameters.CriticalStarParametersBuilder critStarParams = CriticalStarParameters.builder();
        if (attacker.isAlly()) {
            npParameters.npCharge(currentCard.getNpCharge())
                    .defenderClass(defenderClass)
                    .useUndeadNpCorrection(defender.getUndeadNpCorrection())
                    .currentCardType(currentCardType)
                    .chainIndex(chainIndex)
                    .isCriticalStrike(isCriticalStrike)
                    .useFirstCardBoost(firstCardType == ARTS || isTriColorChain)
                    .commandCardBuff(commandCardBuff)
                    .npGenerationBuff(npGenerationBuff)
                    .classNpCorrection(classNpCorrection);

            critStarParams.servantCriticalStarGeneration(currentCard.getCriticalStarGeneration())
                    .defenderClass(defenderClass)
                    .currentCardType(currentCardType)
                    .chainIndex(chainIndex)
                    .isCriticalStrike(isCriticalStrike)
                    .useFirstCardBoost(firstCardType == QUICK || isTriColorChain)
                    .commandCardBuff(commandCardBuff)
                    .critStarGenerationBuff(critStarGenerationBuff);
        }

        final DefNpParameters.DefNpParametersBuilder defNpParameters = DefNpParameters.builder();
        if (defender.isAlly()) {
            final Servant defendServant = (Servant) defender;
            final double attackerClassNpCorrection = attacker.getCombatantData().getUseCustomNpMod()
                    ? attacker.getCombatantData().getCustomNpMod()
                    : getClassNpCorrection(attacker.getFateClass());
            defNpParameters.defNpCharge(defendServant.getDefNpCharge())
                    .attackerClass(attacker.getFateClass())
                    .classNpCorrection(attackerClassNpCorrection)
                    .useUndeadNpCorrection(attacker.getUndeadNpCorrection())
                    .npGenerationBuff(defendServant.applyValuedBuff(simulation, NP_GENERATION_BUFF))
                    .defNpGenerationBuff(defendServant.applyValuedBuff(simulation, DEF_NP_GENERATION_BUFF));
        }

        final boolean skipDamage = shouldSkipDamage(simulation, attacker, defender, currentCard);
        if (!skipDamage) {
            final double commandCardResist = defender.applyValuedBuff(simulation, COMMAND_CARD_RESIST);
            final double defenseUpBuff = defender.applyPositiveBuff(simulation, DEFENSE_BUFF);
            final double defenseDownBuff = defender.applyNegativeBuff(simulation, DEFENSE_BUFF); // value is negative
            final double defenseBuff = ignoreDefense ? defenseDownBuff : defenseUpBuff + defenseDownBuff;
            final double specificDefenseBuff = defender.applyValuedBuff(simulation, SPECIFIC_DEFENSE_BUFF);
            final double percentDefenseBuff = defender.applyValuedBuff(simulation, PERCENT_DEFENSE_BUFF);
            final double damageReductionBuff = defender.applyValuedBuff(simulation, DAMAGE_REDUCTION_BUFF);

            damageParameters.commandCardResist(commandCardResist)
                    .defenseBuff(defenseBuff)
                    .specificDefenseBuff(specificDefenseBuff)
                    .percentDefenseBuff(percentDefenseBuff)
                    .damageReductionBuff(damageReductionBuff);

            if (attacker.isAlly()) {
                npParameters.commandCardResist(commandCardResist);

                critStarParams.commandCardResist(commandCardResist);
            }
        }

        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logDamageParameter(damageParameters.toString());
            if (attacker.isAlly()) {
                simulation.getStatsLogger().logDamageParameter(npParameters.toString());
                simulation.getStatsLogger().logDamageParameter(critStarParams.toString());
            }

            if (defender.isAlly()) {
                simulation.getStatsLogger().logDamageParameter(defNpParameters.toString());
            }
        }

        final int totalDamage = calculateTotalDamage(damageParameters.build());
        final int damageDealt = hitExecution(
                simulation,
                attacker,
                defender,
                currentCard,
                hitsPercentages,
                skipDamage,
                totalDamage,
                npParameters.build(),
                critStarParams.build(),
                defNpParameters.build(),
                (combatant) -> combatant.isAlreadyDead() || combatant.isBuggedOverkill()
        );

        activateEffectActivatingBuff(simulation, attacker, currentCard, POST_ATTACK_EFFECT);
        defender.activateEffectActivatingBuff(simulation, POST_DEFENSE_EFFECT);
        final List<Buff> buffs = defender.getBuffs();
        for (int j = buffs.size() - 1; j >= 0; j -= 1) {
            final Buff buff = buffs.get(j);
            if (buff.getBuffType() == SLEEP) {
                buffs.remove(j);
            }
        }

        simulation.checkBuffStatus();

        // overkill bug
        defender.addCumulativeTurnDamage(damageDealt);
    }

    public static boolean shouldSkipDamage(
            final Simulation simulation,
            final Combatant attacker,
            final Combatant defender,
            final CommandCard currentCard
    ) {
        final boolean hasSpecialInvincible = defender.consumeFirstBuff(simulation, SPECIAL_INVINCIBLE);
        final boolean hasIgnoreInvincible = consumeBuffIfExists(simulation, attacker, currentCard, IGNORE_INVINCIBLE);
        if (hasSpecialInvincible) {
            return true;
        }
        final boolean hasInvincible = defender.consumeFirstBuff(simulation, INVINCIBLE);
        if (hasIgnoreInvincible) {
            return false;
        }
        final boolean hasSureHit = consumeBuffIfExists(simulation, attacker, currentCard, SURE_HIT);
        if (hasInvincible) {
            return true;
        }
        final boolean hasEvade = defender.consumeFirstBuff(simulation, EVADE);
        if (hasSureHit) {
            return false;
        }
        return hasEvade;
    }

    public static double applyBuff(
            final Simulation simulation,
            final Combatant attacker,
            final CommandCard currentCard,
            final BuffType buffType
            ) {
        return attacker.applyValuedBuff(simulation, buffType) + currentCard.applyBuff(simulation, buffType);
    }

    public static void activateEffectActivatingBuff(
            final Simulation simulation,
            final Combatant attacker,
            final CommandCard currentCard,
            final BuffType buffType
    ) {
        attacker.activateEffectActivatingBuff(simulation, buffType);
        currentCard.activateEffectActivatingBuff(simulation, buffType);
    }

    public static boolean consumeBuffIfExists(
            final Simulation simulation,
            final Combatant attacker,
            final CommandCard currentCard,
            final BuffType buffType
    ) {
        return attacker.consumeFirstBuff(simulation, buffType) || currentCard.consumeBuffIfExist(simulation, buffType);
    }

    public static int hitExecution(
            final Simulation simulation,
            final Combatant attacker,
            final Combatant defender,
            final CommandCard currentCard,
            final List<Double> hitsPercentages,
            final boolean skipDamage,
            final int totalDamage,
            final NpParameters npParameters,
            final CriticalStarParameters critStarParams,
            final DefNpParameters defNpParameters,
            final Function<Combatant, Boolean> overkillCheck
    ) {
        int remainingDamage = totalDamage;
        double totalNp = 0;
        double defTotalNp = 0;
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

            final boolean isOverkill = overkillCheck.apply(defender);
            if (isOverkill) {
                overkillCount += 1;
            }

            if (attacker.isAlly()) {
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

            if (defender.isAlly()) {
                final double hitNpGain = calculateDefNpGain(defNpParameters, isOverkill);

                defTotalNp = RoundUtils.roundNearest(hitNpGain + defTotalNp);
                defender.changeNp(hitNpGain);
            }
        }
        simulation.gainStar(RoundUtils.roundNearest(totalCritStar));

        final int damageDealt = totalDamage - remainingDamage;
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logCommandCardAction(
                    attacker.getId(),
                    defender.getId(),
                    currentCard,
                    damageDealt,
                    totalNp,
                    defTotalNp,
                    totalCritStar,
                    overkillCount,
                    hitsPercentages.size()
            );
        }
        return damageDealt;
    }

    public static int calculateTotalDamage(final DamageParameters damageParameters) {
        // fixed values
        final double classAttackCorrection = getClassAttackCorrection(damageParameters.attackerClass);
        final double classAdvantage = damageParameters.classAdvantage;
        final double attributeAdvantage = getAttributeAdvantage(damageParameters.attackerAttribute, damageParameters.defenderAttribute);
        final double commandCardDamageCorrection = getCommandCardDamageCorrection(damageParameters.currentCardType, damageParameters.chainIndex);
        final double busterStartDamageBoost = damageParameters.useFirstCardBoost ? 0.5 : 0;
        final int criticalStrikeDamageCorrection = damageParameters.isCriticalStrike ? 1 : 0;
        final double extraCardBuff = extraCardBuff(damageParameters.currentCardType, damageParameters.isTypeChain);
        final double busterChainDamageAddition = busterChainDamageAddition(
                damageParameters.attack,
                damageParameters.currentCardType,
                damageParameters.isBusterChain
        );

        // capped buffs
        final double commandCardBuff = modifierCap(damageParameters.commandCardBuff, 4, -1);

        final double attackBuff = modifierCap(damageParameters.attackBuff, 4, -1);
        final double defenseBuff = damageParameters.defenseBuff < -1 ? -1 : damageParameters.defenseBuff;

        final double specificAttackBuff = modifierCap(damageParameters.specificAttackBuff, 10, -1);
        final double criticalDamageBuff = modifierCap(damageParameters.criticalDamageBuff, 5, -1);

        final double percentAttackBuff = modifierCap(damageParameters.percentAttackBuff, 10, -1);
        final double percentDefenseBuff = modifierCap(damageParameters.percentDefenseBuff, 1, -1);

        final int totalDamage = (int) ((damageParameters.attack * COMMAND_CARD_DAMAGE_MULTIPLIER *
                ((commandCardDamageCorrection * (1 + commandCardBuff - damageParameters.commandCardResist)) + busterStartDamageBoost) *
                classAttackCorrection * classAdvantage * attributeAdvantage *
                (1 + attackBuff - defenseBuff - damageParameters.specificDefenseBuff) *
                (1 + criticalStrikeDamageCorrection) *
                (1 + specificAttackBuff + criticalDamageBuff * criticalStrikeDamageCorrection) *
                (1 - percentDefenseBuff) *
                (1 + percentAttackBuff) *
                extraCardBuff * damageParameters.totalHits / 100.0 * damageParameters.fixedRandom) +
                busterChainDamageAddition + damageParameters.damageAdditionBuff - damageParameters.damageReductionBuff);
        return Math.max(0, totalDamage);
    }

    public static double calculateNpGain(final NpParameters npParameters, final boolean isOverkill) {
        final double commandCardNpCorrection = getCommandCardNpCorrection(npParameters.currentCardType, npParameters.chainIndex);
        final double artsStartNpBoost = npParameters.useFirstCardBoost ? 1 : 0;
        final double classNpCorrection = npParameters.classNpCorrection;
        final double undeadNpCorrection = npParameters.useUndeadNpCorrection ? 1.2 : 1;
        final int criticalStrikeNpCorrection = npParameters.isCriticalStrike ? 2 : 1;
        final double overkillNpBonus = isOverkill ? 1.5 : 1;

        // capped buffs
        final double commandCardBuff = modifierCap(npParameters.commandCardBuff, 4, -1);

        final double hitNpBeforeRound = npParameters.npCharge * classNpCorrection * undeadNpCorrection *
                (commandCardNpCorrection * (1 + commandCardBuff - npParameters.commandCardResist) + artsStartNpBoost) *
                (1 + npParameters.npGenerationBuff) * criticalStrikeNpCorrection * 10000;
        final double roundedNpBeforeOverkillBonus = ((int) hitNpBeforeRound) * overkillNpBonus;
        final double roundedNp2 = (int) roundedNpBeforeOverkillBonus;

        return Math.max(0, RoundUtils.roundNearest(roundedNp2 / 10000));
    }

    public static double calculateDefNpGain(final DefNpParameters defNpParameters, final boolean isOverkill) {
        final double undeadNpCorrection = defNpParameters.useUndeadNpCorrection ? 1.2 : 1;
        final double overkillNpBonus = isOverkill ? 1.5 : 1;

        final double hitNpBeforeRound = defNpParameters.defNpCharge * defNpParameters.classNpCorrection *
                undeadNpCorrection * (1 + defNpParameters.npGenerationBuff) * (1 + defNpParameters.defNpGenerationBuff);

        final double roundedNpBeforeOverkillBonus = ((int) hitNpBeforeRound) * overkillNpBonus;
        final double roundedNp2 = (int) roundedNpBeforeOverkillBonus;

        return Math.max(0, RoundUtils.roundNearest(roundedNp2 / 10000));
    }

    public static double calculateCritStar(final CriticalStarParameters critStarParams, final boolean isOverkill) {
        final double commandCardCritStarCorrection = getCommandCardCritStarCorrection(critStarParams.currentCardType, critStarParams.chainIndex);
        final double quickStartCritStarBoost = critStarParams.useFirstCardBoost ? 0.2 : 0;
        final double classCritStarCorrection = getClassCritStarCorrection(critStarParams.defenderClass);
        final double criticalStrikeCritStarCorrection = critStarParams.isCriticalStrike ? 0.2 : 0;
        final double overkillCritStarBonus = isOverkill ? 0.3 : 0;

        // capped buffs
        final double commandCardBuff = modifierCap(critStarParams.commandCardBuff, 4, -1);

        final double totalStars = critStarParams.servantCriticalStarGeneration +
                commandCardCritStarCorrection * (1 + commandCardBuff - critStarParams.commandCardResist) +
                quickStartCritStarBoost + classCritStarCorrection + critStarParams.critStarGenerationBuff +
                criticalStrikeCritStarCorrection + overkillCritStarBonus;
        final double roundedStars = RoundUtils.roundDown(totalStars);

        return Math.max(0, roundedStars);
    }

    @Builder
    @ToString
    public static class DamageParameters {
        private final int attack;
        private final int totalHits;
        private final FateClass attackerClass;
        private final FateClass defenderClass;
        private final double classAdvantage;
        private final Attribute attackerAttribute;
        private final Attribute defenderAttribute;
        private final CommandCardType currentCardType;
        private final int chainIndex;
        private final boolean isCriticalStrike;
        private final boolean useFirstCardBoost;
        private final boolean isBusterChain;
        private final boolean isTypeChain;
        private final double commandCardBuff;
        private final double commandCardResist;
        private final double attackBuff;
        private final double defenseBuff;
        private final double specificAttackBuff;
        private final double specificDefenseBuff;
        private final double percentAttackBuff;
        private final double percentDefenseBuff;
        private final double damageAdditionBuff;
        private final double damageReductionBuff;
        private final double criticalDamageBuff;
        private final double fixedRandom;
    }

    @Builder
    @ToString
    public static class NpParameters {
        private final double npCharge;
        private final FateClass defenderClass;
        private final boolean useUndeadNpCorrection;
        private final CommandCardType currentCardType;
        private final int chainIndex;
        private final boolean isCriticalStrike;
        private final boolean useFirstCardBoost;
        private final double commandCardBuff;
        private final double commandCardResist;
        private final double npGenerationBuff;
        private final double classNpCorrection;
    }

    @Builder
    @ToString
    public static class DefNpParameters {
        private final double defNpCharge;
        private final FateClass attackerClass;
        private final boolean useUndeadNpCorrection;
        private final double npGenerationBuff;
        private final double defNpGenerationBuff;
        private final double classNpCorrection;
    }

    @Builder
    @ToString
    public static class CriticalStarParameters {
        private final double servantCriticalStarGeneration;
        private final FateClass defenderClass;
        private final CommandCardType currentCardType;
        private final int chainIndex;
        private final boolean isCriticalStrike;
        private final boolean useFirstCardBoost;
        private final double commandCardBuff;
        private final double commandCardResist;
        private final double critStarGenerationBuff;
    }
}
