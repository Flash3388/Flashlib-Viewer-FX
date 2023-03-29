package com.flash3388.flashlib.viewerfx.services.obsr;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ObsrSecondaryNodeConfig {

    private final StringProperty mTargetAddress;

    public ObsrSecondaryNodeConfig() {
        mTargetAddress = new SimpleStringProperty("127.0.0.1");
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public ObsrSecondaryNodeConfig(ObsrSecondaryNodeConfig other) {
        this();
        copyFrom(other);
    }

    public void copyFrom(ObsrSecondaryNodeConfig other) {
        setTargetAddress(other.getTargetAddress());
    }

    public StringProperty targetAddressProperty() {
        return mTargetAddress;
    }

    public String getTargetAddress() {
        return mTargetAddress.getValue();
    }

    public void setTargetAddress(String targetAddress) {
        mTargetAddress.setValue(targetAddress);
    }
}
