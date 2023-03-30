package com.flash3388.flashlib.viewerfx.gui.controls;

import com.beans.Property;
import com.flash3388.flashlib.robot.hfcs.control.RobotControlData;
import com.flash3388.flashlib.robot.hfcs.state.RobotStateData;
import com.flash3388.flashlib.robot.modes.RobotMode;
import com.flash3388.flashlib.time.Time;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RobotControlOpView extends VBox {

    private final Property<RobotControlData> mControlData;

    private RobotMode mSelectedMode = null;

    private final RobotModeList mModeList;
    private final Button mUseSelectedMode;
    private final Button mUseDisabledMode;

    public RobotControlOpView(Property<RobotControlData> controlData) {
        mControlData = controlData;

        mUseSelectedMode = new Button("Use Selected");
        mUseDisabledMode = new Button("Use Disabled");

        mUseSelectedMode.setOnAction((e)-> {
            if (mSelectedMode == null) {
                return;
            }
            mControlData.set(new RobotControlData(mSelectedMode));
            mUseSelectedMode.setDisable(true);
            mUseDisabledMode.setDisable(false);
        });
        mUseDisabledMode.setOnAction((e)-> {
            mControlData.set(new RobotControlData(RobotMode.DISABLED));
            mUseSelectedMode.setDisable(false);
            mUseDisabledMode.setDisable(true);
        });

        mControlData.set(new RobotControlData(RobotMode.DISABLED));
        mUseSelectedMode.setDisable(true);
        mUseDisabledMode.setDisable(true);

        mModeList = new RobotModeList(new RobotModeList.Listener() {
            @Override
            public void onModeAdded(RobotMode mode) {

            }

            @Override
            public void onModeRemoved(RobotMode mode) {
                if (mode.equals(mSelectedMode)) {
                    mSelectedMode = null;
                    mUseSelectedMode.setDisable(true);
                    mControlData.set(new RobotControlData(RobotMode.DISABLED));
                }
            }

            @Override
            public void onModeSelectionChanged(RobotMode mode) {
                mSelectedMode = mode;
                mUseSelectedMode.setDisable(mSelectedMode == null);
                mControlData.set(new RobotControlData(RobotMode.DISABLED));
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

    }

    public void updateRobotState(RobotStateData data, Time now) {

    }
}
