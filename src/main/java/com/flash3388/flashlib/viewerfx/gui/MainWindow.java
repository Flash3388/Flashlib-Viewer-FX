package com.flash3388.flashlib.viewerfx.gui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindow implements AutoCloseable {

    private final double mWidth;
    private final double mHeight;

    private final Stage mOwner;
    private final BorderPane mRoot;

    public MainWindow(Stage owner, double width, double height) {
        mOwner = owner;
        mWidth = width;
        mHeight = height;
        mRoot = new BorderPane();
    }

    public Scene createScene() {
        return new Scene(mRoot, mWidth, mHeight);
    }

    public void updateClients() {

    }

    @Override
    public void close() throws Exception {

    }
}
