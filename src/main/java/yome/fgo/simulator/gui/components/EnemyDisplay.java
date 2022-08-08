package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;

import java.util.List;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.INFO_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.getClassIcon;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.renderBuffPane;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class EnemyDisplay extends VBox {
    private final SimulationWindow simulationWindow;
    private final int enemyIndex;

    private final ImageView enemyImage;
    private final Label hpLabel;
    private final Label npGaugeLabel;
    private final FlowPane buffsPane;
    private final RadioButton enemyTarget;
    private final ImageView classImage;
    private final Tooltip classButtonTooltip;

    public EnemyDisplay(final SimulationWindow simulationWindow, final int enemyIndex, final ToggleGroup toggleGroup) {
        super();

        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);
        setFillWidth(false);
        setPadding(new Insets(10, 10, 10, 10));
        setStyle("-fx-background-color: white; -fx-border-color: grey; -fx-border-width: 3; -fx-border-radius: 3");

        this.simulationWindow = simulationWindow;
        this.enemyIndex = enemyIndex;

        enemyTarget = new RadioButton();
        enemyTarget.setToggleGroup(toggleGroup);
        enemyTarget.setOnAction(e -> this.simulationWindow.getSimulation().setCurrentAllyTargetIndex(enemyIndex));

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

        final Button enemySelectButton = new Button();
        enemySelectButton.setGraphic(imgAnchor);
        enemySelectButton.setOnAction(e -> enemyTarget.fire());

        getChildren().add(enemySelectButton);

        hpLabel = new Label();
        npGaugeLabel = new Label();

        final HBox generalInfoHBox = new HBox(10);
        generalInfoHBox.setAlignment(Pos.CENTER);
        generalInfoHBox.getChildren().addAll(hpLabel, npGaugeLabel);

        classImage = new ImageView();
        classImage.setFitWidth(INFO_THUMBNAIL_SIZE);
        classImage.setFitHeight(INFO_THUMBNAIL_SIZE);
        final Button classButton = new Button();
        classButton.setGraphic(classImage);
        classButtonTooltip = new Tooltip();
        classButton.setTooltip(classButtonTooltip);
        classButton.setOnAction(e -> {
            final Combatant combatant = this.simulationWindow.getSimulation().getCurrentEnemies().get(this.enemyIndex);
            this.simulationWindow.showClassInfo(combatant.getFateClass());
        });

        final ImageView checkBuffImage = new ImageView();
        checkBuffImage.setFitHeight(INFO_THUMBNAIL_SIZE);
        checkBuffImage.setFitWidth(INFO_THUMBNAIL_SIZE);
        checkBuffImage.setImage(this.simulationWindow.getSimulationImage("checkBuff"));
        final Button viewBuffs = new Button();
        viewBuffs.setOnAction(e -> this.simulationWindow.viewEnemyBuffs(enemyIndex));
        viewBuffs.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "View Buffs")));
        viewBuffs.setGraphic(checkBuffImage);

        final HBox buttonsHBox = new HBox(5);
        buttonsHBox.setAlignment(Pos.CENTER);
        buttonsHBox.getChildren().addAll(classButton, viewBuffs);

        buffsPane = new FlowPane();
        buffsPane.setAlignment(Pos.TOP_CENTER);

        getChildren().addAll(generalInfoHBox, buttonsHBox, buffsPane);
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
        setManaged(true);
        enemyTarget.setText(getTranslation(ENTITY_NAME_SECTION, combatant.getId()));
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

        final FateClass enemyClass = combatant.getFateClass();
        classImage.setImage(simulationWindow.getClassImage(getClassIcon(enemyClass)));
        classButtonTooltip.setText(getTranslation(CLASS_SECTION, enemyClass.name()));

        renderBuffPane(buffsPane, combatant, simulationWindow);
    }

    public void setSelected() {
        enemyTarget.fire();
    }
}
