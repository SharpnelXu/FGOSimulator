package yome.fgo.simulator.gui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.addSplitTraitListener;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class StageNode extends VBox {
    private final TextField maximumEnemiesOnScreenText;
    private final TextField stageTraitsText;
    private final GridPane enemyGrid;
    private final Label errorLabel;
    private final ListContainerVBox stageEffects;
    private final Label stageLabel;
    private int stageNum;

    public StageNode(final int stageNum, final Label errorLabel, final VBox stageVBox) {
        super(10);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setPadding(new Insets(10));
        setStyle(SPECIAL_INFO_BOX_STYLE);

        this.stageNum = stageNum;
        this.errorLabel = errorLabel;

        final List<Node> nodes = getChildren();

        enemyGrid = new GridPane();
        enemyGrid.setHgap(5);
        enemyGrid.setVgap(5);
        for (int i = 0; i < 3; i++) {
            final ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(33.33);
            enemyGrid.getColumnConstraints().add(constraints);
        }
        enemyGrid.getRowConstraints().add(new RowConstraints());

        final HBox stageLabelHBox = new HBox(5);
        stageLabelHBox.setAlignment(Pos.CENTER);
        stageLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        stageLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum);
        stageLabelHBox.getChildren().add(stageLabel);

        final Button addEnemyButton = new Button();
        addEnemyButton.setGraphic(createInfoImageView("addEnemy"));
        addEnemyButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add Enemy")));
        addEnemyButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(ENEMY_DIRECTORY_PATH));
            fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load Enemy Data"));
            final File enemyDataFile = fileChooser.showOpenDialog(null);
            if (enemyDataFile == null) {
                return;
            }

            final EnemyNode enemyNode;
            try {
                enemyNode = new EnemyNode(enemyDataFile, false, errorLabel, enemyGrid, stageNum);
            } catch (final Exception ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + ex.getMessage());
                errorLabel.setVisible(true);
                return;
            }

            addEnemyNode(enemyGrid, enemyNode);
        });

        final Button addServantButton = new Button();
        addServantButton.setGraphic(createInfoImageView("addServant"));
        addServantButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add Servant")));
        addServantButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(SERVANT_DIRECTORY_PATH));
            fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load Enemy Data"));
            final File enemyDataFile = fileChooser.showOpenDialog(null);
            if (enemyDataFile == null) {
                return;
            }

            final EnemyNode enemyNode;
            try {
                enemyNode = new EnemyNode(enemyDataFile, true, errorLabel, enemyGrid, stageNum);
            } catch (final Exception ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + ex.getMessage());
                errorLabel.setVisible(true);
                return;
            }

            addEnemyNode(enemyGrid, enemyNode);
        });

        final Button upButton = new Button();
        upButton.setGraphic(createInfoImageView("up"));
        upButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move stage up")));
        upButton.setOnAction(e -> {
            final ObservableList<Node> items = stageVBox.getChildren();
            if (items.isEmpty() || items.size() == 1) {
                return;
            }

            final int index = items.indexOf(this);
            if (index > 0) {
                final ObservableList<Node> workingCollection = FXCollections.observableArrayList(items);
                Collections.swap(workingCollection, index - 1, index);
                ((StageNode) workingCollection.get(index - 1)).setStageNum(index);
                ((StageNode) workingCollection.get(index)).setStageNum(index + 1);
                items.setAll(workingCollection);
            }
        });

        final Button downButton = new Button();
        downButton.setGraphic(createInfoImageView("down"));
        downButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move stage down")));
        downButton.setOnAction(e -> {
            final ObservableList<Node> items = stageVBox.getChildren();
            if (items.isEmpty() || items.size() == 1) {
                return;
            }

            final int index = items.indexOf(this);
            if (index < items.size() - 1 && index >= 0) {
                final ObservableList<Node> workingCollection = FXCollections.observableArrayList(items);
                Collections.swap(workingCollection, index + 1, index);
                ((StageNode) workingCollection.get(index + 1)).setStageNum(index + 2);
                ((StageNode) workingCollection.get(index)).setStageNum(index + 1);
                items.setAll(workingCollection);
            }
        });

        final Button removeButton = new Button();
        removeButton.setGraphic(createInfoImageView("remove"));
        removeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Remove stage")));
        removeButton.setOnAction(e -> {
            final ObservableList<Node> items = stageVBox.getChildren();
            final List<Node> remainingNodes = new ArrayList<>(items);
            remainingNodes.remove(this);
            for (int i = 0; i < remainingNodes.size(); i += 1) {
                final StageNode stageNode = (StageNode) remainingNodes.get(i);
                stageNode.setStageNum(i + 1);
            }
            items.setAll(remainingNodes);
        });

        final Label maximumEnemyOnScreenLabel = new Label(getTranslation(
                APPLICATION_SECTION,
                "Maximum enemy on screen"
        ));
        maximumEnemiesOnScreenText = new TextField();
        final Label stageTraitLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage Trait"));
        stageTraitsText = new TextField();
        addSplitTraitListener(stageTraitsText, errorLabel);
        final AnchorPane stageTraitTextAnchorPane = wrapInAnchor(stageTraitsText);
        HBox.setHgrow(stageTraitTextAnchorPane, Priority.ALWAYS);
        stageLabelHBox.getChildren()
                .addAll(
                        addEnemyButton,
                        addServantButton,
                        upButton,
                        downButton,
                        removeButton,
                        maximumEnemyOnScreenLabel,
                        maximumEnemiesOnScreenText,
                        stageTraitLabel,
                        stageTraitTextAnchorPane
                );

        stageEffects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Stage Effects"), errorLabel);

        nodes.add(stageLabelHBox);
        nodes.add(enemyGrid);
        nodes.add(new Separator());
        nodes.add(stageEffects);
    }

    private void setStageNum(final int stageNum) {
        this.stageNum = stageNum;
        stageLabel.setText(getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum);
        for (final Node node : enemyGrid.getChildren()) {
            final EnemyNode enemyNode = (EnemyNode) node;
            enemyNode.setStageNum(stageNum);
        }
    }

    public static void addEnemyNode(final GridPane enemyGrid, final EnemyNode node) {
        final int size = enemyGrid.getChildren().size();
        final int rowIndex = size / 3;
        final int colIndex = 2 - size % 3;
        enemyGrid.add(node, colIndex, rowIndex);
        node.setEnemyIndex(size);
    }

    public void loadStageData(final StageData stageData) {
        maximumEnemiesOnScreenText.setText(Integer.toString(stageData.getMaximumEnemiesOnScreen()));

        stageEffects.clear();
        stageEffects.loadEffect(stageData.getEffectsList());

        stageTraitsText.setText(
                stageData.getTraitsList()
                        .stream()
                        .map(s -> getTranslation(TRAIT_SECTION, s))
                        .collect(Collectors.joining(", "))
        );

        for (final EnemyData enemyData : stageData.getEnemyDataList()) {
            final String rootDir = enemyData.getIsServant() ? SERVANT_DIRECTORY_PATH : ENEMY_DIRECTORY_PATH;
            final String filePath = String.format("%s/%s/%s.json", rootDir, enemyData.getEnemyCategories(), enemyData.getEnemyBaseId());
            final File enemyDataFile = new File(filePath);
            if (!enemyDataFile.exists()) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Enemy Data DNE") + filePath);
                errorLabel.setVisible(true);
                continue;
            }

            final EnemyNode enemyNode;
            try {
                enemyNode = new EnemyNode(enemyDataFile, enemyData.getIsServant(), errorLabel, enemyGrid, stageNum);
            } catch (final Exception ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + ex.getMessage());
                errorLabel.setVisible(true);
                return;
            }
            enemyNode.loadEnemyData(enemyData);

            addEnemyNode(enemyGrid, enemyNode);
        }
    }

    public StageData buildStageData() {
        final StageData.Builder builder = StageData.newBuilder();

        try {
            builder.setMaximumEnemiesOnScreen(Integer.parseInt(maximumEnemiesOnScreenText.getText()));
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(
                    getTranslation(APPLICATION_SECTION, "Value not Integer") + ": "
                            + getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum + " "
                            + getTranslation(APPLICATION_SECTION, "Maximum enemy on screen")
            );
            return null;
        }

        final List<EnemyData> enemyData = new ArrayList<>();
        for (final Node node : enemyGrid.getChildren()) {
            final EnemyNode enemyNode = (EnemyNode) node;

            final EnemyData builtData = enemyNode.buildEnemyData();
            if (builtData == null) {
                return null;
            }
            enemyData.add(builtData);
        }
        if (enemyData.isEmpty()) {
            errorLabel.setVisible(true);
            errorLabel.setText(
                    getTranslation(APPLICATION_SECTION, "No added enemies") + ": "
                            + getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum + " "
                            + getTranslation(APPLICATION_SECTION, "Maximum enemy on screen")
            );
            return null;
        }
        builder.addAllEnemyData(enemyData);

        final List<String> traits = Arrays.stream(stageTraitsText.getText().trim().split(COMMA_SPLIT_REGEX))
                .filter(s -> !s.isEmpty())
                .map(TranslationManager::getKeyForTrait)
                .collect(Collectors.toList());

        builder.addAllTraits(traits);
        builder.addAllEffects(stageEffects.buildEffect());

        return builder.build();
    }
}
