package yome.fgo.simulator.utils;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;

public class CommandCardTypeUtils {
    public static final List<CommandCardType> REGULAR_CARD_TYPES = ImmutableList.of(
            QUICK,
            ARTS,
            BUSTER,
            EXTRA
    );
    public static final List<CommandCardType> SELECTABLE_CARD_TYPES = ImmutableList.of(
            QUICK,
            ARTS,
            BUSTER
    );

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
            final boolean isBusterChain
    ) {
        if (isBusterChain && currentCardType != EXTRA) {
            return attack * 0.2;
        } else {
            return 0;
        }
    }

    public static double extraCardBuff(
            final CommandCardType currentCardType,
            final boolean isTypeChain
    ) {
        if (currentCardType == EXTRA) {
            if (isTypeChain) {
                return 3.5;
            } else {
                return 2;
            }
        } else {
            return 1;
        }
    }

    public static double convertDamageRate(
            final double originalDamageRate,
            final CommandCardType originalCardType,
            final CommandCardType currentCardType
    ) {
        switch (originalCardType) {
            case BUSTER:
                switch (currentCardType) {
                    case BUSTER:
                        return originalDamageRate;
                    case ARTS:
                        return RoundUtils.roundNearest(originalDamageRate * 1.5);
                    case QUICK:
                        return RoundUtils.roundNearest(originalDamageRate * 2);
                }
            case ARTS:
                switch (currentCardType) {
                    case BUSTER:
                        return RoundUtils.roundNearest(originalDamageRate * 2 / 3);
                    case ARTS:
                        return originalDamageRate;
                    case QUICK:
                        return RoundUtils.roundNearest(originalDamageRate * 4 / 3);
                }
            case QUICK:
                switch (currentCardType) {
                    case BUSTER:
                        return RoundUtils.roundNearest(originalDamageRate * 0.5);
                    case ARTS:
                        return RoundUtils.roundNearest(originalDamageRate * 0.75);
                    case QUICK:
                        return originalDamageRate;
                }
        }

        throw new IllegalArgumentException("Unrecognized card types, cannot convert. Original: " + originalCardType + ", Current: " + currentCardType);
    }
}
