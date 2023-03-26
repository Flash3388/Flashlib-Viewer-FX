package com.flash3388.flashlib.viewerfx.gui.controls;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class AxisIndicator extends Pane {

    private final Slider mSlider;
    private final Label mValueLbl;

    public AxisIndicator(String name, double width, double height) {
        mSlider = new Slider();
        mSlider.setMin(-1);
        mSlider.setMax(1);
        mSlider.setMajorTickUnit(0.1);
        mSlider.setShowTickLabels(true);
        mSlider.setShowTickMarks(true);
        mSlider.setDisable(true);
        mSlider.setPrefSize(width, height);

        mValueLbl = new Label("0.000");

        HBox lblBox = new HBox();
        lblBox.setSpacing(5);
        lblBox.getChildren().addAll(new Label(name), mValueLbl);

        VBox box = new VBox();
        box.getChildren().addAll(lblBox, mSlider);
        box.setSpacing(1);

        setPrefSize(width, height);
        setMaxSize(width, height);
        getChildren().add(box);
    }

    public void set(double value) {
        mValueLbl.setText(String.format("%.3f", value));
        mSlider.setValue(value);
    }
}
