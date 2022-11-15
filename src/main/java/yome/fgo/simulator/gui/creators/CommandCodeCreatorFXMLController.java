package yome.fgo.simulator.gui.creators;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.CommandCodeData;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.gui.components.ListContainerVBox;
import yome.fgo.simulator.gui.components.ListContainerVBox.Mode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

import static yome.fgo.simulator.ResourceManager.getCCThumbnail;
import static yome.fgo.simulator.ResourceManager.readFile;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.CC_THUMBNAIL_STYLE;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.COMMAND_CODES_DIRECTORY_PATH;

public class CommandCodeCreatorFXMLController implements Initializable {
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
    private VBox buffVBox;
    private ListContainerVBox buffs;


    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Command Code Number"));
        nameLabel.setVisible(false);
        nameLabel.setText(null);

        imgAnchor.setStyle(CC_THUMBNAIL_STYLE);
        final File thumbnailFile = getCCThumbnail("");
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
                    final String ceId = "commandCode" + newValue;
                    if (hasTranslation(ENTITY_NAME_SECTION, ceId)) {
                        nameLabel.setText(getTranslation(ENTITY_NAME_SECTION, ceId));
                        nameLabel.setVisible(true);
                    } else {
                        nameLabel.setVisible(false);
                        nameLabel.setText(null);
                    }
                    try {
                        final int ceNum = Integer.parseInt(newValue);
                        final Image newImg = new Image(new FileInputStream(getCCThumbnail("commandCode" + ceNum)));
                        imgView.setImage(newImg);
                    } catch (final Exception ignored) {
                    }
                }
        );
        rarityLabel.setText(getTranslation(APPLICATION_SECTION, "Rarity"));
        rarityChoices.setItems(FXCollections.observableArrayList(ImmutableList.of(5, 4, 3, 2, 1)));
        rarityChoices.getSelectionModel().selectFirst();

        buffs = new ListContainerVBox(getTranslation(APPLICATION_SECTION, "Buffs"), errorLabel, Mode.BUFF);
        buffVBox.getChildren().add(buffs);

        loadButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        loadButton.setOnAction(e -> loadFrom());
        saveButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
        saveButton.setOnAction(e -> saveTo());
        errorLabel.setVisible(false);
    }

    private void loadFrom() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(COMMAND_CODES_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load Command Code Data"));
        final File ccFile = fileChooser.showOpenDialog(null);
        if (ccFile == null) {
            return;
        }

        final JsonFormat.Parser parser = JsonFormat.parser();
        final CommandCodeData.Builder builder = CommandCodeData.newBuilder();
        try {
            parser.merge(readFile(ccFile), builder);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + " " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        idText.setText(Integer.toString(builder.getCcNum()));
        rarityChoices.getSelectionModel().select(Integer.valueOf(builder.getRarity()));
        buffs.clear();
        buffs.loadBuff(builder.getBuffsList());

        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Load success!"));
    }

    private void saveTo() {
        final int ccNum;
        try {
            ccNum = Integer.parseInt(idText.getText().trim());
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot parse Command Code ID"));
            return;
        }

        final CommandCodeData commandCodeData = CommandCodeData.newBuilder()
                .setCcNum(ccNum)
                .setRarity(rarityChoices.getValue())
                .setId("commandCode" + ccNum)
                .addAllBuffs(buffs.buildBuff())
                .build();

        DataWriter.writeCommandCode(commandCodeData);
        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
    }
}
