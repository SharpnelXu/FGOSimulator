package yome.fgo.simulator.gui.helpers;

import com.google.common.collect.ImmutableMap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Traits;
import yome.fgo.simulator.gui.components.SimulationWindow;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.List;
import java.util.Map;

public class ComponentUtils {
    public static final String PERMANENT_BUFF_STYLE = "-fx-border-color: grey; -fx-border-style: solid; -fx-border-width: 1; -fx-border-radius: 3px";
    public static final String CD_NUMBER_STYLE = "-fx-background-color: rgba(0,0,0,0.78); -fx-border-radius: 3; -fx-border-width: 1";
    public static final String SPECIAL_INFO_BOX_STYLE = "-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-width: 5; -fx-background-color: white";

    public static final int SKILL_THUMBNAIL_SIZE = 50;
    public static final int INFO_THUMBNAIL_SIZE = 30;
    public static final int BUFF_SIZE = 20;

    public static final Map<String, String> FIELD_ICON_MAP = ImmutableMap.of(
            Traits.CITY.name(), "fieldCity",
            Traits.MILLENNIUM_CASTLE.name(), "fieldMillenniumCastle",
            Traits.BURNING.name(), "fieldBurn",
            Traits.FOREST.name(), "fieldForest",
            Traits.SHORE.name(), "fieldShoreline",
            Traits.SUNLIGHT.name(), "fieldSunlight",
            Traits.IMAGINARY_SPACE.name(), "fieldImaginary"
    );

    public static String getFieldIcon(final String fieldTrait) {
        if (FIELD_ICON_MAP.containsKey(fieldTrait)) {
            return FIELD_ICON_MAP.get(fieldTrait);
        }

        return "default";
    }

    public static final Map<FateClass, String> CLASS_ICON_MAP = buildClassIconMap();

    private static Map<FateClass, String> buildClassIconMap() {
        final ImmutableMap.Builder<FateClass, String> builder = ImmutableMap.builder();
        builder.put(FateClass.SABER, "saber");
        builder.put(FateClass.ARCHER, "archer");
        builder.put(FateClass.LANCER, "lancer");
        builder.put(FateClass.RIDER, "rider");
        builder.put(FateClass.CASTER, "caster");
        builder.put(FateClass.ASSASSIN, "assassin");
        builder.put(FateClass.BERSERKER, "berserker");
        builder.put(FateClass.SHIELDER, "shielder");
        builder.put(FateClass.RULER, "ruler");
        builder.put(FateClass.AVENGER, "avenger");
        builder.put(FateClass.MOONCANCER, "mooncancer");
        builder.put(FateClass.ALTEREGO, "alterego");
        builder.put(FateClass.FOREIGNER, "foreigner");
        builder.put(FateClass.PRETENDER, "pretender");
        builder.put(FateClass.BEAST_I, "Beast_I");
        builder.put(FateClass.BEAST_II, "Beast_II");
        builder.put(FateClass.BEAST_III_L, "Beast_III");
        builder.put(FateClass.BEAST_III_R, "Beast_III");
        builder.put(FateClass.BEAST_IV, "Beast_IV");

        return builder.build();
    }

    public static String getClassIcon(final FateClass fateClass) {
        if (CLASS_ICON_MAP.containsKey(fateClass)) {
            return CLASS_ICON_MAP.get(fateClass);
        }
        return "unknown";
    }

    public static AnchorPane wrapInAnchor(final Node node) {
        final AnchorPane imgAnchor = new AnchorPane();
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
        imgAnchor.getChildren().add(node);
        return imgAnchor;
    }

    public static AnchorPane createSkillCdAnchor() {
        final Label cdLabel = new Label("X");
        cdLabel.setFont(new Font(30));
        cdLabel.setStyle("-fx-text-fill: white");
        cdLabel.setAlignment(Pos.CENTER);
        cdLabel.setWrapText(true);
        cdLabel.setMaxWidth(60);

        final AnchorPane cdAnchor = wrapInAnchor(cdLabel);
        cdAnchor.setStyle(CD_NUMBER_STYLE);
        return cdAnchor;
    }

    public static void renderBuffPane(final FlowPane buffsPane, final Combatant combatant, final SimulationWindow simulationWindow) {
        buffsPane.getChildren().clear();
        final List<Buff> buffs = combatant.getBuffs();
        for (final Buff buff : buffs) {
            final ImageView buffImage = new ImageView();
            buffImage.setFitHeight(20);
            buffImage.setFitWidth(20);
            buffImage.setImage(simulationWindow.getBuffImage(buff.getIconName()));
            final AnchorPane buffImgAnchor = wrapInAnchor(buffImage);
            if (buff.isIrremovable()) {
                buffImgAnchor.setStyle(PERMANENT_BUFF_STYLE);
            }
            buffsPane.getChildren().add(buffImgAnchor);
        }
    }

    public static Label createBoldLabel(final String text) {
        final Label newLabel = new Label(text);
        newLabel.setStyle("-fx-font-weight: bold");
        return newLabel;
    }
}
