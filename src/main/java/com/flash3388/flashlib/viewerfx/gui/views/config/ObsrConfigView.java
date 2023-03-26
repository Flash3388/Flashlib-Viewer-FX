package com.flash3388.flashlib.viewerfx.gui.views.config;

import com.flash3388.flashlib.viewerfx.gui.controls.propsheet.TargetAddressPropertyItem;
import com.flash3388.flashlib.viewerfx.services.obsr.ObsrSecondaryNodeConfig;
import com.flash3388.flashlib.viewerfx.services.obsr.ObsrService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PropertySheet;

public class ObsrConfigView extends BorderPane {

    private final ObsrService mService;
    private final ObsrSecondaryNodeConfig mConfig;

    public ObsrConfigView(ObsrService service) {
        mService = service;
        mConfig = new ObsrSecondaryNodeConfig();

        PropertySheet propertySheet = new PropertySheet();
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.getItems().addAll(
                new TargetAddressPropertyItem(mConfig.targetAddressProperty())
        );

        HBox propertySheetPane = new HBox();
        propertySheetPane.setAlignment(Pos.CENTER);
        propertySheetPane.setSpacing(5);
        propertySheetPane.setPadding(new Insets(2));
        propertySheetPane.getChildren().addAll(propertySheet);

        Button apply = new Button("Apply");
        apply.setOnAction((e)-> {
            mService.switchSettingsToSecondaryNode(new ObsrSecondaryNodeConfig(mConfig));
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction((e)-> {
            mConfig.copyFrom(mService.getSetConfig());
        });
        HBox buttonsPane = new HBox();
        buttonsPane.getChildren().addAll(apply, cancel);
        buttonsPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonsPane.setSpacing(5);
        buttonsPane.setPadding(new Insets(2));

        setTop(new Label("OBSR Config"));
        setCenter(propertySheetPane);
        setBottom(buttonsPane);
    }
}
