package com.flash3388.flashlib.viewerfx.gui.controls.obsr;

import com.flash3388.flashlib.net.obsr.EntryModificationEvent;
import com.flash3388.flashlib.net.obsr.ModificationType;
import com.flash3388.flashlib.net.obsr.StoragePath;
import com.flash3388.flashlib.net.obsr.StoredEntry;
import com.flash3388.flashlib.net.obsr.StoredObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ObjectNode extends NodeBase {

    private final String mName;
    private final StoredObject mObject;
    private final ObjectNode mParent;
    private final String mFullPath;
    private final Map<String, EntryNode> mEntries;
    private final Map<String, ObjectNode> mChildren;

    public ObjectNode(String name, StoredObject object, ObjectNode parent, String fullPath) {
        mName = name;
        mObject = object;
        mParent = parent;
        mFullPath = fullPath;
        mEntries = new HashMap<>();
        mChildren = new HashMap<>();

        setValue(this);
        setExpanded(true);
    }

    public StoredObject getObject() {
        return mObject;
    }

    public String getFullPath() {
        return mFullPath;
    }

    public ObjectNode getChildByName(String name) {
        return mChildren.get(name);
    }

    public EntryNode getEntryByName(String name) {
        return mEntries.get(name);
    }

    public void handleChangeEvent(EntryModificationEvent event) {
        EntryNode node = updatePath(event.getPath());
        if (event.getType() != ModificationType.DELETE) {
            node.updateValue();
        } else {
            node.remove();
        }
    }

    public ObjectNode loadChild(String name, String fullPath) {
        if (mChildren.containsKey(name)) {
            return mChildren.get(name);
        }

        StoredObject object = mObject.getChild(name);
        ObjectNode node = new ObjectNode(name, object, this, fullPath);
        getChildren().add(node);
        mChildren.put(name, node);

        return node;
    }

    public EntryNode loadEntry(String name, String fullPath) {
        if (mEntries.containsKey(name)) {
            return mEntries.get(name);
        }

        StoredEntry entry = mObject.getEntry(name);
        EntryNode node = new EntryNode(name, entry, this, fullPath);
        getChildren().add(node);
        mEntries.put(name, node);

        return node;
    }

    public void removeEntry(EntryNode node) {
        mEntries.remove(node.getName());
        getChildren().remove(node);

        if (mEntries.isEmpty() && mParent != null) {
            mParent.removeChild(this);
        }
    }

    private void removeChild(ObjectNode node) {
        mChildren.remove(node.mName);
        getChildren().remove(node);

        if (mEntries.isEmpty() && mChildren.isEmpty() && mParent != null) {
            mParent.removeChild(this);
        }
    }

    private EntryNode updatePath(String path) {
        StoragePath storagePath = StoragePath.create(path);
        ObjectNode parentNode = this;

        String currentPath = "/";
        for (Iterator<String> it = storagePath.iterator(); it.hasNext();) {
            String name = it.next();
            currentPath = currentPath.concat("/" + currentPath);

            if (!currentPath.startsWith(mFullPath)) {
                continue;
            }

            if (it.hasNext()) {
                // object name
                ObjectNode node = parentNode.getChildByName(name);
                if (node == null) {
                    node = parentNode.loadChild(name, currentPath);
                }

                parentNode = node;
            } else {
                // entry name
                return parentNode.loadEntry(name, currentPath);
            }
        }

        throw new AssertionError("should have pointed at an entry");
    }

    @Override
    public String toString() {
        return mName;
    }
}
