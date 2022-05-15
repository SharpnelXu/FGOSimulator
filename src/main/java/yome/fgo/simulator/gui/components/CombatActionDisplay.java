package yome.fgo.simulator.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import yome.fgo.simulator.models.combatants.CombatAction;
import yome.fgo.simulator.models.combatants.CommandCard;

import java.util.List;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@Getter
public class CombatActionDisplay extends StackPane {
    private final int servantIndex;
    private final int cardIndex;
    private final boolean isNp;
    private final Label stateLabel;
    private int state;
    private int actionIndex;

    public CombatActionDisplay(
            final int servantIndex,
            final int cardIndex,
            final boolean isNp,
            final CommandCard commandCard,
            final List<CombatActionDisplay> combatActionDisplays,
            final SimulationWindow simulationWindow
    ) {
        super();

        setMaxHeight(100);
        setMaxWidth(100);

        this.servantIndex = servantIndex;
        this.cardIndex = cardIndex;
        this.isNp = isNp;
        actionIndex = -1;

        final VBox contentVBox = new VBox(5);
        contentVBox.setAlignment(Pos.TOP_CENTER);

        final Button button = new Button();
        final ImageView cardImage = new ImageView();
        cardImage.setFitHeight(80);
        cardImage.setFitWidth(80);
        cardImage.setImage(simulationWindow.getCardImage(commandCard.getCommandCardType()));
        button.setGraphic(cardImage);

        stateLabel = new Label();
        contentVBox.getChildren().addAll(button, stateLabel);
        button.setOnAction(e -> {
            final int maxState = isNp ? 2 : 3;

            if (actionIndex < 0) {
                actionIndex = registerAction(combatActionDisplays);

                if (actionIndex < 0) {
                    return;
                }
            }

            state = (state + 1) % maxState;
            if (state == 0) {
                stateLabel.setText(null);
                combatActionDisplays.set(actionIndex, null);
                actionIndex = -1;
            } else {
                String text = getTranslation(APPLICATION_SECTION, "Action") + (actionIndex + 1);
                stateLabel.setStyle("-fx-text-fill: black");
                if (state == 2) {
                    text = text + getTranslation(APPLICATION_SECTION, "Crit");
                    stateLabel.setStyle("-fx-text-fill: red");
                }
                stateLabel.setText(text);
            }
        });

        getChildren().addAll(contentVBox);

        if (commandCard.getCommandCardStrengthen() > 0) {
            final Label strengthenLabel = new Label(Integer.toString(commandCard.getCommandCardStrengthen()));
            StackPane.setAlignment(strengthenLabel, Pos.TOP_LEFT);
            getChildren().add(strengthenLabel);
        }

        if (commandCard.getCommandCodeData() != null) {
            final ImageView ccThumbnail = new ImageView();
            ccThumbnail.setFitHeight(30);
            ccThumbnail.setFitWidth(30);
            ccThumbnail.setImage(simulationWindow.getCommandCodeImage(commandCard.getCommandCodeData().getId()));
            StackPane.setAlignment(ccThumbnail, Pos.TOP_RIGHT);
            getChildren().add(ccThumbnail);
        }
    }

    public CombatAction buildAction() {
        return isNp ?
                CombatAction.createNoblePhantasmAction(servantIndex) :
                CombatAction.createCommandCardAction(servantIndex, cardIndex, state == 2);
    }

    public int registerAction(final List<CombatActionDisplay> combatActionDisplays) {
        int index = 0;
        for (; index < combatActionDisplays.size(); index += 1) {
            if (combatActionDisplays.get(index) == null) {
                combatActionDisplays.set(index, this);
                return index;
            }
        }
        if (index >= 3) {
            return -1;
        } else {
            combatActionDisplays.add(this);
            return index;
        }
    }
}
