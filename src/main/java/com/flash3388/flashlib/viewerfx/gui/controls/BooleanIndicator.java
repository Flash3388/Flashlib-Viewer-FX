package com.flash3388.flashlib.viewerfx.gui.controls;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BooleanIndicator extends Pane {

    private final Rectangle mRectangle;

    public BooleanIndicator(String name, double width, double height) {
        mRectangle = new Rectangle();
        mRectangle.setWidth(width);
        mRectangle.setHeight(height);
        mRectangle.setFill(Color.RED);

        VBox root = new VBox();
        root.getChildren().addAll(mRectangle, new Label(name));
        root.setSpacing(5.0);
        root.setAlignment(Pos.CENTER);

        getChildren().add(root);
    }

    public void set(boolean on) {
        mRectangle.setFill(on ? Color.GREENYELLOW : Color.RED);
    }
}
