package yome.fgo.simulator.gui.components;

import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.ServantData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.ResourceManager.getEnemyThumbnail;
import static yome.fgo.simulator.ResourceManager.getServantThumbnail;
import static yome.fgo.simulator.ResourceManager.readFile;
import static yome.fgo.simulator.gui.helpers.DataPrinter.printCombatantData;
import static yome.fgo.simulator.gui.components.StageNode.addEnemyNode;
import static yome.fgo.simulator.gui.creators.EnemyCreator.editCombatantData;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class EnemyNode extends VBox {
    private CombatantData baseEnemyData;
    private ServantData baseServantData;
    private CombatantData combatantDataOverride;
    private final boolean isServant;
    private final TextField hpText;
    private final CheckBox customGaugeCheckbox;
    private final TextField customGaugeText;
    private final ChoiceBox<Integer> servantAscensionChoiceBox;
    private final String pathToBaseEnemyData;
    private final ImageView thumbnail;
    private final Label combatantDataLabel;
    private final Label errorLabel;
    private int enemyIndex;
    private int stageNum;

    public EnemyNode(
            final File enemyDataFile,
            final boolean isServant,
            final Label errorLabel,
            final GridPane enemyGrid,
            final int stageNum
    ) throws FileNotFoundException {
        super(10);
        setPadding(new Insets(5));
        setAlignment(Pos.TOP_RIGHT);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setStyle("-fx-background-color: #ffffffdd; -fx-border-color: #727272; -fx-border-style: solid; -fx-border-radius: 6");

        this.errorLabel = errorLabel;
        this.stageNum = stageNum;

        final List<Node> nodes = getChildren();

        this.isServant = isServant;

        // convert the absolute path to URI
        final URI path1 = enemyDataFile.getParentFile().toURI();
        final String rootDir = isServant ? SERVANT_DIRECTORY_PATH : ENEMY_DIRECTORY_PATH;
        final URI path2 = new File(rootDir).toURI();

        // create a relative path from the two paths
        final URI relativePath = path2.relativize(path1);

        // convert the URI to string
        pathToBaseEnemyData = relativePath.getPath();

        final JsonFormat.Parser parser = JsonFormat.parser();
        if (isServant) {
            final ServantData.Builder servantDataBuilder = ServantData.newBuilder();
            try {
                parser.merge(readFile(enemyDataFile), servantDataBuilder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            baseServantData = servantDataBuilder.build();
            baseEnemyData = baseServantData.getServantAscensionData(0).getCombatantData();
        } else {
            final CombatantData.Builder combatantDataBuilder = CombatantData.newBuilder();
            try {
                parser.merge(readFile(enemyDataFile), combatantDataBuilder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            baseEnemyData = combatantDataBuilder.build();
        }

        if (baseEnemyData.getId().isBlank()) {
            throw new RuntimeException("ID is null or empty!");
        }

        combatantDataOverride = baseEnemyData;

        final File thumbnailFile;
        if (isServant) {
            thumbnailFile = getServantThumbnail(baseEnemyData.getId(), 1);
        } else {
            thumbnailFile = getEnemyThumbnail(pathToBaseEnemyData, baseEnemyData.getId());
        }

        final HBox combatantDataHBox = new HBox(10);
        combatantDataHBox.setFillHeight(false);
        thumbnail = new ImageView(new Image(new FileInputStream(thumbnailFile)));
        thumbnail.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        thumbnail.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        final AnchorPane imgAnchor = wrapInAnchor(thumbnail);
        imgAnchor.setStyle(UNIT_THUMBNAIL_STYLE);
        combatantDataLabel = new Label(printCombatantData(baseEnemyData));
        combatantDataLabel.setWrapText(true);
        combatantDataHBox.getChildren().addAll(imgAnchor, combatantDataLabel);

        nodes.add(combatantDataHBox);

        final HBox hpBox = new HBox(5);
        hpBox.setAlignment(Pos.CENTER_LEFT);
        hpBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        final Button leftButton = new Button();
        leftButton.setGraphic(createInfoImageView("left"));
        leftButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move enemy left")));
        leftButton.setOnAction(e -> {
            final ObservableList<Node> items = enemyGrid.getChildren();
            if (items.isEmpty() || items.size() == 1) {
                return;
            }

            final int index = items.indexOf(this);
            if (index < items.size() - 1 && index >= 0 ) {
                final ObservableList<Node> workingCollection = FXCollections.observableArrayList(items);
                Collections.swap(workingCollection, index + 1, index);
                items.clear();
                for (final Node node : workingCollection) {
                    addEnemyNode(enemyGrid, (EnemyNode) node);
                }
            }
        });

        final Button rightButton = new Button();
        rightButton.setGraphic(createInfoImageView("right"));
        rightButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move enemy right")));
        rightButton.setOnAction(e -> {
            final ObservableList<Node> items = enemyGrid.getChildren();
            if (items.isEmpty() || items.size() == 1) {
                return;
            }

            final int index = items.indexOf(this);
            if (index > 0) {
                final ObservableList<Node> workingCollection = FXCollections.observableArrayList(items);
                Collections.swap(workingCollection, index - 1, index);
                items.clear();
                for (final Node node : workingCollection) {
                    addEnemyNode(enemyGrid, (EnemyNode) node);
                }
            }
        });

        final Button editEnemyButton = new Button();
        editEnemyButton.setGraphic(createInfoImageView("edit"));
        editEnemyButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Edit enemy data")));
        editEnemyButton.setOnAction(e -> {
            final CombatantData.Builder builder = combatantDataOverride.toBuilder();
            try {
                editCombatantData(editEnemyButton.getScene().getWindow(), builder);
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
            if (!builder.getId().isEmpty()) {
                combatantDataOverride = builder.build();
                combatantDataLabel.setText(printCombatantData(combatantDataOverride));
            }
        });

        final Button removeButton = new Button();
        removeButton.setGraphic(createInfoImageView("remove"));
        removeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Remove enemy")));
        removeButton.setOnAction(e -> {
            final ObservableList<Node> items = enemyGrid.getChildren();
            final List<Node> remainingNodes = new ArrayList<>(items);
            remainingNodes.remove(this);
            items.clear();
            for (final Node node : remainingNodes) {
                addEnemyNode(enemyGrid, (EnemyNode) node);
            }
        });

        final Label hpLabel = new Label(getTranslation(APPLICATION_SECTION, "HP"));
        hpText = new TextField();
        final AnchorPane hpTextAnchorPane = wrapInAnchor(hpText);
        HBox.setHgrow(hpTextAnchorPane, Priority.ALWAYS);

        hpBox.getChildren().addAll(leftButton, rightButton, editEnemyButton, removeButton, hpLabel, hpTextAnchorPane);
        nodes.add(hpBox);

        final HBox additionalParamsBox = new HBox(10);
        additionalParamsBox.setAlignment(Pos.CENTER_LEFT);
        additionalParamsBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        customGaugeCheckbox = new CheckBox(getTranslation(APPLICATION_SECTION, "Custom Gauge"));
        customGaugeText = new TextField();
        customGaugeText.setMaxWidth(50);
        customGaugeText.setDisable(true);
        customGaugeCheckbox.setOnAction(e -> customGaugeText.setDisable(!customGaugeCheckbox.isSelected()));
        final Label servantAscLabel = new Label(getTranslation(APPLICATION_SECTION, "Servant Asc"));
        servantAscensionChoiceBox = new ChoiceBox<>();
        final List<Integer> ascensions = new ArrayList<>();
        if (isServant) {
            for (int i = 1; i <= baseServantData.getServantAscensionDataCount(); i++) {
                ascensions.add(i);
            }
        } else {
            ascensions.add(1);
        }
        servantAscensionChoiceBox.setItems(FXCollections.observableArrayList(ascensions));
        servantAscensionChoiceBox.getSelectionModel().selectFirst();
        servantAscensionChoiceBox.setMaxWidth(50);
        servantAscensionChoiceBox.setOnAction(e -> changeServantAscension(servantAscensionChoiceBox.getValue()));
        servantAscLabel.setDisable(!isServant);
        servantAscensionChoiceBox.setDisable(!isServant);

        additionalParamsBox.getChildren().addAll(
                customGaugeCheckbox,
                customGaugeText,
                servantAscLabel,
                servantAscensionChoiceBox
        );

        nodes.add(additionalParamsBox);
    }

    private void changeServantAscension(final int asc) {
        try {
            final File thumbnailFile = getServantThumbnail(baseEnemyData.getId(), asc);
            thumbnail.setImage(new Image(new FileInputStream(thumbnailFile)));
        } catch (final FileNotFoundException ignored) {
        }
        baseEnemyData = baseServantData.getServantAscensionData(asc - 1).getCombatantData();
        combatantDataOverride = baseEnemyData;
        combatantDataLabel.setText(printCombatantData(baseEnemyData));
    }

    public void loadEnemyData(final EnemyData enemyData) {
        final String hps = enemyData.getHpBarsList()
                .stream()
                .map(hp -> Integer.toString(hp))
                .collect(Collectors.joining(", "));
        hpText.setText(hps);
        if (enemyData.getIsServant()) {
            servantAscensionChoiceBox.getSelectionModel().select(Integer.valueOf(enemyData.getServantAscension()));
            changeServantAscension(enemyData.getServantAscension());
        }

        if (enemyData.hasCombatantDataOverride()) {
            combatantDataOverride = enemyData.getCombatantDataOverride();
            combatantDataLabel.setText(printCombatantData(combatantDataOverride));
        }

        if (enemyData.getHasCustomMaxNpGauge()) {
            customGaugeCheckbox.setSelected(true);
            customGaugeCheckbox.fireEvent(new ActionEvent());
            customGaugeText.setText(Integer.toString(enemyData.getCustomMaxNpGauge()));
        }
    }

    public EnemyData buildEnemyData() {
        final EnemyData.Builder builder = EnemyData.newBuilder();

        builder.setEnemyBaseId(baseEnemyData.getId());
        builder.setEnemyCategories(pathToBaseEnemyData);

        if (hpText.getText().isBlank()) {
            errorLabel.setVisible(true);
            errorLabel.setText(
                    getTranslation(APPLICATION_SECTION, "HP is empty") + ": "
                            + getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum + " "
                            + getTranslation(APPLICATION_SECTION, "Enemy") + " " + (enemyIndex + 1)
            );
            return null;
        }

        for (final String hpString : hpText.getText().trim().split(COMMA_SPLIT_REGEX)) {
            try {
                builder.addHpBars(Integer.parseInt(hpString));
            } catch (final Exception e) {
                errorLabel.setVisible(true);
                errorLabel.setText(
                        getTranslation(APPLICATION_SECTION, "HP is not integer") + ": "
                                + getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum + " "
                                + getTranslation(APPLICATION_SECTION, "Enemy") + " " + (enemyIndex + 1)
                );
                return null;
            }
        }

        builder.setCombatantDataOverride(combatantDataOverride);

        if (customGaugeCheckbox.isSelected()) {
            builder.setHasCustomMaxNpGauge(true);
            try {
                builder.setCustomMaxNpGauge(Integer.parseInt(customGaugeText.getText()));
            } catch (final Exception e) {
                errorLabel.setVisible(true);
                errorLabel.setText(
                        getTranslation(APPLICATION_SECTION, "Custom gauge not integer") + ": "
                                + getTranslation(APPLICATION_SECTION, "Stage") + " " + stageNum + " "
                                + getTranslation(APPLICATION_SECTION, "Enemy") + " " + (enemyIndex + 1)
                );
                return null;
            }
        }

        if (isServant) {
            builder.setIsServant(true);
            builder.setServantAscension(servantAscensionChoiceBox.getValue());
        }

        return builder.build();
    }

    public void setEnemyIndex(final int size) {
        enemyIndex = size;
    }

    public void setStageNum(final int stageNum) {
        this.stageNum = stageNum;
    }
}
