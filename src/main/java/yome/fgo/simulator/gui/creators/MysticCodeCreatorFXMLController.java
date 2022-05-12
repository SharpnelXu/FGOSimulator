package yome.fgo.simulator.gui.creators;

import com.google.protobuf.util.JsonFormat;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.gui.components.ActiveSkillUpgrade;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.MYSTIC_CODES_DIRECTORY_PATH;

public class MysticCodeCreatorFXMLController implements Initializable {

    @FXML
    private Label errorLabel;

    @FXML
    private Label idLabel;

    @FXML
    private TextField idText;

    @FXML
    private Button loadButton;

    @FXML
    private Label nameLabel;

    @FXML
    private Button saveButton;

    @FXML
    private VBox activeSkillsVBox;


    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Mystic Code Number"));
        nameLabel.setVisible(false);
        nameLabel.setText(null);

        idText.textProperty().addListener(

                (observable, oldValue, newValue) -> {
                    final String ceId = "mysticCode" + newValue;
                    if (hasTranslation(ENTITY_NAME_SECTION, ceId)) {
                        nameLabel.setText(getTranslation(ENTITY_NAME_SECTION, ceId));
                        nameLabel.setVisible(true);
                    } else {
                        nameLabel.setVisible(false);
                        nameLabel.setText(null);
                    }
                }
        );

        for (int i = 0; i < 3; i += 1) {
            activeSkillsVBox.getChildren().add(new ActiveSkillUpgrade());
        }

        loadButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        loadButton.setOnAction(e -> loadFrom());
        saveButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
        saveButton.setOnAction(e -> saveTo());
        errorLabel.setVisible(false);
    }

    private void loadFrom() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(MYSTIC_CODES_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load Mystic Code Data"));
        final File mcDataFile = fileChooser.showOpenDialog(null);
        if (mcDataFile == null) {
            return;
        }

        final JsonFormat.Parser parser = JsonFormat.parser();
        final MysticCodeData.Builder builder = MysticCodeData.newBuilder();
        try {
            parser.merge(new FileReader(mcDataFile), builder);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + " " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        idText.setText(Integer.toString(builder.getMcNum()));

        for (int i = 0; i < 3; i += 1) {
            final ActiveSkillUpgrade activeSkillUpgrade = (ActiveSkillUpgrade) activeSkillsVBox.getChildren().get(i);
            activeSkillUpgrade.setFrom(builder.getActiveSkillData(i));
        }

        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Load success!"));
    }

    private void saveTo() {
        final int mcNum;
        try {
            mcNum = Integer.parseInt(idText.getText().trim());
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot parse Mystic Code ID"));
            return;
        }

        final List<ActiveSkillData> activeSkills = new ArrayList<>();
        for (int i = 0; i < 3; i += 1) {
            final ActiveSkillUpgrade activeSkillUpgrade = (ActiveSkillUpgrade) activeSkillsVBox.getChildren().get(i);
            try {
                final ActiveSkillData activeSkillData = activeSkillUpgrade.build();
                activeSkills.add(activeSkillData);
            } catch (final Exception e) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error building active skill") + (i + 1));
                return;
            }
        }

        final MysticCodeData mysticCodeData = MysticCodeData.newBuilder()
                .setId("mysticCode" + mcNum)
                .setMcNum(mcNum)
                .addAllActiveSkillData(activeSkills)
                .build();

        DataWriter.writeMysticCode(mysticCodeData);
        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
    }
}
