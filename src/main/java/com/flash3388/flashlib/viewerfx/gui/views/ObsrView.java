package com.flash3388.flashlib.viewerfx.gui.views;

import com.flash3388.flashlib.net.obsr.ObjectStorage;
import com.flash3388.flashlib.net.obsr.StoredEntry;
import com.flash3388.flashlib.viewerfx.FlashLibServices;

public class ObsrView extends AbstractView {

    ObjectStorage obsr;

    public ObsrView(FlashLibServices services) {
        // TODO: LISTEN ON ROOT FOR ANY CHANGES
        // TODO: FLASHLIB: ADD LISTENERS TO OBJECTS
        obsr = services.getObsrService();
    }

    public void updateView() {
        StoredEntry entry = obsr.getRoot().getEntry("blabla");
        StoredEntry entry2 = obsr.getRoot().getEntry("blabla2");

        System.out.println(entry.getValue());
        System.out.println(entry2.getValue());
    }

    @Override
    public void close() {

    }
}
