package com.flash3388.flashlib.viewerfx.gui.controls;

import com.flash3388.flashlib.robot.modes.RobotMode;
import com.flash3388.flashlib.viewerfx.gui.Dialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class RobotModeList extends VBox {

    public interface Listener {
        void onModeAdded(RobotMode mode);
        void onModeRemoved(RobotMode mode);
        void onModeSelectionChanged(RobotMode mode);
    }

    private final ListView<RobotMode> mList;

    public RobotModeList(Listener listener) {
        mList = new ListView<>();
        mList.setEditable(false);
        mList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        Button add = new Button("Add");
        add.setOnAction((e)-> {
            Optional<RobotMode> newMode = showInputDialog();
            if (newMode.isPresent()) {
                RobotMode mode = newMode.get();
                mList.getItems().add(mode);
                listener.onModeAdded(mode);
            }
        });
        Button remove = new Button("Remove");
        remove.setOnAction((e)-> {
            RobotMode selected = mList.getSelectionModel().getSelectedItem();
            mList.getItems().remove(selected);

            listener.onModeRemoved(selected);
        });

        mList.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            remove.setDisable(n == null);
            listener.onModeSelectionChanged(n);
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setSpacing(5);
        buttonBox.setPadding(new Insets(2));
        buttonBox.getChildren().addAll(add, remove);

        getChildren().addAll(mList, buttonBox);
    }

    private static Optional<RobotMode> showInputDialog() {
        NumericField keyField = new NumericField(Integer.class);
        TextField nameField = new TextField();
        CheckBox isDisabledField = new CheckBox();

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("Key"), 0, 0);
        gridPane.add(keyField, 1, 0);
        gridPane.add(new Label("Name"), 0, 1);
        gridPane.add(nameField, 1, 1);
        gridPane.add(new Label("Is Disabled"), 0, 2);
        gridPane.add(isDisabledField, 1, 2);

        if (!Dialogs.showCustomApplyDialog(gridPane)) {
            // no save
            return Optional.empty();
        }

        RobotMode robotMode = RobotMode.create(
                nameField.getText(),
                keyField.valueProperty().getValue().intValue(),
                isDisabledField.isSelected());
        return Optional.of(robotMode);
    }
}
