package com.flash3388.flashlib.viewerfx.gui.views;

import com.beans.observables.RegisteredListener;
import com.flash3388.flashlib.net.obsr.ObjectStorage;
import com.flash3388.flashlib.net.obsr.StoredObject;
import com.flash3388.flashlib.viewerfx.gui.controls.obsr.EntryNode;
import com.flash3388.flashlib.viewerfx.gui.controls.obsr.NodeBase;
import com.flash3388.flashlib.viewerfx.gui.controls.obsr.ObjectNode;
import com.flash3388.flashlib.viewerfx.gui.controls.obsr.ObsrEntryControl;
import com.flash3388.flashlib.viewerfx.gui.controls.obsr.ObsrObjectView;
import com.flash3388.flashlib.viewerfx.services.obsr.ObsrService;
import javafx.application.Platform;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ObsrView extends AbstractView {

    private final TreeView<NodeBase> mTreeView;
    private ObjectNode mRootItem;

    private ObsrObjectView mObjectView;
    private ObsrEntryControl mEntryControl;
    private RegisteredListener mListener;

    public ObsrView(ObsrService service) {
        Pane dataPane = new VBox();

        mTreeView = new TreeView<>();
        mTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        mTreeView.setEditable(false);
        mTreeView.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            dataPane.getChildren().clear();

            close();

            if (n instanceof ObjectNode) {
                ObjectNode node = (ObjectNode) n;
                mObjectView = new ObsrObjectView(node);
                dataPane.getChildren().add(mObjectView);
            } else {
                EntryNode entryNode = (EntryNode) n;
                mEntryControl = new ObsrEntryControl(entryNode.getName(), entryNode.getEntry());
                dataPane.getChildren().add(mEntryControl);
            }
        });


        SplitPane mainPane = new SplitPane();
        setCenter(mainPane);
        mainPane.getItems().addAll(mTreeView, dataPane);

        service.serviceProperty().addListener((obs, o, n)-> {
            refreshService(n);
        });
    }

    public void updateView() {
        if (mEntryControl != null) {
            mEntryControl.updateValue();
        }
    }

    @Override
    public void close() {
        if (mObjectView != null) {
            mObjectView.close();
            mObjectView = null;
        }
        if (mEntryControl != null) {
            mEntryControl.close();
            mEntryControl = null;
        }
        if (mListener != null) {
            mListener.remove();
            mListener = null;
        }
    }

    private synchronized void refreshService(ObjectStorage obsr) {
        close();

        StoredObject root = obsr.getRoot();
        mRootItem = new ObjectNode("ROOT", root, null, "/");
        mTreeView.setRoot(mRootItem);

        mListener = root.addListener((event)-> {
            Platform.runLater(()-> {
                mRootItem.handleChangeEvent(event);
            });
        });
    }
}
