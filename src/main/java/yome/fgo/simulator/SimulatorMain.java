package yome.fgo.simulator;

import javafx.stage.Stage;
import javafx.stage.Window;
import yome.fgo.data.proto.FgoStorageData;
import yome.fgo.simulator.gui.creators.BuffBuilder;
import yome.fgo.simulator.gui.creators.ConditionBuilder;
import yome.fgo.simulator.gui.creators.EffectBuilder;
import yome.fgo.simulator.gui.creators.EnemyCreator;
import yome.fgo.simulator.gui.creators.VariationBuilder;
import yome.fgo.simulator.translation.TranslationManager;

import static yome.fgo.simulator.gui.creators.VariationBuilder.createVariation;

public class SimulatorMain {
    public static void main(String[] args) {
        TranslationManager.setTranslations("zh_CN");
//        BuffBuilder.main(args);
        EffectBuilder.main(args);

//        ConditionBuilder.main(args);
//        EnemyCreator.main(args);
//        VariationBuilder.main(args);

//        final FgoStorageData.VariationData.Builder builder = FgoStorageData.VariationData.newBuilder();
//        try {
//            createVariation(new Stage(), builder);
//            System.out.println(builder);
//        } catch (final Exception e) {
//            System.out.println("bad happened");
//        }
    }
}