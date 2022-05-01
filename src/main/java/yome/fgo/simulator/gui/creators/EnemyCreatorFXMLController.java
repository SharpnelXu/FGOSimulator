package yome.fgo.simulator.gui.creators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.Alignment;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static yome.fgo.simulator.utils.FilePathUtils.ENEMY_DIRECTORY_PATH;

public class EnemyCreatorFXMLController implements Initializable {

    @FXML
    private CheckBox align1;

    @FXML
    private CheckBox align2;

    @FXML
    private CheckBox align3;

    @FXML
    private CheckBox align4;

    @FXML
    private CheckBox align5;

    @FXML
    private CheckBox align6;

    @FXML
    private CheckBox align7;

    @FXML
    private CheckBox align8;

    @FXML
    private CheckBox align9;

    @FXML
    private Label alignmentLabel;

    @FXML
    private ChoiceBox<Attribute> attributeCombo;

    @FXML
    private Label attributeLabel;

    @FXML
    private Label deathRateLabel;

    @FXML
    private TextField deathRateTextField;

    @FXML
    private ChoiceBox<FateClass> fateClassCombo;

    @FXML
    private Label fateClassLabel;

    @FXML
    private ChoiceBox<Gender> genderCombo;

    @FXML
    private Label genderLabel;

    @FXML
    private Label idLabel;

    @FXML
    private TextField idTextField;

    @FXML
    private Button loadFromButton;

    @FXML
    private ChoiceBox<Integer> rarityCombo;

    @FXML
    private Label rarityLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Label traitLabel;

    @FXML
    private TextField traitText;

    @FXML
    private CheckBox useUndeadCheck;

    @FXML
    private Label errorLabel;

    private List<CheckBox> alignBoxes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idLabel.setText("ID: ");
        deathRateLabel.setText("Death Rate (%): ");
        useUndeadCheck.setText("Use Undead Correction");

        rarityLabel.setText("Rarity: ");
        rarityCombo.setItems(FXCollections.observableArrayList(5, 4, 3, 2, 1, 0));
        rarityCombo.getSelectionModel().selectFirst();

        fateClassLabel.setText("Class: ");

        final List<FateClass> fateClasses = Lists.newArrayList(FateClass.values());
        fateClasses.remove(FateClass.NO_CLASS);
        fateClasses.remove(FateClass.UNRECOGNIZED);
        fateClassCombo.setItems(FXCollections.observableArrayList(fateClasses));
        fateClassCombo.getSelectionModel().selectFirst();

        genderLabel.setText("Gender: ");
        final List<Gender> genderClasses = Lists.newArrayList(Gender.values());
        genderClasses.remove(Gender.UNRECOGNIZED);
        genderCombo.setItems(FXCollections.observableArrayList(genderClasses));
        genderCombo.getSelectionModel().selectFirst();

        attributeLabel.setText("Attribute: ");

        final List<Attribute> attributeClasses = Lists.newArrayList(Attribute.values());
        attributeClasses.remove(Attribute.NO_ATTRIBUTE);
        attributeClasses.remove(Attribute.UNRECOGNIZED);
        attributeCombo.setItems(FXCollections.observableArrayList(attributeClasses));
        attributeCombo.getSelectionModel().selectFirst();

        alignmentLabel.setText("Alignments: ");
        alignBoxes = new ArrayList<>();
        align1.setText(Alignment.LAWFUL.name());
        align2.setText(Alignment.NEUTRAL.name());
        align3.setText(Alignment.CHAOTIC.name());
        align4.setText(Alignment.GOOD.name());
        align5.setText(Alignment.BALANCED.name());
        align6.setText(Alignment.EVIL.name());
        align7.setText(Alignment.INSANE.name());
        align8.setText(Alignment.BRIDE.name());
        align9.setText(Alignment.SUMMER.name());

        alignBoxes.addAll(ImmutableList.of(align1, align2, align3, align4, align5, align6, align7, align8, align9));

        traitLabel.setText("Traits: ");

        loadFromButton.setText("Load From...");
        saveButton.setText("Save To...");

        errorLabel.setVisible(false);
    }

    @FXML
    public void onSaveButtonClick() {
        final String id = idTextField.getText();
        if (id == null || id.isEmpty()) {
            errorLabel.setText("ID is null or empty!");
            errorLabel.setVisible(true);
            return;
        }

        final double deathRate;
        try {
            deathRate = RoundUtils.roundNearest(Double.parseDouble(deathRateTextField.getText()) / 100);
        } catch (final Exception e) {
            errorLabel.setText("Death Rate is not a valid double.");
            errorLabel.setVisible(true);
            return;
        }

        if (deathRate < 0) {
            errorLabel.setText("Death Rate is negative.");
            errorLabel.setVisible(true);
            return;
        }

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(ENEMY_DIRECTORY_PATH));
        fileChooser.setTitle("Save Enemy Data");
        fileChooser.setInitialFileName(id + ".json");

        final File saveFile = fileChooser.showSaveDialog(null);
        if (saveFile == null) {
            return;
        }

        final List<Alignment> checkedAlignments = alignBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(checkBox -> Alignment.valueOf(checkBox.getText()))
                .collect(Collectors.toList());

        final List<String> traits = Arrays.stream(traitText.getText().split("\s*[，,]\s*"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        final CombatantData combatantData = CombatantData.newBuilder().setId(id)
                .setDeathRate(deathRate)
                .setUndeadNpCorrection(useUndeadCheck.isSelected())
                .setRarity(rarityCombo.getValue())
                .setFateClass(fateClassCombo.getValue())
                .setGender(genderCombo.getValue())
                .setAttribute(attributeCombo.getValue())
                .addAllAlignments(checkedAlignments)
                .addAllTraits(traits)
                .build();

        try {
            DataWriter.writeMessage(combatantData, saveFile.getAbsolutePath());
            errorLabel.setText("Save success!");
            errorLabel.setVisible(true);
        } catch (final Exception e) {
            errorLabel.setText("Error while saving enemy! " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    @FXML
    public void onLoadFromButtonClick() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(ENEMY_DIRECTORY_PATH));
        fileChooser.setTitle("Load Enemy Data");
        final File enemyDataFile = fileChooser.showOpenDialog(null);
        if (enemyDataFile == null) {
            return;
        }

        final JsonFormat.Parser parser = JsonFormat.parser();
        final CombatantData.Builder combatantDataBuilder = CombatantData.newBuilder();
        try {
            parser.merge(new FileReader(enemyDataFile), combatantDataBuilder);
        } catch (final Exception e) {
            errorLabel.setText("Error loading file! " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        final String id = combatantDataBuilder.getId();
        if (id == null || id.isEmpty()) {
            errorLabel.setText("Loaded file has no valid ID!");
            errorLabel.setVisible(true);
            return;
        }

        final double deathRate = combatantDataBuilder.getDeathRate() * 100;
        if (deathRate < 0) {
            errorLabel.setText("Loaded file has negative Death Rate: " + String.format("%.2f", deathRate));
        }


        final Integer rarity = combatantDataBuilder.getRarity();
        if (!rarityCombo.getItems().contains(rarity)) {
            errorLabel.setText("Unrecognized rarity from file: " + rarity);
            errorLabel.setVisible(true);
            return;
        }

        final FateClass fateClass = combatantDataBuilder.getFateClass();
        if (!fateClassCombo.getItems().contains(fateClass)) {
            errorLabel.setText("Unrecognized class from file: " + fateClass);
            errorLabel.setVisible(true);
            return;
        }

        final Gender gender = combatantDataBuilder.getGender();
        if (!genderCombo.getItems().contains(gender)) {
            errorLabel.setText("Unrecognized gender from file: " + gender);
            errorLabel.setVisible(true);
            return;
        }

        final Attribute attribute = combatantDataBuilder.getAttribute();
        if (!attributeCombo.getItems().contains(attribute)) {
            errorLabel.setText("Unrecognized attribute from file: " + attribute);
            errorLabel.setVisible(true);
            return;
        }

        idTextField.setText(id);
        deathRateTextField.setText(String.format("%.2f", deathRate));
        useUndeadCheck.setSelected(combatantDataBuilder.getUndeadNpCorrection());
        rarityCombo.getSelectionModel().select(rarity);
        fateClassCombo.getSelectionModel().select(fateClass);
        genderCombo.getSelectionModel().select(gender);
        attributeCombo.getSelectionModel().select(attribute);

        final List<String> selectedAlignments = combatantDataBuilder.getAlignmentsList()
                .stream()
                .map(Alignment::name)
                .collect(Collectors.toList());

        for (final CheckBox checkBox : alignBoxes) {
            checkBox.setSelected(selectedAlignments.stream().anyMatch(checkBox.getText()::equalsIgnoreCase));
        }

        if (combatantDataBuilder.getTraitsCount() != 0) {
            traitText.setText(String.join(", ", combatantDataBuilder.getTraitsList()));
        }

        errorLabel.setText("Load success!");
        errorLabel.setVisible(true);
    }
}
