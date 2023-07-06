package com.flash3388.flashlib.viewerfx.services.nt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NtConfig {

    private final StringProperty mServer;

    public NtConfig() {
        mServer = new SimpleStringProperty("localhost");
    }

    public NtConfig(NtConfig other) {
        this();
        copyFrom(other);
    }

    public void copyFrom(NtConfig other) {
        setServer(other.getServer());
    }

    public StringProperty serverProperty() {
        return mServer;
    }

    public String getServer() {
        return mServer.get();
    }

    public void setServer(String server) {
        mServer.setValue(server);
    }
}
