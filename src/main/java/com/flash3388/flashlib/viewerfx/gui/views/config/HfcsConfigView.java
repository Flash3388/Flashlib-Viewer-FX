package com.flash3388.flashlib.viewerfx.gui.views.config;

import com.flash3388.flashlib.viewerfx.gui.controls.propsheet.BindPortPropertyItem;
import com.flash3388.flashlib.viewerfx.gui.controls.propsheet.TargetAddressPropertyItem;
import com.flash3388.flashlib.viewerfx.gui.controls.propsheet.TargetPortPropertyItem;
import com.flash3388.flashlib.viewerfx.services.hfcs.HfcsService;
import com.flash3388.flashlib.viewerfx.services.hfcs.HfcsSingleTargetConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PropertySheet;

public class HfcsConfigView extends BorderPane {

    private final HfcsService mService;
    private final HfcsSingleTargetConfig mConfig;

    public HfcsConfigView(HfcsService service) {
        mService = service;
        mConfig = new HfcsSingleTargetConfig();

        PropertySheet propertySheet = new PropertySheet();
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.getItems().addAll(
                new BindPortPropertyItem(mConfig.bindPortProperty()),
                new TargetAddressPropertyItem(mConfig.targetAddressProperty()),
                new TargetPortPropertyItem(mConfig.targetPortProperty())
        );

        HBox propertySheetPane = new HBox();
        propertySheetPane.setAlignment(Pos.CENTER);
        propertySheetPane.setSpacing(5);
        propertySheetPane.setPadding(new Insets(2));
        propertySheetPane.getChildren().addAll(propertySheet);

        Button apply = new Button("Apply");
        apply.setOnAction((e)-> {
            mService.switchSettingsToSingleTarget(new HfcsSingleTargetConfig(mConfig));
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

        setTop(new Label("HFCS Config"));
        setCenter(propertySheetPane);
        setBottom(buttonsPane);
    }
}
