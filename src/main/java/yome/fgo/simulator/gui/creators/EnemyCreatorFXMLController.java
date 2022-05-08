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
import javafx.stage.Stage;
import yome.fgo.data.proto.FgoStorageData.Alignment;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.gui.components.EnumConverter;
import yome.fgo.simulator.translation.TranslationManager;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static yome.fgo.simulator.gui.helpers.ComponentMaker.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.addSplitTraitListener;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.fillFateClass;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getKeyForTrait;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasTranslation;
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

    private CombatantData.Builder combatantDataBuilder;

    @FXML
    private Button cancelButton;

    @FXML
    private Button buildButton;

    public void setParentBuilder(final CombatantData.Builder combatantDataBuilder) {
        this.combatantDataBuilder = combatantDataBuilder;
        loadCombatantData(combatantDataBuilder);

        idTextField.setDisable(true);
        idLabel.setDisable(true);

        loadFromButton.setVisible(false);
        loadFromButton.setManaged(false);
        saveButton.setVisible(false);
        saveButton.setManaged(false);
        cancelButton.setManaged(true);
        cancelButton.setVisible(true);
        buildButton.setManaged(true);
        buildButton.setVisible(true);
    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "ID"));
        deathRateLabel.setText(getTranslation(APPLICATION_SECTION, "Death Rate (%)"));
        useUndeadCheck.setText(getTranslation(APPLICATION_SECTION, "Use Undead Correction"));

        rarityLabel.setText(getTranslation(APPLICATION_SECTION, "Rarity"));
        rarityCombo.setItems(FXCollections.observableArrayList(5, 4, 3, 2, 1, 0));
        rarityCombo.getSelectionModel().selectFirst();

        fateClassLabel.setText(getTranslation(APPLICATION_SECTION, "Class"));
        fillFateClass(fateClassCombo);

        genderLabel.setText(getTranslation(APPLICATION_SECTION, "Gender"));
        final List<Gender> genderClasses = Lists.newArrayList(Gender.values());
        genderClasses.remove(Gender.UNRECOGNIZED);
        genderCombo.setConverter(new EnumConverter<>(TRAIT_SECTION));
        genderCombo.setItems(FXCollections.observableArrayList(genderClasses));
        genderCombo.getSelectionModel().selectFirst();

        attributeLabel.setText(getTranslation(APPLICATION_SECTION, "Attribute"));

        final List<Attribute> attributeClasses = Lists.newArrayList(Attribute.values());
        attributeClasses.remove(Attribute.NO_ATTRIBUTE);
        attributeClasses.remove(Attribute.UNRECOGNIZED);
        attributeCombo.setConverter(new EnumConverter<>(TRAIT_SECTION));
        attributeCombo.setItems(FXCollections.observableArrayList(attributeClasses));
        attributeCombo.getSelectionModel().selectFirst();

        alignmentLabel.setText(getTranslation(APPLICATION_SECTION, "Alignments"));
        alignBoxes = new ArrayList<>();
        align1.setText(getTranslation(TRAIT_SECTION, Alignment.LAWFUL.name()));
        align2.setText(getTranslation(TRAIT_SECTION, Alignment.NEUTRAL.name()));
        align3.setText(getTranslation(TRAIT_SECTION, Alignment.CHAOTIC.name()));
        align4.setText(getTranslation(TRAIT_SECTION, Alignment.GOOD.name()));
        align5.setText(getTranslation(TRAIT_SECTION, Alignment.BALANCED.name()));
        align6.setText(getTranslation(TRAIT_SECTION, Alignment.EVIL.name()));
        align7.setText(getTranslation(TRAIT_SECTION, Alignment.INSANE.name()));
        align8.setText(getTranslation(TRAIT_SECTION, Alignment.BRIDE.name()));
        align9.setText(getTranslation(TRAIT_SECTION, Alignment.SUMMER.name()));

        alignBoxes.addAll(ImmutableList.of(align1, align2, align3, align4, align5, align6, align7, align8, align9));

        traitLabel.setText(getTranslation(APPLICATION_SECTION, "Traits"));

        addSplitTraitListener(traitText, errorLabel);

        loadFromButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        saveButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
        cancelButton.setText(getTranslation(APPLICATION_SECTION, "Cancel"));
        buildButton.setText(getTranslation(APPLICATION_SECTION, "Build"));

        cancelButton.setOnAction(e -> {
            if (combatantDataBuilder != null) {
                combatantDataBuilder.clearId();
            }
            final Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
        buildButton.setOnAction(e -> {
            if (combatantDataBuilder != null) {
                final boolean buildSuccess = buildCombatantData(combatantDataBuilder);

                if (!buildSuccess) {
                    return;
                }
            }
            final Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        cancelButton.setManaged(false);
        cancelButton.setVisible(false);
        buildButton.setManaged(false);
        buildButton.setVisible(false);

        errorLabel.setVisible(false);
    }

    private boolean buildCombatantData(final CombatantData.Builder combatantDataBuilder) {
        combatantDataBuilder.clear();
        final String id = idTextField.getText();
        if (id == null || id.isEmpty()) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "ID is null or empty!"));
            errorLabel.setVisible(true);
            return false;
        }

        final double deathRate;
        try {
            deathRate = RoundUtils.roundNearest(Double.parseDouble(deathRateTextField.getText()) / 100);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Death Rate is not a valid double."));
            errorLabel.setVisible(true);
            return false;
        }

        if (deathRate < 0) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Death Rate is negative."));
            errorLabel.setVisible(true);
            return false;
        }

        final List<Alignment> checkedAlignments = alignBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(checkBox -> Alignment.valueOf(getKeyForTrait(checkBox.getText())))
                .collect(Collectors.toList());

        final List<String> traits = Arrays.stream(traitText.getText().trim().split(COMMA_SPLIT_REGEX))
                .filter(s -> !s.isEmpty())
                .map(TranslationManager::getKeyForTrait)
                .collect(Collectors.toList());
        combatantDataBuilder.setId(id)
                .setDeathRate(deathRate)
                .setUndeadNpCorrection(useUndeadCheck.isSelected())
                .setRarity(rarityCombo.getValue())
                .setFateClass(fateClassCombo.getValue())
                .setGender(genderCombo.getValue())
                .setAttribute(attributeCombo.getValue())
                .addAllAlignments(checkedAlignments)
                .addAllTraits(traits)
                .build();
        return true;
    }

    @FXML
    public void onSaveButtonClick() {

        final CombatantData.Builder combatantDataBuilder = CombatantData.newBuilder();
        final boolean buildSuccess = buildCombatantData(combatantDataBuilder);
        if (!buildSuccess) {
            return;
        }

        final CombatantData combatantData = combatantDataBuilder.build();

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(ENEMY_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Save Enemy Data"));
        fileChooser.setInitialFileName(idTextField.getText() + ".json");

        final File saveFile = fileChooser.showSaveDialog(null);
        if (saveFile == null) {
            return;
        }

        try {
            DataWriter.writeMessage(combatantData, saveFile.getAbsolutePath());
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
            errorLabel.setVisible(true);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error while saving enemy!") + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    @FXML
    public void onLoadFromButtonClick() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(ENEMY_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load Enemy Data"));
        final File enemyDataFile = fileChooser.showOpenDialog(null);
        if (enemyDataFile == null) {
            return;
        }

        final JsonFormat.Parser parser = JsonFormat.parser();
        final CombatantData.Builder combatantDataBuilder = CombatantData.newBuilder();
        try {
            parser.merge(new FileReader(enemyDataFile), combatantDataBuilder);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + " " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        loadCombatantData(combatantDataBuilder);
    }

    public void loadCombatantData(final CombatantData.Builder combatantData) {
        final String id = combatantData.getId();
        if (id == null || id.isBlank()) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Loaded file has no valid ID!"));
            errorLabel.setVisible(true);
            return;
        }

        final double deathRate = RoundUtils.roundNearest(combatantData.getDeathRate() * 100);
        if (deathRate < 0) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Loaded file has negative Death Rate:") +
                                       " " + String.format("%.2f", deathRate));
        }


        final Integer rarity = combatantData.getRarity();
        if (!rarityCombo.getItems().contains(rarity)) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Unrecognized rarity from file:") + rarity);
            errorLabel.setVisible(true);
            return;
        }

        final FateClass fateClass = combatantData.getFateClass();
        if (!fateClassCombo.getItems().contains(fateClass)) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Unrecognized class from file:") + " " + fateClass);
            errorLabel.setVisible(true);
            return;
        }

        final Gender gender = combatantData.getGender();
        if (!genderCombo.getItems().contains(gender)) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Unrecognized gender from file:") + " " + gender);
            errorLabel.setVisible(true);
            return;
        }

        final Attribute attribute = combatantData.getAttribute();
        if (!attributeCombo.getItems().contains(attribute)) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Unrecognized attribute from file:") + " " + attribute);
            errorLabel.setVisible(true);
            return;
        }

        idTextField.setText(id);
        deathRateTextField.setText(String.format("%.2f", deathRate));
        useUndeadCheck.setSelected(combatantData.getUndeadNpCorrection());
        rarityCombo.getSelectionModel().select(rarity);
        fateClassCombo.getSelectionModel().select(fateClass);
        genderCombo.getSelectionModel().select(gender);
        attributeCombo.getSelectionModel().select(attribute);

        final List<String> selectedAlignments = combatantData.getAlignmentsList()
                .stream()
                .map(alignment -> getTranslation(TRAIT_SECTION, alignment.name()))
                .collect(Collectors.toList());

        for (final CheckBox checkBox : alignBoxes) {
            checkBox.setSelected(selectedAlignments.stream().anyMatch(checkBox.getText()::equalsIgnoreCase));
        }

        final AtomicBoolean allTraitsFound = new AtomicBoolean(true);
        if (combatantData.getTraitsCount() != 0) {
            traitText.setText(combatantData.getTraitsList()
                                      .stream()
                                      .map(s -> {
                                          if (!hasTranslation(TRAIT_SECTION, s)) {
                                              allTraitsFound.set(false);
                                          }

                                          return getTranslation(TRAIT_SECTION, s);
                                      })
                                      .collect(Collectors.joining(", "))
            );
        } else {
            traitText.setText("");
        }

        if (allTraitsFound.get()) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Load success!"));
            errorLabel.setVisible(true);
        }
    }
}
