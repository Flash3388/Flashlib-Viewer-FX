package com.flash3388.flashlib.viewerfx.gui.controls.propsheet;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

public class BindPortPropertyItem  implements PropertySheet.Item {

    private final IntegerProperty mProperty;

    public BindPortPropertyItem(IntegerProperty property) {
        mProperty = property;
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    @Override
    public String getCategory() {
        return "Bind";
    }

    @Override
    public String getName() {
        return "Bind Port";
    }

    @Override
    public String getDescription() {
        return "Port to bind the local socket to";
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
