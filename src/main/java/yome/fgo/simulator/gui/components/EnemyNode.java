package yome.fgo.simulator.gui.components;

import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.ServantData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.ResourceManager.getEnemyThumbnail;
import static yome.fgo.simulator.ResourceManager.getServantThumbnail;
import static yome.fgo.simulator.gui.components.DataPrinter.printCombatantData;
import static yome.fgo.simulator.gui.creators.EnemyCreator.editCombatantData;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.models.combatants.Combatant.mergeWithOverride;
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
        pathToBaseEnemyData = relativePath.getPath();

        final JsonFormat.Parser parser = JsonFormat.parser();
        if (isServant) {
            final ServantData.Builder servantDataBuilder = ServantData.newBuilder();
            try {
                parser.merge(new FileReader(enemyDataFile), servantDataBuilder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            baseServantData = servantDataBuilder.build();
            baseEnemyData = baseServantData.getServantAscensionData(0).getCombatantData();
        } else {
            final CombatantData.Builder combatantDataBuilder = CombatantData.newBuilder();
            try {
                parser.merge(new FileReader(enemyDataFile), combatantDataBuilder);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            baseEnemyData = combatantDataBuilder.build();
        }

        if (baseEnemyData.getId().isBlank()) {
            throw new RuntimeException("ID is null or empty!");
        }

        final File thumbnailFile;
        if (isServant) {
            thumbnailFile = getServantThumbnail(baseEnemyData.getId(), 1);
        } else {
            thumbnailFile = getEnemyThumbnail(pathToBaseEnemyData, baseEnemyData.getId());
        }

        final HBox combatantDataHBox = new HBox();
        combatantDataHBox.setSpacing(10);
        combatantDataHBox.setFillHeight(false);
        thumbnail = new ImageView(new Image(new FileInputStream(thumbnailFile)));
        thumbnail.setFitHeight(100);
        thumbnail.setFitWidth(100);
        final AnchorPane imgAnchor = new AnchorPane();
        AnchorPane.setTopAnchor(thumbnail, 0.0);
        AnchorPane.setBottomAnchor(thumbnail, 0.0);
        AnchorPane.setLeftAnchor(thumbnail, 0.0);
        AnchorPane.setRightAnchor(thumbnail, 0.0);
        imgAnchor.setStyle("-fx-border-color: rgba(73,73,73,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        imgAnchor.getChildren().add(thumbnail);
        combatantDataLabel = new Label(printCombatantData(baseEnemyData));
        combatantDataLabel.setWrapText(true);
        combatantDataHBox.getChildren().addAll(imgAnchor, combatantDataLabel);

        nodes.add(combatantDataHBox);

        final HBox buttonHBox = new HBox();
        buttonHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        buttonHBox.setAlignment(Pos.CENTER_RIGHT);
        buttonHBox.setSpacing(10);
        final Button editEnemyButton = new Button(getTranslation(APPLICATION_SECTION, "Edit"));
        editEnemyButton.setOnAction(e -> {
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
        });
        errorLabel = new Label();
        errorLabel.setVisible(false);
        errorLabel.setStyle("-fx-text-fill: red");
        buttonHBox.getChildren().addAll(errorLabel, editEnemyButton);

        nodes.add(buttonHBox);

        final HBox hpBox = new HBox();
        hpBox.setAlignment(Pos.CENTER_LEFT);
        hpBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        hpBox.setSpacing(10);
        final Label hpLabel = new Label(getTranslation(APPLICATION_SECTION, "HP"));
        final AnchorPane hpTextAnchorPane = new AnchorPane();
        HBox.setHgrow(hpTextAnchorPane, Priority.ALWAYS);
        hpText = new TextField();
        AnchorPane.setTopAnchor(hpText, 0.0);
        AnchorPane.setBottomAnchor(hpText, 0.0);
        AnchorPane.setLeftAnchor(hpText, 0.0);
        AnchorPane.setRightAnchor(hpText, 0.0);
        hpTextAnchorPane.getChildren().add(hpText);
        hpBox.getChildren().addAll(hpLabel, hpTextAnchorPane);

        nodes.add(hpBox);

        final HBox additionalParamsBox = new HBox();
        additionalParamsBox.setAlignment(Pos.CENTER_LEFT);
        additionalParamsBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        additionalParamsBox.setSpacing(10);
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
            combatantDataOverride = mergeWithOverride(baseEnemyData, enemyData.getCombatantDataOverride());
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
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "HP is empty"));
            hpText.requestFocus();
            return null;
        }

        for (final String hpString : hpText.getText().trim().split(COMMA_SPLIT_REGEX)) {
            try {
                builder.addHpBars(Integer.parseInt(hpString));
            } catch (final Exception e) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Integer"));
                hpText.requestFocus();
                return null;
            }
        }

        if (combatantDataOverride != null) {
            builder.setCombatantDataOverride(combatantDataOverride);
        }

        if (customGaugeCheckbox.isSelected()) {
            builder.setHasCustomMaxNpGauge(true);
            try {
                builder.setCustomMaxNpGauge(Integer.parseInt(customGaugeText.getText()));
            } catch (final Exception e) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Value not Integer"));
                customGaugeText.requestFocus();
                return null;
            }
        }

        if (isServant) {
            builder.setIsServant(true);
            builder.setServantAscension(servantAscensionChoiceBox.getValue());
        }

        return builder.build();
    }
}
