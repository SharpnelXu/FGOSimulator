package yome.fgo.simulator;

import org.apache.commons.configuration2.INIConfiguration;
import yome.fgo.simulator.gui.creators.MainMenu;
import yome.fgo.simulator.translation.TranslationManager;

import static yome.fgo.simulator.ResourceManager.readFile;
import static yome.fgo.simulator.utils.FilePathUtils.USER_DIR;

public class SimulatorMain {
    public static final String VERSION_STRING = "v1.06.1";

    public static void main(String[] args) {
        loadOptions();
        MainMenu.main(args);
    }

    public static void loadOptions() {
        final INIConfiguration options = new INIConfiguration();
        options.setSeparatorUsedInInput("=");

        try {
            final String fileName = USER_DIR + "/options.ini";
            options.read(readFile(fileName));
            final String langString = options.getSection("Language").getString("lang");
            TranslationManager.setTranslations(langString);
        } catch (final Exception e) {
            TranslationManager.setTranslations("zh_CN");
        }
    }
}