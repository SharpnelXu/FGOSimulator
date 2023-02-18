package yome.fgo.simulator.gui.creators;

import com.google.common.collect.Lists;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.CraftEssencePreference;
import yome.fgo.data.proto.FgoStorageData.Formation;
import yome.fgo.data.proto.FgoStorageData.MysticCodePreference;
import yome.fgo.data.proto.FgoStorageData.ServantPreference;
import yome.fgo.simulator.gui.components.CraftEssenceDataAnchorPane;
import yome.fgo.simulator.gui.components.MysticCodeDataAnchorPane;
import yome.fgo.simulator.gui.components.ServantDataAnchorPane;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.ResourceManager.CRAFT_ESSENCE_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.MYSTIC_CODE_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.SERVANT_DATA_ANCHOR_MAP;
import static yome.fgo.simulator.ResourceManager.getUnknownServantThumbnail;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.DEFAULT_18;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.LIST_ITEM_STYLE;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class FormationFXMLController implements Initializable {
    @FXML
    private VBox formationVBox;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
    }

    public void fillFormations(
            final List<Formation> formationList,
            final Formation.Builder builder
    ) {
        Image unknown = null;
        try {
            unknown = new Image(new FileInputStream(getUnknownServantThumbnail()));
        } catch (final FileNotFoundException ignored) {
        }

        for (int h = 0; h < formationList.size(); h += 1) {
            final Formation formation = formationList.get(h);

            final HBox nameHBox = new HBox(20);
            nameHBox.setAlignment(Pos.CENTER_LEFT);
            final Button selectButton = new Button(getTranslation(APPLICATION_SECTION, "Select Formation"));
            selectButton.setFont(DEFAULT_18);
            final Button deleteButton = new Button(getTranslation(APPLICATION_SECTION, "Delete Formation"));
            deleteButton.setFont(DEFAULT_18);
            final Label formationName = new Label(formation.getName());
            formationName.setFont(DEFAULT_18);
            nameHBox.getChildren().addAll(selectButton, deleteButton, formationName);

            final HBox partyHBox = new HBox(15);
            final List<ServantPreference> servantPreferences = formation.getServantsList();
            final List<CraftEssencePreference> craftEssencePreferences = formation.getCraftEssencesList();
            for (int i = 0; i < formation.getServantsCount(); i += 1) {
                final VBox memberVBox = new VBox(20);
                final ServantPreference servantPreference = servantPreferences.get(i);
                final ServantDataAnchorPane servant;
                final int servantNo = servantPreference.getServantNo();
                if (SERVANT_DATA_ANCHOR_MAP.containsKey(servantNo)) {
                    final ServantDataAnchorPane reference = SERVANT_DATA_ANCHOR_MAP.get(servantNo);
                    servant = new ServantDataAnchorPane(reference.getServantData(), reference.getAscensionImages());
                    servant.getImageView().setImage(servant.getAscensionImages().get(servantPreference.getOption().getAscension() - 1));
                } else {
                    servant = new ServantDataAnchorPane(null, Lists.newArrayList(unknown));
                }

                final CraftEssenceDataAnchorPane craftEssence;
                final int ceNo = craftEssencePreferences.get(i).getCraftEssenceNo();
                if (CRAFT_ESSENCE_DATA_ANCHOR_MAP.containsKey(ceNo)) {
                    final CraftEssenceDataAnchorPane reference = CRAFT_ESSENCE_DATA_ANCHOR_MAP.get(ceNo);
                    craftEssence = new CraftEssenceDataAnchorPane(reference.getCraftEssenceData(), reference.getImage());
                } else {
                    craftEssence = new CraftEssenceDataAnchorPane(null, unknown);
                }

                memberVBox.getChildren().addAll(servant, craftEssence);
                partyHBox.getChildren().add(memberVBox);
            }

            partyHBox.getChildren().add(new Separator(Orientation.VERTICAL));

            final MysticCodePreference mysticCodePreference = formation.getMysticCode();
            final MysticCodeDataAnchorPane mysticCode = new MysticCodeDataAnchorPane();
            final MysticCodeDataAnchorPane reference = MYSTIC_CODE_DATA_ANCHOR_MAP.get(mysticCodePreference.getMysticCodeNo());
            mysticCode.setFrom(reference.getMysticCodeData(), reference.getImages(), mysticCodePreference.getOption().getGender());

            partyHBox.getChildren().add(mysticCode);

            final VBox itemVBox = new VBox(10);
            itemVBox.setPadding(new Insets(20));
            itemVBox.setStyle(LIST_ITEM_STYLE);
            itemVBox.getChildren().addAll(nameHBox, partyHBox);
            formationVBox.getChildren().add(itemVBox);

            selectButton.setOnAction(e -> {
                builder.mergeFrom(formation);
                final Stage stage = (Stage) formationVBox.getScene().getWindow();
                stage.close();
            });
            deleteButton.setOnAction(e -> {
                formationVBox.getChildren().remove(itemVBox);
                formationList.remove(formation);
            });
        }
    }
}