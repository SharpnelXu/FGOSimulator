package yome.fgo.simulator.gui.creators;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import yome.fgo.simulator.gui.components.ServantAscensionTab;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.SERVANT_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasTranslation;

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

        loadButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        saveButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
    }
}
