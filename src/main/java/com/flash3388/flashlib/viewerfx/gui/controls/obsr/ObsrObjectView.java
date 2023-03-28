package com.flash3388.flashlib.viewerfx.gui.controls.obsr;

import com.beans.observables.RegisteredListener;
import javafx.application.Platform;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

public class ObsrObjectView extends BorderPane implements AutoCloseable {

    private final TreeView<NodeBase> mTreeView;
    private final RegisteredListener mListener;

    public ObsrObjectView(ObjectNode rootItem) {
        mTreeView = new TreeView<>();
        mTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        mTreeView.setEditable(false);
        mTreeView.setRoot(rootItem);
        setCenter(mTreeView);

        mListener = rootItem.getObject().addListener((event)-> {
            Platform.runLater(()-> {
                rootItem.handleChangeEvent(event);
            });
        });
    }

    @Override
    public void close() {
        mListener.remove();
    }
}
