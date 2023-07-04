package com.flash3388.flashlib.viewerfx.services.hfcs;

import com.castle.exceptions.ServiceException;
import com.flash3388.flashlib.net.hfcs.HfcsRegistry;
import com.flash3388.flashlib.net.hfcs.impl.HfcsServiceBase;
import com.flash3388.flashlib.net.hfcs.impl.HfcsServices;
import com.flash3388.flashlib.net.util.NetInterfaces;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.util.unique.InstanceId;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;

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
        /*HfcsServiceBase service = HfcsServices.unicast(
                mInstanceId,
                mClock,
                new InetSocketAddress(config.getBindPort()),
                new InetSocketAddress(
                        config.getTargetAddress(),
                        config.getTargetPort()
                ));*/

        HfcsServiceBase service;
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName("lo");
            InetAddress inetAddress = InetAddress.getByName("224.0.0.251");
            service = HfcsServices.multicast(mInstanceId, mClock,
                    new InetSocketAddress(5000),
                    5005,
                    networkInterface,
                    inetAddress);

        } catch (Throwable t) {
            throw new Error(t);
        }

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
