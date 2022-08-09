package yome.fgo.simulator.gui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.ServantData;

import java.util.List;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;

@NoArgsConstructor
@Getter
public class ServantDataAnchorPane extends AnchorPane {
    private ServantData servantData;
    private List<Image> ascensionImages;
    private ImageView imageView;

    public ServantDataAnchorPane(final ServantData servantData, final List<Image> ascensionImages) {
        super();
        setFrom(servantData, ascensionImages);
    }

    public void setFrom(final ServantData servantData, final List<Image> ascensionImages) {
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setStyle(UNIT_THUMBNAIL_STYLE);
        this.servantData = servantData;
        this.ascensionImages = ascensionImages;
        imageView = new ImageView(ascensionImages.get(0));
        imageView.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        imageView.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        getChildren().clear();
        wrapInAnchor(this, imageView);
    }
}
