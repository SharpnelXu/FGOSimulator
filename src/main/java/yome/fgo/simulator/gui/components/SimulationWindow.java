package yome.fgo.simulator.gui.components;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.gui.components.StatsLogger.LogLevel;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.CombatAction;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.NoblePhantasm;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.craftessences.CraftEssence;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.mysticcodes.MysticCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static yome.fgo.simulator.ResourceManager.getBuffIcon;
import static yome.fgo.simulator.ResourceManager.getCCThumbnail;
import static yome.fgo.simulator.ResourceManager.getEnemyThumbnail;
import static yome.fgo.simulator.ResourceManager.getServantThumbnail;
import static yome.fgo.simulator.ResourceManager.getSkillIcon;
import static yome.fgo.simulator.gui.components.DataPrinter.printBasicCombatantData;
import static yome.fgo.simulator.gui.components.DataPrinter.printEffectData;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.BUFF_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.PERMANENT_BUFF_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENEMY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.BUFF_ICON_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.CARD_IMAGE_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.CLASS_ICON_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.COMMAND_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.MYSTIC_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SIMULATION_ICON_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SKILL_ICON_DIRECTORY_PATH;

public class SimulationWindow {
    private final Map<String, Image> imageCache = new HashMap<>();
    private final Parent root;
    private final Simulation simulation;
    private final List<ServantDisplay> servantDisplays;
    private final MiscDisplay miscDisplay;
    private final GridPane enemyGrid;
    private final StatsLogger statsLogger;
    private final ToggleGroup enemyToggle;
    private final VBox contentVBox;
    private final VBox specialSelectionVBox;
    private final VBox showBuffsVBox;
    private final VBox combatActionsVBox;
    private final FateClassInfoVBox fateClassInfoVBox;

    public SimulationWindow(
            final LevelData levelData,
            final List<ServantData> servantData,
            final List<ServantOption> servantOptions,
            final List<CraftEssenceData> craftEssenceData,
            final List<CraftEssenceOption> craftEssenceOptions,
            final MysticCodeData mysticCodeData,
            final MysticCodeOption mysticCodeOption,
            final double probabilityThreshold,
            final double randomValue
    ) {
        final StackPane stackPane = new StackPane();
        stackPane.setPrefHeight(800);
        stackPane.setPrefWidth(1600);
        stackPane.setAlignment(Pos.CENTER);

        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);
        stackPane.getChildren().addAll(scrollPane);

        root = stackPane;

        contentVBox = new VBox();
        contentVBox.setSpacing(10);
        contentVBox.setPadding(new Insets(10, 10, 10, 10));
        contentVBox.setAlignment(Pos.TOP_CENTER);
        scrollPane.setContent(contentVBox);

        final Label levelName = new Label(levelData.getId());

        contentVBox.getChildren().add(levelName);

        final Level level = new Level(levelData);

        final List<Servant> servants = new ArrayList<>();
        for (int i = 0; i < servantData.size(); i += 1) {
            if (servantData.get(i) != null) {
                final Servant servant = new Servant(servantData.get(i), servantOptions.get(i));
                if (craftEssenceData.get(i) != null) {
                    servant.equipCraftEssence(new CraftEssence(craftEssenceData.get(i), craftEssenceOptions.get(i)));
                }

                servants.add(servant);
            }
        }

        final MysticCode mysticCode = new MysticCode(mysticCodeData, mysticCodeOption);

        simulation = new Simulation(level, servants, mysticCode);
        simulation.setProbabilityThreshold(probabilityThreshold);
        simulation.setFixedRandom(randomValue);

        final HBox servantHBox = new HBox();
        servantHBox.setSpacing(10);

        servantDisplays = new ArrayList<>();
        final ToggleGroup allyTargetToggle = new ToggleGroup();
        for (int i = 0; i < 3; i += 1) {
            final ServantDisplay servantDisplay = new ServantDisplay(this, i, allyTargetToggle);
            servantDisplays.add(servantDisplay);
            servantHBox.getChildren().add(servantDisplay);
        }
        servantHBox.getChildren().add(new Separator(Orientation.VERTICAL));
        miscDisplay = new MiscDisplay(this);
        servantHBox.getChildren().add(miscDisplay);

        contentVBox.getChildren().addAll(servantHBox, new Separator());

        enemyGrid = new GridPane();
        enemyGrid.setHgap(5);
        enemyGrid.setVgap(5);
        for (int i = 0; i < 3; i++) {
            final ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(33.33);
            enemyGrid.getColumnConstraints().add(constraints);
        }
        enemyGrid.getRowConstraints().add(new RowConstraints());

        contentVBox.getChildren().addAll(enemyGrid, new Separator());

        statsLogger = new StatsLogger();
        statsLogger.setLogLevel(LogLevel.EFFECT);

        contentVBox.getChildren().add(statsLogger);

        enemyToggle = new ToggleGroup();

        simulation.setStatsLogger(statsLogger);

        specialSelectionVBox = new VBox();
        specialSelectionVBox.setVisible(false);
        specialSelectionVBox.setSpacing(10);
        specialSelectionVBox.setPadding(new Insets(30));
        specialSelectionVBox.setStyle(SPECIAL_INFO_BOX_STYLE);
        specialSelectionVBox.setAlignment(Pos.TOP_CENTER);
        specialSelectionVBox.setEffect(new DropShadow());
        stackPane.getChildren().add(new Group(specialSelectionVBox));

        showBuffsVBox = new VBox();
        showBuffsVBox.setVisible(false);
        showBuffsVBox.setSpacing(10);
        showBuffsVBox.setPadding(new Insets(30));
        showBuffsVBox.setStyle(SPECIAL_INFO_BOX_STYLE);
        showBuffsVBox.setAlignment(Pos.TOP_CENTER);
        showBuffsVBox.setFillWidth(false);
        showBuffsVBox.setEffect(new DropShadow());

        stackPane.getChildren().add(new Group(showBuffsVBox));

        combatActionsVBox = new VBox();
        combatActionsVBox.setVisible(false);
        combatActionsVBox.setSpacing(10);
        combatActionsVBox.setPadding(new Insets(30));
        combatActionsVBox.setStyle(SPECIAL_INFO_BOX_STYLE);
        combatActionsVBox.setAlignment(Pos.TOP_CENTER);
        combatActionsVBox.setEffect(new DropShadow());
        stackPane.getChildren().add(new Group(combatActionsVBox));

        fateClassInfoVBox = new FateClassInfoVBox(contentVBox);
        fateClassInfoVBox.setVisible(false);
        stackPane.getChildren().add(new Group(fateClassInfoVBox));

        simulation.initiate();

        render();
    }

    /**
     * Callback function to set scene related properties because I didn't put Scene in this class
     */
    public void init() {
        root.getScene().heightProperty().addListener(e -> {
            final double height = root.getScene().getHeight();
            combatActionsVBox.setMaxHeight(height);
            showBuffsVBox.setMaxHeight(height);
            specialSelectionVBox.setMaxHeight(height);
            fateClassInfoVBox.setMaxHeight(height);
        });
        root.getScene().widthProperty().addListener(e -> {
            final double width = root.getScene().getWidth();
            combatActionsVBox.setMaxWidth(width);
            showBuffsVBox.setMaxWidth(width);
            specialSelectionVBox.setMaxWidth(width);
            fateClassInfoVBox.setMaxWidth(width);
        });
    }

    public void setLogLevel(final LogLevel logLevel) {
        statsLogger.setLogLevel(logLevel);
    }

    public Parent getRoot() {
        return root;
    }

    public void render() {
        for (final ServantDisplay servantDisplay : servantDisplays) {
            servantDisplay.renderServant();
        }
        servantDisplays.get(simulation.getCurrentAllyTargetIndex()).setSelected();
        miscDisplay.renderMisc();

        final List<Combatant> enemies = simulation.getCurrentEnemies();
        final List<Node> nodes = enemyGrid.getChildren();
        final int maxIndex = Math.max(enemies.size(), nodes.size());
        for (int i = 0; i < maxIndex; i += 1) {
            if (nodes.size() > i) {
                ((EnemyDisplay) nodes.get(i)).renderEnemy();
            } else {
                final EnemyDisplay node = new EnemyDisplay(this, i, enemyToggle);
                final int size = nodes.size();
                final int rowIndex = size / 3;
                final int colIndex = 2 - size % 3;
                enemyGrid.add(node, colIndex, rowIndex);
                node.renderEnemy();
            }
        }
        ((EnemyDisplay) enemyGrid.getChildren().get(simulation.getCurrentEnemyTargetIndex())).setSelected();
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Image getCardImage(final CommandCardType cardType) {
        final String cardString = cardType.name().toLowerCase();
        final String path = String.format("%s/%s.png", CARD_IMAGE_DIRECTORY_PATH, cardString);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, new File(path));
    }

    public Image getBuffImage(final String iconName) {
        final String path = String.format("%s/%s.png", BUFF_ICON_DIRECTORY_PATH, iconName);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, getBuffIcon(iconName));
    }

    public Image getSkillImage(final String iconName) {
        final String path = String.format("%s/%s.png", SKILL_ICON_DIRECTORY_PATH, iconName);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, getSkillIcon(iconName));
    }

    public Image getClassImage(final String iconName) {
        final String path = String.format("%s/%s.png", CLASS_ICON_DIRECTORY_PATH, iconName);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, new File(path));
    }

    public Image getSimulationImage(final String iconName) {
        final String path = String.format("%s/%s.png", SIMULATION_ICON_DIRECTORY_PATH, iconName);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, new File(path));
    }

    public Image getMysticCodeImage(final String id, final String genderString) {
        final String path = String.format("%s/%s/%s_%s.png", MYSTIC_CODES_DIRECTORY_PATH, id, id, genderString);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, new File(path));
    }

    public Image getCommandCodeImage(final String id) {
        final String path = String.format("%s/%s/%s.png", COMMAND_CODES_DIRECTORY_PATH, id, id);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, getCCThumbnail(id));
    }

    public Image getEnemyImage(final String pathToData, final String enemyId) {
        final String path = String.format("%s/%s/%s.png", ENEMY_DIRECTORY_PATH, pathToData, enemyId);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, getEnemyThumbnail(pathToData, enemyId));
    }

    public Image getServantImage(final String servantId, final int ascension) {
        final String path = String.format("%s/%s/%s_asc%d_thumbnail.png", SERVANT_DIRECTORY_PATH, servantId, servantId, ascension);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, getServantThumbnail(servantId, ascension));
    }

    private Image getImage(final String path, final File file) {
        Image image = null;
        try {
            image = new Image(new FileInputStream(file));
        } catch (final FileNotFoundException ignored) {}

        imageCache.put(path, image);
        return image;
    }

    public void showSpecialTargetSelectionWindow(
            final SpecialActivationParams specialActivationParams,
            final int servantIndex,
            final int skillIndex
    ) {
        specialSelectionVBox.getChildren().clear();
        specialSelectionVBox.setVisible(true);
        switch (specialActivationParams.getSpecialTarget()) {
            case ORDER_CHANGE -> {
                final Label orderChangeDesc = new Label(getTranslation(APPLICATION_SECTION, "Select servants to swap"));
                specialSelectionVBox.getChildren().add(orderChangeDesc);
                final HBox servantsHBox = new HBox();
                servantsHBox.setAlignment(Pos.CENTER);
                servantsHBox.setSpacing(10);
                final ToggleGroup onFieldToggleGroup = new ToggleGroup();
                final List<OrderChangeChoice> onFieldChoices = new ArrayList<>();
                for (int i = 0; i < simulation.getCurrentServants().size(); i += 1) {
                    final Servant servant = simulation.getCurrentServants().get(i);
                    if (servant == null || !servant.isSelectable()) {
                        continue;
                    }
                    final OrderChangeChoice orderChangeChoice = new OrderChangeChoice(
                            onFieldToggleGroup,
                            i,
                            servant,
                            this
                    );
                    servantsHBox.getChildren().add(orderChangeChoice);
                    onFieldChoices.add(orderChangeChoice);
                }
                servantsHBox.getChildren().add(new Separator(Orientation.VERTICAL));
                final ToggleGroup backupToggleGroup = new ToggleGroup();
                final List<OrderChangeChoice> backupChoices = new ArrayList<>();
                for (int i = 0; i < simulation.getBackupServants().size(); i += 1) {
                    final Servant servant = simulation.getBackupServants().get(i);
                    final OrderChangeChoice orderChangeChoice = new OrderChangeChoice(
                            backupToggleGroup,
                            i,
                            servant,
                            this
                    );
                    servantsHBox.getChildren().add(orderChangeChoice);
                    backupChoices.add(orderChangeChoice);
                }
                specialSelectionVBox.getChildren().add(servantsHBox);
                final HBox buttonHBox = new HBox();
                buttonHBox.setAlignment(Pos.CENTER);
                buttonHBox.setSpacing(10);
                final Button cancelButton = new Button(getTranslation(APPLICATION_SECTION, "Cancel"));
                cancelButton.setOnAction(e -> {
                    specialSelectionVBox.setVisible(false);
                    contentVBox.setDisable(false);
                });
                final Button selectButton = new Button(getTranslation(APPLICATION_SECTION, "Select"));
                selectButton.setOnAction(e -> {
                    if (onFieldChoices.stream().noneMatch(OrderChangeChoice::isSelected) ||
                            backupChoices.stream().noneMatch(OrderChangeChoice::isSelected)) {
                        return;
                    }

                    specialSelectionVBox.setVisible(false);
                    contentVBox.setDisable(false);

                    final List<Integer> selections = new ArrayList<>();
                    final int onFieldIndex = onFieldChoices.stream()
                            .filter(OrderChangeChoice::isSelected)
                            .collect(Collectors.toList())
                            .get(0)
                            .getIndex();

                    selections.add(onFieldIndex);

                    final int backupIndex = backupChoices.stream()
                            .filter(OrderChangeChoice::isSelected)
                            .collect(Collectors.toList())
                            .get(0)
                            .getIndex();

                    selections.add(backupIndex);

                    final String servantIdOnField = simulation.getCurrentServants().get(onFieldIndex).getId();
                    final String servantIdInBackup = simulation.getBackupServants().get(backupIndex).getId();
                    statsLogger.logEffect(
                            String.format(
                                    getTranslation(APPLICATION_SECTION, "Selected %s and %s (backup) to swap"),
                                    getTranslation(ENEMY_NAME_SECTION, servantIdOnField),
                                    getTranslation(ENEMY_NAME_SECTION, servantIdInBackup)
                            )
                    );

                    simulation.setOrderChangeSelections(selections);

                    if (servantIndex < 0) {
                        simulation.activateMysticCodeSkill(skillIndex);
                        render();
                    } else {
                        simulation.activateServantSkill(servantIndex, skillIndex);
                        render();
                    }
                });
                buttonHBox.getChildren().addAll(cancelButton, selectButton);
                specialSelectionVBox.getChildren().add(buttonHBox);
            }
            case CARD_TYPE -> {
                final Label cardTypeDesc = new Label(getTranslation(APPLICATION_SECTION, "Select card type"));
                specialSelectionVBox.getChildren().add(cardTypeDesc);
                final HBox cardTypesHBox = new HBox();
                cardTypesHBox.setAlignment(Pos.CENTER);
                cardTypesHBox.setSpacing(10);
                final List<CommandCardType> selectionTypes = specialActivationParams.getCardTypeSelectionsList();
                for (final CommandCardType commandCardType : selectionTypes) {
                    final Button cardTypeSelectButton = new Button();
                    final ImageView cardImage = new ImageView();
                    cardImage.setFitHeight(80);
                    cardImage.setFitWidth(80);
                    cardImage.setImage(getCardImage(commandCardType));
                    cardTypeSelectButton.setGraphic(cardImage);

                    cardTypeSelectButton.setOnAction(e -> {
                        specialSelectionVBox.setVisible(false);
                        contentVBox.setDisable(false);
                        simulation.setSelectedCommandCardType(commandCardType);
                        statsLogger.logEffect(
                                String.format(
                                        getTranslation(APPLICATION_SECTION, "Selected card type %s"),
                                        getTranslation(COMMAND_CARD_TYPE_SECTION, commandCardType.name())
                                )
                        );
                        if (servantIndex < 0) {
                            simulation.activateMysticCodeSkill(skillIndex);
                            render();
                        } else {
                            simulation.activateServantSkill(servantIndex, skillIndex);
                            render();
                        }
                    });
                    cardTypesHBox.getChildren().add(cardTypeSelectButton);
                }
                specialSelectionVBox.getChildren().add(cardTypesHBox);
                final Button cardTypeCancelButton = new Button(getTranslation(APPLICATION_SECTION, "Cancel"));
                cardTypeCancelButton.setOnAction(e ->{
                    specialSelectionVBox.setVisible(false);
                    contentVBox.setDisable(false);
                });
                specialSelectionVBox.getChildren().add(cardTypeCancelButton);
            }
            case RANDOM_EFFECT -> {
                final Label effectSelectDesc = new Label(getTranslation(APPLICATION_SECTION, "Select random effect"));
                specialSelectionVBox.getChildren().add(effectSelectDesc);
                final List<EffectData> randomEffectSelections = specialActivationParams.getRandomEffectSelectionsList();
                for (final EffectData effectData : randomEffectSelections) {
                    final Button randomEffectSelectionButton = new Button(printEffectData(effectData));
                    randomEffectSelectionButton.setMaxWidth(600);
                    randomEffectSelectionButton.setWrapText(true);

                    randomEffectSelectionButton.setOnAction(e -> {
                        specialSelectionVBox.setVisible(false);
                        contentVBox.setDisable(false);
                        simulation.setSelectedEffectData(effectData);
                        statsLogger.logEffect(
                                String.format(
                                        getTranslation(APPLICATION_SECTION, "Selected random effect %s"),
                                        printEffectData(effectData)
                                )
                        );
                        if (servantIndex < 0) {
                            simulation.activateMysticCodeSkill(skillIndex);
                            render();
                        } else {
                            simulation.activateServantSkill(servantIndex, skillIndex);
                            render();
                        }
                    });
                    specialSelectionVBox.getChildren().add(randomEffectSelectionButton);
                }
                final Button randomEffectCancelButton = new Button(getTranslation(APPLICATION_SECTION, "Cancel"));
                randomEffectCancelButton.setOnAction(e -> {
                    specialSelectionVBox.setVisible(false);
                    contentVBox.setDisable(false);
                });
                specialSelectionVBox.getChildren().add(randomEffectCancelButton);
            }
        }

        contentVBox.setDisable(true);
    }

    public void viewEnemyBuffs(final int enemyIndex) {
        showBuffsVBox.setVisible(true);
        showBuffsVBox.getChildren().clear();

        final Combatant combatant = simulation.getCurrentEnemies().get(enemyIndex);
        final Image svrImg = combatant instanceof Servant ?
                getServantImage(combatant.getId(), ((Servant) combatant).getAscension()) :
                getEnemyImage(combatant.getEnemyData().getEnemyCategories(), combatant.getId());
        populateShowBuffsVBox(svrImg, combatant);
    }

    public void viewServantBuffs(final int servantIndex) {
        showBuffsVBox.setVisible(true);
        showBuffsVBox.getChildren().clear();

        final Servant servant = simulation.getCurrentServants().get(servantIndex);
        final Image svrImg = getServantImage(servant.getId(), servant.getAscension());
        populateShowBuffsVBox(svrImg, servant);
    }

    private void populateShowBuffsVBox(final Image image, final Combatant combatant) {
        final Button closeButton = new Button(getTranslation(APPLICATION_SECTION, "Close"));
        closeButton.setOnAction(e -> {
            showBuffsVBox.setVisible(false);
            contentVBox.setDisable(false);
        });

        final AnchorPane imgAnchor = createServantImage(image);

        final String hpBarIndex = combatant.getHpBars().size() != 1 ?
                ", " + getTranslation(APPLICATION_SECTION, "HP Bar Index") + ": " + (combatant.getCurrentHpBarIndex() + 1) :
                "";

        final Label infoLabel = new Label(
                printBasicCombatantData(combatant.getCombatantData()) + ", " +
                        getTranslation(APPLICATION_SECTION, "HP Bar") + ": " + combatant.getHpBars() + hpBarIndex
        );

        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(600);
        final Label traitLabel = new Label(
                getTranslation(APPLICATION_SECTION, "Traits") + ": " +
                        combatant.getAllTraits(simulation).stream()
                                .map(trait -> getTranslation(TRAIT_SECTION, trait))
                                .collect(Collectors.toList())
        );
        traitLabel.setWrapText(true);
        traitLabel.setMaxWidth(600);
        showBuffsVBox.getChildren().addAll(closeButton, imgAnchor, infoLabel, traitLabel);

        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        final GridPane buffGrid = new GridPane();
        buffGrid.setPadding(new Insets(10));
        buffGrid.setHgap(5);
        buffGrid.setVgap(5);
        for (int i = 0; i < 3; i++) {
            final ColumnConstraints constraints = new ColumnConstraints();
            buffGrid.getColumnConstraints().add(constraints);
        }
        buffGrid.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
        buffGrid.getColumnConstraints().get(1).setMinWidth(300);
        buffGrid.getColumnConstraints().get(2).setHalignment(HPos.RIGHT);
        scrollPane.setContent(buffGrid);

        final List<Buff> buffs = combatant.getBuffs();
        for (int i = 0; i < buffs.size(); i += 1) {
            final Buff buff = buffs.get(i);
            final ImageView buffIcon = new ImageView(getBuffImage(buff.getIconName()));
            buffIcon.setFitWidth(BUFF_SIZE);
            buffIcon.setFitHeight(BUFF_SIZE);
            final AnchorPane buffImgAnchor = wrapInAnchor(buffIcon);
            if (buff.isIrremovable()) {
                buffImgAnchor.setStyle(PERMANENT_BUFF_STYLE);
            }
            buffGrid.add(buffImgAnchor, 0, i);
            final Label buffLabel = new Label(buff.toString());
            buffLabel.setWrapText(true);
            buffLabel.setMaxWidth(550);
            buffGrid.add(buffLabel, 1, i);
            final Label durationLabel = new Label(buff.durationString());
            buffGrid.add(durationLabel, 2, i);
            final RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setValignment(VPos.CENTER);
            buffGrid.getRowConstraints().add(rowConstraints);
        }

        showBuffsVBox.getChildren().add(scrollPane);
        contentVBox.setDisable(true);
    }

    public static AnchorPane createServantImage(final Image image) {
        final ImageView servantThumbnail = new ImageView();
        servantThumbnail.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        servantThumbnail.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        servantThumbnail.setImage(image);
        final AnchorPane imgAnchor = wrapInAnchor(servantThumbnail);
        imgAnchor.setStyle(UNIT_THUMBNAIL_STYLE);
        return imgAnchor;
    }

    public void executeCombatActions() {
        combatActionsVBox.setVisible(true);
        combatActionsVBox.getChildren().clear();
        final VBox servantVBox = new VBox(10);
        servantVBox.setAlignment(Pos.TOP_CENTER);
        servantVBox.getChildren().add(new Label(getTranslation(APPLICATION_SECTION, "Servants")));
        final VBox commandCardsVBox = new VBox(10);
        commandCardsVBox.setAlignment(Pos.TOP_CENTER);
        commandCardsVBox.getChildren().add(new Label(getTranslation(APPLICATION_SECTION, "Command Card")));
        final VBox npCardsVBox = new VBox(10);
        npCardsVBox.setAlignment(Pos.TOP_CENTER);
        npCardsVBox.getChildren().add(new Label(getTranslation(APPLICATION_SECTION, "NP Card")));

        final List<CombatActionDisplay> combatActionDisplays = new ArrayList<>();
        for (int i = 0; i < simulation.getCurrentServants().size(); i += 1) {
            final Servant servant = simulation.getCurrentServants().get(i);
            if (servant == null) {
                continue;
            }
            servantVBox.getChildren().add(createServantImage(getServantImage(servant.getId(), servant.getAscension())));
            final HBox servantCardHBox = new HBox(10);
            servantCardHBox.setAlignment(Pos.TOP_CENTER);
            for (int j = 0; j < 5; j += 1) {
                final CommandCard commandCard = servant.getCommandCard(j);
                final CombatActionDisplay combatActionDisplay = new CombatActionDisplay(
                        i,
                        j,
                        false,
                        commandCard,
                        combatActionDisplays,
                        this
                );
                servantCardHBox.getChildren().add(combatActionDisplay);
            }
            commandCardsVBox.getChildren().add(servantCardHBox);

            final NoblePhantasm noblePhantasm = servant.getNoblePhantasm();
            final CombatActionDisplay npDisplay = new CombatActionDisplay(
                    i,
                    6,
                    true,
                    noblePhantasm,
                    combatActionDisplays,
                    this
            );
            npCardsVBox.getChildren().add(npDisplay);
        }

        final Button cancelButton = new Button(getTranslation(APPLICATION_SECTION, "Cancel"));
        cancelButton.setOnAction(e -> {
            combatActionsVBox.setVisible(false);
            contentVBox.setDisable(false);
        });
        final Button executeButton = new Button(getTranslation(APPLICATION_SECTION, "Execute"));
        executeButton.setOnAction(e -> {
            if (combatActionDisplays.stream().noneMatch(Objects::nonNull)) {
                return;
            }

            final List<CombatAction> combatActions = combatActionDisplays.stream()
                    .filter(Objects::nonNull)
                    .map(CombatActionDisplay::buildAction)
                    .collect(Collectors.toList());

            combatActionsVBox.setVisible(false);
            contentVBox.setDisable(false);
            simulation.executeCombatActions(combatActions);
            render();
        });

        final HBox cardsHBox = new HBox(15);
        cardsHBox.setAlignment(Pos.CENTER);
        cardsHBox.getChildren().addAll(
                servantVBox,
                commandCardsVBox,
                npCardsVBox
        );

        final HBox buttonHBox = new HBox(10);
        buttonHBox.setAlignment(Pos.CENTER);
        buttonHBox.getChildren().addAll(cancelButton, executeButton);

        combatActionsVBox.getChildren().addAll(cardsHBox, buttonHBox);
        contentVBox.setDisable(true);
    }

    public void showClassInfo(final FateClass fateClass) {
        fateClassInfoVBox.renderClass(fateClass, this);
        fateClassInfoVBox.setVisible(true);
        contentVBox.setDisable(true);
    }
}
