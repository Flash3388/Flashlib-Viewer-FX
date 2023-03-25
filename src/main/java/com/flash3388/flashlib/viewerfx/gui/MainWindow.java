package com.flash3388.flashlib.viewerfx.gui;

import com.castle.exceptions.ServiceException;
import com.flash3388.flashlib.viewerfx.FlashLibServices;
import com.flash3388.flashlib.viewerfx.gui.views.AbstractView;
import com.flash3388.flashlib.viewerfx.gui.views.InstancesView;
import com.flash3388.flashlib.viewerfx.gui.views.JoystickView;
import com.flash3388.flashlib.viewerfx.gui.views.ObsrView;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

public class MainWindow implements AutoCloseable {

    private final double mWidth;
    private final double mHeight;

    private final Stage mOwner;
    private final BorderPane mRoot;

    private final ObsrView mObsrView;
    private final InstancesView mInstancesView;
    private final JoystickView mJoystickView;

    private final AtomicReference<AbstractView> mSelectedView;

    public MainWindow(Stage owner, double width, double height, FlashLibServices services) throws ServiceException {
        mOwner = owner;
        mWidth = width;
        mHeight = height;
        mRoot = new BorderPane();

        mObsrView = new ObsrView(services);
        mInstancesView = new InstancesView(services);
        mJoystickView = new JoystickView(services);

        mSelectedView = new AtomicReference<>();
    }

    public Scene createScene() {
        Tab obsrTab = new Tab("OBSR");
        obsrTab.setContent(mObsrView);
        obsrTab.setClosable(false);

        Tab instancesTab = new Tab("Instances");
        instancesTab.setContent(mInstancesView);
        instancesTab.setClosable(false);

        Tab joysticksTab = new Tab("Joystick");
        joysticksTab.setContent(mJoystickView);
        joysticksTab.setClosable(false);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(obsrTab, instancesTab, joysticksTab);
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
    public void close() throws Exception {
        mJoystickView.close();
    }
}
