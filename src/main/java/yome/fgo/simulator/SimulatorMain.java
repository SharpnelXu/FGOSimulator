package yome.fgo.simulator;

import yome.fgo.simulator.gui.creators.ServantCreator;
import yome.fgo.simulator.translation.TranslationManager;

public class SimulatorMain {
    public static void main(String[] args) {
        TranslationManager.setTranslations("zh_CN");

        ServantCreator.main(args);
    }
}