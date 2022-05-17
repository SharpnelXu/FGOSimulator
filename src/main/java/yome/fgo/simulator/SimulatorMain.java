package yome.fgo.simulator;

import org.apache.commons.configuration2.INIConfiguration;
import yome.fgo.simulator.gui.creators.MainMenu;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.FileReader;

import static yome.fgo.simulator.utils.FilePathUtils.USER_DIR;

public class SimulatorMain {
    public static void main(String[] args) {
        final INIConfiguration options = new INIConfiguration();
        options.setSeparatorUsedInInput("=");

        try {
            options.read(new FileReader(USER_DIR + "/options.ini"));
            final String langString = options.getSection("Language").getString("lang");
            TranslationManager.setTranslations(langString);
        } catch (final Exception e) {
            TranslationManager.setTranslations("zh_CN");
        }

        MainMenu.main(args);
    }
}