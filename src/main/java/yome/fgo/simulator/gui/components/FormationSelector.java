package yome.fgo.simulator.gui.components;

import com.google.common.collect.Lists;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillUpgrades;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static yome.fgo.simulator.ResourceManager.getUnknownServantThumbnail;
import static yome.fgo.simulator.gui.components.DataPrinter.printServantOption;
import static yome.fgo.simulator.gui.creators.EntitySelector.selectServant;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class FormationSelector extends VBox {
    private final RadioButton support;
    private ServantDataWrapper selectedServant;
    private ServantOption servantOption;
    private final ServantDataWrapper defaultSelection;
    private final Label servantOptionLabel;

    public FormationSelector(
            final ToggleGroup toggleGroup,
            final Label errorLabel,
            final Map<Integer, ServantDataWrapper> servantDataMap
    ) {
        super();

        setSpacing(10);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(this, Priority.ALWAYS);

        support = new RadioButton(getTranslation(APPLICATION_SECTION, "In Formation"));
        support.setToggleGroup(toggleGroup);
        support.setOnAction(e -> {
            if (support.isSelected()) {
                support.setText(getTranslation(APPLICATION_SECTION, "Support"));
            } else {
                support.setText(getTranslation(APPLICATION_SECTION, "In Formation"));
            }
        });

        final Button servantSelectButton = new Button();
        Image unknown = null;
        try {
            unknown = new Image(new FileInputStream(getUnknownServantThumbnail()));
        } catch (final FileNotFoundException ignored) {
        }
        defaultSelection = new ServantDataWrapper(null, Lists.newArrayList(unknown));
        selectedServant = defaultSelection;
        final Label servantNameLabel = new Label(getTranslation(APPLICATION_SECTION, "No servant selected"));
        servantNameLabel.setWrapText(true);
        servantNameLabel.setAlignment(Pos.CENTER);
        servantOptionLabel = new Label();
        servantOptionLabel.setWrapText(true);

        final Button editServantOptionButton = new Button(getTranslation(APPLICATION_SECTION, "Edit Servant Option"));
        editServantOptionButton.setDisable(true);
        editServantOptionButton.setOnAction(e -> editServantOption());

        servantSelectButton.setGraphic(selectedServant);
        servantSelectButton.setOnAction(e -> {
            try {
                final ServantDataWrapper selection = selectServant(this.getScene().getWindow(), servantDataMap);
                if (selection != null) {
                    selectedServant = selection;
                    servantSelectButton.setGraphic(selectedServant);
                    servantOption = createDefaultServantOption(selectedServant.getServantData());
                    servantNameLabel.setText(
                            getTranslation(
                                    ENTITY_NAME_SECTION,
                                    selectedServant.getServantData()
                                            .getServantAscensionData(0)
                                            .getCombatantData()
                                            .getId()
                            )
                    );
                    servantOptionLabel.setText(printServantOption(servantOption));
                    servantNameLabel.setMaxWidth(getWidth());
                    servantOptionLabel.setMaxWidth(getWidth());
                    editServantOptionButton.setDisable(false);
                }
            } catch (final IOException ex) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
            }
        });

        final Button removeServantButton = new Button(getTranslation(APPLICATION_SECTION, "Remove selection"));
        removeServantButton.setOnAction(e -> {
            selectedServant = defaultSelection;
            servantSelectButton.setGraphic(selectedServant);
            servantOption = null;
            servantNameLabel.setText(getTranslation(APPLICATION_SECTION, "No servant selected"));
            servantNameLabel.setMaxWidth(getWidth());
            servantOptionLabel.setText(null);
            servantOptionLabel.setMaxWidth(getWidth());
            editServantOptionButton.setDisable(true);
        });

        getChildren().addAll(support, servantSelectButton, removeServantButton, servantNameLabel, editServantOptionButton, servantOptionLabel);
    }

    private ServantOption createDefaultServantOption(final ServantData servantData) {
        final ServantAscensionData servantAscensionData = servantData.getServantAscensionData(0);
        final int rarity = servantAscensionData.getCombatantData().getRarity();

        final ServantOption.Builder builder = ServantOption.newBuilder()
                .setAscension(1)
                .setServantLevel(defaultServantLevel(rarity))
                .setAttackStatusUp(1000)
                .setHealthStatusUp(1000)
                .setNoblePhantasmRank(servantAscensionData.getNoblePhantasmUpgrades().getNoblePhantasmDataCount())
                .setNoblePhantasmLevel(5);

        for (final ActiveSkillUpgrades activeSkillUpgrades : servantAscensionData.getActiveSkillUpgradesList()) {
            builder.addActiveSkillRanks(activeSkillUpgrades.getActiveSkillDataCount());
            builder.addActiveSkillLevels(10);
        }
        for (int i = 0; i < servantAscensionData.getAppendSkillDataCount(); i += 1) {
            builder.addAppendSkillLevels(0);
        }
        builder.setBond(10);

        return builder.build();
    }

    private int defaultServantLevel(final int rarity) {
        return switch (rarity) {
            case 5 -> 90;
            case 4 -> 80;
            case 3 -> 70;
            case 2, 0 -> 65;
            case 1 -> 60;
            default -> 1;
        };
    }

    public boolean isSupport() {
        return support.isSelected();
    }

    private void editServantOption() {
        final Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initOwner(this.getScene().getWindow());

        final ServantOption.Builder builder = servantOption.toBuilder();
        final ServantOptionEditor editor = new ServantOptionEditor(
                new ServantDataWrapper(selectedServant.getServantData(), selectedServant.getAscensionImages()),
                builder
        );
        final Parent root = editor.getRoot();
        final Scene scene = new Scene(root);

        newStage.setTitle(TranslationManager.getTranslation(APPLICATION_SECTION, "ServantSelector"));
        newStage.setScene(scene);

        newStage.showAndWait();

        if (builder.getAscension() != -1) {
            servantOption = builder.build();
            servantOptionLabel.setText(printServantOption(servantOption));
            servantOptionLabel.setMaxWidth(getWidth());
            selectedServant.getImageView().setImage(selectedServant.getAscensionImages().get(servantOption.getAscension() - 1));
        }
    }
}
