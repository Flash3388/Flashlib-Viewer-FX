package com.flash3388.flashlib.viewerfx.gui.controls;

import com.beans.Property;
import com.flash3388.flashlib.robot.hfcs.control.RobotControlData;
import com.flash3388.flashlib.robot.hfcs.state.RobotStateData;
import com.flash3388.flashlib.robot.modes.RobotMode;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.time.Time;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class RobotControlOpView extends VBox {

    private static final Time CHANGE_MODE_TIMEOUT = Time.seconds(1);

    private final Property<RobotControlData> mControlData;
    private final Clock mClock;

    private RobotMode mSelectedMode = null;

    private final RobotModeList mModeList;
    private final Button mUseSelectedMode;
    private final Button mUseDisabledMode;

    private Time mChangedModeAt;
    private RobotMode mWantedMode;
    private RobotMode mActualMode;
    private boolean mStatusOk = false;

    public RobotControlOpView(Property<RobotControlData> controlData, Clock clock) {
        mControlData = controlData;
        mClock = clock;

        mUseSelectedMode = new Button("Use Selected");
        mUseDisabledMode = new Button("Use Disabled");

        mUseSelectedMode.setOnAction((e)-> {
            if (mSelectedMode == null) {
                return;
            }

            setMode(mSelectedMode);
        });
        mUseDisabledMode.setOnAction((e)-> {
            setMode(RobotMode.DISABLED);
        });

        setMode(RobotMode.DISABLED);
        mUseSelectedMode.setDisable(true);
        mUseDisabledMode.setDisable(true);
        setStatusOk();

        mModeList = new RobotModeList(new RobotModeList.Listener() {
            @Override
            public void onModeAdded(RobotMode mode) {

            }

            @Override
            public void onModeRemoved(RobotMode mode) {
                if (mode.equals(mSelectedMode)) {
                    mSelectedMode = null;
                    mUseSelectedMode.setDisable(true);
                    setMode(RobotMode.DISABLED);
                }
            }

            @Override
            public void onModeSelectionChanged(RobotMode mode) {
                mSelectedMode = mode;
                mUseSelectedMode.setDisable(mSelectedMode == null);
                setMode(RobotMode.DISABLED);
            }
        });

        HBox box = new HBox();
        box.setPadding(new Insets(5));
        box.setSpacing(0);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(mUseSelectedMode, mUseDisabledMode);

        getChildren().addAll(box, mModeList);
    }

    public void update(Time now) {
        if (mWantedMode.equals(mActualMode)) {
            // mode is as wanted
            setStatusOk();
        } else if (now.sub(mChangedModeAt).after(CHANGE_MODE_TIMEOUT)) {
            // mode didn't change like wanted
            setStatusError();
        }
    }

    public void updateRobotState(RobotStateData data, Time now) {
        mActualMode = data.getCurrentMode();
    }

    private void setMode(RobotMode mode) {
        mControlData.set(new RobotControlData(mode));
        mUseSelectedMode.setDisable(!mode.equals(RobotMode.DISABLED));
        mUseDisabledMode.setDisable(mode.equals(RobotMode.DISABLED));

        mWantedMode = mode;
        mChangedModeAt = mClock.currentTime();
    }

    private void setStatusOk() {
        if (mStatusOk) {
            return;
        }
        mStatusOk = true;

        setBorder(new Border(
                new BorderStroke(
                        Color.GREENYELLOW,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        BorderStroke.MEDIUM)
        ));
    }

    private void setStatusError() {
        if (!mStatusOk) {
            return;
        }
        mStatusOk = false;

        setBorder(new Border(
                new BorderStroke(
                        Color.RED,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        BorderStroke.MEDIUM)
        ));
    }
}
