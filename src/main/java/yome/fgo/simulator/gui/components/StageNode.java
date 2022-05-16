package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class StageNode extends VBox {
    private final TextField maximumEnemiesOnScreenText;
    private final TextField stageTraitsText;
    private final ListView<DataWrapper<EffectData>> stageEffectsList;
    private final GridPane enemyGrid;
    private final Label errorLabel;

    public StageNode(final int stageNum) {
        super();
        setSpacing(10);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        final List<Node> nodes = getChildren();

        final HBox stageLabelHBox = new HBox();
        stageLabelHBox.setAlignment(Pos.CENTER);
        stageLabelHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        stageLabelHBox.setPadding(new Insets(10, 10, 10, 10));
        stageLabelHBox.setStyle("-fx-border-color: grey");
        final Label stageLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum);
        stageLabelHBox.getChildren().add(stageLabel);

        nodes.add(stageLabelHBox);

        final HBox stagePropertyHBox = new HBox();
        stagePropertyHBox.setAlignment(Pos.CENTER_LEFT);
        stagePropertyHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        stagePropertyHBox.setSpacing(10);
        final Label maximumEnemyOnScreenLabel = new Label(getTranslation(
                APPLICATION_SECTION,
                "Maximum enemy on screen"
        ));
        maximumEnemiesOnScreenText = new TextField();
        final Label stageTraitLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage Trait"));
        final AnchorPane stageTraitTextAnchorPane = new AnchorPane();
        HBox.setHgrow(stageTraitTextAnchorPane, Priority.ALWAYS);
        stageTraitsText = new TextField();
        AnchorPane.setTopAnchor(stageTraitsText, 0.0);
        AnchorPane.setBottomAnchor(stageTraitsText, 0.0);
        AnchorPane.setLeftAnchor(stageTraitsText, 0.0);
        AnchorPane.setRightAnchor(stageTraitsText, 0.0);
        stageTraitTextAnchorPane.getChildren().add(stageTraitsText);
        stagePropertyHBox.getChildren()
                .addAll(maximumEnemyOnScreenLabel,
                        maximumEnemiesOnScreenText,
                        stageTraitLabel,
                        stageTraitTextAnchorPane
                );

        nodes.add(stagePropertyHBox);

        final HBox stageEffectsHBox = new HBox();
        stageEffectsHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        stageEffectsHBox.setSpacing(10);
        final Label stageEffectsLabel = new Label(getTranslation(APPLICATION_SECTION, "Stage Effects"));
        final VBox stageEffectListVBox = new VBox();
        HBox.setHgrow(stageEffectListVBox, Priority.ALWAYS);
        stageEffectListVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        stageEffectListVBox.setSpacing(10);
        errorLabel = new Label();
        errorLabel.setVisible(false);
        errorLabel.setStyle("-fx-text-fill: red");
        stageEffectsList = new ListView<>();
        stageEffectsList.setMaxHeight(75);
        stageEffectsList.setCellFactory(new EffectsCellFactory(errorLabel));
        final HBox stageEffectAddHBox = new HBox();
        stageEffectAddHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        stageEffectAddHBox.setAlignment(Pos.CENTER_RIGHT);
        stageEffectAddHBox.setSpacing(10);
        final Button addEffectButton = new Button(getTranslation(APPLICATION_SECTION, "Add Effect"));
        addEffectButton.setOnAction(e -> {
            try {
                final EffectData.Builder builder = EffectData.newBuilder();
                createEffect(addEffectButton.getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    stageEffectsList.getItems().add(new DataWrapper<>(builder.build()));
                }
            } catch (final IOException exception) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + exception);
                errorLabel.setVisible(true);
            }
        });
        stageEffectAddHBox.getChildren().addAll(errorLabel, addEffectButton);
        stageEffectListVBox.getChildren().addAll(stageEffectsList, stageEffectAddHBox);
        stageEffectsHBox.getChildren().addAll(stageEffectsLabel, stageEffectListVBox);

        nodes.add(stageEffectsHBox);

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
                enemyNode = new EnemyNode(enemyDataFile, false);
            } catch (final Exception ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + ex.getMessage());
                errorLabel.setVisible(true);
                return;
            }

            addEnemyNode(enemyNode);
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
                enemyNode = new EnemyNode(enemyDataFile, true);
            } catch (final Exception ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + ex.getMessage());
                errorLabel.setVisible(true);
                return;
            }

            addEnemyNode(enemyNode);
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

    private void addEnemyNode(final EnemyNode node) {
        final int size = enemyGrid.getChildren().size();
        final int rowIndex = size / 3;
        final int colIndex = 2 - size % 3;
        enemyGrid.add(node, colIndex, rowIndex);
    }

    public void loadStageData(final StageData stageData) {
        maximumEnemiesOnScreenText.setText(Integer.toString(stageData.getMaximumEnemiesOnScreen()));

        stageEffectsList.getItems().clear();
        stageEffectsList.getItems().addAll(stageData.getEffectsList().stream().map(DataWrapper::new).collect(Collectors.toList()));
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
                enemyNode = new EnemyNode(enemyDataFile, enemyData.getIsServant());
            } catch (final Exception ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + ex.getMessage());
                errorLabel.setVisible(true);
                return;
            }
            enemyNode.loadEnemyData(enemyData);

            addEnemyNode(enemyNode);
        }
    }

    public StageData buildStageData() {
        final StageData.Builder builder = StageData.newBuilder();

        try {
            builder.setMaximumEnemiesOnScreen(Integer.parseInt(maximumEnemiesOnScreenText.getText()));
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Integer"));
            maximumEnemiesOnScreenText.requestFocus();
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
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Integer"));
            enemyGrid.requestFocus();
            return null;
        }
        builder.addAllEnemyData(enemyData);

        final List<String> traits = Arrays.stream(stageTraitsText.getText().trim().split(COMMA_SPLIT_REGEX))
                .filter(s -> !s.isEmpty())
                .map(TranslationManager::getKeyForTrait)
                .collect(Collectors.toList());

        builder.addAllTraits(traits);
        builder.addAllEffects(stageEffectsList.getItems().stream().map(e -> e.protoData).collect(Collectors.toList()));

        return builder.build();
    }
}
