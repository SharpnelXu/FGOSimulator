package yome.fgo.simulator.gui.components;

import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.ServantData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.gui.components.DataPrinter.printCombatantData;
import static yome.fgo.simulator.gui.creators.EnemyCreator.editCombatantData;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class EnemyNode extends VBox {
    private CombatantData baseEnemyData;
    private ServantData baseServantData;
    private boolean isServant;
    private TextField hpText;
    private CheckBox customGaugeCheckbox;
    private TextField customGaugeText;
    private ChoiceBox<Integer> servantAscensionChoiceBox;
    private String pathToBaseEnemyData;
    private ImageView thumbnail;
    private Label combatantDataLabel;
    private CombatantData combatantDataOverride;


    public EnemyNode() {

    }

    public EnemyNode(final File enemyDataFile, final boolean isServant) throws FileNotFoundException {
        super();
        setSpacing(10);
        setPadding(new Insets(5, 5, 5, 5));
        setAlignment(Pos.TOP_RIGHT);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setStyle("-fx-background-color: #ffffffdd; -fx-border-color: #727272; -fx-border-style: solid; -fx-border-radius: 6");

        final List<Node> nodes = getChildren();

        this.isServant = isServant;

        // convert the absolute path to URI
        final URI path1 = enemyDataFile.getParentFile().toURI();
        final String rootDir = isServant ? SERVANT_DIRECTORY_PATH : ENEMY_DIRECTORY_PATH;
        final URI path2 = new File(rootDir).toURI();

        // create a relative path from the two paths
        final URI relativePath = path2.relativize(path1);

        // convert the URI to string
        this.pathToBaseEnemyData = relativePath.getPath();

        final JsonFormat.Parser parser = JsonFormat.parser();
        if (isServant) {
            final ServantData.Builder servantDataBuilder = ServantData.newBuilder();
            try {
                parser.merge(new FileReader(enemyDataFile), servantDataBuilder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            this.baseServantData = servantDataBuilder.build();
            this.baseEnemyData = this.baseServantData.getServantAscensionData(0).getCombatantData();
        } else {
            final CombatantData.Builder combatantDataBuilder = CombatantData.newBuilder();
            try {
                parser.merge(new FileReader(enemyDataFile), combatantDataBuilder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            this.baseEnemyData = combatantDataBuilder.build();
        }

        if (this.baseEnemyData.getId().isBlank()) {
            throw new RuntimeException("ID is null or empty!");
        }

        final File thumbnailFile;
        if (isServant) {
            thumbnailFile = getServantThumbnailPath(String.format("%s/%s", SERVANT_DIRECTORY_PATH, pathToBaseEnemyData), baseEnemyData.getId(), 1);
        } else {
            thumbnailFile = getEnemyThumbnailPath(String.format("%s/%s", rootDir, pathToBaseEnemyData), baseEnemyData.getId());
        }

        final HBox combatantDataHBox = new HBox();
        combatantDataHBox.setSpacing(10);
        this.thumbnail = new ImageView(new Image(new FileInputStream(thumbnailFile)));
        this.thumbnail.setFitHeight(100);
        this.thumbnail.setFitWidth(100);
        final AnchorPane imgAnchor = new AnchorPane();
        AnchorPane.setTopAnchor(this.thumbnail, 0.0);
        AnchorPane.setBottomAnchor(this.thumbnail, 0.0);
        AnchorPane.setLeftAnchor(this.thumbnail, 0.0);
        AnchorPane.setRightAnchor(this.thumbnail, 0.0);
        imgAnchor.setStyle("-fx-border-color: rgba(73,73,73,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        imgAnchor.getChildren().add(this.thumbnail);
        combatantDataLabel = new Label(printCombatantData(this.baseEnemyData));
        combatantDataLabel.setWrapText(true);
        combatantDataHBox.getChildren().addAll(imgAnchor, combatantDataLabel);

        nodes.add(combatantDataHBox);

        final Button editEnemyButton = new Button(getTranslation(APPLICATION_SECTION, "Edit"));
        editEnemyButton.setOnAction(e -> {
            if (!isServant) {
                final CombatantData.Builder builder = combatantDataOverride == null ?
                        baseEnemyData.toBuilder() :
                        combatantDataOverride.toBuilder();
                try {
                    editCombatantData(editEnemyButton.getScene().getWindow(), builder);
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
                if (!builder.getId().isEmpty()) {
                    combatantDataOverride = builder.build();
                    combatantDataLabel.setText(printCombatantData(combatantDataOverride));
                }
            }
            // TODO: servant edit when servant editor is completed
        });

        nodes.add(editEnemyButton);

        final HBox hpBox = new HBox();
        hpBox.setAlignment(Pos.CENTER_LEFT);
        hpBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        hpBox.setSpacing(10);
        final Label hpLabel = new Label(getTranslation(APPLICATION_SECTION, "HP"));
        final AnchorPane hpTextAnchorPane = new AnchorPane();
        HBox.setHgrow(hpTextAnchorPane, Priority.ALWAYS);
        this.hpText = new TextField();
        AnchorPane.setTopAnchor(this.hpText, 0.0);
        AnchorPane.setBottomAnchor(this.hpText, 0.0);
        AnchorPane.setLeftAnchor(this.hpText, 0.0);
        AnchorPane.setRightAnchor(this.hpText, 0.0);
        hpTextAnchorPane.getChildren().add(this.hpText);
        hpBox.getChildren().addAll(hpLabel, hpTextAnchorPane);

        nodes.add(hpBox);

        final HBox additionalParamsBox = new HBox();
        additionalParamsBox.setAlignment(Pos.CENTER_LEFT);
        additionalParamsBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        additionalParamsBox.setSpacing(10);
        this.customGaugeCheckbox = new CheckBox(getTranslation(APPLICATION_SECTION, "Custom Gauge"));
        this.customGaugeText = new TextField();
        this.customGaugeText.setMaxWidth(50);
        this.customGaugeText.setDisable(true);
        this.customGaugeCheckbox.setOnAction(e -> this.customGaugeText.setDisable(!this.customGaugeCheckbox.isSelected()));
        final Label servantAscLabel = new Label(getTranslation(APPLICATION_SECTION, "Servant Asc"));
        this.servantAscensionChoiceBox = new ChoiceBox<>();
        final List<Integer> ascensions = new ArrayList<>();
        if (isServant) {
            for (int i = 1; i <= this.baseServantData.getServantAscensionDataCount(); i++) {
                ascensions.add(i);
            }
        } else {
            ascensions.add(1);
        }
        this.servantAscensionChoiceBox.setItems(FXCollections.observableArrayList(ascensions));
        this.servantAscensionChoiceBox.setMaxWidth(50);
        this.servantAscensionChoiceBox.setOnAction(e -> {
            final int asc = this.servantAscensionChoiceBox.getValue();
            try {
                this.thumbnail.setImage(new Image(new FileInputStream(getServantThumbnailPath(pathToBaseEnemyData, this.baseEnemyData.getId(), asc))));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex); // should never be hit
            }
            this.baseEnemyData = this.baseServantData.getServantAscensionData(asc - 1).getCombatantData();
            this.combatantDataLabel.setText(printCombatantData(this.baseEnemyData));
        });
        servantAscLabel.setDisable(!isServant);
        this.servantAscensionChoiceBox.setDisable(!isServant);
        additionalParamsBox.getChildren().addAll(
                this.customGaugeCheckbox,
                this.customGaugeText,
                servantAscLabel,
                this.servantAscensionChoiceBox
        );

        nodes.add(additionalParamsBox);
    }

    public static File getEnemyThumbnailPath(final String path, final String id) {
        final File thumbImg = new File(String.format("%s/%s.png", path, id));
        if (thumbImg.exists()) {
            return thumbImg;
        }

        return new File(String.format("%s/defaultEnemy_thumbnail.png", ENEMY_DIRECTORY_PATH));
    }

    public static File getServantThumbnailPath(final String path, final String id, final int ascension) {
        final File ascImg = new File(String.format("%s/%s_asc%d_thumbnail.png", path, id, ascension));
        if (ascImg.exists()) {
            return ascImg;
        }

        final File svrImg = new File(String.format("%s/%s_thumbnail.png", path, id));
        if (svrImg.exists()) {
            return svrImg;
        }

        return new File(String.format("%s/defaultServant_thumbnail.png", SERVANT_DIRECTORY_PATH));
    }
}
