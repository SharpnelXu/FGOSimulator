package yome.fgo.simulator;

import com.google.protobuf.util.JsonFormat;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import static yome.fgo.simulator.utils.FilePathUtils.CRAFT_ESSENCE_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.LEVEL_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class ResourceManager {
    private static final Map<String, CombatantData> ENEMY_DATA_MAP = new HashMap<>();
    private static final Map<String, ServantData> SERVANT_DATA_MAP = new HashMap<>();
    private static final Map<String, CraftEssenceData> CRAFT_ESSENCE_DATA_MAP = new HashMap<>();

    public static CombatantData getEnemyCombatantData(final String enemyCategories, final String id) {
        if (!ENEMY_DATA_MAP.containsKey(id)) {
            final String directoryPath = String.format("%s/%s/%s/%s.json", ENEMY_DIRECTORY_PATH, enemyCategories, id, id);
            final File enemyDataFile = new File(directoryPath);
            if (enemyDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final CombatantData.Builder combatantDataBuilder = CombatantData.newBuilder();
                try {
                    parser.merge(new FileReader(enemyDataFile), combatantDataBuilder);

                    ENEMY_DATA_MAP.put(id, combatantDataBuilder.build());
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                ENEMY_DATA_MAP.put(id, CombatantData.newBuilder().setId(id).build());
            }
        }

        return ENEMY_DATA_MAP.get(id);
    }

    public static ServantData getServantData(final String id) {
        if (!SERVANT_DATA_MAP.containsKey(id)) {
            final String directoryPath = String.format("%s/%s/%s.json", SERVANT_DIRECTORY_PATH, id, id);
            final File servantDataFile = new File(directoryPath);
            if (servantDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final ServantData.Builder servantDataBuilder = ServantData.newBuilder();
                try {
                    parser.merge(new FileReader(servantDataFile), servantDataBuilder);

                    SERVANT_DATA_MAP.put(id, servantDataBuilder.build());
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                final CombatantData emptyCombatantData = CombatantData.newBuilder().setId(id).build();
                final ServantAscensionData emptyAscensionData =
                        ServantAscensionData.newBuilder().setCombatantData(emptyCombatantData).build();
                SERVANT_DATA_MAP.put(id, ServantData.newBuilder().addServantAscensionData(emptyAscensionData).build());
            }
        }

        return SERVANT_DATA_MAP.get(id);
    }

    public static CraftEssenceData getCraftEssenceData(final String id) {
        if (!CRAFT_ESSENCE_DATA_MAP.containsKey(id)) {
            final String directoryPath = String.format("%s/%s/%s.json", CRAFT_ESSENCE_DIRECTORY_PATH, id, id);
            final File craftEssenceDataFile = new File(directoryPath);
            if (craftEssenceDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final CraftEssenceData.Builder craftEssenceDataBuilder = CraftEssenceData.newBuilder();
                try {
                    parser.merge(new FileReader(craftEssenceDataFile), craftEssenceDataBuilder);

                    CRAFT_ESSENCE_DATA_MAP.put(id, craftEssenceDataBuilder.build());
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                CRAFT_ESSENCE_DATA_MAP.put(id, CraftEssenceData.newBuilder().setId(id).build());
            }
        }

        return CRAFT_ESSENCE_DATA_MAP.get(id);
    }

    public static LevelData getLevelData(final String path, final String id) {
        final String directoryPath = String.format("%s/%s/%s.json", LEVEL_DIRECTORY_PATH, path, id);
        final File dataFile = new File(directoryPath);
        final LevelData.Builder builder = LevelData.newBuilder();
        if (dataFile.exists()) {
            final JsonFormat.Parser parser = JsonFormat.parser();
            try {
                parser.merge(new FileReader(dataFile), builder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        return builder.build();
    }
}
