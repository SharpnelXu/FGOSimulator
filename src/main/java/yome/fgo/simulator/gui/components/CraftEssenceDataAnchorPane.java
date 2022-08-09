package yome.fgo.simulator.gui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;

@NoArgsConstructor
@Getter
public class CraftEssenceDataAnchorPane extends AnchorPane {
    private CraftEssenceData craftEssenceData;
    private Image image;
    private ImageView imageView;

    public CraftEssenceDataAnchorPane(final CraftEssenceData craftEssenceData, final Image image) {
        super();
        setFrom(craftEssenceData, image);
    }

    public void setFrom(final CraftEssenceData craftEssenceData, final Image image) {
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setStyle(UNIT_THUMBNAIL_STYLE);
        this.craftEssenceData = craftEssenceData;
        this.image = image;
        imageView = new ImageView(image);
        imageView.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        imageView.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        getChildren().clear();
        wrapInAnchor(this, imageView);
    }
}
