package com.flash3388.flashlib.viewerfx.gui;

import com.castle.exceptions.ServiceException;
import com.castle.util.closeables.Closeables;
import com.flash3388.flashlib.viewerfx.gui.views.ConfigView;
import com.flash3388.flashlib.viewerfx.gui.views.NtSchedulerView;
import com.flash3388.flashlib.viewerfx.gui.views.StreamView;
import com.flash3388.flashlib.viewerfx.services.FlashLibServices;
import com.flash3388.flashlib.viewerfx.gui.views.AbstractView;
import com.flash3388.flashlib.viewerfx.gui.views.RobotControlView;
import com.flash3388.flashlib.viewerfx.gui.views.JoystickView;
import com.flash3388.flashlib.viewerfx.gui.views.ObsrView;
import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

public class MainWindow extends AbstractView {

    private final double mWidth;
    private final double mHeight;

    private final Stage mOwner;
    private final BorderPane mRoot;

    private final ObsrView mObsrView;
    private final RobotControlView mRobotControlView;
    private final JoystickView mJoystickView;
    private final ConfigView mConfigView;
    private final StreamView mStreamView;
    private final NtSchedulerView mNtSchedulerView;

    private final AtomicReference<AbstractView> mSelectedView;

    public MainWindow(Stage owner, double width, double height, FlashLibServices services) throws ServiceException {
        mOwner = owner;
        mWidth = width;
        mHeight = height;
        mRoot = new BorderPane();

        mObsrView = new ObsrView(services.getObsrService());
        mRobotControlView = new RobotControlView(services.getHfcsService(), services.getClock());
        mJoystickView = new JoystickView(services.getHfcsService());
        mConfigView = new ConfigView(services);
        mStreamView = new StreamView();
        mNtSchedulerView = new NtSchedulerView(services.getNtInstance());

        mSelectedView = new AtomicReference<>();
    }

    public Scene createScene() {
        Tab obsrTab = new Tab("OBSR");
        obsrTab.setContent(mObsrView);
        obsrTab.setClosable(false);

        Tab instancesTab = new Tab("Robot Control");
        instancesTab.setContent(mRobotControlView);
        instancesTab.setClosable(false);

        Tab joysticksTab = new Tab("Joystick");
        joysticksTab.setContent(mJoystickView);
        joysticksTab.setClosable(false);

        Tab configTab = new Tab("Config");
        configTab.setContent(mConfigView);
        configTab.setClosable(false);

        Tab streamTab = new Tab("Streams");
        streamTab.setContent(mStreamView);
        streamTab.setClosable(false);

        Tab ntSchedulerView = new Tab("NT Scheduler");
        ntSchedulerView.setContent(mNtSchedulerView);
        ntSchedulerView.setClosable(false);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(obsrTab, instancesTab, joysticksTab, configTab, streamTab, ntSchedulerView);
        tabPane.setSide(Side.LEFT);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            mSelectedView.set((AbstractView) n.getContent());
        });

        mSelectedView.set(mObsrView);
        tabPane.getSelectionModel().select(0);

        mRoot.setCenter(tabPane);
        return new Scene(mRoot, mWidth, mHeight);
    }

    public void updateView() {
        AbstractView view = mSelectedView.get();
        if (view != null) {
            Platform.runLater(view::updateView);
        }
    }

    @Override
    public void close() {
        Closeables.silentClose(mJoystickView);
        Closeables.silentClose(mStreamView);
    }
}
