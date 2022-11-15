package yome.fgo.simulator;

import com.google.protobuf.util.JsonFormat;
import javafx.scene.image.Image;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCodeData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.UserPreference;
import yome.fgo.simulator.gui.components.CommandCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.CraftEssenceDataAnchorPane;
import yome.fgo.simulator.gui.components.MysticCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.ServantDataAnchorPane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static yome.fgo.simulator.utils.FilePathUtils.BUFF_ICON_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.COMMAND_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.CRAFT_ESSENCE_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.LEVEL_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.MYSTIC_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SKILL_ICON_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.USER_PREFERENCE_FILE_PATH;

public class ResourceManager {
    /**
     * Originally going to build a in memory system but realized it needs some sort of updates to refresh memory,
     * so now just call `*_ANCHOR_MAP` to get data.
     */
    @Deprecated
    private static final Map<String, CombatantData> ENEMY_DATA_MAP = new HashMap<>();
    @Deprecated
    private static final Map<String, ServantData> SERVANT_DATA_MAP = new HashMap<>();
    @Deprecated
    private static final Map<String, CraftEssenceData> CRAFT_ESSENCE_DATA_MAP = new HashMap<>();

    public static final Map<Integer, ServantDataAnchorPane> SERVANT_DATA_ANCHOR_MAP = new TreeMap<>();
    public static final Map<Integer, CraftEssenceDataAnchorPane> CRAFT_ESSENCE_DATA_ANCHOR_MAP = new TreeMap<>();
    public static final Map<Integer, MysticCodeDataAnchorPane> MYSTIC_CODE_DATA_ANCHOR_MAP = new TreeMap<>();
    public static final Map<Integer, CommandCodeDataAnchorPane> COMMAND_CODE_DATA_ANCHOR_MAP = new TreeMap<>();

    public static BufferedReader readFile(final String fileName) throws FileNotFoundException {
        final FileInputStream fileInputStream = new FileInputStream(fileName);
        final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

    public static BufferedReader readFile(final File file) throws FileNotFoundException {
        final FileInputStream fileInputStream = new FileInputStream(file);
        final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

    @Deprecated
    public static CombatantData getEnemyCombatantData(final String enemyCategories, final String id) {
        if (!ENEMY_DATA_MAP.containsKey(id)) {
            final String directoryPath = String.format("%s/%s/%s.json", ENEMY_DIRECTORY_PATH, enemyCategories, id);
            final File enemyDataFile = new File(directoryPath);
            if (enemyDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final CombatantData.Builder combatantDataBuilder = CombatantData.newBuilder();
                try {
                    parser.merge(readFile(enemyDataFile), combatantDataBuilder);

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

    @Deprecated
    public static ServantData getServantData(final String id) {
        if (!SERVANT_DATA_MAP.containsKey(id)) {
            final String directoryPath = String.format("%s/%s/%s.json", SERVANT_DIRECTORY_PATH, id, id);
            final File servantDataFile = new File(directoryPath);
            if (servantDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final ServantData.Builder servantDataBuilder = ServantData.newBuilder();
                try {
                    parser.merge(readFile(servantDataFile), servantDataBuilder);

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

    @Deprecated
    public static CraftEssenceData getCraftEssenceData(final String id) {
        if (!CRAFT_ESSENCE_DATA_MAP.containsKey(id)) {
            final String directoryPath = String.format("%s/%s/%s.json", CRAFT_ESSENCE_DIRECTORY_PATH, id, id);
            final File craftEssenceDataFile = new File(directoryPath);
            if (craftEssenceDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final CraftEssenceData.Builder craftEssenceDataBuilder = CraftEssenceData.newBuilder();
                try {
                    parser.merge(readFile(craftEssenceDataFile), craftEssenceDataBuilder);

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

    public static void rebuildDataMap() {
        buildServantSortMap();
        buildCCSortMap();
        buildCESortMap();
        buildMCSortMap();
    }

    public static void buildServantSortMap() {
        SERVANT_DATA_ANCHOR_MAP.clear();
        final File servantDirectory = new File(SERVANT_DIRECTORY_PATH);
        Image defaultImage = null;
        try {
            defaultImage = new Image(new FileInputStream(String.format("%s/defaultServant_thumbnail.png", SERVANT_DIRECTORY_PATH)));
        } catch (final FileNotFoundException ignored) {
        }
        for (final String directoryName : Objects.requireNonNull(servantDirectory.list())) {
            final File servantDataFile = new File(String.format("%s/%s/%s.json", SERVANT_DIRECTORY_PATH, directoryName, directoryName));
            if (servantDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final ServantData.Builder servantDataBuilder = ServantData.newBuilder();
                try {
                    parser.merge(readFile(servantDataFile), servantDataBuilder);
                } catch (final Exception e) {
                    continue;
                }

                final File defaultSvrImgFile = new File(String.format("%s/%s/%s_thumbnail.png", SERVANT_DIRECTORY_PATH, directoryName, directoryName));
                Image servantDefaultImage;
                try {
                    servantDefaultImage = defaultSvrImgFile.exists() ?
                            new Image(new FileInputStream(defaultSvrImgFile)) :
                            defaultImage;
                } catch (final FileNotFoundException e) {
                    servantDefaultImage = defaultImage;
                }

                final ServantData servantData = servantDataBuilder.build();
                final List<Image> ascensionImages = new ArrayList<>();
                for (int i = 1; i <= servantData.getServantAscensionDataCount(); i += 1) {
                    final File ascImgFile = new File(String.format("%s/%s/%s_asc%d_thumbnail.png", SERVANT_DIRECTORY_PATH, directoryName, directoryName, i));
                    Image servantAscensionImage;
                    try {
                        servantAscensionImage = ascImgFile.exists() ?
                                new Image(new FileInputStream(ascImgFile)) :
                                servantDefaultImage;
                    } catch (final FileNotFoundException e) {
                        servantAscensionImage = servantDefaultImage;
                    }
                    ascensionImages.add(servantAscensionImage);
                }
                SERVANT_DATA_ANCHOR_MAP.put(servantData.getServantNum(), new ServantDataAnchorPane(servantData, ascensionImages));
            }
        }
    }

    public static void buildCESortMap() {
        CRAFT_ESSENCE_DATA_ANCHOR_MAP.clear();
        final File servantDirectory = new File(CRAFT_ESSENCE_DIRECTORY_PATH);
        Image defaultImage = null;
        try {
            defaultImage = new Image(new FileInputStream(String.format("%s/defaultCE_thumbnail.png", CRAFT_ESSENCE_DIRECTORY_PATH)));
        } catch (final FileNotFoundException ignored) {
        }
        for (final String directoryName : Objects.requireNonNull(servantDirectory.list())) {
            final File ceDataFile = new File(String.format("%s/%s/%s.json", CRAFT_ESSENCE_DIRECTORY_PATH, directoryName, directoryName));
            if (ceDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final CraftEssenceData.Builder ceDataBuilder = CraftEssenceData.newBuilder();
                try {
                    parser.merge(readFile(ceDataFile), ceDataBuilder);
                } catch (final Exception e) {
                    continue;
                }

                final File ceThumbnail = new File(String.format("%s/%s/%s_thumbnail.png", CRAFT_ESSENCE_DIRECTORY_PATH, directoryName, directoryName));
                Image ceImage;
                try {
                    ceImage = ceThumbnail.exists() ?
                            new Image(new FileInputStream(ceThumbnail)) :
                            defaultImage;
                } catch (final FileNotFoundException e) {
                    ceImage = defaultImage;
                }

                final CraftEssenceData ceData = ceDataBuilder.build();
                CRAFT_ESSENCE_DATA_ANCHOR_MAP.put(ceData.getCeNum(), new CraftEssenceDataAnchorPane(ceData, ceImage));
            }
        }
    }

    public static void buildMCSortMap() {
        MYSTIC_CODE_DATA_ANCHOR_MAP.clear();
        final File servantDirectory = new File(MYSTIC_CODES_DIRECTORY_PATH);
        for (final String directoryName : Objects.requireNonNull(servantDirectory.list())) {
            final File dataFile = new File(String.format("%s/%s/%s.json", MYSTIC_CODES_DIRECTORY_PATH, directoryName, directoryName));
            if (dataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final MysticCodeData.Builder builder = MysticCodeData.newBuilder();
                try {
                    parser.merge(readFile(dataFile), builder);
                } catch (final Exception e) {
                    continue;
                }

                final List<Image> mcImages = new ArrayList<>();
                try {
                    final Image maleImage = new Image(new FileInputStream(
                            String.format("%s/%s/%s_male.png", MYSTIC_CODES_DIRECTORY_PATH, directoryName, directoryName))
                    );
                    mcImages.add(maleImage);
                } catch (final FileNotFoundException e) {
                    mcImages.add(null);
                }
                try {
                    final Image femaleImage = new Image(new FileInputStream(
                            String.format("%s/%s/%s_female.png", MYSTIC_CODES_DIRECTORY_PATH, directoryName, directoryName))
                    );
                    mcImages.add(femaleImage);
                } catch (final FileNotFoundException e) {
                    mcImages.add(null);
                }

                final MysticCodeData data = builder.build();
                MYSTIC_CODE_DATA_ANCHOR_MAP.put(data.getMcNum(), new MysticCodeDataAnchorPane(data, mcImages));
            }
        }
    }

    public static void buildCCSortMap() {
        COMMAND_CODE_DATA_ANCHOR_MAP.clear();
        final File servantDirectory = new File(COMMAND_CODES_DIRECTORY_PATH);
        Image defaultImage = null;
        try {
            defaultImage = new Image(new FileInputStream(String.format("%s/default.png", CRAFT_ESSENCE_DIRECTORY_PATH)));
        } catch (final FileNotFoundException ignored) {
        }
        for (final String directoryName : Objects.requireNonNull(servantDirectory.list())) {
            final File ccDataFile = new File(String.format("%s/%s/%s.json", COMMAND_CODES_DIRECTORY_PATH, directoryName, directoryName));
            if (ccDataFile.exists()) {
                final JsonFormat.Parser parser = JsonFormat.parser();
                final CommandCodeData.Builder ccDataBuilder = CommandCodeData.newBuilder();
                try {
                    parser.merge(readFile(ccDataFile), ccDataBuilder);
                } catch (final Exception e) {
                    continue;
                }

                final File ccThumbnail = new File(String.format("%s/%s/%s.png", COMMAND_CODES_DIRECTORY_PATH, directoryName, directoryName));
                Image ccImage;
                try {
                    ccImage = ccThumbnail.exists() ?
                            new Image(new FileInputStream(ccThumbnail)) :
                            defaultImage;
                } catch (final FileNotFoundException e) {
                    ccImage = defaultImage;
                }

                final CommandCodeData ccData = ccDataBuilder.build();
                COMMAND_CODE_DATA_ANCHOR_MAP.put(ccData.getCcNum(), new CommandCodeDataAnchorPane(ccData, ccImage));
            }
        }
    }

    public static LevelData getLevelData(final String path, final String id) {
        final String directoryPath = String.format("%s/%s/%s.json", LEVEL_DIRECTORY_PATH, path, id);
        final File dataFile = new File(directoryPath);
        final LevelData.Builder builder = LevelData.newBuilder();
        if (dataFile.exists()) {
            final JsonFormat.Parser parser = JsonFormat.parser();
            try {
                parser.merge(readFile(dataFile), builder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        return builder.build();
    }

    public static CommandCodeData getCommandCodeData(final String id) {
        final String directoryPath = String.format("%s/%s/%s.json", COMMAND_CODES_DIRECTORY_PATH, id, id);
        final File dataFile = new File(directoryPath);
        final CommandCodeData.Builder builder = CommandCodeData.newBuilder();
        if (dataFile.exists()) {
            final JsonFormat.Parser parser = JsonFormat.parser();
            try {
                parser.merge(readFile(dataFile), builder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        return builder.build();
    }

    public static File getEnemyThumbnail(final String path, final String id) {
        final File thumbImg = new File(String.format("%s/%s/%s.png", ENEMY_DIRECTORY_PATH, path, id));
        if (thumbImg.exists()) {
            return thumbImg;
        }

        return new File(String.format("%s/defaultEnemy_thumbnail.png", ENEMY_DIRECTORY_PATH));
    }

    public static File getUnknownServantThumbnail() {
        return new File(String.format("%s/unknownServant_thumbnail.png", SERVANT_DIRECTORY_PATH));
    }

    public static File getServantThumbnail(final String id, final int ascension) {
        final File ascImg = new File(String.format("%s/%s/%s_asc%d_thumbnail.png", SERVANT_DIRECTORY_PATH, id, id, ascension));
        if (ascImg.exists()) {
            return ascImg;
        }

        final File svrImg = new File(String.format("%s/%s/%s_thumbnail.png", SERVANT_DIRECTORY_PATH, id, id));
        if (svrImg.exists()) {
            return svrImg;
        }

        return new File(String.format("%s/defaultServant_thumbnail.png", SERVANT_DIRECTORY_PATH));
    }

    public static File getCCThumbnail(final String id) {
        final File ascImg = new File(String.format("%s/%s/%s.png", COMMAND_CODES_DIRECTORY_PATH, id, id));
        if (ascImg.exists()) {
            return ascImg;
        }

        return new File(String.format("%s/default.png", COMMAND_CODES_DIRECTORY_PATH));
    }

    public static File getCEThumbnail(final String id) {
        final File ascImg = new File(String.format("%s/%s/%s_thumbnail.png", CRAFT_ESSENCE_DIRECTORY_PATH, id, id));
        if (ascImg.exists()) {
            return ascImg;
        }

        return new File(String.format("%s/defaultCE_thumbnail.png", CRAFT_ESSENCE_DIRECTORY_PATH));
    }

    public static File getSkillIcon(final String id) {
        final File ascImg = new File(String.format("%s/%s.png", SKILL_ICON_DIRECTORY_PATH, id));
        if (ascImg.exists()) {
            return ascImg;
        }

        return new File(String.format("%s/default.png", SKILL_ICON_DIRECTORY_PATH));
    }

    public static File getBuffIcon(final String id) {
        final File ascImg = new File(String.format("%s/%s.png", BUFF_ICON_DIRECTORY_PATH, id));
        if (ascImg.exists()) {
            return ascImg;
        }

        return new File(String.format("%s/default.png", BUFF_ICON_DIRECTORY_PATH));
    }

    public static UserPreference getUserPreference() {
        final File userPrefsFile = new File(USER_PREFERENCE_FILE_PATH);

        final UserPreference.Builder builder = UserPreference.newBuilder();
        if (userPrefsFile.exists()) {
            final JsonFormat.Parser parser = JsonFormat.parser();
            try {
                parser.merge(readFile(userPrefsFile), builder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        return builder.build();
    }
}
