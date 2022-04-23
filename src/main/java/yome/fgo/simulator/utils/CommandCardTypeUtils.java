package yome.fgo.simulator.utils;

import yome.fgo.data.proto.FgoStorageData.CommandCardType;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;

public class CommandCardTypeUtils {
    public static boolean isValidCardPos(
            final CommandCardType commandCardType,
            final int chainIndex
    ) {
        return (chainIndex == 3 && commandCardType == EXTRA) ||
                (chainIndex >= 0 && chainIndex < 3 && commandCardType != EXTRA);
    }

    public static double getCommandCardDamageCorrection(
            final CommandCardType commandCardType,
            final int chainIndex
    ) {
        if (!isValidCardPos(commandCardType, chainIndex)) {
            throw new IllegalArgumentException("For now FGO only supports 3 regular cards per action: " + chainIndex);
        }

        switch (commandCardType) {
            case EXTRA:
                return 1;
            case BUSTER:
                if (chainIndex == 0) {
                    return 1.5;
                } else if (chainIndex == 1) {
                    return 1.8;
                } else {
                    return 2.1;
                }
            case ARTS:
                if (chainIndex == 0) {
                    return 1;
                } else if (chainIndex == 1) {
                    return 1.2;
                } else {
                    return 1.4;
                }
            case QUICK:
                if (chainIndex == 0) {
                    return 0.8;
                } else if (chainIndex == 1) {
                    return 0.96;
                } else {
                    return 1.12;
                }
            default:
                throw new IllegalArgumentException("Unrecognized command card type: " + commandCardType);
        }
    }

    public static double getCommandCardNpCorrection(final CommandCardType commandCardType, final int chainIndex) {
        if (!isValidCardPos(commandCardType, chainIndex)) {
            throw new IllegalArgumentException("For now FGO only supports 3 regular cards per action: " + chainIndex);
        }

        switch (commandCardType) {
            case EXTRA:
                return 1;
            case BUSTER:
                return 0;
            case ARTS:
                if (chainIndex == 0) {
                    return 3;
                } else if (chainIndex == 1) {
                    return 4.5;
                } else {
                    return 6;
                }
            case QUICK:
                if (chainIndex == 0) {
                    return 1;
                } else if (chainIndex == 1) {
                    return 1.5;
                } else {
                    return 2;
                }
            default:
                throw new IllegalArgumentException("Unrecognized command card type: " + commandCardType);
        }
    }

    public static double getCommandCardCritStarCorrection(
            final CommandCardType commandCardType,
            final int chainIndex
    ) {
        if (!isValidCardPos(commandCardType, chainIndex)) {
            throw new IllegalArgumentException("For now FGO only supports 3 regular cards per action: " + chainIndex);
        }

        switch (commandCardType) {
            case EXTRA:
                return 1;
            case BUSTER:
                if (chainIndex == 0) {
                    return 0.1;
                } else if (chainIndex == 1) {
                    return 0.15;
                } else {
                    return 0.2;
                }
            case ARTS:
                return 0;
            case QUICK:
                if (chainIndex == 0) {
                    return 0.8;
                } else if (chainIndex == 1) {
                    return 1.3;
                } else {
                    return 1.8;
                }
            default:
                throw new IllegalArgumentException("Unrecognized command card type: " + commandCardType);
        }
    }

    public static double modifierCap(final double modifier, final double upperBound, final double lowerBound) {
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException(String.format(
                    "Lower bound %.2f is greater than upper bound %.2f",
                    lowerBound,
                    upperBound
            ));
        }

        if (modifier > upperBound) {
            return upperBound;
        } else return Math.max(modifier, lowerBound);
    }

    public static boolean isBusterChain(final boolean isTypeChain, final CommandCardType firstCardType) {
        return isTypeChain && firstCardType == BUSTER;
    }

    public static double busterChainDamageAddition(
            final int attack,
            final CommandCardType currentCardType,
            final boolean isTypeChain,
            final CommandCardType firstCardType
    ) {
        if (isBusterChain(isTypeChain, firstCardType) && currentCardType != EXTRA) {
            return attack * 0.2;
        } else {
            return 0;
        }
    }

    public static double extraCardBuff(
            final CommandCardType currentCardType,
            final boolean isTypeChain,
            final CommandCardType firstCardType
    ) {
        if (currentCardType == EXTRA) {
            if (isBusterChain(isTypeChain, firstCardType)) {
                return 3.5;
            } else {
                return 2;
            }
        } else {
            return 1;
        }
    }
}