package com.flash3388.flashlib.viewerfx.services.hfcs;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HfcsSingleTargetConfig {

    private final StringProperty mTargetAddress;
    private final IntegerProperty mTargetPort;
    private final IntegerProperty mBindPort;

    public HfcsSingleTargetConfig() {
        mTargetAddress = new SimpleStringProperty("127.0.0.1");
        mTargetPort = new SimpleIntegerProperty(5005);
        mBindPort = new SimpleIntegerProperty(5000);
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public HfcsSingleTargetConfig(HfcsSingleTargetConfig other) {
        this();
        copyFrom(other);
    }

    public void copyFrom(HfcsSingleTargetConfig other) {
        setTargetAddress(other.getTargetAddress());
        setTargetPort(other.getTargetPort());
        setBindPort(other.getBindPort());
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

    public IntegerProperty targetPortProperty() {
        return mTargetPort;
    }

    public int getTargetPort() {
        return mTargetPort.get();
    }

    public void setTargetPort(int targetPort) {
        mTargetPort.set(targetPort);
    }

    public IntegerProperty bindPortProperty() {
        return mBindPort;
    }

    public int getBindPort() {
        return mBindPort.get();
    }

    public void setBindPort(int bindPort) {
        mBindPort.set(bindPort);
    }
}
