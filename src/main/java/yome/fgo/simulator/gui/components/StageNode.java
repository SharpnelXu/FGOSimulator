package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.utils.FilePathUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;

public class StageNode extends VBox {
    private int stageNum;
    private TextField maximumEnemiesOnScreenText;
    private TextField stageTraitsText;
    private ListView<EffectData> stageEffectsList;
    private GridPane enemyGrid;
    private List<EnemyNode> enemyNodes; // TODO enemyGrid.getChildren can replace this
    private Label errorLabel;

    public StageNode(final int stageNum) {
        super();
        setSpacing(10);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        this.stageNum = stageNum;

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
        final Label maximumEnemyOnScreenLabel = new Label(getTranslation(APPLICATION_SECTION, "Maximum enemy on screen"));
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
        stagePropertyHBox.getChildren().addAll(maximumEnemyOnScreenLabel, maximumEnemiesOnScreenText, stageTraitLabel, stageTraitTextAnchorPane);

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
        final ListView<EffectData> stageEffectsList = new ListView<>();
        stageEffectsList.setPrefSize(USE_COMPUTED_SIZE, 50);
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
                    stageEffectsList.getItems().add(builder.build());
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

            final int size = enemyGrid.getChildren().size();
            final int rowIndex = size / 3;
            final int colIndex = 2 - size % 3;

            try {
                enemyGrid.add(new EnemyNode(enemyDataFile, false), colIndex, rowIndex);
            } catch (final Exception ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + ex.getMessage());
                errorLabel.setVisible(true);
            }
        });
        final Button addServantButton = new Button(getTranslation(APPLICATION_SECTION, "Add Servant"));
        addServantButton.setOnAction(e -> {
            final int size = enemyGrid.getChildren().size();
            // TODO: add an enemyNode when clicked
            final int rowIndex = size / 3;
            final int colIndex = 2 - size % 3;
            final ImageView imageView = new ImageView(FilePathUtils.SERVANT_DIRECTORY_PATH + "/servant321/servant321_asc1_thumbnail.png");
            enemyGrid.add(imageView, colIndex, rowIndex);
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
}
