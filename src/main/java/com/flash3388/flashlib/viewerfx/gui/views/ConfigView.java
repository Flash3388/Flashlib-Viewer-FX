package com.flash3388.flashlib.viewerfx.gui.views;

import com.flash3388.flashlib.viewerfx.gui.controls.propsheet.BindPortPropertyItem;
import com.flash3388.flashlib.viewerfx.gui.controls.propsheet.TargetAddressPropertyItem;
import com.flash3388.flashlib.viewerfx.gui.controls.propsheet.TargetPortPropertyItem;
import com.flash3388.flashlib.viewerfx.services.FlashLibServices;
import com.flash3388.flashlib.viewerfx.services.hfcs.HfcsService;
import com.flash3388.flashlib.viewerfx.services.hfcs.HfcsSingleTargetConfig;
import com.flash3388.flashlib.viewerfx.services.obsr.ObsrSecondaryNodeConfig;
import com.flash3388.flashlib.viewerfx.services.obsr.ObsrService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PropertySheet;

public class ConfigView extends BorderPane {

    private final HfcsService mHfcsService;
    private final HfcsSingleTargetConfig mHfcsConfig;

    private final ObsrService mObsrService;
    private final ObsrSecondaryNodeConfig mObsrConfig;

    public ConfigView(FlashLibServices services) {
        mHfcsService = services.getHfcsService();
        mHfcsConfig = new HfcsSingleTargetConfig();
        mObsrService = services.getObsrService();
        mObsrConfig = new ObsrSecondaryNodeConfig();

        PropertySheet propertySheet = new PropertySheet();
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.setModeSwitcherVisible(false);
        propertySheet.getItems().addAll(
                new BindPortPropertyItem(mHfcsConfig.bindPortProperty(), "HFCS"),
                new TargetAddressPropertyItem(mHfcsConfig.targetAddressProperty(), "HFCS"),
                new TargetPortPropertyItem(mHfcsConfig.targetPortProperty(), "HFCS"),

                new TargetAddressPropertyItem(mObsrConfig.targetAddressProperty(), "OBSR")
        );

        HBox propertySheetPane = new HBox();
        propertySheetPane.setAlignment(Pos.CENTER);
        propertySheetPane.setSpacing(5);
        propertySheetPane.setPadding(new Insets(2));
        propertySheetPane.getChildren().addAll(propertySheet);

        Button apply = new Button("Apply");
        apply.setOnAction((e)-> {
            mHfcsService.switchSettingsToSingleTarget(new HfcsSingleTargetConfig(mHfcsConfig));
            mObsrService.switchSettingsToSecondaryNode(new ObsrSecondaryNodeConfig(mObsrConfig));
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction((e)-> {
            mHfcsConfig.copyFrom(mHfcsService.getSetConfig());
            mObsrConfig.copyFrom(mObsrService.getSetConfig());
        });
        HBox buttonsPane = new HBox();
        buttonsPane.getChildren().addAll(apply, cancel);
        buttonsPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonsPane.setSpacing(5);
        buttonsPane.setPadding(new Insets(2));

        setCenter(propertySheetPane);
        setBottom(buttonsPane);
    }
}
