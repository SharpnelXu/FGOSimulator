package yome.fgo.simulator.gui.helpers;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import yome.fgo.simulator.gui.components.SimulationWindow;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.List;

public class ComponentUtils {
    public static final String PERMANENT_BUFF_STYLE = "-fx-border-color: grey; -fx-border-style: solid; -fx-border-width: 1; -fx-border-radius: 3px";
    public static final String CD_NUMBER_STYLE = "-fx-background-color: rgba(0,0,0,0.78); -fx-border-radius: 3; -fx-border-width: 1";

    public static final int SKILL_THUMBNAIL_SIZE = 50;

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
        final Label cdLabel = new Label();
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
}
