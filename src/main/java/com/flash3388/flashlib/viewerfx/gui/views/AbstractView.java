package com.flash3388.flashlib.viewerfx.gui.views;

import javafx.scene.layout.BorderPane;

public abstract class AbstractView extends BorderPane implements AutoCloseable {

    public abstract void updateView();
    public abstract void close();
}
