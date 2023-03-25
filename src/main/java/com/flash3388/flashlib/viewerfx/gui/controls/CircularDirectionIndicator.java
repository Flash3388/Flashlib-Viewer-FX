package com.flash3388.flashlib.viewerfx.gui.controls;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CircularDirectionIndicator extends Pane {

    private final double mRadius;
    private final Canvas mLineCanvas;
    private final Label mValueLabel;

    public CircularDirectionIndicator(String name, double radius) {
        mRadius = radius;

        StackPane graphicalData = new StackPane();

        Circle outerCircle = new Circle(
                graphicalData.getWidth() * 0.5,
                graphicalData.getHeight() * 0.5,
                radius);
        outerCircle.setFill(Color.TRANSPARENT);
        outerCircle.setStroke(Color.BLACK);

        mLineCanvas = new Canvas();
        mLineCanvas.setWidth(116);
        mLineCanvas.setHeight(100);
        mLineCanvas.getGraphicsContext2D().strokeLine(
                mLineCanvas.getWidth() * 0.5,
                mLineCanvas.getHeight() * 0.5,
                mLineCanvas.getWidth() * 0.5 + radius,
                mLineCanvas.getHeight() * 0.5);

        graphicalData.getChildren().addAll(outerCircle, mLineCanvas);

        mValueLabel = new Label("0");

        HBox infoBox = new HBox();
        infoBox.getChildren().addAll(new Label(name + ":"), mValueLabel);
        infoBox.setSpacing(5.0);
        infoBox.setAlignment(Pos.CENTER);

        VBox root = new VBox();
        root.getChildren().addAll(graphicalData, infoBox);
        root.setAlignment(Pos.CENTER);

        getChildren().add(root);
    }

    public void setValue(double value) {
        if (value < 0) {
            clear();
            return;
        }

        mValueLabel.setText(String.format("%.3f", value));

        mLineCanvas.getGraphicsContext2D().clearRect(0, 0,
                mLineCanvas.getWidth(),
                mLineCanvas.getHeight());

        double valueRadians = Math.toRadians(value);
        mLineCanvas.getGraphicsContext2D().strokeLine(
                mLineCanvas.getWidth() * 0.5,
                mLineCanvas.getHeight() * 0.5,
                mLineCanvas.getWidth() * 0.5 + mRadius * Math.cos(valueRadians),
                mLineCanvas.getHeight() * 0.5 + mRadius * Math.sin(valueRadians));
    }

    public void clear() {
        mValueLabel.setText("");
        mLineCanvas.getGraphicsContext2D().clearRect(0, 0,
                mLineCanvas.getWidth(),
                mLineCanvas.getHeight());
    }
}
