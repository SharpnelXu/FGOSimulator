package yome.fgo.simulator;

import com.google.protobuf.util.JsonFormat;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class ResourceManager {
    private static final Map<String, CombatantData> ENEMY_DATA_MAP = new HashMap<>();
    private static final Map<String, ServantData> SERVANT_DATA_MAP = new HashMap<>();

    public static CombatantData getEnemyCombatantData(final String id) {
        if (!ENEMY_DATA_MAP.containsKey(id)) {
            final File enemyDataFile = new File(ENEMY_DIRECTORY_PATH + "/" + id + "/" + id + ".json");
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
            final File enemyDataFile = new File(SERVANT_DIRECTORY_PATH + "/" + id + "/" + id + ".json");
            if (enemyDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final ServantData.Builder combatantDataBuilder = ServantData.newBuilder();
                try {
                    parser.merge(new FileReader(enemyDataFile), combatantDataBuilder);

                    SERVANT_DATA_MAP.put(id, combatantDataBuilder.build());
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

    public static CombatantData getServantCombatantData(final String id, final int ascension) {
        final ServantData servantData = getServantData(id);
        if (ascension > servantData.getServantAscensionDataCount()) {
            return servantData.getServantAscensionData(0).getCombatantData();
        } else {
            return servantData.getServantAscensionData(ascension - 1).getCombatantData();
        }
    }
}
