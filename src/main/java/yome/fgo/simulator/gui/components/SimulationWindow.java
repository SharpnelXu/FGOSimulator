package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.simulator.gui.components.StatsLogger.LogLevel;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.craftessences.CraftEssence;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.mysticcodes.MysticCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static yome.fgo.simulator.ResourceManager.getBuffIcon;
import static yome.fgo.simulator.ResourceManager.getEnemyThumbnail;
import static yome.fgo.simulator.ResourceManager.getMCImage;
import static yome.fgo.simulator.ResourceManager.getServantThumbnail;
import static yome.fgo.simulator.ResourceManager.getSkillIcon;
import static yome.fgo.simulator.utils.FilePathUtils.BUFF_ICON_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.MYSTIC_CODES_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SKILL_ICON_DIRECTORY_PATH;

public class SimulationWindow {
    private final Parent root;
    private final Simulation simulation;
    private final List<ServantDisplay> servantDisplays;
    private final MiscDisplay miscDisplay;
    private final GridPane enemyGrid;
    private final Map<String, Image> imageCache;
    private final StatsLogger statsLogger;
    private final ToggleGroup enemyToggle;

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
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);

        root = scrollPane;

        final StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.TOP_CENTER);
        scrollPane.setContent(stackPane);

        final VBox contentVBox = new VBox();
        contentVBox.setSpacing(10);
        contentVBox.setPadding(new Insets(10, 10, 10, 10));
        contentVBox.setAlignment(Pos.TOP_CENTER);
        stackPane.getChildren().addAll(contentVBox);

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
        simulation.initiate();

        imageCache = new HashMap<>();

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
        miscDisplay.renderMysticCode();

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

    public Image getMysticCodeImage(final String id, final Gender gender) {
        final String genderString = gender == Gender.MALE ? "male" : "female";
        final String path = String.format("%s/%s_%s.png", MYSTIC_CODES_DIRECTORY_PATH, id, genderString);
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        return getImage(path, getMCImage(id, gender));
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
}
