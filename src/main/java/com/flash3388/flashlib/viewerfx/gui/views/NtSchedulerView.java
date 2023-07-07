package com.flash3388.flashlib.viewerfx.gui.views;

import com.castle.util.closeables.Closer;
import com.flash3388.flashlib.viewerfx.services.nt.NtService;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;

public class NtSchedulerView extends AbstractView {

    private final ListView<InstanceNode> mInstancesList;
    private NetworkTable mInstancesTable;
    private int mInstancesListenerHandle;

    public NtSchedulerView(NtService ntService) {
        mInstancesTable = null;
        mInstancesListenerHandle = -1;

        mInstancesList = new ListView<>();
        mInstancesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        mInstancesList.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            setCenter(n.getDataPane());
        });

        ntService.serviceProperty().addListener((obs, o, n)-> {
            refreshService(n);
        });

        VBox leftPane = new VBox();
        leftPane.setAlignment(Pos.CENTER_LEFT);
        leftPane.setPadding(new Insets(5));
        leftPane.setSpacing(5);
        leftPane.getChildren().addAll(mInstancesList);
        leftPane.setPrefWidth(200);
        setLeft(leftPane);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void close() {
        mInstancesList.getItems().forEach(InstanceNode::close);
        mInstancesList.getItems().clear();

        if (mInstancesTable != null) {
            mInstancesTable.removeTableListener(mInstancesListenerHandle);
        }
    }

    private void refreshService(NetworkTableInstance ntInstance) {
        close();

        mInstancesTable = ntInstance.getTable("FlashLib").getSubTable("instances");
        mInstancesListenerHandle = mInstancesTable.addSubTableListener((parent, name, table) -> {
            Platform.runLater(()-> {
                mInstancesList.getItems().add(new InstanceNode(table, name));
            });
        }, false);
    }

    private static class InstanceNode implements Closeable {

        private final NetworkTable mTable;
        private final NetworkTable mSchedulerTable;
        private final String mName;

        private final ObservableList<ActionNode> mActionNodes;
        private final FilteredList<ActionNode> mActionNodesFiltered;

        private final VBox mDataPane;
        private final Node mDataPaneRoot;

        private final Closer mCloser;

        InstanceNode(NetworkTable table, String name) {
            mTable = table;
            mSchedulerTable = table.getSubTable("FlashLib").getSubTable("Scheduler");
            mName = name;

            mActionNodes = FXCollections.observableArrayList();
            mActionNodesFiltered = new FilteredList<>(mActionNodes);
            mActionNodesFiltered.setPredicate(null);

            ScrollPane scrollPane = new ScrollPane();
            VBox dataPane = new VBox();
            scrollPane.setContent(dataPane);
            mDataPane = dataPane;

            TextField searchBar = new TextField();
            searchBar.textProperty().addListener((obs, o, n)-> {
                mActionNodesFiltered.setPredicate((node)-> {
                    if (n.isBlank()) return true;
                    return node.doesMatchSearchString(n.toLowerCase());
                });

                dataPane.getChildren().clear();
                dataPane.getChildren().addAll(mActionNodesFiltered);
            });

            VBox rootBar = new VBox();
            rootBar.setAlignment(Pos.TOP_CENTER);
            rootBar.setPadding(new Insets(5));
            rootBar.setSpacing(5);
            rootBar.getChildren().addAll(searchBar, scrollPane);
            mDataPaneRoot = rootBar;

            mCloser = Closer.empty();

            int listenerHandle = mSchedulerTable.addSubTableListener((parent, subName, subTable) -> {
                ActionNode node = new ActionNode(subTable);
                mCloser.add(node);

                Platform.runLater(()-> {
                    mActionNodes.add(node);

                    dataPane.getChildren().clear();
                    dataPane.getChildren().addAll(mActionNodesFiltered);
                });
            }, false);
            mCloser.add(()-> mSchedulerTable.removeTableListener(listenerHandle));
        }

        public Node getDataPane() {
            return mDataPaneRoot;
        }

        @Override
        public String toString() {
            return mName;
        }

        @Override
        public void close() {
            mDataPane.getChildren().clear();
            try {
                mCloser.close();
            } catch (Exception e) {}
        }
    }

    private static class ActionNode extends BorderPane implements Closeable {

        private final String mName;
        private final String mClass;

        private final Collection<ActionNode> mChildrenNodes;
        private final Closer mCloser;

        private ActionNode(NetworkTable table) {
            mCloser = Closer.empty();

            NetworkTableEntry nameEntry = table.getEntry("name");
            mName = nameEntry.getString("");

            NetworkTableEntry classEntry = table.getEntry("class");
            mClass = classEntry.getString("");

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
            requirementsList.setPlaceholder(new Label("No Requirements"));
            String requirementsString = requirementsEntry.getString(null);
            if (requirementsString != null && !requirementsString.equals("[]")) {
                requirementsString = requirementsString.substring(1, requirementsString.length() - 1);
                requirementsList.getItems().addAll(requirementsString.split(","));
            }

            TableView<ActionPropertyNode> propertiesView = new TableView<>();
            TableColumn<ActionPropertyNode, String> keyColumn = new TableColumn<>("Key");
            keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            propertiesView.getColumns().add(keyColumn);
            TableColumn<ActionPropertyNode, String> valueColumn = new TableColumn<>("Value");
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            propertiesView.getColumns().add(valueColumn);
            propertiesView.setPlaceholder(new Label("No Properties"));

            NetworkTable propertiesTable = table.getSubTable("properties");
            System.out.println(propertiesTable.getKeys());
            propertiesTable.addEntryListener((table1, key, entry, value, flags)-> {
                Platform.runLater(()-> {
                    for (ActionPropertyNode node : propertiesView.getItems()) {
                        if (node.getKey().equals(key)) {
                            node.setValue(value);
                            propertiesView.refresh();
                            return;
                        }
                    }

                    // not found
                    ActionPropertyNode node = new ActionPropertyNode(key, null);
                    node.setValue(value);
                    propertiesView.getItems().add(node);
                });
            }, EntryListenerFlags.kUpdate | EntryListenerFlags.kNew | EntryListenerFlags.kImmediate);

            VBox rightPane = new VBox();
            rightPane.setAlignment(Pos.TOP_RIGHT);
            rightPane.setPadding(new Insets(5));
            rightPane.setSpacing(5);
            rightPane.setPrefSize(250, 150);
            rightPane.getChildren().addAll(propertiesView);
            setRight(rightPane);

            VBox leftPane = new VBox();
            leftPane.setAlignment(Pos.TOP_LEFT);
            leftPane.setPadding(new Insets(5));
            leftPane.setSpacing(5);
            leftPane.setPrefSize(150, 100);
            leftPane.getChildren().addAll(requirementsList);
            setLeft(leftPane);

            Label phaseLabel = new Label();
            phaseLabel.setText(phaseEntry.getString(""));
            int phaseListenerHandle = phaseEntry.addListener((notification)-> {
                Platform.runLater(()-> {
                    phaseLabel.setText(notification.value.getString());
                });
            }, EntryListenerFlags.kUpdate);
            mCloser.add(()-> phaseEntry.removeListener(phaseListenerHandle));

            Label statusLabel = new Label();
            statusLabel.setText(statusEntry.getString(""));
            int statusListenerHandle = statusEntry.addListener((notification)-> {
                Platform.runLater(()-> {
                    statusLabel.setText(notification.value.getString());
                });
            }, EntryListenerFlags.kUpdate);
            mCloser.add(()-> statusEntry.removeListener(statusListenerHandle));

            int deleteListenerHandle = statusEntry.addListener((notification)-> {
                Platform.runLater(()-> {
                    phaseLabel.setText("END");
                });
            }, EntryListenerFlags.kDelete);
            mCloser.add(()-> statusEntry.removeListener(deleteListenerHandle));

            mChildrenNodes = new ArrayList<>();
            HBox childrenPane = new HBox();
            childrenPane.setPadding(new Insets(10));
            int childrenTableListener = table.addSubTableListener((parent, subName, subTable)-> {
                if (subName.equals("properties")) return;

                ActionNode node = new ActionNode(subTable);
                mCloser.add(node);

                Platform.runLater(()-> {
                    mChildrenNodes.add(node);
                    childrenPane.getChildren().add(node);
                });
            }, false);
            mCloser.add(()-> table.removeTableListener(childrenTableListener));

            ScrollPane childrenRoot = new ScrollPane();
            childrenRoot.setContent(childrenPane);
            childrenRoot.setPadding(new Insets(5));
            childrenRoot.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            setBottom(childrenRoot);

            VBox centerPane = new VBox();
            centerPane.setAlignment(Pos.CENTER_LEFT);
            centerPane.setPadding(new Insets(5));
            centerPane.setSpacing(5);
            centerPane.getChildren().addAll(phaseLabel, statusLabel);
            setCenter(centerPane);
        }

        public boolean doesMatchSearchString(String searchString) {
            if (mName.toLowerCase().contains(searchString) || mClass.toLowerCase().contains(searchString)) {
                return true;
            }

            for (ActionNode child : mChildrenNodes) {
                if (child.doesMatchSearchString(searchString)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void close() {
            try {
                mCloser.close();
            } catch (Exception e) {}
        }
    }

    public static class ActionPropertyNode {

        private final String mKey;
        private String mValue;

        private ActionPropertyNode(String key, String value) {
            mKey = key;
            mValue = value;
        }


        public String getKey() {
            return mKey;
        }

        public String getValue() {
            return mValue;
        }

        public void setValue(String value) {
            mValue = value;
        }

        public void setValue(NetworkTableValue value) {
            setValue(value.getValue().toString());
        }
    }
}
