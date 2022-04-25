package yome.fgo.data.writer.creaftessences;

import com.google.protobuf.util.JsonFormat;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;

import java.io.IOException;
import java.io.StringReader;

import static yome.fgo.data.writer.DataWriter.writeCraftEssence;

public class CraftEssence1080 {
    public static void main(final String[] args) {
        final String id = "craftEssence1080";
        final String atkStats = "[\n" +
                "  500,\n" +
                "  515,\n" +
                "  530,\n" +
                "  545,\n" +
                "  560,\n" +
                "  575,\n" +
                "  590,\n" +
                "  605,\n" +
                "  620,\n" +
                "  635,\n" +
                "  651,\n" +
                "  666,\n" +
                "  681,\n" +
                "  696,\n" +
                "  711,\n" +
                "  726,\n" +
                "  741,\n" +
                "  756,\n" +
                "  771,\n" +
                "  786,\n" +
                "  803,\n" +
                "  818,\n" +
                "  833,\n" +
                "  848,\n" +
                "  863,\n" +
                "  878,\n" +
                "  893,\n" +
                "  908,\n" +
                "  923,\n" +
                "  938,\n" +
                "  954,\n" +
                "  969,\n" +
                "  984,\n" +
                "  999,\n" +
                "  1014,\n" +
                "  1029,\n" +
                "  1044,\n" +
                "  1059,\n" +
                "  1074,\n" +
                "  1089,\n" +
                "  1106,\n" +
                "  1121,\n" +
                "  1136,\n" +
                "  1151,\n" +
                "  1166,\n" +
                "  1181,\n" +
                "  1196,\n" +
                "  1211,\n" +
                "  1226,\n" +
                "  1241,\n" +
                "  1257,\n" +
                "  1272,\n" +
                "  1287,\n" +
                "  1302,\n" +
                "  1317,\n" +
                "  1332,\n" +
                "  1347,\n" +
                "  1362,\n" +
                "  1377,\n" +
                "  1392,\n" +
                "  1409,\n" +
                "  1424,\n" +
                "  1439,\n" +
                "  1454,\n" +
                "  1469,\n" +
                "  1484,\n" +
                "  1499,\n" +
                "  1514,\n" +
                "  1529,\n" +
                "  1544,\n" +
                "  1560,\n" +
                "  1575,\n" +
                "  1590,\n" +
                "  1605,\n" +
                "  1620,\n" +
                "  1635,\n" +
                "  1650,\n" +
                "  1665,\n" +
                "  1680,\n" +
                "  1695,\n" +
                "  1712,\n" +
                "  1727,\n" +
                "  1742,\n" +
                "  1757,\n" +
                "  1772,\n" +
                "  1787,\n" +
                "  1802,\n" +
                "  1817,\n" +
                "  1832,\n" +
                "  1847,\n" +
                "  1863,\n" +
                "  1878,\n" +
                "  1893,\n" +
                "  1908,\n" +
                "  1923,\n" +
                "  1938,\n" +
                "  1953,\n" +
                "  1968,\n" +
                "  1983,\n" +
                "  2000\n" +
                "]";
        final String hpStats = "[\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0,\n" +
                "  0\n" +
                "]";
        final CraftEssenceData.Builder craftEssenceData = CraftEssenceData.newBuilder()
                .setId(id)
                .setRarity(5)
                .setCost(12);
        try {
            JsonFormat.parser().merge(new StringReader("{\"atkStats\":" + atkStats + ", \"hpStats\":" + hpStats + "}"), craftEssenceData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeCraftEssence(craftEssenceData.build());
    }
}
