package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
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
import yome.fgo.data.proto.FgoStorageData.Gender;
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
import static yome.fgo.simulator.ResourceManager.getCardImageFile;
import static yome.fgo.simulator.ResourceManager.getEnemyThumbnail;
import static yome.fgo.simulator.ResourceManager.getMCImage;
import static yome.fgo.simulator.ResourceManager.getServantThumbnail;
import static yome.fgo.simulator.ResourceManager.getSkillIcon;
import static yome.fgo.simulator.gui.components.DataPrinter.printEffectData;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.BUFF_ICON_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.CARD_IMAGE_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.COMMAND_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.MYSTIC_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;
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
    private final VBox specialSelectionVBox;
    private final VBox showBuffsVBox;
    private final HBox combatActionsHBox;

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
        stackPane.setAlignment(Pos.CENTER);

        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);
        stackPane.getChildren().addAll(scrollPane);

        root = stackPane;

        final VBox contentVBox = new VBox();
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

        contentVBox.getChildren().add(statsLogger);

        enemyToggle = new ToggleGroup();

        simulation.setStatsLogger(statsLogger);

        specialSelectionVBox = new VBox();
        specialSelectionVBox.setVisible(false);
        specialSelectionVBox.setSpacing(10);
        specialSelectionVBox.setPadding(new Insets(10));
        specialSelectionVBox.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 1; -fx-background-color: white");
        specialSelectionVBox.setAlignment(Pos.TOP_CENTER);
        specialSelectionVBox.setFocusTraversable(true);
        StackPane.setMargin(specialSelectionVBox, new Insets(300, 200, 300, 200));
        stackPane.getChildren().add(specialSelectionVBox);

        showBuffsVBox = new VBox();
        showBuffsVBox.setVisible(false);
        showBuffsVBox.setSpacing(5);
        showBuffsVBox.setPadding(new Insets(10));
        showBuffsVBox.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 1; -fx-background-color: white");
        showBuffsVBox.setAlignment(Pos.TOP_CENTER);
        showBuffsVBox.setFocusTraversable(true);
        showBuffsVBox.setFillWidth(false);
        StackPane.setMargin(showBuffsVBox, new Insets(100, 300, 100, 300));
        stackPane.getChildren().add(showBuffsVBox);

        combatActionsHBox = new HBox();
        combatActionsHBox.setVisible(false);
        combatActionsHBox.setSpacing(5);
        combatActionsHBox.setPadding(new Insets(10));
        combatActionsHBox.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 1; -fx-background-color: white");
        combatActionsHBox.setAlignment(Pos.TOP_CENTER);
        combatActionsHBox.setFocusTraversable(true);
        StackPane.setMargin(combatActionsHBox, new Insets(300, 200, 300, 200));
        stackPane.getChildren().add(combatActionsHBox);

        simulation.initiate();

        render();
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
        for (int i = 0; i < enemies.size(); i += 1) {
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
        return getImage(path, getCardImageFile(cardString));
    }

    public Image getBuffImage(final String iconName) {
        final String path = String.format("%s/%s.png", BUFF_ICON_DIRECTORY_PATH, iconName);
        return getImage(path, getBuffIcon(iconName));
    }

    public Image getSkillImage(final String iconName) {
        final String path = String.format("%s/%s.png", SKILL_ICON_DIRECTORY_PATH, iconName);
        return getImage(path, getSkillIcon(iconName));
    }

    public Image getMysticCodeImage(final String id, final Gender gender) {
        final String genderString = gender == Gender.MALE ? "male" : "female";
        final String path = String.format("%s/%s_%s.png", MYSTIC_CODES_DIRECTORY_PATH, id, genderString);
        return getImage(path, getMCImage(id, gender));
    }

    public Image getCommandCodeImage(final String id) {
        final String path = String.format("%s/%s/%s.png", COMMAND_CODES_DIRECTORY_PATH, id, id);
        return getImage(path, getCCThumbnail(id));
    }

    public Image getEnemyImage(final String pathToData, final String enemyId) {
        final String path = String.format("%s/%s/%s.png", ENEMY_DIRECTORY_PATH, pathToData, enemyId);
        return getImage(path, getEnemyThumbnail(pathToData, enemyId));
    }

    public Image getServantImage(final String servantId, final int ascension) {
        final String path = String.format("%s/%s/%s_asc%d_thumbnail.png", SERVANT_DIRECTORY_PATH, servantId, servantId, ascension);
        return getImage(path, getServantThumbnail(servantId, ascension));
    }

    private Image getImage(final String path, final File file) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

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
                    if (servant == null) {
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
                cancelButton.setOnAction(e -> specialSelectionVBox.setVisible(false));
                final Button selectButton = new Button(getTranslation(APPLICATION_SECTION, "Select"));
                selectButton.setOnAction(e -> {
                    if (onFieldChoices.stream().noneMatch(OrderChangeChoice::isSelected) ||
                            backupChoices.stream().noneMatch(OrderChangeChoice::isSelected)) {
                        return;
                    }

                    specialSelectionVBox.setVisible(false);

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
                        simulation.setSelectedCommandCardType(commandCardType);
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
                cardTypeCancelButton.setOnAction(e -> specialSelectionVBox.setVisible(false));
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
                        simulation.setSelectedEffectData(effectData);
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
                randomEffectCancelButton.setOnAction(e -> specialSelectionVBox.setVisible(false));
                specialSelectionVBox.getChildren().add(randomEffectCancelButton);
            }
        }
    }

    public void viewEnemyBuffs(final int enemyIndex) {
        showBuffsVBox.setMaxHeight(root.getScene().getHeight() - 100);
        showBuffsVBox.setVisible(true);
        showBuffsVBox.getChildren().clear();

        final Combatant combatant = simulation.getCurrentEnemies().get(enemyIndex);
        final Image svrImg = combatant instanceof Servant ?
                getServantImage(combatant.getId(), ((Servant) combatant).getAscension()) :
                getEnemyImage(combatant.getEnemyData().getEnemyCategories(), combatant.getId());
        populateShowBuffsVBox(svrImg, combatant.getBuffs());
    }

    public void viewServantBuffs(final int servantIndex) {
        showBuffsVBox.setMaxHeight(root.getScene().getHeight() - 100);
        showBuffsVBox.setVisible(true);
        showBuffsVBox.getChildren().clear();

        final Servant servant = simulation.getCurrentServants().get(servantIndex);
        final Image svrImg = getServantImage(servant.getId(), servant.getAscension());
        populateShowBuffsVBox(svrImg, servant.getBuffs());
    }

    private void populateShowBuffsVBox(final Image image, final List<Buff> buffs) {
        final Button closeButton = new Button(getTranslation(APPLICATION_SECTION, "Close"));
        closeButton.setOnAction(e -> showBuffsVBox.setVisible(false));

        final AnchorPane imgAnchor = createServantImage(image);

        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        final GridPane buffGrid = new GridPane();
        buffGrid.setHgap(5);
        buffGrid.setVgap(5);
        for (int i = 0; i < 3; i++) {
            final ColumnConstraints constraints = new ColumnConstraints();
            buffGrid.getColumnConstraints().add(constraints);
        }
        buffGrid.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
        buffGrid.getColumnConstraints().get(1).setMinWidth(200);
        scrollPane.setContent(buffGrid);

        showBuffsVBox.getChildren().addAll(closeButton, imgAnchor);
        for (int i = 0; i < buffs.size(); i += 1) {
            final Buff buff = buffs.get(i);
            final ImageView buffIcon = new ImageView(getBuffImage(buff.getIconName()));
            buffIcon.setFitWidth(20);
            buffIcon.setFitHeight(20);
            buffGrid.add(buffIcon, 0, i);
            final Label buffLabel = new Label(buff.toString());
            buffGrid.add(buffLabel, 1, i);
            final Label durationLabel = new Label(buff.durationString());
            buffGrid.add(durationLabel, 2, i);
            final RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setValignment(VPos.CENTER);
            buffGrid.getRowConstraints().add(rowConstraints);
        }
        showBuffsVBox.getChildren().add(scrollPane);
    }

    public static AnchorPane createServantImage(final Image image) {
        final ImageView servantThumbnail = new ImageView();
        servantThumbnail.setFitHeight(100);
        servantThumbnail.setFitWidth(100);
        servantThumbnail.setImage(image);
        final AnchorPane imgAnchor = new AnchorPane();
        imgAnchor.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        AnchorPane.setBottomAnchor(servantThumbnail, 0.0);
        AnchorPane.setTopAnchor(servantThumbnail, 0.0);
        AnchorPane.setLeftAnchor(servantThumbnail, 0.0);
        AnchorPane.setRightAnchor(servantThumbnail, 0.0);
        imgAnchor.getChildren().add(servantThumbnail);
        return imgAnchor;
    }

    public void executeCombatActions() {
        combatActionsHBox.setVisible(true);
        combatActionsHBox.getChildren().clear();
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
        cancelButton.setOnAction(e -> combatActionsHBox.setVisible(false));
        final Button executeButton = new Button(getTranslation(APPLICATION_SECTION, "Execute"));
        executeButton.setOnAction(e -> {
            if (combatActionDisplays.stream().noneMatch(Objects::nonNull)) {
                return;
            }

            final List<CombatAction> combatActions = combatActionDisplays.stream()
                    .filter(Objects::nonNull)
                    .map(CombatActionDisplay::buildAction)
                    .collect(Collectors.toList());

            combatActionsHBox.setVisible(false);
            simulation.executeCombatActions(combatActions);
            render();
        });

        final HBox buttonHBox = new HBox(10);
        buttonHBox.getChildren().addAll(cancelButton, executeButton);
        commandCardsVBox.getChildren().add(buttonHBox);

        combatActionsHBox.getChildren().addAll(
                servantVBox,
                new Separator(Orientation.VERTICAL),
                commandCardsVBox,
                new Separator(Orientation.VERTICAL),
                npCardsVBox
        );
    }
}
