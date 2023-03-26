package com.flash3388.flashlib.viewerfx.gui.controls.propsheet;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

public class TargetPortPropertyItem implements PropertySheet.Item {

    private final IntegerProperty mProperty;

    public TargetPortPropertyItem(IntegerProperty property) {
        mProperty = property;
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    @Override
    public String getCategory() {
        return "Target";
    }

    @Override
    public String getName() {
        return "Target Port";
    }

    @Override
    public String getDescription() {
        return "Port of the target socket to communicate with";
    }

    @Override
    public Object getValue() {
        return mProperty.getValue();
    }

    @Override
    public void setValue(Object value) {
        mProperty.set((Integer) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(mProperty);
    }
}
