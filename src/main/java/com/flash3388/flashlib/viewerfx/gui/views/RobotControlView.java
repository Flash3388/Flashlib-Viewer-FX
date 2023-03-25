package com.flash3388.flashlib.viewerfx.gui.views;

import com.beans.Property;
import com.flash3388.flashlib.net.hfcs.HfcsRegistry;
import com.flash3388.flashlib.net.hfcs.ping.HfcsPing;
import com.flash3388.flashlib.robot.hfcs.control.HfcsRobotControl;
import com.flash3388.flashlib.robot.hfcs.control.RobotControlData;
import com.flash3388.flashlib.robot.hfcs.state.HfcsRobotState;
import com.flash3388.flashlib.robot.hfcs.state.RobotStateData;
import com.flash3388.flashlib.time.Time;
import com.flash3388.flashlib.util.unique.InstanceId;
import com.flash3388.flashlib.viewerfx.FlashLibServices;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class RobotControlView extends AbstractView {

    private static final Time PING_INTERVAL = Time.seconds(5);
    private static final Time CONTROL_INTERVAL = Time.seconds(1);

    private final HfcsRegistry mHfcsRegistry;
    private final Map<InstanceId, InstanceNode> mNodes;
    private final FlowPane mRoot;

    public RobotControlView(FlashLibServices services) {
        mHfcsRegistry = services.getHfcsService();
        mNodes = new HashMap<>();
        mRoot = new FlowPane();
        setCenter(mRoot);

        HfcsRobotState.registerReceiver(mHfcsRegistry, this::updateInstanceRobotState);
        HfcsPing.registerSender(mHfcsRegistry, services.getClock(), PING_INTERVAL);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void close() {

    }

    private void updateInstanceRobotState(InstanceId instanceId, RobotStateData robotStateData) {
        Platform.runLater(()-> {
            InstanceNode node = mNodes.get(instanceId);
            if (node == null) {
                node = createNewInstance(instanceId);
            }

            node.updateRobotState(robotStateData);
        });
    }

    private InstanceNode createNewInstance(InstanceId instanceId) {
        Property<RobotControlData> controlDataProperty = HfcsRobotControl.registerProvider(
                mHfcsRegistry,
                CONTROL_INTERVAL,
                instanceId);

        InstanceNode node = new InstanceNode(instanceId, controlDataProperty);
        mNodes.put(instanceId, node);
        mRoot.getChildren().add(node);

        return node;
    }

    private static class InstanceNode extends AnchorPane {

        private final Property<RobotControlData> mControlDataProperty;
        private final Label mMode;
        private final Label mTime;

        public InstanceNode(InstanceId instanceId, Property<RobotControlData> controlDataProperty) {
            mControlDataProperty = controlDataProperty;

            VBox root = new VBox();
            getChildren().add(root);
            setTopAnchor(root, 0D);
            setBottomAnchor(root, 0D);
            setLeftAnchor(root, 0D);
            setRightAnchor(root, 0D);

            Label label = new Label(instanceId.toString());
            HBox instanceIdBox = new HBox();
            instanceIdBox.getChildren().add(label);
            root.getChildren().add(instanceIdBox);

            mMode = new Label("");
            mTime = new Label("");
            root.getChildren().addAll(mMode, mTime);
        }

        public void updateRobotState(RobotStateData data) {
            mMode.setText(data.getCurrentMode().getName());
            mTime.setText(data.getClockTime().valueAsSeconds() + " seconds");
        }
    }
}
