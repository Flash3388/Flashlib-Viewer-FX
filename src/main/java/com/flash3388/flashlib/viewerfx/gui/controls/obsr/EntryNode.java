package com.flash3388.flashlib.viewerfx.gui.controls.obsr;

import com.flash3388.flashlib.net.obsr.StoredEntry;

public class EntryNode extends NodeBase {
    private final String mName;
    private final StoredEntry mEntry;
    private final ObjectNode mParentNode;
    private final String mFullPath;

    public EntryNode(String name, StoredEntry entry, ObjectNode parentNode, String fullPath) {
        mName = name;
        mEntry = entry;
        mParentNode = parentNode;
        mFullPath = fullPath;

        setValue(this);
        setExpanded(true);
    }

    public String getName() {
        return mName;
    }

    public StoredEntry getEntry() {
        return mEntry;
    }

    public void updateValue() {
        setValue(null);
        setValue(this);
    }

    public void remove() {
        mParentNode.removeEntry(this);
    }

    @Override
    public String toString() {
        return String.format("%s = %s", mName, mEntry.getValue());
    }
    
}
