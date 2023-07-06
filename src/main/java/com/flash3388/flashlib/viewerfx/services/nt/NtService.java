package com.flash3388.flashlib.viewerfx.services.nt;

import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class NtService {

    private final Property<NetworkTableInstance> mService;
    private NtConfig mSetConfig;

    public NtService() {
        mService = new SimpleObjectProperty<>();
        mSetConfig = new NtConfig();
    }

    public Property<? extends NetworkTableInstance> serviceProperty() {
        return mService;
    }

    public NtConfig getSetConfig() {
        return mSetConfig;
    }

    public synchronized void start() {
        // use default settings
        switchSettingsToSecondaryNode(new NtConfig());
    }

    public synchronized void stop() {
        mService.getValue().close();
    }

    public synchronized void switchSettingsToSecondaryNode(NtConfig config) {
        NetworkTableInstance instance = NetworkTableInstance.create();
        instance.setServer(config.getServer());

        mSetConfig = config;
        switchSettings(instance);
    }

    private synchronized void switchSettings(NetworkTableInstance instance) {
        NetworkTableInstance old = mService.getValue();
        if (old != null) {
            old.close();
        }

        mService.setValue(instance);
        instance.startClient();
    }
}
