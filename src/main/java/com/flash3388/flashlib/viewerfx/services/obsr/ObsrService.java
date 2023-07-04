package com.flash3388.flashlib.viewerfx.services.obsr;

import com.castle.exceptions.ServiceException;
import com.flash3388.flashlib.net.obsr.ObjectStorage;
import com.flash3388.flashlib.net.obsr.impl.ObsrSecondaryNodeService;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.util.unique.InstanceId;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class ObsrService {

    private final InstanceId mInstanceId;
    private final Clock mClock;

    private final Property<ObsrSecondaryNodeService> mService;
    private ObsrSecondaryNodeConfig mSetConfig;

    public ObsrService(InstanceId instanceId, Clock clock) {
        mInstanceId = instanceId;
        mClock = clock;


        mService = new SimpleObjectProperty<>(null);
    }

    public Property<? extends ObjectStorage> serviceProperty() {
        return mService;
    }

    public ObsrSecondaryNodeConfig getSetConfig() {
        return mSetConfig;
    }

    public synchronized void start() {
        // use default settings
        switchSettingsToSecondaryNode(new ObsrSecondaryNodeConfig());
    }

    public synchronized void stop() {
        mService.getValue().close();
    }

    public synchronized void switchSettingsToSecondaryNode(ObsrSecondaryNodeConfig config) {
        ObsrSecondaryNodeService service = new ObsrSecondaryNodeService(
                mInstanceId,
                mClock,
                config.getTargetAddress());

        switchSettings(service);
        mSetConfig = config;
    }

    private synchronized void switchSettings(ObsrSecondaryNodeService newService) {
        ObsrSecondaryNodeService service = mService.getValue();
        if (service != null) {
            service.close();
        }

        mService.setValue(newService);
        try {
            newService.start();
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
