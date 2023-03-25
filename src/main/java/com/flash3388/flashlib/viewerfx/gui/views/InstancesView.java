package com.flash3388.flashlib.viewerfx.gui.views;

import com.castle.exceptions.ServiceException;
import com.flash3388.flashlib.net.hfcs.impl.HfcsBroadcastService;
import com.flash3388.flashlib.net.hfcs.ping.HfcsPing;
import com.flash3388.flashlib.robot.hfcs.control.HfcsRobotControl;
import com.flash3388.flashlib.robot.hfcs.state.HfcsRobotState;
import com.flash3388.flashlib.robot.hfcs.state.RobotStateData;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.time.SystemMillisClock;
import com.flash3388.flashlib.time.Time;
import com.flash3388.flashlib.util.unique.InstanceId;
import com.flash3388.flashlib.util.unique.InstanceIdGenerator;
import com.flash3388.flashlib.viewerfx.FlashLibServices;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class InstancesView extends AbstractView {

    private final Map<InstanceId, InstanceNode> mNodes;

    private final FlowPane mRoot;

    public InstancesView(FlashLibServices services) {
        mNodes = new HashMap<>();
        mRoot = new FlowPane();
        setCenter(mRoot);

        HfcsRobotState.registerReceiver(services.getHfcsService(), this::updateInstanceRobotState);
        HfcsPing.registerSender(services.getHfcsService(), services.getClock(), Time.seconds(5));
    }

    private void updateInstanceRobotState(InstanceId instanceId, RobotStateData robotStateData) {
        Platform.runLater(()-> {
            InstanceNode node = mNodes.get(instanceId);
            if (node == null) {
                node = new InstanceNode(instanceId);
                mNodes.put(instanceId, node);
                mRoot.getChildren().add(node);
            }

            node.updateRobotState(robotStateData);
        });
    }

    @Override
    public void updateView() {

    }

    @Override
    public void close() {

    }

    private static class InstanceNode extends AnchorPane {

        private final Label mMode;
        private final Label mTime;

        public InstanceNode(InstanceId instanceId) {
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
