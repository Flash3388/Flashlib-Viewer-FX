package com.flash3388.flashlib.viewerfx.gui.controls;

import com.beans.Property;
import com.flash3388.flashlib.robot.hfcs.control.RobotControlData;
import com.flash3388.flashlib.robot.hfcs.state.RobotStateData;
import com.flash3388.flashlib.robot.modes.RobotMode;
import com.flash3388.flashlib.time.Time;
import com.flash3388.flashlib.util.unique.InstanceId;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class RobotStatusView extends AnchorPane {

    private static final Time TIMEOUT = Time.seconds(5);

    private final Label mCurrentModeLbl;
    private final Label mUpTime;
    private Time mLastUpdated;
    private boolean mIsTimedOut;
    private RobotMode mLastMode;


    public RobotStatusView(InstanceId instanceId) {
        VBox root = new VBox();
        getChildren().add(root);

        AnchorPane.setTopAnchor(root, 0D);
        AnchorPane.setBottomAnchor(root, 0D);
        AnchorPane.setLeftAnchor(root, 0D);
        AnchorPane.setRightAnchor(root, 0D);

        mCurrentModeLbl = new Label("");
        mUpTime = new Label("");

        Label label = new Label(instanceId.toString());
        HBox instanceIdBox = new HBox();
        instanceIdBox.setAlignment(Pos.CENTER);
        instanceIdBox.getChildren().add(label);
        root.getChildren().add(instanceIdBox);

        HBox uptimeBox = new HBox();
        uptimeBox.setSpacing(2);
        uptimeBox.setPadding(new Insets(1));
        uptimeBox.setAlignment(Pos.CENTER_LEFT);
        uptimeBox.getChildren().addAll(new Label("Uptime:"), mUpTime, new Label("seconds"));

        HBox modeBox = new HBox();
        modeBox.setSpacing(2);
        modeBox.setPadding(new Insets(1));
        modeBox.setAlignment(Pos.CENTER_LEFT);
        modeBox.getChildren().addAll(new Label("Current Mode:"), mCurrentModeLbl);

        root.getChildren().addAll(uptimeBox, modeBox);

        mLastMode = null;
        mIsTimedOut = false;
        setStatusDisabled();
    }

    public void update(Time now) {
        if (!mIsTimedOut && now.sub(mLastUpdated).after(TIMEOUT)) {
            mIsTimedOut = true;
            setStatusTimeout();
        }
    }

    public void updateRobotState(RobotStateData data, Time now) {
        if (mIsTimedOut) {
            mIsTimedOut = false;
            setStatusDisabled();
        }

        mUpTime.setText(String.valueOf(data.getClockTime().valueAsSeconds()));
        mLastUpdated = now;

        RobotMode robotMode = data.getCurrentMode();
        if (mLastMode == null || !robotMode.equals(mLastMode)) {
            mLastMode = robotMode;

            mCurrentModeLbl.setText(String.format("%s [%d]",
                    robotMode.getName(), robotMode.getKey()));

            if (robotMode.isDisabled()) {
                setStatusDisabled();
            } else {
                setStatusEnabled();
            }
        }
    }

    private void setStatusEnabled() {
        setBorder(new Border(
                new BorderStroke(
                        Color.GREENYELLOW,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        BorderStroke.MEDIUM)
        ));
        setBackground(new Background(new BackgroundFill(
                Color.GREENYELLOW,
                CornerRadii.EMPTY,
                new Insets(0)
        )));
    }

    private void setStatusDisabled() {
        setBorder(new Border(
                new BorderStroke(
                        Color.RED,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        BorderStroke.MEDIUM)
        ));
        setBackground(new Background(new BackgroundFill(
                Color.GREENYELLOW,
                CornerRadii.EMPTY,
                new Insets(0)
        )));
    }

    private void setStatusTimeout() {
        setBorder(new Border(
                new BorderStroke(
                        Color.RED,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        BorderStroke.MEDIUM)
        ));
        setBackground(new Background(new BackgroundFill(
                Color.RED,
                CornerRadii.EMPTY,
                new Insets(0)
        )));
    }
}
