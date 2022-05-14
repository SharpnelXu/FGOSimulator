package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.List;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class EnemyDisplay extends VBox {
    private final SimulationWindow simulationWindow;
    private final int enemyIndex;

    private final ImageView enemyImage;
    private final Label hpLabel;
    private final Label npGaugeLabel;
    private final FlowPane buffsPane;
    private final RadioButton enemyTarget;

    public EnemyDisplay(final SimulationWindow simulationWindow, final int enemyIndex, final ToggleGroup toggleGroup) {
        super();

        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);
        setFillWidth(false);
        setPadding(new Insets(10, 10, 10, 10));
        setStyle("-fx-background-color: white; -fx-border-color: grey; -fx-border-width: 3; -fx-border-radius: 3");
        HBox.setHgrow(this, Priority.ALWAYS);

        this.simulationWindow = simulationWindow;
        this.enemyIndex = enemyIndex;

        enemyTarget = new RadioButton();
        enemyTarget.setToggleGroup(toggleGroup);
        enemyTarget.setOnAction(e -> {
            if (enemyTarget.isSelected()) {
                this.simulationWindow.getSimulation().setCurrentEnemyTargetIndex(enemyIndex);
                enemyTarget.setText(getTranslation(APPLICATION_SECTION, "asTarget"));
            } else {
                enemyTarget.setText(null);
            }
        });

        getChildren().add(enemyTarget);

        enemyImage = new ImageView();
        enemyImage.setFitHeight(100);
        enemyImage.setFitWidth(100);
        final AnchorPane imgAnchor = new AnchorPane();
        imgAnchor.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        AnchorPane.setBottomAnchor(enemyImage, 0.0);
        AnchorPane.setTopAnchor(enemyImage, 0.0);
        AnchorPane.setLeftAnchor(enemyImage, 0.0);
        AnchorPane.setRightAnchor(enemyImage, 0.0);
        imgAnchor.getChildren().add(enemyImage);

        getChildren().add(imgAnchor);

        hpLabel = new Label();
        npGaugeLabel = new Label();
        buffsPane = new FlowPane();
        buffsPane.setAlignment(Pos.TOP_CENTER);

        getChildren().addAll(hpLabel, npGaugeLabel, buffsPane);
    }

    public void renderEnemy() {
        final Simulation simulation = simulationWindow.getSimulation();
        final List<Combatant> currentEnemies = simulation.getCurrentEnemies();
        if (currentEnemies.size() <= enemyIndex) {
            setVisible(false);
            setManaged(false);
            return;
        }

        final Combatant combatant = currentEnemies.get(enemyIndex);

        if (combatant == null) {
            setVisible(false);
            setManaged(false);
            return;
        }

        setVisible(true);
        if (combatant instanceof Servant) {
            final Servant servant = (Servant) combatant;
            enemyImage.setImage(simulationWindow.getServantImage(servant.getId(), servant.getAscension()));
        } else {
            enemyImage.setImage(simulationWindow.getEnemyImage(combatant.getEnemyData().getEnemyCategories(), combatant.getId()));
        }

        hpLabel.setText(
                String.format(
                        "%s: %d/%d",
                        getTranslation(APPLICATION_SECTION, "HP"),
                        combatant.getCurrentHp(),
                        combatant.getMaxHp()
                )
        );

        npGaugeLabel.setText(
                String.format(
                        "%s: %d/%d",
                        getTranslation(APPLICATION_SECTION, "NP Gauge"),
                        combatant.getCurrentNpGauge(),
                        combatant.getMaxNpGauge()
                )
        );

        buffsPane.getChildren().clear();
        final List<Buff> buffs = combatant.getBuffs();
        final int maxDisplay = Math.min(buffs.size(), 30);
        for (int i = 0; i < maxDisplay; i += 1) {
            final Buff buff = buffs.get(i);
            final ImageView buffImage = new ImageView();
            buffImage.setFitHeight(20);
            buffImage.setFitWidth(20);
            buffImage.setImage(simulationWindow.getBuffImage(buff.getIconName()));
            buffsPane.getChildren().add(buffImage);
        }
    }

    public void setSelected() {
        enemyTarget.setSelected(true);
    }
}
