package com.flash3388.flashlib.viewerfx.gui.views;

import com.castle.concurrent.service.TerminalService;
import com.castle.exceptions.ServiceException;
import com.flash3388.flashlib.hid.sdl2.hfcs.Sdl2HfcsHid;
import com.flash3388.flashlib.robot.hfcs.hid.HfcsHid;
import com.flash3388.flashlib.robot.hfcs.hid.HidData;
import com.flash3388.flashlib.robot.hfcs.hid.RawHidData;
import com.flash3388.flashlib.time.Time;
import com.flash3388.flashlib.viewerfx.FlashLibServices;
import com.flash3388.flashlib.viewerfx.gui.controls.AxisIndicator;
import com.flash3388.flashlib.viewerfx.gui.controls.BooleanIndicator;
import com.flash3388.flashlib.viewerfx.gui.controls.CircularDirectionIndicator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class JoystickView extends AbstractView {

    private final TerminalService mHidService;
    private final HidData mHidData;

    private final JoystickNode[] mNodes;

    public JoystickView(FlashLibServices services) throws ServiceException {
        mHidData = HfcsHid.createProvider(services.getHfcsService(), Time.seconds(1));
        mHidService = Sdl2HfcsHid.initialize(mHidData);
        mHidService.start();

        VBox pane = new VBox();
        pane.setSpacing(20);
        pane.setPadding(new Insets(5));
        mNodes = new JoystickNode[RawHidData.MAX_HID];
        for (int i = 0; i < mNodes.length; i++) {
            mNodes[i] = new JoystickNode(i);
            pane.getChildren().add(mNodes[i]);
        }

        setCenter(pane);
    }

    @Override
    public void updateView() {
        for (int i = 0; i < mNodes.length; i++) {
            if (mHidData.hasChannel(i)) {
                mNodes[i].update(mHidData);
                mNodes[i].show();
            } else {
                mNodes[i].hide();
            }
        }
    }

    @Override
    public void close() {
        mHidService.close();
    }

    private static class JoystickNode extends AnchorPane {

        private final int mHid;
        private final AxisIndicator[] mAxes;
        private final BooleanIndicator[] mButtons;
        private final CircularDirectionIndicator[] mPovs;

        private boolean mIsHidden;

        public JoystickNode(int hid) {
            mHid = hid;
            mAxes = new AxisIndicator[RawHidData.MAX_AXES];
            mButtons = new BooleanIndicator[RawHidData.MAX_BUTTONS];
            mPovs = new CircularDirectionIndicator[RawHidData.MAX_POVS];
            mIsHidden = false;

            FlowPane axesPane = new FlowPane();
            axesPane.setHgap(5);
            axesPane.setVgap(5);
            for (int i = 0; i < mAxes.length; i++) {
                mAxes[i] = new AxisIndicator(String.valueOf(i), 100, 50);
                axesPane.getChildren().add(mAxes[i]);
            }

            FlowPane buttonsPane = new FlowPane();
            buttonsPane.setHgap(2);
            buttonsPane.setVgap(2);
            for (int i = 0; i < mButtons.length; i++) {
                mButtons[i] = new BooleanIndicator(String.valueOf(i+1), 20, 10);
                buttonsPane.getChildren().add(mButtons[i]);
            }

            FlowPane povsPane = new FlowPane();
            povsPane.setHgap(2);
            povsPane.setVgap(2);
            for (int i = 0; i < mPovs.length; i++) {
                mPovs[i] = new CircularDirectionIndicator(String.valueOf(i), 20);
                povsPane.getChildren().add(mPovs[i]);
            }

            VBox buttonsAndPovs = new VBox();
            buttonsAndPovs.getChildren().addAll(buttonsPane, povsPane);
            buttonsAndPovs.setSpacing(5);

            Label nameLbl = new Label(String.valueOf(mHid));
            nameLbl.setPadding(new Insets(5));
            HBox box = new HBox();
            box.setSpacing(2);
            box.setAlignment(Pos.CENTER);
            box.getChildren().addAll(nameLbl, axesPane, buttonsAndPovs);

            getChildren().add(box);
        }

        public void update(HidData data) {
            for (int i = 0; i < mAxes.length; i++) {
                mAxes[i].set(data.getAxisValue(mHid, i));
            }

            for (int i = 0; i < mButtons.length; i++) {
                mButtons[i].set(data.getButtonValue(mHid, i));
            }

            for (int i = 0; i < mPovs.length; i++) {
                mPovs[i].setValue(data.getPovValue(mHid, i));
            }
        }

        public void reset() {
            for (int i = 0; i < mAxes.length; i++) {
                mAxes[i].set(0);
            }

            for (int i = 0; i < mButtons.length; i++) {
                mButtons[i].set(false);
            }

            for (int i = 0; i < mPovs.length; i++) {
                mPovs[i].setValue(-1);
            }
        }

        public void show() {
            if(!mIsHidden) {
                return;
            }

            setDisable(false);
            setVisible(true);
            mIsHidden = false;
        }

        public void hide() {
            if(mIsHidden) {
                return;
            }

            reset();
            setDisable(true);
            setVisible(false);
            mIsHidden = true;
        }
    }
}
