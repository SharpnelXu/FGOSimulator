package yome.fgo.data.writer;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import static yome.fgo.simulator.utils.FilePathUtils.CRAFT_ESSENCE_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class DataWriter {

    public static void writeEnemy(final CombatantData combatantData, final String category, final String subCategory) {
        final String directoryPath = String.format("%s/%s/%s", ENEMY_DIRECTORY_PATH, category, subCategory);
        writeMessage(combatantData, directoryPath, combatantData.getId());
    }

    public static void writeServant(final ServantData servantData) {
        final String id = servantData.getServantAscensionData(0).getCombatantData().getId();
        writeMessage(servantData, SERVANT_DIRECTORY_PATH, id);
    }

    public static void writeCraftEssence(final CraftEssenceData craftEssenceData) {
        writeMessage(craftEssenceData, CRAFT_ESSENCE_DIRECTORY_PATH, craftEssenceData.getId());
    }

    public static void writeMessage(final Message message, final String directoryPath, final String id) {
        final File newDirectory = new File(directoryPath + "/" + id);
        if (!newDirectory.exists()) {
            newDirectory.mkdirs();
        }

        final File newFile = new File(directoryPath + "/" + id + "/" + id + ".json");
        final Printer printer = JsonFormat.printer();
        try (PrintStream printStream = new PrintStream(newFile)) {
            printStream.println(printer.print(message));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Double> generateSkillValues(final double base, final double step) {
        final ImmutableList.Builder<Double> builder = ImmutableList.builder();
        for (int i = 0; i < 9; i++) {
            final double roundedValue = RoundUtils.roundNearest(base + i * step);
            builder.add(roundedValue);
        }
        builder.add(RoundUtils.roundNearest(base + step * 10));
        return builder.build();
    }
}
