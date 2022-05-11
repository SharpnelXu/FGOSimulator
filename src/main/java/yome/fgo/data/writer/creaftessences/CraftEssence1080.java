package yome.fgo.data.writer.creaftessences;

import com.google.protobuf.util.JsonFormat;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;

import java.io.IOException;
import java.io.StringReader;

import static yome.fgo.data.writer.DataWriter.writeCraftEssence;

public class CraftEssence1080 {
    public static void main(final String[] args) {
        final String id = "craftEssence1080";
        final String status = "{\"lv\": 1, \"HP\": 0, \"ATK\": 500},{\"lv\": 2, \"HP\": 0, \"ATK\": 515},{\"lv\": 3, \"HP\": 0, \"ATK\": 530},{\"lv\": 4, \"HP\": 0, \"ATK\": 545},{\"lv\": 5, \"HP\": 0, \"ATK\": 560},{\"lv\": 6, \"HP\": 0, \"ATK\": 575},{\"lv\": 7, \"HP\": 0, \"ATK\": 590},{\"lv\": 8, \"HP\": 0, \"ATK\": 605},{\"lv\": 9, \"HP\": 0, \"ATK\": 620},{\"lv\": 10, \"HP\": 0, \"ATK\": 635},{\"lv\": 11, \"HP\": 0, \"ATK\": 651},{\"lv\": 12, \"HP\": 0, \"ATK\": 666},{\"lv\": 13, \"HP\": 0, \"ATK\": 681},{\"lv\": 14, \"HP\": 0, \"ATK\": 696},{\"lv\": 15, \"HP\": 0, \"ATK\": 711},{\"lv\": 16, \"HP\": 0, \"ATK\": 726},{\"lv\": 17, \"HP\": 0, \"ATK\": 741},{\"lv\": 18, \"HP\": 0, \"ATK\": 756},{\"lv\": 19, \"HP\": 0, \"ATK\": 771},{\"lv\": 20, \"HP\": 0, \"ATK\": 786},{\"lv\": 21, \"HP\": 0, \"ATK\": 803},{\"lv\": 22, \"HP\": 0, \"ATK\": 818},{\"lv\": 23, \"HP\": 0, \"ATK\": 833},{\"lv\": 24, \"HP\": 0, \"ATK\": 848},{\"lv\": 25, \"HP\": 0, \"ATK\": 863},{\"lv\": 26, \"HP\": 0, \"ATK\": 878},{\"lv\": 27, \"HP\": 0, \"ATK\": 893},{\"lv\": 28, \"HP\": 0, \"ATK\": 908},{\"lv\": 29, \"HP\": 0, \"ATK\": 923},{\"lv\": 30, \"HP\": 0, \"ATK\": 938},{\"lv\": 31, \"HP\": 0, \"ATK\": 954},{\"lv\": 32, \"HP\": 0, \"ATK\": 969},{\"lv\": 33, \"HP\": 0, \"ATK\": 984},{\"lv\": 34, \"HP\": 0, \"ATK\": 999},{\"lv\": 35, \"HP\": 0, \"ATK\": 1014},{\"lv\": 36, \"HP\": 0, \"ATK\": 1029},{\"lv\": 37, \"HP\": 0, \"ATK\": 1044},{\"lv\": 38, \"HP\": 0, \"ATK\": 1059},{\"lv\": 39, \"HP\": 0, \"ATK\": 1074},{\"lv\": 40, \"HP\": 0, \"ATK\": 1089},{\"lv\": 41, \"HP\": 0, \"ATK\": 1106},{\"lv\": 42, \"HP\": 0, \"ATK\": 1121},{\"lv\": 43, \"HP\": 0, \"ATK\": 1136},{\"lv\": 44, \"HP\": 0, \"ATK\": 1151},{\"lv\": 45, \"HP\": 0, \"ATK\": 1166},{\"lv\": 46, \"HP\": 0, \"ATK\": 1181},{\"lv\": 47, \"HP\": 0, \"ATK\": 1196},{\"lv\": 48, \"HP\": 0, \"ATK\": 1211},{\"lv\": 49, \"HP\": 0, \"ATK\": 1226},{\"lv\": 50, \"HP\": 0, \"ATK\": 1241},{\"lv\": 51, \"HP\": 0, \"ATK\": 1257},{\"lv\": 52, \"HP\": 0, \"ATK\": 1272},{\"lv\": 53, \"HP\": 0, \"ATK\": 1287},{\"lv\": 54, \"HP\": 0, \"ATK\": 1302},{\"lv\": 55, \"HP\": 0, \"ATK\": 1317},{\"lv\": 56, \"HP\": 0, \"ATK\": 1332},{\"lv\": 57, \"HP\": 0, \"ATK\": 1347},{\"lv\": 58, \"HP\": 0, \"ATK\": 1362},{\"lv\": 59, \"HP\": 0, \"ATK\": 1377},{\"lv\": 60, \"HP\": 0, \"ATK\": 1392},{\"lv\": 61, \"HP\": 0, \"ATK\": 1409},{\"lv\": 62, \"HP\": 0, \"ATK\": 1424},{\"lv\": 63, \"HP\": 0, \"ATK\": 1439},{\"lv\": 64, \"HP\": 0, \"ATK\": 1454},{\"lv\": 65, \"HP\": 0, \"ATK\": 1469},{\"lv\": 66, \"HP\": 0, \"ATK\": 1484},{\"lv\": 67, \"HP\": 0, \"ATK\": 1499},{\"lv\": 68, \"HP\": 0, \"ATK\": 1514},{\"lv\": 69, \"HP\": 0, \"ATK\": 1529},{\"lv\": 70, \"HP\": 0, \"ATK\": 1544},{\"lv\": 71, \"HP\": 0, \"ATK\": 1560},{\"lv\": 72, \"HP\": 0, \"ATK\": 1575},{\"lv\": 73, \"HP\": 0, \"ATK\": 1590},{\"lv\": 74, \"HP\": 0, \"ATK\": 1605},{\"lv\": 75, \"HP\": 0, \"ATK\": 1620},{\"lv\": 76, \"HP\": 0, \"ATK\": 1635},{\"lv\": 77, \"HP\": 0, \"ATK\": 1650},{\"lv\": 78, \"HP\": 0, \"ATK\": 1665},{\"lv\": 79, \"HP\": 0, \"ATK\": 1680},{\"lv\": 80, \"HP\": 0, \"ATK\": 1695},{\"lv\": 81, \"HP\": 0, \"ATK\": 1712},{\"lv\": 82, \"HP\": 0, \"ATK\": 1727},{\"lv\": 83, \"HP\": 0, \"ATK\": 1742},{\"lv\": 84, \"HP\": 0, \"ATK\": 1757},{\"lv\": 85, \"HP\": 0, \"ATK\": 1772},{\"lv\": 86, \"HP\": 0, \"ATK\": 1787},{\"lv\": 87, \"HP\": 0, \"ATK\": 1802},{\"lv\": 88, \"HP\": 0, \"ATK\": 1817},{\"lv\": 89, \"HP\": 0, \"ATK\": 1832},{\"lv\": 90, \"HP\": 0, \"ATK\": 1847},{\"lv\": 91, \"HP\": 0, \"ATK\": 1863},{\"lv\": 92, \"HP\": 0, \"ATK\": 1878},{\"lv\": 93, \"HP\": 0, \"ATK\": 1893},{\"lv\": 94, \"HP\": 0, \"ATK\": 1908},{\"lv\": 95, \"HP\": 0, \"ATK\": 1923},{\"lv\": 96, \"HP\": 0, \"ATK\": 1938},{\"lv\": 97, \"HP\": 0, \"ATK\": 1953},{\"lv\": 98, \"HP\": 0, \"ATK\": 1968},{\"lv\": 99, \"HP\": 0, \"ATK\": 1983},{\"lv\": 100, \"HP\": 0, \"ATK\": 2000}";
        final CraftEssenceData.Builder craftEssenceData = CraftEssenceData.newBuilder()
                .setId(id)
                .setCeNum(1080)
                .setRarity(5)
                .setCost(12);
        try {
            JsonFormat.parser().merge(new StringReader("{\"statusData\":[" + status + "]}"), craftEssenceData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeCraftEssence(craftEssenceData.build());
    }
}
