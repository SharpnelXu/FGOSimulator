package yome.fgo.simulator.utils;

import com.google.common.annotations.VisibleForTesting;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.ClassAdvantageChangeBuff;

import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;

public class FateClassUtils {
    public static int getClassMaxNpGauge(final FateClass fateClass) {
        return switch (fateClass) {
            case ARCHER, ASSASSIN, ALTEREGO, MOONCANCER -> 3;
            case SABER, LANCER, RULER, PRETENDER, SHIELDER -> 4;
            default -> 5;
        };
    }

    public static double getClassAttackCorrection(final FateClass fateClass) {
        return switch (fateClass) {
            case LANCER -> 1.05;
            case ARCHER -> 0.95;
            case BERSERKER, RULER, AVENGER -> 1.1;
            case CASTER, ASSASSIN -> 0.9;
            default -> 1;
        };
    }
    public static double getClassNpCorrection(final FateClass fateClass) {
        return switch (fateClass) {
            case CASTER, MOONCANCER -> 1.2;
            case RIDER -> 1.1;
            case ASSASSIN -> 0.9;
            case BERSERKER -> 0.8;
            default -> 1;
        };
    }
    public static double getClassCritStarCorrection(final FateClass fateClass) {
        return switch (fateClass) {
            case ARCHER, ALTEREGO -> 0.05;
            case LANCER -> -0.05;
            case RIDER -> 0.1;
            case ASSASSIN, AVENGER, PRETENDER -> -0.1;
            case FOREIGNER -> 0.2;
            default -> 0;
        };
    }

    public static double getClassAdvantage(final Simulation simulation, final Combatant attacker, final Combatant defender) {
        final FateClass attackerClass = attacker.getFateClass();
        final FateClass defenderClass = defender.getFateClass();

        double baseRate = getClassAdvantage(attackerClass, defenderClass);

        for (final Buff buff : attacker.getBuffs()) {
            if (buff instanceof ClassAdvantageChangeBuff && buff.shouldApply(simulation)) {
                baseRate = ((ClassAdvantageChangeBuff) buff).asAttacker(baseRate, defenderClass);
            }
        }

        for (final Buff buff : defender.getBuffs()) {
            if (buff instanceof ClassAdvantageChangeBuff && buff.shouldApply(simulation)) {
                baseRate = ((ClassAdvantageChangeBuff) buff).asDefender(baseRate, attackerClass);
            }
        }

        return baseRate;
    }

    @VisibleForTesting
    static double getClassAdvantage(final FateClass attackerClass, final FateClass defenderClass) {
        switch (attackerClass) {
            case SABER:
                return switch (defenderClass) {
                    case ARCHER, RULER -> 0.5;
                    case LANCER, BERSERKER -> 2.0;
                    default -> 1.0;
                };
            case ARCHER:
                return switch (defenderClass) {
                    case LANCER, RULER -> 0.5;
                    case SABER, BERSERKER -> 2.0;
                    default -> 1.0;
                };
            case LANCER:
                return switch (defenderClass) {
                    case SABER, RULER -> 0.5;
                    case ARCHER, BERSERKER -> 2.0;
                    default -> 1.0;
                };
            case RIDER:
                return switch (defenderClass) {
                    case ASSASSIN, RULER -> 0.5;
                    case CASTER, BERSERKER, BEAST_I -> 2.0;
                    default -> 1.0;
                };
            case CASTER:
                return switch (defenderClass) {
                    case RIDER, RULER -> 0.5;
                    case ASSASSIN, BERSERKER, BEAST_I -> 2.0;
                    default -> 1.0;
                };
            case ASSASSIN:
                return switch (defenderClass) {
                    case CASTER, RULER -> 0.5;
                    case RIDER, BERSERKER, BEAST_I -> 2.0;
                    default -> 1.0;
                };
            case BERSERKER:
                return switch (defenderClass) {
                    case FOREIGNER -> 0.5;
                    case SHIELDER, BEAST_II, BEAST_III_R, BEAST_III_L, BEAST_IV -> 1.0;
                    default -> 1.5;
                };
            case RULER:
                return switch (defenderClass) {
                    case AVENGER -> 0.5;
                    case BERSERKER, MOONCANCER -> 2.0;
                    default -> 1.0;
                };
            case AVENGER:
                return switch (defenderClass) {
                    case MOONCANCER -> 0.5;
                    case BERSERKER, RULER -> 2.0;
                    default -> 1.0;
                };
            case MOONCANCER:
                return switch (defenderClass) {
                    case RULER -> 0.5;
                    case BERSERKER, AVENGER -> 2.0;
                    case BEAST_III_R -> 1.2;
                    default -> 1.0;
                };
            case ALTEREGO:
                return switch (defenderClass) {
                    case SABER, ARCHER, LANCER, PRETENDER -> 0.5;
                    case RIDER, CASTER, ASSASSIN -> 1.5;
                    case BERSERKER, FOREIGNER -> 2.0;
                    case BEAST_III_R, BEAST_III_L -> 1.2;
                    default -> 1.0;
                };
            case FOREIGNER:
                return switch (defenderClass) {
                    case ALTEREGO -> 0.5;
                    case BEAST_III_L -> 1.2;
                    case BERSERKER, FOREIGNER, PRETENDER -> 2.0;
                    default -> 1.0;
                };
            case PRETENDER:
                return switch (defenderClass) {
                    case RIDER, CASTER, ASSASSIN, FOREIGNER -> 0.5;
                    case SABER, ARCHER, LANCER -> 1.5;
                    case BERSERKER, ALTEREGO -> 2.0;
                    default -> 1.0;
                };
            case BEAST_I:
                return switch (defenderClass) {
                    case AVENGER -> 0.5;
                    case SABER, ARCHER, LANCER, BERSERKER -> 2.0;
                    default -> 1.0;
                };
            case BEAST_IV:
                if (defenderClass == CASTER) {
                    return 0.5;
                }
                return 1.0;
            default:
                return 1.0;
        }
    }
}
