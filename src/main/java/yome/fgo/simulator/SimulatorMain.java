package yome.fgo.simulator;

import yome.fgo.simulator.gui.creators.MainMenu;
import yome.fgo.simulator.translation.TranslationManager;

public class SimulatorMain {
    public static void main(String[] args) {
        TranslationManager.setTranslations("zh_CN");

        MainMenu.main(args);
    }
}