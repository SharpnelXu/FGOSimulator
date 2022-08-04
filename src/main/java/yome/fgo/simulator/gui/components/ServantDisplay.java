package yome.fgo.simulator.gui.components;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.checkerframework.checker.units.qual.A;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.SpecialActivationTarget.NO_SPECIAL_TARGET;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ServantDisplay extends VBox {
    private final SimulationWindow simulationWindow;
    private final int servantIndex;

    private final ImageView servantThumbnail;
    private final List<Button> skillButtons;
    private final List<ImageView> skillThumbnails;
    private final List<AnchorPane> skillCoolDownHides;
    private final Label atkLabel;
    private final Label hpLabel;
    private final Label npLabel;
    private final FlowPane buffsPane;
    private final RadioButton allyTarget;

    public ServantDisplay(final SimulationWindow simulationWindow, final int servantIndex, final ToggleGroup toggleGroup) {
        super();
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);
        setFillWidth(false);
        setPadding(new Insets(10, 10, 10, 10));
        setStyle("-fx-background-color: white; -fx-border-color: grey; -fx-border-width: 3; -fx-border-radius: 3");
        HBox.setHgrow(this, Priority.ALWAYS);

        this.simulationWindow = simulationWindow;
        this.servantIndex = servantIndex;

        allyTarget = new RadioButton();
        allyTarget.setToggleGroup(toggleGroup);
        allyTarget.setOnAction(e -> {
            this.simulationWindow.getSimulation().setCurrentAllyTargetIndex(servantIndex);
            allyTarget.setText(getTranslation(APPLICATION_SECTION, "asTarget"));
            this.simulationWindow.targetSync();
        });

        getChildren().add(allyTarget);

        servantThumbnail = new ImageView();
        servantThumbnail.setFitHeight(100);
        servantThumbnail.setFitWidth(100);
        final AnchorPane imgAnchor = new AnchorPane();
        imgAnchor.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        AnchorPane.setBottomAnchor(servantThumbnail, 0.0);
        AnchorPane.setTopAnchor(servantThumbnail, 0.0);
        AnchorPane.setLeftAnchor(servantThumbnail, 0.0);
        AnchorPane.setRightAnchor(servantThumbnail, 0.0);
        imgAnchor.getChildren().add(servantThumbnail);

        final Button servantSelectButton = new Button();
        servantSelectButton.setGraphic(imgAnchor);
        servantSelectButton.setOnAction(e -> allyTarget.fire());

        getChildren().add(servantSelectButton);

        final HBox skillHBoxes = new HBox();
        skillHBoxes.setSpacing(10);
        skillHBoxes.setAlignment(Pos.CENTER);

        skillButtons = new ArrayList<>();
        skillThumbnails = new ArrayList<>();
        skillCoolDownHides = new ArrayList<>();

        for (int i = 0; i < 3; i += 1) {
            final StackPane stackPane = new StackPane();

            final Button skillButton = new Button();
            skillButtons.add(skillButton);

            final ImageView skillImgView = new ImageView();
            skillImgView.setFitWidth(60);
            skillImgView.setFitHeight(60);
            skillThumbnails.add(skillImgView);
            final AnchorPane skillImgAnchor = new AnchorPane();
            skillImgView.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 1");
            AnchorPane.setBottomAnchor(skillImgView, 0.0);
            AnchorPane.setTopAnchor(skillImgView, 0.0);
            AnchorPane.setLeftAnchor(skillImgView, 0.0);
            AnchorPane.setRightAnchor(skillImgView, 0.0);
            skillImgAnchor.getChildren().add(skillImgView);
            final int iI = i;
            skillButton.setOnAction(e -> activateSkill(iI));
            skillButton.setGraphic(skillImgView);

            final AnchorPane cdAnchor = new AnchorPane();
            cdAnchor.setStyle("-fx-background-color: rgba(0,0,0,0.78); -fx-border-radius: 3; -fx-border-width: 1");
            final Label cdLabel = new Label();
            cdLabel.setFont(new Font(30));
            cdLabel.setStyle("-fx-text-fill: white");
            cdLabel.setAlignment(Pos.CENTER);
            cdLabel.setWrapText(true);
            cdLabel.setMaxWidth(60);
            AnchorPane.setBottomAnchor(cdLabel, 0.0);
            AnchorPane.setTopAnchor(cdLabel, 0.0);
            AnchorPane.setLeftAnchor(cdLabel, 0.0);
            AnchorPane.setRightAnchor(cdLabel, 0.0);
            cdAnchor.getChildren().add(cdLabel);
            skillCoolDownHides.add(cdAnchor);

            stackPane.getChildren().addAll(skillButton, cdAnchor);
            skillHBoxes.getChildren().add(stackPane);
        }

        getChildren().add(skillHBoxes);

        final Button viewBuffs = new Button(getTranslation(APPLICATION_SECTION, "View Buffs"));
        viewBuffs.setOnAction(e -> this.simulationWindow.viewServantBuffs(servantIndex));

        final HBox viewHBox = new HBox(10);
        viewHBox.getChildren().addAll(viewBuffs);

        atkLabel = new Label();
        hpLabel = new Label();
        npLabel = new Label();
        buffsPane = new FlowPane();
        buffsPane.setAlignment(Pos.TOP_CENTER);
        buffsPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        getChildren().addAll(viewHBox, atkLabel, hpLabel, npLabel, buffsPane);
    }

    private void activateSkill(final int skillIndex) {
        final Simulation simulation = simulationWindow.getSimulation();
        final SpecialActivationParams specialActivationParams =
                simulation.getCurrentServants().get(servantIndex).getActiveSkillSpecialTarget(simulation, skillIndex);
        if (specialActivationParams == null || specialActivationParams.getSpecialTarget() == NO_SPECIAL_TARGET) {
            simulationWindow.getSimulation().activateServantSkill(servantIndex, skillIndex);
            simulationWindow.render();
        } else {
            simulationWindow.showSpecialTargetSelectionWindow(specialActivationParams, servantIndex, skillIndex);
        }
    }

    public void renderServant() {
        final Simulation simulation = simulationWindow.getSimulation();
        final List<Servant> currentServants = simulation.getCurrentServants();
        if (currentServants.size() <= servantIndex) {
            setVisible(false);
            return;
        }

        final Servant servant = currentServants.get(servantIndex);

        if (servant == null) {
            setVisible(false);
            return;
        }

        setVisible(true);
        servantThumbnail.setImage(simulationWindow.getServantImage(servant.getId(), servant.getAscension()));

        for (int i = 0; i < 3; i += 1) {
            skillThumbnails.get(i).setImage(simulationWindow.getSkillImage(servant.getActiveSkillIconPath(simulation, i)));

            final boolean canActivate = servant.canActivateActiveSkill(simulation, i);
            skillButtons.get(i).setDisable(!canActivate);
            final AnchorPane skillHide = skillCoolDownHides.get(i);
            skillHide.setVisible(!canActivate);
            if (!canActivate) {
                final Label label = (Label) skillHide.getChildren().get(0);
                final int currentCoolDown = servant.getActiveSkills().get(i).getCurrentCoolDown();

                if (servant.isSkillInaccessible() || currentCoolDown == 0) {
                    label.setText(getTranslation(APPLICATION_SECTION, "Skill Inaccessible"));
                } else {
                    label.setText(Integer.toString(currentCoolDown));
                }
            }
        }

        atkLabel.setText(
                String.format(
                        "%s: %d",
                        getTranslation(APPLICATION_SECTION, "ATK"),
                        servant.getAttack()
                )
        );

        hpLabel.setText(
                String.format(
                        "%s: %d/%d",
                        getTranslation(APPLICATION_SECTION, "HP"),
                        servant.getCurrentHp(),
                        servant.getMaxHp()
                )
        );

        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        npLabel.setText(
                String.format(
                        "%s: %s",
                        getTranslation(APPLICATION_SECTION, "NP"),
                        numberFormat.format(servant.getCurrentNp())
                )
        );

        buffsPane.getChildren().clear();
        final List<Buff> buffs = servant.getBuffs();
        for (final Buff buff : buffs) {
            final ImageView buffImage = new ImageView();
            buffImage.setFitHeight(20);
            buffImage.setFitWidth(20);
            buffImage.setImage(simulationWindow.getBuffImage(buff.getIconName()));
            buffsPane.getChildren().add(buffImage);
        }
    }

    public void setSelected() {
        allyTarget.fire();
    }

    public void targetSync() {
        if (!allyTarget.isSelected()) {
            allyTarget.setText(null);
        }
    }
}
