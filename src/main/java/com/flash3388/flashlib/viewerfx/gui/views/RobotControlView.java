package com.flash3388.flashlib.viewerfx.gui.views;

import com.beans.Property;
import com.flash3388.flashlib.net.hfcs.HfcsRegistry;
import com.flash3388.flashlib.net.hfcs.ping.HfcsPing;
import com.flash3388.flashlib.robot.hfcs.control.HfcsRobotControl;
import com.flash3388.flashlib.robot.hfcs.control.RobotControlData;
import com.flash3388.flashlib.robot.hfcs.state.HfcsRobotState;
import com.flash3388.flashlib.robot.hfcs.state.RobotStateData;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.time.Time;
import com.flash3388.flashlib.util.unique.InstanceId;
import com.flash3388.flashlib.viewerfx.gui.controls.RobotControlOpView;
import com.flash3388.flashlib.viewerfx.gui.controls.RobotStatusView;
import com.flash3388.flashlib.viewerfx.services.hfcs.HfcsService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class RobotControlView extends AbstractView {

    private static final Time PING_INTERVAL = Time.seconds(5);
    private static final Time CONTROL_INTERVAL = Time.milliseconds(200);

    private final Clock mClock;
    private final Map<InstanceId, InstanceNode> mNodes;
    private final Pane mNodeDisplayRoot;
    private final ListView<InstanceId> mInstancesList;

    private HfcsRegistry mHfcsRegistry;

    public RobotControlView(HfcsService service, Clock clock) {
        mClock = clock;

        service.serviceProperty().addListener((obs, o, n)-> {
            refreshService(n);
        });

        mNodes = new HashMap<>();

        mInstancesList = new ListView<>();
        mInstancesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        mInstancesList.setEditable(false);
        mInstancesList.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            replaceDisplayedNode(n);
        });

        VBox root = new VBox();
        root.setSpacing(5);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(5));
        mNodeDisplayRoot = root;

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(mInstancesList, mNodeDisplayRoot);
        setCenter(splitPane);
    }

    @Override
    public synchronized void updateView() {
        Time now = mClock.currentTime();
        for (InstanceNode node : mNodes.values()) {
            node.update(now);
        }
    }

    @Override
    public void close() {

    }

    private synchronized void refreshService(HfcsRegistry registry) {
        mNodes.clear();
        mInstancesList.getItems().clear();

        mHfcsRegistry = registry;
        HfcsRobotState.registerReceiver(registry, this::updateInstanceRobotState);
        HfcsPing.registerSender(registry, mClock, PING_INTERVAL);
    }

    private void updateInstanceRobotState(InstanceId instanceId, RobotStateData robotStateData) {
        Platform.runLater(()-> {
            synchronized (this) {
                InstanceNode node = mNodes.get(instanceId);
                if (node == null) {
                    node = createNewInstance(instanceId);
                }

                node.updateRobotState(robotStateData, mClock.currentTime());
            }
        });
    }

    private InstanceNode createNewInstance(InstanceId instanceId) {
        Property<RobotControlData> controlDataProperty =
                HfcsRobotControl.registerProvider(mHfcsRegistry, CONTROL_INTERVAL, instanceId);
        InstanceNode node = new InstanceNode(instanceId, mClock, controlDataProperty);
        mNodes.put(instanceId, node);
        mInstancesList.getItems().add(instanceId);

        return node;
    }

    private void replaceDisplayedNode(InstanceId instanceId) {
        InstanceNode node = mNodes.get(instanceId);
        mNodeDisplayRoot.getChildren().clear();
        mNodeDisplayRoot.getChildren().add(node);
    }

    private static class InstanceNode extends BorderPane {

        private final RobotStatusView mStatusView;
        private final RobotControlOpView mControlOpView;

        public InstanceNode(InstanceId instanceId,
                            Clock clock,
                            Property<RobotControlData> controlDataProperty) {
            mStatusView = new RobotStatusView(instanceId);
            setTop(mStatusView);

            mControlOpView = new RobotControlOpView(controlDataProperty, clock);
            setCenter(mControlOpView);
        }

        public void update(Time now) {
            mStatusView.update(now);
            mControlOpView.update(now);
        }

        public void updateRobotState(RobotStateData data, Time now) {
            mStatusView.updateRobotState(data, now);
            mControlOpView.updateRobotState(data, now);
        }
    }
}
