package yome.fgo.simulator.models.effects;

import lombok.Builder;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.effects.buffs.AttackBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardResist;
import yome.fgo.simulator.models.effects.buffs.CriticalDamageBuff;
import yome.fgo.simulator.models.effects.buffs.CriticalStarGenerationBuff;
import yome.fgo.simulator.models.effects.buffs.DamageAdditionBuff;
import yome.fgo.simulator.models.effects.buffs.DamageReductionBuff;
import yome.fgo.simulator.models.effects.buffs.Evade;
import yome.fgo.simulator.models.effects.buffs.IgnoreDefenceBuff;
import yome.fgo.simulator.models.effects.buffs.IgnoreInvincible;
import yome.fgo.simulator.models.effects.buffs.Invincible;
import yome.fgo.simulator.models.effects.buffs.NpGenerationBuff;
import yome.fgo.simulator.models.effects.buffs.PercentAttackBuff;
import yome.fgo.simulator.models.effects.buffs.PercentDefenseBuff;
import yome.fgo.simulator.models.effects.buffs.PostAttackEffect;
import yome.fgo.simulator.models.effects.buffs.PreAttackEffect;
import yome.fgo.simulator.models.effects.buffs.SpecialInvincible;
import yome.fgo.simulator.models.effects.buffs.SpecificAttackBuff;
import yome.fgo.simulator.models.effects.buffs.SpecificDefenseBuff;
import yome.fgo.simulator.models.effects.buffs.SureHit;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.utils.AttributeUtils.getAttributeAdvantage;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.busterChainDamageAddition;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.extraCardBuff;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardCritStarCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardDamageCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardNpCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.modifierCap;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAdvantage;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAttackCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassCritStarCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassNpCorrection;

public class CommandCardExecution {
    public static final double COMMAND_CARD_DAMAGE_MULTIPLIER = 0.23;

    public static void executeCommandCard(
            final Simulation simulation,
            final int chainIndex,
            final boolean isCriticalStrike,
            final CommandCardType firstCardType,
            final boolean isTypeChain
    ) {
        final Combatant attacker = simulation.getAttacker();
        final Combatant defender = simulation.getDefender();
        final FateClass defenderClass = defender.getFateClass();
        final CommandCard currentCard = simulation.getCurrentCommandCard();
        final CommandCardType currentCardType = currentCard.getCommandCardType();
        final List<Integer> hitsPercentages = currentCard.getHitPercentages();

        attacker.activateEffectActivatingBuff(simulation, PreAttackEffect.class);

        final double commandCardBuff = attacker.applyBuff(simulation, CommandCardBuff.class);
        final double commandCardResist = defender.applyBuff(simulation, CommandCardResist.class);

        final double attackBuff = attacker.applyBuff(simulation, AttackBuff.class);

        final double defenseUpBuff = defender.applyDefenseUpBuff(simulation);
        final double defenseDownBuff = Math.max(defender.applyDefenseDownBuff(simulation), -1); // this is negative
        final boolean ignoreDefense = attacker.consumeBuffIfExist(simulation, IgnoreDefenceBuff.class);
        final double defenseBuff = ignoreDefense ? defenseDownBuff : defenseUpBuff + defenseDownBuff;
        final double specificDefenseBuff = defender.applyBuff(simulation, SpecificDefenseBuff.class);

        final double specificAttackBuff = attacker.applyBuff(simulation, SpecificAttackBuff.class);
        final double criticalDamageBuff = isCriticalStrike ? attacker.applyBuff(simulation, CriticalDamageBuff.class) : 0;

        final double percentDefenseBuff = defender.applyBuff(simulation, PercentDefenseBuff.class);
        final double percentAttackBuff = attacker.applyBuff(simulation, PercentAttackBuff.class);

        final double damageAdditionBuff = attacker.applyBuff(simulation, DamageAdditionBuff.class);
        final double damageReductionBuff = defender.applyBuff(simulation, DamageReductionBuff.class);

        final double npGenerationBuff = attacker.applyBuff(simulation, NpGenerationBuff.class);

        final double critStarGenerationBuff = attacker.applyBuff(simulation, CriticalStarGenerationBuff.class);

        final DamageParameters damageParameters = DamageParameters.builder()
                .attack(attacker.getAttack() + currentCard.getCommandCardStrengthen())
                .totalHits(currentCard.getTotalHits())
                .attackerClass(attacker.getFateClass())
                .defenderClass(defenderClass)
                .attackerAttribute(attacker.getAttribute())
                .defenderAttribute(defender.getAttribute())
                .currentCardType(currentCardType)
                .chainIndex(chainIndex)
                .isCriticalStrike(isCriticalStrike)
                .isTypeChain(isTypeChain)
                .firstCardType(firstCardType)
                .commandCardBuff(commandCardBuff)
                .commandCardResist(commandCardResist)
                .attackBuff(attackBuff)
                .defenseBuff(defenseBuff)
                .specificAttackBuff(specificAttackBuff)
                .specificDefenseBuff(specificDefenseBuff)
                .percentAttackBuff(percentAttackBuff)
                .percentDefenseBuff(percentDefenseBuff)
                .damageAdditionBuff(damageAdditionBuff)
                .damageReductionBuff(damageReductionBuff)
                .criticalDamageBuff(criticalDamageBuff)
                .fixedRandom(simulation.getFixedRandom())
                .build();


        final boolean skipDamage = shouldSkipDamage(simulation, attacker, defender);

        final int totalDamage = calculateTotalDamage(damageParameters);

        int remainingDamage = totalDamage;

        double totalCritStar = 0;
        for (int i = 0; i < hitsPercentages.size(); i++) {
            if (!skipDamage) {
                final int hitsPercentage = hitsPercentages.get(i);
                final int hitDamage;
                if (i < hitsPercentages.size() - 1) {
                    hitDamage = (int) (totalDamage * hitsPercentage / 100.0);
                } else {
                    hitDamage = remainingDamage;
                }

                remainingDamage -= hitDamage;

                defender.receiveDamage(hitDamage);
            }

            final boolean isOverkill = defender.isAlreadyDead() || defender.isBuggedOverkill();

            final NpParameters npParameters = NpParameters.builder()
                    .npCharge(currentCard.getNpCharge())
                    .defenderClass(defenderClass)
                    .useUndeadNpCorrection(defender.getUndeadNpCorrection())
                    .currentCardType(currentCardType)
                    .chainIndex(chainIndex)
                    .isCriticalStrike(isCriticalStrike)
                    .firstCardType(firstCardType)
                    .isOverkill(isOverkill)
                    .commandCardBuff(commandCardBuff)
                    .commandCardResist(commandCardResist)
                    .npGenerationBuff(npGenerationBuff)
                    .build();

            attacker.changeNp(calculateNpGain(npParameters));

            final CriticalStarParameters critStarParams = CriticalStarParameters.builder()
                    .servantCriticalStarGeneration(currentCard.getCriticalStarGeneration())
                    .defenderClass(defenderClass)
                    .currentCardType(currentCardType)
                    .chainIndex(chainIndex)
                    .isCriticalStrike(isCriticalStrike)
                    .firstCardType(firstCardType)
                    .isOverkill(isOverkill)
                    .commandCardBuff(commandCardBuff)
                    .commandCardResist(commandCardResist)
                    .critStarGenerationBuff(critStarGenerationBuff)
                    .build();

            final double hitStars = calculateCritStar(critStarParams);
            if (hitStars > 3) {
                totalCritStar += 3;
            } else {
                totalCritStar += hitStars;
            }
        }
        simulation.gainStar(RoundUtils.roundNearest(totalCritStar));

        attacker.activateEffectActivatingBuff(simulation, PostAttackEffect.class);

        simulation.checkBuffStatus();

        // overkill bug
        defender.addCumulativeTurnDamage(totalDamage - remainingDamage);
    }

    public static boolean shouldSkipDamage(final Simulation simulation, final Combatant attacker, final Combatant defender) {
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

    public static int calculateTotalDamage(DamageParameters damageParameters) {
        // fixed values
        final double classAttackCorrection = getClassAttackCorrection(damageParameters.attackerClass);
        final double classAdvantage = getClassAdvantage(damageParameters.attackerClass, damageParameters.defenderClass);
        final double attributeAdvantage = getAttributeAdvantage(damageParameters.attackerAttribute, damageParameters.defenderAttribute);
        final double commandCardDamageCorrection = getCommandCardDamageCorrection(damageParameters.currentCardType, damageParameters.chainIndex);
        final double busterStartDamageBoost = damageParameters.firstCardType == BUSTER ? 0.5 : 0;
        final int criticalStrikeDamageCorrection = damageParameters.isCriticalStrike ? 1 : 0;
        final double extraCardBuff = extraCardBuff(damageParameters.currentCardType, damageParameters.isTypeChain, damageParameters.firstCardType);
        final double busterChainDamageAddition = busterChainDamageAddition(
                damageParameters.attack,
                damageParameters.currentCardType,
                damageParameters.isTypeChain,
                damageParameters.firstCardType
        );

        // capped buffs
        final double commandCardBuff = modifierCap(damageParameters.commandCardBuff, 4, -1);

        final double attackBuff = modifierCap(damageParameters.attackBuff, 4, -1);
        final double defenseBuff = modifierCap(damageParameters.defenseBuff, 4, -1);

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

    public static double calculateNpGain(final NpParameters npParameters) {
        final double commandCardNpCorrection = getCommandCardNpCorrection(npParameters.currentCardType, npParameters.chainIndex);
        final double artsStartNpBoost = npParameters.firstCardType == ARTS ? 1 : 0;
        final double classNpCorrection = getClassNpCorrection(npParameters.defenderClass);
        final double undeadNpCorrection = npParameters.useUndeadNpCorrection ? 1.2 : 1;
        final int criticalStrikeNpCorrection = npParameters.isCriticalStrike ? 2 : 1;
        final double overkillNpBonus = npParameters.isOverkill ? 1.5 : 1;

        // capped buffs
        final double commandCardBuff = modifierCap(npParameters.commandCardBuff, 4, -1);

        final double hitNpBeforeRound = npParameters.npCharge * classNpCorrection * undeadNpCorrection *
                (commandCardNpCorrection * (1 + commandCardBuff - npParameters.commandCardResist) + artsStartNpBoost) *
                (1 + npParameters.npGenerationBuff) * criticalStrikeNpCorrection * 10000;
        final double roundedNpBeforeOverkillBonus = ((int) hitNpBeforeRound) * overkillNpBonus;
        final double roundedNp2 = (int) roundedNpBeforeOverkillBonus;

        return Math.max(0, RoundUtils.roundNearest(roundedNp2 / 10000));
    }

    public static double calculateCritStar(final CriticalStarParameters critStarParams) {
        final double commandCardCritStarCorrection = getCommandCardCritStarCorrection(critStarParams.currentCardType, critStarParams.chainIndex);
        final double quickStartCritStarBoost = critStarParams.firstCardType == QUICK ? 0.2 : 0;
        final double classCritStarCorrection = getClassCritStarCorrection(critStarParams.defenderClass);
        final double criticalStrikeCritStarCorrection = critStarParams.isCriticalStrike ? 0.2 : 0;
        final double overkillCritStarBonus = critStarParams.isOverkill ? 0.3 : 0;

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
    public static class DamageParameters {
        private final int attack;
        private final int totalHits;
        private final FateClass attackerClass;
        private final FateClass defenderClass;
        private final Attribute attackerAttribute;
        private final Attribute defenderAttribute;
        private final CommandCardType currentCardType;
        private final int chainIndex;
        private final boolean isCriticalStrike;
        private final CommandCardType firstCardType;
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
    public static class NpParameters {
        private final double npCharge;
        private final FateClass defenderClass;
        private final boolean useUndeadNpCorrection;
        private final CommandCardType currentCardType;
        private final int chainIndex;
        private final boolean isCriticalStrike;
        private final CommandCardType firstCardType;
        private final double commandCardBuff;
        private final double commandCardResist;
        private final double npGenerationBuff;
        private boolean isOverkill;
    }

    @Builder
    public static class CriticalStarParameters {
        private final double servantCriticalStarGeneration;
        private final FateClass defenderClass;
        private final CommandCardType currentCardType;
        private final int chainIndex;
        private final boolean isCriticalStrike;
        private final CommandCardType firstCardType;
        private final double commandCardBuff;
        private final double commandCardResist;
        private final double critStarGenerationBuff;
        private boolean isOverkill;
    }
}
