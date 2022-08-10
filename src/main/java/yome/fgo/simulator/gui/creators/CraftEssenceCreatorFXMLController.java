package yome.fgo.simulator.gui.creators;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;
import yome.fgo.data.proto.FgoStorageData.Status;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.gui.components.ListContainerVBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.ResourceManager.getCEThumbnail;
import static yome.fgo.simulator.ResourceManager.readFile;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.CRAFT_ESSENCE_DIRECTORY_PATH;

public class CraftEssenceCreatorFXMLController implements Initializable {

    @FXML
    private Label costLabel;

    @FXML
    private TextField costText;

    @FXML
    private Label errorLabel;

    @FXML
    private Label idLabel;

    @FXML
    private TextField idText;

    @FXML
    private AnchorPane imgAnchor;

    @FXML
    private ImageView imgView;

    @FXML
    private Button loadButton;

    @FXML
    private Label nameLabel;

    @FXML
    private ChoiceBox<Integer> rarityChoices;

    @FXML
    private Label rarityLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea statusText;

    @FXML
    private VBox effectVBox;
    private ListContainerVBox effects;


    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Craft Essence Number"));
        nameLabel.setVisible(false);
        nameLabel.setText(null);

        imgAnchor.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        final File thumbnailFile = getCEThumbnail("");
        Image image = null;
        try {
            image = new Image(new FileInputStream(thumbnailFile));
        } catch (final FileNotFoundException ignored) {
        }
        if (image != null) {
            imgView.setImage(image);
        }
        idText.textProperty().addListener(

                (observable, oldValue, newValue) -> {
                    final String ceId = "craftEssence" + newValue;
                    if (hasTranslation(ENTITY_NAME_SECTION, ceId)) {
                        nameLabel.setText(getTranslation(ENTITY_NAME_SECTION, ceId));
                        nameLabel.setVisible(true);
                    } else {
                        nameLabel.setVisible(false);
                        nameLabel.setText(null);
                    }
                    try {
                        final int ceNum = Integer.parseInt(newValue);
                        final Image newImg = new Image(new FileInputStream(getCEThumbnail("craftEssence" + ceNum)));
                        imgView.setImage(newImg);
                    } catch (final Exception ignored) {
                    }
                }
        );
        rarityLabel.setText(getTranslation(APPLICATION_SECTION, "Rarity"));
        rarityChoices.setItems(FXCollections.observableArrayList(ImmutableList.of(5, 4, 3, 2, 1)));
        rarityChoices.getSelectionModel().selectFirst();
        costLabel.setText(getTranslation(APPLICATION_SECTION, "Craft Essence Cost"));

        effects = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Effects"), errorLabel);
        effectVBox.getChildren().add(effects);

        statusLabel.setText(getTranslation(APPLICATION_SECTION, "Craft Essence Status"));
        loadButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        loadButton.setOnAction(e -> loadFrom());
        saveButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
        saveButton.setOnAction(e -> saveTo());
        errorLabel.setVisible(false);
    }

    private void loadFrom() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(CRAFT_ESSENCE_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load CE Data"));
        final File ceDataFile = fileChooser.showOpenDialog(null);
        if (ceDataFile == null) {
            return;
        }

        final JsonFormat.Parser parser = JsonFormat.parser();
        final CraftEssenceData.Builder builder = CraftEssenceData.newBuilder();
        try {
            parser.merge(readFile(ceDataFile), builder);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + " " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        loadFrom(builder);
    }

    private void loadFrom(final CraftEssenceData.Builder builder) {
        idText.setText(Integer.toString(builder.getCeNum()));
        rarityChoices.getSelectionModel().select(Integer.valueOf(builder.getRarity()));
        costText.setText(Integer.toString(builder.getCost()));
        effects.clear();
        effects.loadEffect(builder.getEffectsList());

        final List<String> statusStrings = new ArrayList<>();
        final JsonFormat.Printer printer = JsonFormat.printer();
        try {
            for (final Status status : builder.getStatusDataList()) {
                statusStrings.add(printer.print(status).replaceAll("[\\n\\t ]", ""));
            }
        } catch (final Exception ignored) {
        }
        statusText.setText(String.join(", ", statusStrings));

        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Load success!"));
    }

    private void saveTo() {
        final int ceNum;
        try {
            ceNum = Integer.parseInt(idText.getText().trim());
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot parse CE ID"));
            return;
        }

        final int cost;
        try {
            cost = Integer.parseInt(costText.getText().trim());
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cost is not a valid integer"));
            return;
        }

        final CraftEssenceData.Builder status = CraftEssenceData.newBuilder();
        try {
            JsonFormat.parser().merge(new StringReader("{\"statusData\":[" + statusText.getText().trim() + "]}"), status);
        } catch (IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "CE status not parsable"));
            errorLabel.setVisible(true);
        }

        final CraftEssenceData craftEssenceData = CraftEssenceData.newBuilder()
                .setCeNum(ceNum)
                .setCost(cost)
                .setRarity(rarityChoices.getValue())
                .setId("craftEssence" + ceNum)
                .addAllEffects(effects.buildEffect())
                .addAllStatusData(status.getStatusDataList())
                .build();

        DataWriter.writeCraftEssence(craftEssenceData);
        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
    }

    public void setPreviewMode(final CraftEssenceData ceData) {
        saveButton.setDisable(true);
        loadButton.setDisable(true);
        loadFrom(ceData.toBuilder());
    }
}
