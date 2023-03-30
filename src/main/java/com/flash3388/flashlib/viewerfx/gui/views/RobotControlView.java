package com.flash3388.flashlib.viewerfx.gui.views;

import com.beans.Property;
import com.flash3388.flashlib.net.hfcs.HfcsRegistry;
import com.flash3388.flashlib.net.hfcs.ping.HfcsPing;
import com.flash3388.flashlib.robot.hfcs.control.HfcsRobotControl;
import com.flash3388.flashlib.robot.hfcs.control.RobotControlData;
import com.flash3388.flashlib.robot.hfcs.state.HfcsRobotState;
import com.flash3388.flashlib.robot.hfcs.state.RobotStateData;
import com.flash3388.flashlib.robot.modes.RobotMode;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.time.Time;
import com.flash3388.flashlib.util.unique.InstanceId;
import com.flash3388.flashlib.viewerfx.services.hfcs.HfcsService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class RobotControlView extends AbstractView {

    private static final Time PING_INTERVAL = Time.seconds(5);
    private static final Time CONTROL_INTERVAL = Time.seconds(1);

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
        mHfcsRegistry = registry;
        HfcsRobotState.registerReceiver(registry, this::updateInstanceRobotState);
        HfcsPing.registerSender(registry, mClock, PING_INTERVAL);

        for (Map.Entry<InstanceId, InstanceNode> entry : mNodes.entrySet()) {
            Property<RobotControlData> controlDataProperty =
                    HfcsRobotControl.registerProvider(registry, CONTROL_INTERVAL, entry.getKey());
            entry.getValue().setControlDataProperty(controlDataProperty);
        }
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
        InstanceNode node = new InstanceNode(instanceId);
        mNodes.put(instanceId, node);
        mInstancesList.getItems().add(instanceId);

        Property<RobotControlData> controlDataProperty =
                HfcsRobotControl.registerProvider(mHfcsRegistry, CONTROL_INTERVAL, instanceId);
        node.setControlDataProperty(controlDataProperty);

        return node;
    }

    private void replaceDisplayedNode(InstanceId instanceId) {
        InstanceNode node = mNodes.get(instanceId);
        mNodeDisplayRoot.getChildren().clear();
        mNodeDisplayRoot.getChildren().add(node);
    }

    private static class InstanceNode extends AnchorPane {

        private static final Time TIMEOUT = Time.seconds(5);

        private final Label mCurrentModeLbl;
        private final Label mUpTime;
        private Time mLastUpdated;
        private boolean mIsTimedOut;
        private RobotMode mLastMode;
        private Property<RobotControlData> mControlData;

        public InstanceNode(InstanceId instanceId) {
            VBox root = new VBox();
            getChildren().add(root);

            setTopAnchor(root, 0D);
            setBottomAnchor(root, 0D);
            setLeftAnchor(root, 0D);
            setRightAnchor(root, 0D);

            mLastMode = null;
            mIsTimedOut = false;
            setStatusDisabled();

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

        public void setControlDataProperty(Property<RobotControlData> controlDataProperty) {
            mControlData = controlDataProperty;
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
}
