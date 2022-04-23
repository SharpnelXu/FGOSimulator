package yome.fgo.simulator.utils;

import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.Attribute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Attribute.BEAST;
import static yome.fgo.data.proto.FgoStorageData.Attribute.EARTH;
import static yome.fgo.data.proto.FgoStorageData.Attribute.MAN;
import static yome.fgo.data.proto.FgoStorageData.Attribute.SKY;
import static yome.fgo.data.proto.FgoStorageData.Attribute.STAR;
import static yome.fgo.simulator.utils.AttributeUtils.getAttributeAdvantage;

public class AttributeUtilsTest {
    public static final Attribute[] ATTRIBUTES = {SKY, EARTH, MAN, STAR, BEAST};
    public static final double[][] EXPECTED_RESULTS = {
            {1.0, 1.1, 0.9, 1.0, 1.0},
            {0.9, 1.0, 1.1, 1.0, 1.0},
            {1.1, 0.9, 1.0, 1.0, 1.0},
            {1.0, 1.0, 1.0, 1.0, 1.1},
            {1.0, 1.0, 1.0, 1.1, 1.0}
    };

    @Test
    public void testGetAttributeAdvantage() {
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            for (int j = 0; j < ATTRIBUTES.length; j++) {
                assertEquals(EXPECTED_RESULTS[i][j], getAttributeAdvantage(ATTRIBUTES[i], ATTRIBUTES[j]));
            }
        }
    }
}
