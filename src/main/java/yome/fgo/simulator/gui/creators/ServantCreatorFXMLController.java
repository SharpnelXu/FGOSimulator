package yome.fgo.simulator.gui.creators;

import com.google.protobuf.util.JsonFormat;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.gui.components.ServantAscensionTab;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.SERVANT_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class ServantCreatorFXMLController implements Initializable {
    @FXML
    private Button duplicateAscButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label idLabel;

    @FXML
    private TextField idText;

    @FXML
    private Button loadButton;

    @FXML
    private Button removeAscButton;

    @FXML
    private Button saveButton;

    @FXML
    private TabPane servantAscTabPane;

    @FXML
    private Label servantNameLabel;

    @FXML
    private Label servantIdErrorLabel;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Servant Number"));
        servantNameLabel.setText(null);
        servantIdErrorLabel.setVisible(false);
        idText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final String servantId = "servant" + newValue;
                    if (hasTranslation(SERVANT_NAME_SECTION, servantId)) {
                        servantNameLabel.setText(getTranslation(SERVANT_NAME_SECTION, servantId));
                        servantNameLabel.setVisible(true);
                    } else {
                        servantNameLabel.setVisible(false);
                    }
                    try {
                        final int servantNo = Integer.parseInt(idText.getText().trim());
                        for (int i = 1; i <= servantAscTabPane.getTabs().size(); i += 1) {
                            final ServantAscensionTab ascensionTab = (ServantAscensionTab) servantAscTabPane.getTabs().get(i - 1).getContent();
                            ascensionTab.setServantNo(servantNo, i);
                        }
                    } catch (final Exception e) {
                        servantIdErrorLabel.setVisible(true);
                        servantIdErrorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot parse servant ID"));
                    }
                }
        );

        removeAscButton.setText(getTranslation(APPLICATION_SECTION, "Remove Ascension"));
        duplicateAscButton.setText(getTranslation(APPLICATION_SECTION, "Add Ascension"));

        servantAscTabPane.getTabs().clear();
        servantAscTabPane.getTabs().add(new Tab(getTranslation(APPLICATION_SECTION, "Servant Asc") + 1, new ServantAscensionTab(0, 1)));
        removeAscButton.setOnAction(e -> {
            final List<Tab> tabs = servantAscTabPane.getTabs();
            if (tabs.size() > 1) {
                tabs.remove(tabs.size() - 1);
            }
        });
        duplicateAscButton.setOnAction(e -> {
            final List<Tab> tabs = servantAscTabPane.getTabs();
            final String tabName = getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + (tabs.size() + 1);
            final ServantAscensionTab base = (ServantAscensionTab) tabs.get(tabs.size() - 1).getContent();
            int servantNo = 0;
            try {
                servantNo = Integer.parseInt(idText.getText().trim());
            } catch (final Exception ignored) {
            }
            tabs.add(new Tab(tabName, new ServantAscensionTab(base, servantNo, tabs.size() + 1)));
        });

        errorLabel.setVisible(false);
        loadButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        loadButton.setOnAction(e -> loadFrom());
        saveButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
        saveButton.setOnAction(e -> saveTo());
    }

    private void loadFrom() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(SERVANT_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load Servant Data"));
        final File servantDataFile = fileChooser.showOpenDialog(null);
        if (servantDataFile == null) {
            return;
        }

        final JsonFormat.Parser parser = JsonFormat.parser();
        final ServantData.Builder servantDataBuilder = ServantData.newBuilder();
        try {
            parser.merge(new FileReader(servantDataFile), servantDataBuilder);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + " " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        idText.setText(Integer.toString(servantDataBuilder.getServantNum()));

        servantAscTabPane.getTabs().clear();
        for (int i = 0; i < servantDataBuilder.getServantAscensionDataList().size(); i += 1) {
            final List<Tab> tabs = servantAscTabPane.getTabs();
            final String tabName = getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + (tabs.size() + 1);
            final ServantAscensionTab ascensionTab = new ServantAscensionTab(
                    servantDataBuilder.getServantAscensionData(i),
                    servantDataBuilder.getServantNum(),
                    tabs.size() + 1
            );
            tabs.add(new Tab(tabName, ascensionTab));
        }
    }

    private void saveTo() {
        final int servantNo;
        try {
            servantNo = Integer.parseInt(idText.getText().trim());
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot parse servant ID"));
            return;
        }

        final List<ServantAscensionData> servantAscensionData = new ArrayList<>();
        for (final Tab tab : servantAscTabPane.getTabs()) {
            final ServantAscensionTab servantAscensionTab = (ServantAscensionTab) tab.getContent();
            final ServantAscensionData data = servantAscensionTab.build(servantNo);
            if (data == null) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Servant ascension data error") + tab.getText());
                return;
            }
            servantAscensionData.add(data);
        }

        final ServantData servantData = ServantData.newBuilder()
                .setServantNum(servantNo)
                .addAllServantAscensionData(servantAscensionData)
                .build();

        DataWriter.writeServant(servantData);
        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
        for (final Tab tab : servantAscTabPane.getTabs()) {
            final ServantAscensionTab servantAscensionTab = (ServantAscensionTab) tab.getContent();
            servantAscensionTab.clearError();
        }
    }
}
