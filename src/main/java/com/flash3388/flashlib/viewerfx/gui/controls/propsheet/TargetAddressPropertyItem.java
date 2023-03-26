package com.flash3388.flashlib.viewerfx.gui.controls.propsheet;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

public class TargetAddressPropertyItem implements PropertySheet.Item {

    private final StringProperty mProperty;

    public TargetAddressPropertyItem(StringProperty property) {
        mProperty = property;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public String getCategory() {
        return "Target";
    }

    @Override
    public String getName() {
        return "Target Address";
    }

    @Override
    public String getDescription() {
        return "IP/Hostname of the target socket to communicate with";
    }

    @Override
    public Object getValue() {
        return mProperty.getValue();
    }

    @Override
    public void setValue(Object value) {
        mProperty.set((String) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(mProperty);
    }
}
