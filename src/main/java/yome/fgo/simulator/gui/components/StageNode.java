package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.gui.helpers.ComponentMaker.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
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
    private int stageNum;

    public StageNode(final int stageNum, final Label errorLabel) {
        super(10);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setPadding(new Insets(10));
        setStyle(SPECIAL_INFO_BOX_STYLE);

        this.stageNum = stageNum;
        this.errorLabel = errorLabel;

        final List<Node> nodes = getChildren();

        final HBox stageLabelHBox = new HBox(10);
        stageLabelHBox.setAlignment(Pos.CENTER);
        stageLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label stageLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum);
        stageLabelHBox.getChildren().add(stageLabel);

        final Label maximumEnemyOnScreenLabel = new Label(getTranslation(
                APPLICATION_SECTION,
                "Maximum enemy on screen"
        ));
        maximumEnemiesOnScreenText = new TextField();
        final Label stageTraitLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage Trait"));
        stageTraitsText = new TextField();
        final AnchorPane stageTraitTextAnchorPane = wrapInAnchor(stageTraitsText);
        HBox.setHgrow(stageTraitTextAnchorPane, Priority.ALWAYS);
        stageLabelHBox.getChildren()
                .addAll(maximumEnemyOnScreenLabel,
                        maximumEnemiesOnScreenText,
                        stageTraitLabel,
                        stageTraitTextAnchorPane
                );

        nodes.add(stageLabelHBox);

        stageEffects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Stage Effects"), errorLabel);

        nodes.add(stageEffects);

        enemyGrid = new GridPane();
        enemyGrid.setHgap(5);
        enemyGrid.setVgap(5);
        for (int i = 0; i < 3; i++) {
            final ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(33.33);
            enemyGrid.getColumnConstraints().add(constraints);
        }
        enemyGrid.getRowConstraints().add(new RowConstraints());

        nodes.add(enemyGrid);

        final HBox gridButtonsHBox = new HBox();
        gridButtonsHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        gridButtonsHBox.setSpacing(10);
        gridButtonsHBox.setAlignment(Pos.TOP_RIGHT);
        final Button addEnemyButton = new Button(getTranslation(APPLICATION_SECTION, "Add Enemy"));
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
        final Button addServantButton = new Button(getTranslation(APPLICATION_SECTION, "Add Servant"));
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
        final Button removeCombatantButton = new Button(getTranslation(APPLICATION_SECTION, "Remove Combatant"));
        removeCombatantButton.setOnAction(e -> {
            final int count = enemyGrid.getChildren().size();
            if (count > 0) {
                enemyGrid.getChildren().remove(count - 1);
            }
        });

        gridButtonsHBox.getChildren().addAll(removeCombatantButton, addServantButton, addEnemyButton);

        nodes.add(gridButtonsHBox);
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
        stageEffects.load(stageData.getEffectsList());

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
            return null;
        }
        builder.addAllEnemyData(enemyData);

        final List<String> traits = Arrays.stream(stageTraitsText.getText().trim().split(COMMA_SPLIT_REGEX))
                .filter(s -> !s.isEmpty())
                .map(TranslationManager::getKeyForTrait)
                .collect(Collectors.toList());

        builder.addAllTraits(traits);
        builder.addAllEffects(stageEffects.build());

        return builder.build();
    }
}
