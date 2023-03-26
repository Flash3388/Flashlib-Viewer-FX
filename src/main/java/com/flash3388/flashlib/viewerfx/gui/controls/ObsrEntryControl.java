package com.flash3388.flashlib.viewerfx.gui.controls;

import com.beans.observables.RegisteredListener;
import com.flash3388.flashlib.net.obsr.StoredEntry;
import com.flash3388.flashlib.net.obsr.Value;
import com.flash3388.flashlib.net.obsr.ValueType;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.concurrent.atomic.AtomicBoolean;

public class ObsrEntryControl extends BorderPane {

    private final StoredEntry mEntry;
    private ValueType mLastSeenType;

    private final HBox mValueDisplayPane;
    private final ComboBox<ValueType> mValueTypeSelector;
    private final AtomicBoolean mIgnoreIncoming;
    private ValueDisplay mCurrentValueDisplay;

    public ObsrEntryControl(String name, StoredEntry entry) {
        mEntry = entry;
        mIgnoreIncoming = new AtomicBoolean(false);

        HBox top = new HBox();
        top.getChildren().add(new Label(name));
        top.setAlignment(Pos.CENTER);
        setTop(top);

        mValueTypeSelector = new ComboBox<>();
        mValueTypeSelector.getItems().addAll(ValueType.values());
        mValueTypeSelector.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            if (mLastSeenType == n) {
                return;
            }

            mIgnoreIncoming.set(true);
            resetValue(n);
            mIgnoreIncoming.set(false);
        });
        setLeft(mValueTypeSelector);

        mValueDisplayPane = new HBox();
        mValueDisplayPane.setAlignment(Pos.CENTER);
        setCenter(mValueDisplayPane);
        mCurrentValueDisplay = null;

        changeDisplay(ValueType.EMPTY);
    }

    public void updateValue() {
        if (mIgnoreIncoming.get()) {
            return;
        }

        Value value = mEntry.getValue();
        if (value.getType() != mLastSeenType) {
            changeDisplay(value.getType());
        }

        mCurrentValueDisplay.setValueFromEntry(value);
    }

    public void close() {
        if (mCurrentValueDisplay != null) {
            mCurrentValueDisplay.close();
            mCurrentValueDisplay = null;
        }
    }

    private void changeDisplay(ValueType type) {
        close();

        switch (type) {
            case STRING: {
                mCurrentValueDisplay = new StringValueDisplay(mEntry);
                break;
            }
            case INT: {
                mCurrentValueDisplay = new NumericValueDisplay(mEntry, type, int.class);
                break;
            }
            case LONG: {
                mCurrentValueDisplay = new NumericValueDisplay(mEntry, type, long.class);
                break;
            }
            case DOUBLE: {
                mCurrentValueDisplay = new NumericValueDisplay(mEntry, type, double.class);
                break;
            }
            case BOOLEAN: {
                mCurrentValueDisplay = new BooleanValueDisplay(mEntry);
                break;
            }
            case RAW: {
                mCurrentValueDisplay = new RawValueDisplay(mEntry);
                break;
            }
            case EMPTY: {
                mCurrentValueDisplay = new EmptyValueDisplay(mEntry);
                break;
            }
            default:
                throw new AssertionError("unknown type");
        }

        mValueDisplayPane.getChildren().clear();
        mValueDisplayPane.getChildren().add(mCurrentValueDisplay);
        mLastSeenType = type;
        mValueTypeSelector.getSelectionModel().select(type);
    }

    private void resetValue(ValueType type) {
        close();
        mEntry.clearValue();

        switch (type) {
            case STRING:
                mEntry.setString("");
                break;
            case BOOLEAN:
                mEntry.setBoolean(false);
                break;
            case INT:
                mEntry.setInt(0);
                break;
            case LONG:
                mEntry.valueProperty().set(new Value(type, 0L));
                break;
            case DOUBLE:
                mEntry.setDouble(0);
                break;
            case RAW:
                mEntry.setRaw(new byte[0]);
                break;
            case EMPTY:
                mEntry.clearValue();
                break;
        }

        changeDisplay(type);
    }

    private static abstract class ValueDisplay extends Pane implements AutoCloseable {

        private final StoredEntry mEntry;
        private final ValueType mSupportedType;

        private final AtomicBoolean mIsModifying;
        private final RegisteredListener mChangeListener;

        protected ValueDisplay(StoredEntry entry, ValueType supportedType) {
            mEntry = entry;
            mSupportedType = supportedType;
            mIsModifying = new AtomicBoolean(false);

            mChangeListener = mEntry.valueProperty().addChangeListener((event)-> {
                if (mIsModifying.get()) {
                    return;
                }

                setValueFromEntry(event.getNewValue());
            });
        }

        public void updateValueInEntry(Value value) {
            assert value.getType() == mSupportedType;
            mIsModifying.set(true);
            mEntry.valueProperty().set(value);
            mIsModifying.set(false);
        }

        @Override
        public void close() {
            mChangeListener.remove();
        }

        abstract void setValueFromEntry(Value value);
    }

    private static class StringValueDisplay extends ValueDisplay {

        private final TextField mField;

        private StringValueDisplay(StoredEntry entry) {
            super(entry, ValueType.STRING);

            mField = new TextField(entry.getString(""));
            mField.textProperty().addListener((obs, o, n)-> {
                updateValueInEntry(new Value(ValueType.STRING, n));
            });
            getChildren().add(mField);
        }

        @Override
        public void setValueFromEntry(Value value) {
            mField.setText(value.getString(""));
        }
    }

    private static class NumericValueDisplay extends ValueDisplay {

        private final NumericField mField;
        private final ValueType mValueType;

        private NumericValueDisplay(StoredEntry entry,
                                    ValueType valueType,
                                    Class<? extends Number> type) {
            super(entry, valueType);

            mValueType = valueType;
            mField = new NumericField(type);
            mField.valueProperty().addListener((obs, o, n)-> {
                updateValueInEntry(new Value(valueType, n));
            });

            switch (valueType) {
                case INT:
                    mField.valueProperty().setValue(entry.getInt(0));
                    break;
                case LONG:
                    mField.valueProperty().setValue(entry.getValue().getLong(0));
                    break;
                case DOUBLE:
                    mField.valueProperty().setValue(entry.getDouble(0));
                    break;
            }
            mField.setText(String.valueOf(mField.valueProperty().getValue()));

            getChildren().add(mField);
        }

        @Override
        public void setValueFromEntry(Value value) {
            Number number = null;
            switch (mValueType) {
                case INT:
                    number = value.getInt(0);
                    break;
                case LONG:
                    number = value.getLong(0);
                    break;
                case DOUBLE:
                    number = value.getDouble(0);
                    break;
                default:
                    throw new AssertionError("type is not a known numeric type");
            }

            mField.valueProperty().setValue(number);
        }
    }

    private static class BooleanValueDisplay extends ValueDisplay {

        private final CheckBox mField;

        private BooleanValueDisplay(StoredEntry entry) {
            super(entry, ValueType.BOOLEAN);

            mField = new CheckBox();
            mField.selectedProperty().addListener((obs, o, n)-> {
                updateValueInEntry(new Value(ValueType.BOOLEAN, n));
            });
            mField.setSelected(entry.getBoolean(false));

            getChildren().add(mField);
        }

        @Override
        public void setValueFromEntry(Value value) {
            mField.setSelected(value.getBoolean(false));
        }
    }

    private static class RawValueDisplay extends ValueDisplay {

        private final TextField mField;

        private RawValueDisplay(StoredEntry entry) {
            super(entry, ValueType.RAW);

            mField = new TextField();
            mField.setText(bytesToHex(entry.getRaw(null)));
            getChildren().add(mField);
        }

        @Override
        public void setValueFromEntry(Value value) {
            mField.setText(bytesToHex(value.getRaw(null)));
        }

        public static String bytesToHex(byte[] bytes) {
            final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bytes.length * 2];

            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = HEX_ARRAY[v >>> 4];
                hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }

            return new String(hexChars);
        }
    }

    private static class EmptyValueDisplay extends ValueDisplay {

        private EmptyValueDisplay(StoredEntry entry) {
            super(entry, ValueType.EMPTY);
        }

        @Override
        public void setValueFromEntry(Value value) {

        }
    }
}
