package yome.fgo.simulator.gui.creators;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CommandCodeData;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.gui.components.BuffsCellFactory;
import yome.fgo.simulator.gui.components.DataWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static yome.fgo.simulator.ResourceManager.getCCThumbnail;
import static yome.fgo.simulator.ResourceManager.readFile;
import static yome.fgo.simulator.gui.creators.BuffBuilder.createBuff;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.COMMAND_CODES_DIRECTORY_PATH;

public class CommandCodeCreatorFXMLController implements Initializable {

    @FXML
    private Button addBuffsButton;

    @FXML
    private Label buffsLabel;

    @FXML
    private ListView<DataWrapper<BuffData>> buffsList;

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


    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Command Code Number"));
        nameLabel.setVisible(false);
        nameLabel.setText(null);

        imgAnchor.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2; -fx-background-color: rgba(220,245,255,0.73)");
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
        buffsLabel.setText(getTranslation(APPLICATION_SECTION, "Buffs"));
        buffsList.setCellFactory(new BuffsCellFactory(errorLabel));
        buffsList.setItems(FXCollections.observableArrayList());
        addBuffsButton.setText(getTranslation(APPLICATION_SECTION, "Add Buff"));
        addBuffsButton.setOnAction(e -> {
            try {
                final BuffData.Builder builder = BuffData.newBuilder();
                createBuff(addBuffsButton.getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    buffsList.getItems().add(new DataWrapper<>(builder.build()));
                }
            } catch (final IOException ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + ex);
                errorLabel.setVisible(true);
            }
        });
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
        buffsList.getItems().clear();
        buffsList.getItems().addAll(builder.getBuffsList().stream().map(DataWrapper::new).collect(Collectors.toList()));

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
                .addAllBuffs(buffsList.getItems().stream().map(wrapper -> wrapper.protoData).collect(Collectors.toList()))
                .build();

        DataWriter.writeCommandCode(commandCodeData);
        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
    }
}
