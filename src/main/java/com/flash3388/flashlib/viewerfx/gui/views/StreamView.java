package com.flash3388.flashlib.viewerfx.gui.views;

import com.castle.exceptions.ServiceException;
import com.castle.util.closeables.Closeables;
import com.flash3388.flashlib.util.logging.StubLogger;
import com.flash3388.flashlib.viewerfx.gui.Dialogs;
import com.flash3388.flashlib.vision.jpeg.client.MjpegClient;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class StreamView extends AbstractView {

    private final ListView<StreamNode> mListView;
    private final Pane mVideoPane;

    public StreamView() {
        mListView = new ListView<>();
        mListView.setEditable(false);
        mListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        mVideoPane = new VBox();

        Button add = new Button("Add");
        add.setOnAction((e)-> {
            Optional<StreamNode> optional = showInputDialog();
            if (optional.isPresent()) {
                StreamNode node = optional.get();
                mListView.getItems().add(node);
            }
        });
        Button remove = new Button("Remove");
        remove.setOnAction((e)-> {
            StreamNode selected = mListView.getSelectionModel().getSelectedItem();
            mListView.getItems().remove(selected);
            Closeables.silentClose(selected);
        });

        remove.setDisable(true);

        mListView.getSelectionModel().selectedItemProperty().addListener((obs, o, n)-> {
            if (o != null) {
                mVideoPane.getChildren().clear();
            }

            if (n != null) {
                n.start();
                mVideoPane.getChildren().add(n);
                remove.setDisable(false);
            } else {
                remove.setDisable(true);
            }
        });

        setCenter(mVideoPane);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setSpacing(5);
        buttonBox.setPadding(new Insets(2));
        buttonBox.getChildren().addAll(add, remove);

        VBox leftPane = new VBox();
        leftPane.setAlignment(Pos.CENTER_LEFT);
        leftPane.setPadding(new Insets(5));
        leftPane.setSpacing(5);
        leftPane.getChildren().addAll(mListView, buttonBox);
        setLeft(leftPane);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void close() {
        mVideoPane.getChildren().clear();
        for (StreamNode node : mListView.getItems()) {
            Closeables.silentClose(node);
        }
    }

    private static Optional<StreamNode> showInputDialog() {
        TextField nameField = new TextField();
        TextField urlField = new TextField();

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("name"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("URL"), 0, 1);
        gridPane.add(urlField, 1, 1);

        if (!Dialogs.showCustomApplyDialog(gridPane)) {
            // no save
            return Optional.empty();
        }

        return Optional.of(new StreamNode(nameField.getText(), urlField.getText()));
    }

    private static class StreamNode extends VBox implements Closeable {

        private final String mName;
        private final MjpegClient mClient;

        public StreamNode(String name, String url) {
            mName = name;

            HBox infoBox = new HBox();
            infoBox.setAlignment(Pos.TOP_CENTER);
            infoBox.getChildren().add(new Label(name));

            setAlignment(Pos.CENTER);

            try {
                ImageView imageView = new ImageView();
                mClient = MjpegClient.create(new URL(url), (image)-> {
                    Image fxImage = SwingFXUtils.toFXImage((BufferedImage) image.toAwt(), null);
                    imageView.setImage(fxImage);
                }, new StubLogger());
                getChildren().addAll(infoBox, imageView);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        public void start() {
            if (mClient.isRunning()) {
                return;
            }

            try {
                mClient.start();
            } catch (ServiceException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws IOException {
            mClient.close();
        }

        @Override
        public String toString() {
            return mName;
        }
    }
}
