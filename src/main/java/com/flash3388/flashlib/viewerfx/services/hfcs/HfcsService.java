package com.flash3388.flashlib.viewerfx.services.hfcs;

import com.castle.exceptions.ServiceException;
import com.flash3388.flashlib.net.hfcs.HfcsRegistry;
import com.flash3388.flashlib.net.hfcs.impl.HfcsServiceBase;
import com.flash3388.flashlib.net.hfcs.impl.HfcsUnicastService;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.util.unique.InstanceId;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.net.InetSocketAddress;

public class HfcsService {

    private final InstanceId mInstanceId;
    private final Clock mClock;

    private final Property<HfcsServiceBase> mService;
    private HfcsSingleTargetConfig mSetConfig;

    public HfcsService(InstanceId instanceId, Clock clock) {
        mInstanceId = instanceId;
        mClock = clock;
        
        
        mService = new SimpleObjectProperty<>(null);
    }

    public Property<? extends HfcsRegistry> serviceProperty() {
        return mService;
    }

    public HfcsSingleTargetConfig getSetConfig() {
        return mSetConfig;
    }

    public synchronized void start() {
        // use default settings
        switchSettingsToSingleTarget(new HfcsSingleTargetConfig());
    }

    public synchronized void stop() {
        mService.getValue().close();
    }

    public synchronized void switchSettingsToSingleTarget(HfcsSingleTargetConfig config) {
        HfcsServiceBase service = new HfcsUnicastService(
                mInstanceId,
                mClock,
                config.getBindPort(),
                new InetSocketAddress(
                        config.getTargetAddress(),
                        config.getTargetPort()
                ));

        switchSettings(service);
        mSetConfig = config;
    }

    private synchronized void switchSettings(HfcsServiceBase newService) {
        HfcsServiceBase service = mService.getValue(); 
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
