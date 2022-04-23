package yome.fgo.simulator.utils;

import yome.fgo.data.proto.FgoStorageData.Attribute;

import static yome.fgo.data.proto.FgoStorageData.Attribute.BEAST;
import static yome.fgo.data.proto.FgoStorageData.Attribute.STAR;

public class AttributeUtils {
    public static double getAttributeAdvantage(final Attribute attackerAttribute, final Attribute defenderAttribute) {
        switch (attackerAttribute) {
            case SKY:
                switch (defenderAttribute) {
                    case EARTH:
                        return 1.1;
                    case MAN:
                        return 0.9;
                    default:
                        return 1.0;
                }
            case EARTH:
                switch (defenderAttribute) {
                    case MAN:
                        return 1.1;
                    case SKY:
                        return 0.9;
                    default:
                        return 1.0;
                }
            case MAN:
                switch (defenderAttribute) {
                    case SKY:
                        return 1.1;
                    case EARTH:
                        return 0.9;
                    default:
                        return 1.0;
                }
            case STAR:
                if (defenderAttribute == BEAST) {
                    return 1.1;
                } else {
                    return 1.0;
                }
            case BEAST:
                if (defenderAttribute == STAR) {
                    return 1.1;
                } else {
                    return 1.0;
                }
            default:
                return 1.0;
        }
    }
}
