package com.flash3388.flashlib.viewerfx.gui.views.config;

import com.flash3388.flashlib.viewerfx.services.FlashLibServices;
import javafx.scene.control.SplitPane;

public class ConfigView extends SplitPane {

    public ConfigView(FlashLibServices services) {
        HfcsConfigView hfcsConfigView = new HfcsConfigView(services.getHfcsService());
        ObsrConfigView obsrConfigView = new ObsrConfigView(services.getObsrService());

        getItems().addAll(hfcsConfigView, obsrConfigView);
    }
}
