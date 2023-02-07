package yome.fgo.simulator.utils;

import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;

import static yome.fgo.data.proto.FgoStorageData.FateClass.ALTEREGO;
import static yome.fgo.data.proto.FgoStorageData.FateClass.ARCHER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.ASSASSIN;
import static yome.fgo.data.proto.FgoStorageData.FateClass.AVENGER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_I;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_II;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_III_L;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_III_R;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_IV;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BERSERKER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.FOREIGNER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.LANCER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.MOONCANCER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.PRETENDER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RIDER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RULER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.SABER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.SHIELDER;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CLASS_ADVANTAGE_CHANGE_BUFF;

public class FateClassUtils {
    public static final FateClass[] ALL_CLASSES = {SABER, ARCHER, LANCER, RIDER, CASTER, ASSASSIN, BERSERKER, RULER, AVENGER, ALTEREGO, MOONCANCER, FOREIGNER, PRETENDER, SHIELDER, BEAST_I, BEAST_II, BEAST_III_R, BEAST_III_L, BEAST_IV};

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

        for (final Buff buff : attacker.fetchBuffs(CLASS_ADVANTAGE_CHANGE_BUFF)) {
            if (buff.shouldApply(simulation)) {
                baseRate = buff.asAttacker(baseRate, defenderClass);
                buff.setApplied();
            }
        }

        for (final Buff buff : defender.fetchBuffs(CLASS_ADVANTAGE_CHANGE_BUFF)) {
            if (buff.shouldApply(simulation)) {
                baseRate = buff.asDefender(baseRate, attackerClass);
                buff.setApplied();
            }
        }

        return baseRate;
    }

    public static double getClassAdvantage(final FateClass attackerClass, final FateClass defenderClass) {
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

    public static int getBaseDeathRate(final FateClass fateClass) {
        return switch (fateClass) {
            case SABER, RULER, SHIELDER -> 35;
            case ARCHER -> 45;
            case LANCER -> 40;
            case RIDER, ALTEREGO -> 50;
            case CASTER -> 60;
            case ASSASSIN -> 55;
            case BERSERKER -> 65;
            case AVENGER, FOREIGNER -> 10;
            case MOONCANCER -> 1;
            default -> 0;
        };
    }

    public static int getBaseStarWeight(final FateClass fateClass) {
        return switch (fateClass) {
            case SABER, ASSASSIN, RULER, ALTEREGO, PRETENDER, SHIELDER -> 100;
            case ARCHER, FOREIGNER -> 150;
            case LANCER -> 90;
            case RIDER -> 200;
            case CASTER, MOONCANCER -> 50;
            case BERSERKER -> 10;
            case AVENGER -> 30;
            default -> 0;
        };
    }

    public static int getBaseStarGen(final FateClass fateClass) {
        return switch (fateClass) {
            case SABER, RULER, ALTEREGO, SHIELDER -> 10;
            case ARCHER -> 8;
            case LANCER -> 12;
            case RIDER -> 9;
            case CASTER -> 11;
            case ASSASSIN -> 25;
            case BERSERKER -> 5;
            case AVENGER -> 6;
            case MOONCANCER, FOREIGNER -> 15;
            case PRETENDER -> 20;
            default -> 0;
        };
    }

    public static int getActionCount(final FateClass fateClass) {
        return switch (fateClass) {
            case CASTER, BERSERKER -> 2;
            default -> 3;
        };
    }

    public static int getActionPriority(final FateClass fateClass) {
        return switch (fateClass) {
            case SABER, ARCHER, RIDER, BERSERKER, RULER, SHIELDER -> 50;
            case LANCER -> 150;
            case CASTER, FOREIGNER -> 25;
            case ASSASSIN, ALTEREGO -> 100;
            case AVENGER -> 200;
            case MOONCANCER -> 20;
            default -> 0;
        };
    }
}
