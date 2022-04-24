package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.CommandCardExecution.CriticalStarParameters;
import yome.fgo.simulator.models.effects.CommandCardExecution.NpParameters;
import yome.fgo.simulator.models.effects.buffs.AttackBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardResist;
import yome.fgo.simulator.models.effects.buffs.CriticalStarGenerationBuff;
import yome.fgo.simulator.models.effects.buffs.DamageAdditionBuff;
import yome.fgo.simulator.models.effects.buffs.DamageReductionBuff;
import yome.fgo.simulator.models.effects.buffs.DefenseBuff;
import yome.fgo.simulator.models.effects.buffs.NpDamageBuff;
import yome.fgo.simulator.models.effects.buffs.NpGenerationBuff;
import yome.fgo.simulator.models.effects.buffs.PercentDefenseBuff;
import yome.fgo.simulator.models.effects.buffs.SpecificAttackBuff;
import yome.fgo.simulator.models.effects.buffs.SpecificDefenseBuff;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ANY;
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateCritStar;
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateNpGainPercentage;
import static yome.fgo.simulator.utils.AttributeUtils.getAttributeAdvantage;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardDamageCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.modifierCap;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAdvantage;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAttackCorrection;

@SuperBuilder
public class NoblePhantasmDamage extends Effect {
    public static final double NP_DAMAGE_MULTIPLIER = 0.23;

    private final Target target;
    private final List<Double> damageRates;
    private final List<Double> npSpecificDamageRates;
    private final boolean isNpSpecificDamageOverchargedEffect;

    @Override
    public void apply(final Simulation simulation, final int level) {
        final CommandCard currentCard = simulation.getCurrentCommandCard();
        final CommandCardType currentCardType = currentCard.getCommandCardType();
        final List<Integer> hitsPercentages = currentCard.getHitPercentages();
        final Servant attacker = simulation.getActivator();
        simulation.setAttacker(attacker);

        final double damageRate = isOverchargedEffect ? damageRates.get(level - 1) : damageRates.get(0);

        final double commandCardBuff = attacker.applyBuff(simulation, CommandCardBuff.class);
        final double attackBuff = attacker.applyBuff(simulation, AttackBuff.class);
        final double specificAttackBuff = attacker.applyBuff(simulation, SpecificAttackBuff.class);
        final double npDamageBuff = attacker.applyBuff(simulation, NpDamageBuff.class);
        final double percentAttackBuff = attacker.applyBuff(simulation, SpecificAttackBuff.class);
        final double damageAdditionBuff = attacker.applyBuff(simulation, DamageAdditionBuff.class);

        final double npGenerationBuff = attacker.applyBuff(simulation, NpGenerationBuff.class);

        final double critStarGenerationBuff = attacker.applyBuff(simulation, CriticalStarGenerationBuff.class);

        for (final Combatant defender : TargetUtils.getTargets(simulation, target)) {
            simulation.setDefender(defender);
            final FateClass defenderClass = defender.getFateClass();
            final double npSpecificDamageRate;
            if (applyCondition.evaluate(simulation)) {
                if (isNpSpecificDamageOverchargedEffect) {
                    npSpecificDamageRate = npSpecificDamageRates.get(level - 1);
                } else {
                    npSpecificDamageRate = npSpecificDamageRates.get(0);
                }
            } else {
                npSpecificDamageRate = 1.0;
            }

            final double commandCardResist = defender.applyBuff(simulation, CommandCardResist.class);
            final double defenseBuff = defender.applyBuff(simulation, DefenseBuff.class);
            final double specificDefenseBuff = defender.applyBuff(simulation, SpecificDefenseBuff.class);
            final double percentDefenseBuff = defender.applyBuff(simulation, PercentDefenseBuff.class);
            final double damageReductionBuff = defender.applyBuff(simulation, DamageReductionBuff.class);

            final NpDamageParameters npDamageParams = NpDamageParameters.builder()
                    .attack(attacker.getAttack())
                    .totalHits(currentCard.getTotalHits())
                    .damageRate(damageRate)
                    .npSpecificAttackRate(npSpecificDamageRate)
                    .attackerClass(attacker.getFateClass())
                    .defenderClass(defenderClass)
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

            final int totalDamage = calculateTotalNpDamage(npDamageParams);

            final boolean skipDamage = defender.activateEvade(simulation);

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

                final boolean isOverkill = defender.isAlreadyDead();

                final NpParameters npParameters = NpParameters.builder()
                        .npCharge(currentCard.getNpCharge())
                        .defenderClass(defenderClass)
                        .useUndeadNpCorrection(defender.getUndeadNpCorrection())
                        .currentCardType(currentCardType)
                        .chainIndex(0)
                        .isCriticalStrike(false)
                        .firstCardType(ANY) // works as long as not ARTS
                        .isOverkill(isOverkill)
                        .commandCardBuff(commandCardBuff)
                        .commandCardResist(commandCardResist)
                        .npGenerationBuff(npGenerationBuff)
                        .build();

                attacker.changeNp(calculateNpGainPercentage(npParameters));

                final CriticalStarParameters critStarParams = CriticalStarParameters.builder()
                        .servantCriticalStarGeneration(currentCard.getCriticalStarGeneration())
                        .defenderClass(defenderClass)
                        .currentCardType(currentCardType)
                        .chainIndex(0)
                        .isCriticalStrike(false)
                        .firstCardType(ANY) // works as long as not QUICK
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
            simulation.gainStar(totalCritStar);

            simulation.setDefender(null);
        }
        simulation.setAttacker(null);
    }

    public static int calculateTotalNpDamage(final NpDamageParameters npDamageParams) {
        // fixed values
        final double classAttackCorrection = getClassAttackCorrection(npDamageParams.attackerClass);
        final double classAdvantage = getClassAdvantage(npDamageParams.attackerClass, npDamageParams.defenderClass);
        final double attributeAdvantage = getAttributeAdvantage(npDamageParams.attackerAttribute, npDamageParams.defenderAttribute);
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
    public static class NpDamageParameters {
        private final int attack;
        private final double damageRate;
        private final int totalHits;
        private final double npSpecificAttackRate;
        private final FateClass attackerClass;
        private final FateClass defenderClass;
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
