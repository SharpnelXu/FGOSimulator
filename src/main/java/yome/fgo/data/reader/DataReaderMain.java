package yome.fgo.data.reader;

import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import yome.fgo.data.proto.FgoStorageData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.ServantData;

import java.io.File;
import java.io.FileReader;

public class DataReaderMain {
    public static final String ENEMY_DIRECTORY_PATH = System.getProperty("user.dir") + "/enemies";
    public static final String SERVANT_DIRECTORY_PATH = System.getProperty("user.dir") + "/servants";
    public static void main(String[] args) {
        final String id = "test";

        final File newFile = new File(SERVANT_DIRECTORY_PATH + "/" + id + "/" + id + ".json");
        final Parser parser = JsonFormat.parser();
        final ServantData.Builder dataBuilder = ServantData.newBuilder();
        try {
            parser.merge(new FileReader(newFile), dataBuilder);
            final ServantData servantData = dataBuilder.build();
            System.out.println(servantData);
            System.out.println(servantData.getServantAscensionData(0).hasNoblePhantasmUpgrades());
            System.out.println(servantData.getServantAscensionData(0).getNoblePhantasmUpgrades());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void readEnemy(final String id) {
        final File newFile = new File(ENEMY_DIRECTORY_PATH + "/" + id + "/" + id + ".json");
        final Parser parser = JsonFormat.parser();
        final CombatantData.Builder dataBuilder = CombatantData.newBuilder();
        try {
            parser.merge(new FileReader(newFile), dataBuilder);
            final CombatantData combatantData = dataBuilder.build();
            System.out.println(combatantData);
            System.out.println(combatantData.getAttribute());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
