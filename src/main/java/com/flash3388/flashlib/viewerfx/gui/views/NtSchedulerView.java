package com.flash3388.flashlib.viewerfx.gui.views;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.Closeable;
import java.io.IOException;

public class NtSchedulerView extends AbstractView {

    public NtSchedulerView(NetworkTableInstance ntInstance) {
        ListView<InstanceNode> instancesList = new ListView<>();
        instancesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        instancesList.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            setCenter(n.getDataPane());
        });

        NetworkTable instancesTable = ntInstance.getTable("FlashLib").getSubTable("instances");
        instancesTable.addSubTableListener((parent, name, table) -> {
            instancesList.getItems().add(new InstanceNode(table, name));
        }, false);

        VBox leftPane = new VBox();
        leftPane.setAlignment(Pos.CENTER_LEFT);
        leftPane.setPadding(new Insets(5));
        leftPane.setSpacing(5);
        leftPane.getChildren().addAll(instancesList);
        setLeft(leftPane);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void close() {

    }

    private static class InstanceNode {

        private final NetworkTable mTable;
        private final NetworkTable mSchedulerTable;
        private final String mName;

        private final Node mDataPane;

        InstanceNode(NetworkTable table, String name) {
            mTable = table;
            mSchedulerTable = table.getSubTable("FlashLib").getSubTable("Scheduler");
            mName = name;

            ScrollPane scrollPane = new ScrollPane();
            FlowPane dataPane = new FlowPane();
            scrollPane.setContent(dataPane);
            mDataPane = scrollPane;

            mSchedulerTable.addSubTableListener((parent, subName, subTable) -> {
                dataPane.getChildren().add(new ActionNode(subTable));
            }, false);
        }

        public Node getDataPane() {
            return mDataPane;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private static class ActionNode extends BorderPane implements Closeable {

        private ActionNode(NetworkTable table) {
            NetworkTableEntry nameEntry = table.getEntry("name");
            NetworkTableEntry classEntry = table.getEntry("class");
            NetworkTableEntry timeoutEntry = table.getEntry("timeout");
            NetworkTableEntry requirementsEntry = table.getEntry("requirements");

            NetworkTableEntry statusEntry = table.getEntry("status");
            NetworkTableEntry phaseEntry = table.getEntry("phase");

            VBox topPane = new VBox();
            topPane.setAlignment(Pos.TOP_LEFT);
            topPane.setPadding(new Insets(5));
            topPane.setSpacing(5);
            topPane.getChildren().addAll(
                    new Label(nameEntry.getString("")),
                    new Label(classEntry.getString("")));
            setTop(topPane);

            ListView<String> requirementsList = new ListView<>();
            String requirementsString = requirementsEntry.getString("[]");
            requirementsString = requirementsString.substring(1, requirementsString.length() - 1);
            requirementsList.getItems().addAll(requirementsString.split(","));
            requirementsList.setPlaceholder(new Label("No Requirements"));

            VBox leftPane = new VBox();
            leftPane.setAlignment(Pos.TOP_LEFT);
            leftPane.setPadding(new Insets(5));
            leftPane.setSpacing(5);
            leftPane.setPrefSize(150, 100);
            leftPane.getChildren().addAll(requirementsList);
            setLeft(leftPane);

            Label phaseLabel = new Label();
            phaseLabel.setText(phaseEntry.getString(""));
            phaseEntry.addListener((notification)-> {
                phaseLabel.setText(notification.value.getString());
            }, EntryListenerFlags.kUpdate);

            Label statusLabel = new Label();
            statusLabel.setText(statusEntry.getString(""));
            statusEntry.addListener((notification)-> {
                statusLabel.setText(notification.value.getString());
            }, EntryListenerFlags.kUpdate);

            VBox centerPane = new VBox();
            centerPane.setAlignment(Pos.CENTER_LEFT);
            centerPane.setPadding(new Insets(5));
            centerPane.setSpacing(5);
            centerPane.getChildren().addAll(phaseLabel, statusLabel);
            setCenter(centerPane);
        }

        @Override
        public void close() throws IOException {

        }
    }
}
