package yome.fgo.data.writer;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCodeData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static yome.fgo.simulator.utils.FilePathUtils.COMMAND_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.CRAFT_ESSENCE_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.LEVEL_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.MYSTIC_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class DataWriter {
    public static void writeMessage(final Message message, final String directoryPath) {
        final File newFile = new File(directoryPath);

        final Printer printer = JsonFormat.printer();
        try (final PrintStream printStream = new PrintStream(newFile, StandardCharsets.UTF_8)) {
            printStream.println(printer.print(message));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeEnemy(final CombatantData combatantData, final String category, final String subCategory) {
        final String directoryPath = String.format("%s/%s/%s/%s", ENEMY_DIRECTORY_PATH, category, subCategory, combatantData.getId());
        writeMessage(combatantData, directoryPath, combatantData.getId());
    }

    public static void writeServant(final ServantData servantData) {
        final String id = servantData.getServantAscensionData(0).getCombatantData().getId();
        final String directoryPath = String.format("%s/%s", SERVANT_DIRECTORY_PATH, id);
        writeMessage(servantData, directoryPath, id);
    }

    public static void writeCommandCode(final CommandCodeData commandCodeData) {
        final String directoryPath = String.format("%s/%s", COMMAND_CODES_DIRECTORY_PATH, commandCodeData.getId());
        writeMessage(commandCodeData, directoryPath, commandCodeData.getId());
    }

    public static void writeCraftEssence(final CraftEssenceData craftEssenceData) {
        final String directoryPath = String.format("%s/%s", CRAFT_ESSENCE_DIRECTORY_PATH, craftEssenceData.getId());
        writeMessage(craftEssenceData, directoryPath, craftEssenceData.getId());
    }

    public static void writeMysticCode(final MysticCodeData mysticCodeData) {
        final String directoryPath = String.format("%s/%s", MYSTIC_CODES_DIRECTORY_PATH, mysticCodeData.getId());
        writeMessage(mysticCodeData, directoryPath, mysticCodeData.getId());
    }

    public static void writeLevel(final LevelData levelData, final String subPath) {
        final String directoryPath = String.format("%s/%s", LEVEL_DIRECTORY_PATH, subPath);
        writeMessage(levelData, directoryPath, levelData.getId());
    }

    public static void writeMessage(final Message message, final String directoryPath, final String id) {
        final File newDirectory = new File(directoryPath);
        if (!newDirectory.exists()) {
            newDirectory.mkdirs();
        }

        final File newFile = new File(directoryPath +  "/" + id + ".json");
        final Printer printer = JsonFormat.printer();
        try (final PrintStream printStream = new PrintStream(newFile, StandardCharsets.UTF_8)) {
            printStream.println(printer.print(message));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Double> generateSkillValues(final double base, final double step) {
        final ImmutableList.Builder<Double> builder = ImmutableList.builder();
        for (int i = 0; i < 9; i += 1) {
            final double roundedValue = RoundUtils.roundNearest(base + i * step);
            builder.add(roundedValue);
        }
        builder.add(RoundUtils.roundNearest(base + step * 10));
        return builder.build();
    }
}
