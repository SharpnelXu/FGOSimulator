package yome.fgo.data.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static yome.fgo.simulator.translation.TranslationManager.MOONCELL_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class AtlasDataConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String[] STAGES_NUMBER = {"一"};

    public static String convertAtlasData(final String atlasJsonData) throws JsonProcessingException {
        final JsonNode root = OBJECT_MAPPER.readTree(atlasJsonData);
        final StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("{{关卡配置\n")
                .append("|开放条件=\n")
                .append("|名称jp=").append(root.get("name").asText()).append('\n')
                .append("|名称cn=\n")
                .append("|标题背景颜色=Cyan\n")
                .append("|图标=\n")
                .append("|推荐等级=").append(root.get("recommendLv").asText()).append('\n')
                .append("|牵绊=").append(root.get("bond").asInt()).append('\n')
                .append("|经验=").append(root.get("exp").asInt()).append('\n')
                .append("|QP=").append(root.get("qp").asInt()).append('\n');

        resultBuilder.append("|可重复=");
        if (root.get("afterClear").asText().equals("repeatLast")) {
            resultBuilder.append("1");
        } else {
            resultBuilder.append("0");
        }
        resultBuilder.append('\n');
        resultBuilder.append("|一AP=").append(root.get("consume").asInt()).append("\n");
        resultBuilder.append("|一地点jp=");
        final String spotName = root.get("spotName").asText();
        if (!spotName.equals("Chaldea Gate")) {
            resultBuilder.append(spotName);
        }
        resultBuilder.append('\n');
        resultBuilder.append("|一地点cn=\n");

        final JsonNode stages = root.get("stages");
        for (int i = 0; i < stages.size(); i += 1) {
            resultBuilder.append("|一").append(i + 1).append("=");
            if (i == stages.size() - 1) {
                resultBuilder.append("FATAL BATTLE");
            } else {
                resultBuilder.append("BATTLE");
            }
            resultBuilder.append("\n");

            final JsonNode enemies = stages.get(i).get("enemies");
            for (int j = 0; j < enemies.size(); j += 1) {
                resultBuilder.append("|一").append(i + 1).append("敌人");
                final JsonNode enemy = enemies.get(j);
                resultBuilder.append(enemy.get("deckId").asInt())
                        .append("={{敌人");

                final String enemyRole = enemy.get("roleType").asText();
                final JsonNode svt = enemy.get("svt");
                if (enemyRole.equals("servant")) {
                    resultBuilder.append("3|");
                    resultBuilder.append(getTranslation(MOONCELL_SECTION, "servant" + svt.get("collectionNo").asInt()));
                } else {
                    if (enemyRole.equals("danger")) {
                        resultBuilder.append("2|");
                    } else {
                        resultBuilder.append("1|");
                    }
                    resultBuilder.append(getTranslation(MOONCELL_SECTION, "enemy" + svt.get("id").asInt()));
                }
                resultBuilder.append("|");
                resultBuilder.append(enemy.get("name").asText());
                resultBuilder.append("|");
                resultBuilder.append(getTranslation(MOONCELL_SECTION, svt.get("className").asText()));
                resultBuilder.append("|");
                resultBuilder.append(enemy.get("lv").asText());
                resultBuilder.append("|");
                resultBuilder.append(enemy.get("hp").asText());
                resultBuilder.append("}}\n");
            }
        }

        resultBuilder.append("|一战利品=");
        final JsonNode drops = root.get("drops");
        resultBuilder.append(buildItemListString(drops, false));
        resultBuilder.append("\n");

        resultBuilder.append("|通关奖励=");

        final JsonNode gifts = root.get("gifts");
        resultBuilder.append(buildItemListString(gifts, true));
        resultBuilder.append("\n");

        resultBuilder.append("}}");
        return resultBuilder.toString();
    }

    public static String buildItemListString(final JsonNode items, boolean gift) {
        final Map<Integer, String> ceDrop = new TreeMap<>();
        final Map<Integer, String> commonItemDrop = new TreeMap<>();
        final Map<Integer, String> materialDrop = new TreeMap<>();
        final Map<Integer, String> eventItemDrop = new TreeMap<>();
        for (int i = 0; i < items.size(); i += 1) {
            final StringBuilder dropStringBuilder = new StringBuilder();
            dropStringBuilder.append("{{");
            final JsonNode drop = items.get(i);
            final int id = drop.get("objectId").asInt();
            if (drop.get("type").asText().equals("servant")) {
                dropStringBuilder.append("礼装小图标|");
                dropStringBuilder.append(getTranslation(MOONCELL_SECTION, "craftEssence" + id));
                dropStringBuilder.append("}}");
                ceDrop.put(id, dropStringBuilder.toString());
            } else {
                dropStringBuilder.append("道具|");
                dropStringBuilder.append(getTranslation(MOONCELL_SECTION, "item" + id));
                dropStringBuilder.append("}}");
                if (gift) {
                    dropStringBuilder.append("×").append(drop.get("num").asInt());
                }
                final String dropString = dropStringBuilder.toString();
                if (id > 8000) {
                    eventItemDrop.put(id, dropString);
                } else if (id >= 7000) {
                    commonItemDrop.put(id, dropString);
                } else if (id >= 6500) {
                    materialDrop.put(MATERIAL_ORDER_MAP.get(id), dropString);
                } else {
                    commonItemDrop.put(id, dropString);
                }
            }
        }
        final List<String> dropsList = new ArrayList<>();
        dropsList.addAll(ceDrop.values());
        dropsList.addAll(commonItemDrop.values());
        dropsList.addAll(materialDrop.values());
        dropsList.addAll(eventItemDrop.values());
        return String.join(" ", dropsList);
    }

    public static final Map<Integer, Integer> MATERIAL_ORDER_MAP = buildMaterialOrderMap();

    public static Map<Integer, Integer> buildMaterialOrderMap() {
        final ImmutableMap.Builder<Integer, Integer> builder = ImmutableMap.builder();

        builder.put(6503, 0);
        builder.put(6516, 1);
        builder.put(6512, 2);
        builder.put(6505, 3);
        builder.put(6522, 4);
        builder.put(6527, 5);
        builder.put(6530, 6);
        builder.put(6533, 7);
        builder.put(6534, 8);
        builder.put(6549, 9);
        builder.put(6551, 10);
        builder.put(6552, 11);

        builder.put(6502, 100);
        builder.put(6508, 101);
        builder.put(6515, 102);
        builder.put(6509, 103);
        builder.put(6501, 104);
        builder.put(6510, 105);
        builder.put(6511, 106);
        builder.put(6514, 107);
        builder.put(6513, 108);
        builder.put(6524, 109);
        builder.put(6526, 110);
        builder.put(6532, 111);
        builder.put(6535, 112);
        builder.put(6537, 113);
        builder.put(6536, 114);
        builder.put(6538, 115);
        builder.put(6541, 116);
        builder.put(6543, 117);
        builder.put(6545, 118);
        builder.put(6547, 119);
        builder.put(6550, 120);

        builder.put(6507, 201);
        builder.put(6517, 202);
        builder.put(6506, 203);
        builder.put(6518, 204);
        builder.put(6519, 205);
        builder.put(6520, 206);
        builder.put(6521, 207);
        builder.put(6523, 208);
        builder.put(6525, 209);
        builder.put(6528, 210);
        builder.put(6529, 211);
        builder.put(6531, 212);
        builder.put(6539, 213);
        builder.put(6540, 214);
        builder.put(6542, 215);
        builder.put(6544, 216);
        builder.put(6546, 217);
        builder.put(6548, 218);

        builder.put(6999, 999);

        return builder.build();
    }
}
