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
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.gui.creators.ServantCreator;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.DecreaseActiveSkillCoolDown;
import yome.fgo.simulator.models.effects.ForceInstantDeath;
import yome.fgo.simulator.models.effects.GrantBuff;
import yome.fgo.simulator.models.effects.buffs.EndOfTurnEffect;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.data.proto.FgoStorageData.Target.TARGETED_ALLY;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.INFO_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SKILL_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_DISPLAY_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createSkillCdAnchor;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.getClassIcon;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.renderBuffPane;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ServantDisplay extends VBox {
    private static final EffectData FORCE_INSTANT_DEATH_ON_TARGETED_ALLY = EffectData.newBuilder()
            .setType(GrantBuff.class.getSimpleName())
            .setTarget(TARGETED_ALLY)
            .addBuffData(
                    BuffData.newBuilder()
                            .setType(EndOfTurnEffect.class.getSimpleName())
                            .setNumTurnsActive(1)
                            .setBuffIcon("leaveField")
                            .addSubEffects(
                                    EffectData.newBuilder()
                                            .setType(ForceInstantDeath.class.getSimpleName())
                                            .setTarget(SELF)
                            )
            )
            .build();
    private static final EffectData DECREASE_COOL_DOWN_FOR_TARGETED_ALLY = EffectData.newBuilder()
            .setType(DecreaseActiveSkillCoolDown.class.getSimpleName())
            .setTarget(TARGETED_ALLY)
            .addIntValues(99)
            .build();

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
    private final ImageView classImage;
    private final Tooltip classButtonTooltip;

    public ServantDisplay(final SimulationWindow simulationWindow, final int servantIndex, final ToggleGroup toggleGroup) {
        super();
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));
        setStyle(UNIT_DISPLAY_STYLE);
        HBox.setHgrow(this, Priority.ALWAYS);

        this.simulationWindow = simulationWindow;
        this.servantIndex = servantIndex;

        allyTarget = new RadioButton();
        allyTarget.setToggleGroup(toggleGroup);
        allyTarget.setOnAction(e -> this.simulationWindow.getSimulation().setCurrentAllyTargetIndex(servantIndex));

        getChildren().add(allyTarget);

        servantThumbnail = new ImageView();
        servantThumbnail.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        servantThumbnail.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        final AnchorPane imgAnchor = wrapInAnchor(servantThumbnail);
        imgAnchor.setStyle(UNIT_THUMBNAIL_STYLE);

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
            skillImgView.setFitWidth(SKILL_THUMBNAIL_SIZE);
            skillImgView.setFitHeight(SKILL_THUMBNAIL_SIZE);
            skillThumbnails.add(skillImgView);
            final int iI = i;
            skillButton.setOnAction(e -> activateSkill(iI));
            skillButton.setGraphic(skillImgView);

            final AnchorPane cdAnchor = createSkillCdAnchor();
            skillCoolDownHides.add(cdAnchor);

            stackPane.getChildren().addAll(skillButton, cdAnchor);
            skillHBoxes.getChildren().add(stackPane);
        }

        getChildren().add(skillHBoxes);

        atkLabel = new Label();
        hpLabel = new Label();
        npLabel = new Label();

        final HBox generalInfoHBox = new HBox(10);
        generalInfoHBox.setAlignment(Pos.CENTER);
        generalInfoHBox.getChildren().addAll(atkLabel, hpLabel, npLabel);

        classImage = new ImageView();
        classImage.setFitWidth(INFO_THUMBNAIL_SIZE);
        classImage.setFitHeight(INFO_THUMBNAIL_SIZE);
        final Button classButton = new Button();
        classButton.setGraphic(classImage);
        classButtonTooltip = new Tooltip();
        classButton.setTooltip(classButtonTooltip);
        classButton.setOnAction(e -> {
            final Combatant combatant = this.simulationWindow.getSimulation().getCurrentServants().get(this.servantIndex);
            this.simulationWindow.showClassInfo(combatant.getFateClass());
        });

        final ImageView checkBuffImage = new ImageView();
        checkBuffImage.setFitHeight(INFO_THUMBNAIL_SIZE);
        checkBuffImage.setFitWidth(INFO_THUMBNAIL_SIZE);
        checkBuffImage.setImage(this.simulationWindow.getSimulationImage("checkBuff"));
        final Button viewBuffs = new Button();
        viewBuffs.setOnAction(e -> this.simulationWindow.viewServantBuffs(servantIndex));
        viewBuffs.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "View Buffs")));
        viewBuffs.setGraphic(checkBuffImage);

        final ImageView servantInfoImage = new ImageView();
        servantInfoImage.setFitHeight(INFO_THUMBNAIL_SIZE);
        servantInfoImage.setFitWidth(INFO_THUMBNAIL_SIZE);
        servantInfoImage.setImage(this.simulationWindow.getSimulationImage("info"));
        final Button servantInfo = new Button();
        servantInfo.setOnAction(e -> {
            try {
                final List<Servant> currentServants = this.simulationWindow.getSimulation().getCurrentServants();
                if (currentServants.size() > this.servantIndex) {
                    final Servant servant = currentServants.get(this.servantIndex);
                    if (servant != null) {
                        ServantCreator.preview(this.getScene().getWindow(), servant.getServantData());
                    }
                }
            } catch (final IOException ignored) {
            }
        });
        servantInfo.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Servant details")));
        servantInfo.setGraphic(servantInfoImage);

        final ImageView decreaseCDImage = new ImageView(this.simulationWindow.getSimulationImage("skillCharge"));
        decreaseCDImage.setFitHeight(INFO_THUMBNAIL_SIZE);
        decreaseCDImage.setFitWidth(INFO_THUMBNAIL_SIZE);
        final Button chargeSkillButton = new Button();
        chargeSkillButton.setOnAction(e -> activateTargetedEffect(DECREASE_COOL_DOWN_FOR_TARGETED_ALLY));
        chargeSkillButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Charge skill for this servant")));
        chargeSkillButton.setGraphic(decreaseCDImage);

        final ImageView forceInstantDeathImg = new ImageView(this.simulationWindow.getSimulationImage("forceInstantDeath"));
        forceInstantDeathImg.setFitHeight(INFO_THUMBNAIL_SIZE);
        forceInstantDeathImg.setFitWidth(INFO_THUMBNAIL_SIZE);
        final Button forceInstantDeathButton = new Button();
        forceInstantDeathButton.setOnAction(e -> activateTargetedEffect(FORCE_INSTANT_DEATH_ON_TARGETED_ALLY));
        forceInstantDeathButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Force instant death on turn end")));
        forceInstantDeathButton.setGraphic(forceInstantDeathImg);

        final HBox buttonHBox = new HBox(5);
        buttonHBox.setAlignment(Pos.CENTER);
        buttonHBox.getChildren().addAll(classButton, viewBuffs, servantInfo, chargeSkillButton, forceInstantDeathButton);

        buffsPane = new FlowPane();
        buffsPane.setAlignment(Pos.TOP_CENTER);
        buffsPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        getChildren().addAll(generalInfoHBox, buttonHBox, buffsPane);
    }

    private void activateSkill(final int skillIndex) {
        simulationWindow.getSimulation().activateServantSkill(servantIndex, skillIndex);
        simulationWindow.render();
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
        allyTarget.setText(getTranslation(ENTITY_NAME_SECTION, servant.getId()));
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

        final FateClass servantClass = servant.getFateClass();
        classImage.setImage(simulationWindow.getClassImage(getClassIcon(servantClass)));
        classButtonTooltip.setText(getTranslation(CLASS_SECTION, servantClass.name()));

        renderBuffPane(buffsPane, servant, simulationWindow);
    }

    public void setSelected() {
        allyTarget.fire();
    }

    private void activateTargetedEffect(final EffectData effectData) {
        try {
            final Simulation simulation = this.simulationWindow.getSimulation();
            final int currentAllyIndex = simulation.getCurrentAllyTargetIndex();
            simulation.setCurrentAllyTargetIndex(this.servantIndex);
            simulation.activateCustomEffect(effectData);
            simulation.setCurrentAllyTargetIndex(currentAllyIndex);
            this.simulationWindow.render();
        } catch (final Exception ignored) {
        }
    }
}
