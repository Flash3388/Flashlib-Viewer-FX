package com.flash3388.flashlib.viewerfx.gui.views;

import com.flash3388.flashlib.net.obsr.ObjectStorage;
import com.flash3388.flashlib.net.obsr.StoragePath;
import com.flash3388.flashlib.net.obsr.StoredEntry;
import com.flash3388.flashlib.net.obsr.StoredObject;
import com.flash3388.flashlib.viewerfx.FlashLibServices;
import com.flash3388.flashlib.viewerfx.gui.controls.ObsrEntryControl;
import javafx.application.Platform;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ObsrView extends AbstractView {

    private final TreeView<NodeBase> mTreeView;
    private final ObjectNode mRootItem;

    private ObsrEntryControl mEntryControl;

    public ObsrView(FlashLibServices services) {
        mTreeView = new TreeView<>();
        mTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        mTreeView.setEditable(false);
        mTreeView.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            if (mEntryControl != null) {
                mEntryControl.close();
            }
            if (n instanceof ObjectNode) {
                setCenter(null);
                return;
            }

            EntryNode entryNode = ((EntryNode)n);
            mEntryControl = new ObsrEntryControl(entryNode.getName(), entryNode.getEntry());
            setCenter(mEntryControl);
        });

        ObjectStorage obsr = services.getObsrService();

        StoredObject root = obsr.getRoot();
        mRootItem = new ObjectNode("ROOT", root);
        mTreeView.setRoot(mRootItem);

        obsr = services.getObsrService();
        obsr.getRoot().addListener((event)-> updatePath(event.getPath()));

        setLeft(mTreeView);
    }

    public void updateView() {
        mRootItem.updateChildren();

        if (mEntryControl != null) {
            mEntryControl.updateValue();
        }
    }

    @Override
    public void close() {

    }

    private void updatePath(String path) {
        Platform.runLater(()-> {
            StoragePath storagePath = StoragePath.create(path);
            ObjectNode parentNode = mRootItem;

            for (Iterator<String> it = storagePath.iterator(); it.hasNext();) {
                String name = it.next();

                if (it.hasNext()) {
                    // object name
                    ObjectNode node = parentNode.getChildByName(name);
                    if (node == null) {
                        node = parentNode.loadChild(name);
                    }

                    parentNode = node;
                } else {
                    // entry name
                    parentNode.loadEntry(name);
                }
            }
        });
    }

    private static class NodeBase extends TreeItem<NodeBase> {

    }

    private static class ObjectNode extends NodeBase {

        private final String mName;
        private final StoredObject mObject;
        private final Map<String, EntryNode> mEntries;
        private final Map<String, ObjectNode> mChildren;

        public ObjectNode(String name, StoredObject object) {
            mName = name;
            mObject = object;
            mEntries = new HashMap<>();
            mChildren = new HashMap<>();

            setValue(this);
            setExpanded(true);
        }

        public ObjectNode getChildByName(String name) {
            return mChildren.get(name);
        }

        public EntryNode getEntryByName(String name) {
            return mEntries.get(name);
        }

        public ObjectNode loadChild(String name) {
            if (mChildren.containsKey(name)) {
                return mChildren.get(name);
            }

            StoredObject object = mObject.getChild(name);
            ObjectNode node = new ObjectNode(name, object);
            getChildren().add(node);
            mChildren.put(name, node);

            return node;
        }

        public EntryNode loadEntry(String name) {
            if (mEntries.containsKey(name)) {
                return mEntries.get(name);
            }

            StoredEntry entry = mObject.getEntry(name);
            EntryNode node = new EntryNode(name, entry);
            getChildren().add(node);
            mEntries.put(name, node);

            return node;
        }

        public void updateChildren() {
            for (EntryNode entry : mEntries.values()) {
                entry.updateValue();
            }

            for (ObjectNode child : mChildren.values()) {
                child.updateChildren();
            }
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private static class EntryNode extends NodeBase {
        private final String mName;
        private final StoredEntry mEntry;

        public EntryNode(String name, StoredEntry entry) {
            mName = name;
            mEntry = entry;

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

        @Override
        public String toString() {
            return String.format("%s = %s", mName, mEntry.getValue());
        }
    }
}
