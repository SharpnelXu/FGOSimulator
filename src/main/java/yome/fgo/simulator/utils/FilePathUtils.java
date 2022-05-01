package yome.fgo.simulator.utils;

import java.io.File;
import java.net.URISyntaxException;

/**
 * From <a href="https://stackoverflow.com/questions/4032957/how-to-get-the-real-path-of-java-application-at-runtime/43553093#43553093">StackOverflow.</a>
 */
public class FilePathUtils {
    public static final String USER_DIR = getProgramDirectory();
    public static final String ENEMY_DIRECTORY_PATH = USER_DIR + "\\Enemies";
    public static final String SERVANT_DIRECTORY_PATH = USER_DIR + "\\Servants";
    public static final String CRAFT_ESSENCE_DIRECTORY_PATH = USER_DIR + "\\CraftEssences";
    public static final String LEVEL_DIRECTORY_PATH = USER_DIR + "\\Levels";

    public static final String TRANSLATION_DIRECTORY_PATH = USER_DIR + "\\Translations";

    private static String getJarName() {
        return new File(FilePathUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    private static boolean runningFromJAR() {
        String jarName = getJarName();
        return jarName.contains(".jar");
    }

    public static String getProgramDirectory() {
        if (runningFromJAR()) {
            return getCurrentJARDirectory();
        } else {
            return getCurrentProjectDirectory();
        }
    }

    private static String getCurrentProjectDirectory() {
        return new File("").getAbsolutePath();
    }

    private static String getCurrentJARDirectory() {
        try {
            return new File(FilePathUtils.class.getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI()
                                    .getPath()).getParentFile().getParent();
        } catch (URISyntaxException exception) {
            exception.printStackTrace();
        }

        return null;
    }
}