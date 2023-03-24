package com.flash3388.flashlib.viewerfx;

import com.castle.code.Natives;
import com.castle.exceptions.CodeLoadException;
import com.castle.exceptions.FindException;
import com.castle.util.closeables.Closeables;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.flash3388.flashlib.viewerfx.gui.ApplicationGui;
import com.flash3388.flashlib.viewerfx.gui.Dialogs;
import com.flash3388.flashlib.viewerfx.gui.MainWindow;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FlashlibViewerFx {

    private static final boolean FORCE_FULL_SCREEN = false;

    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 550;

    private final ProgramOptions mProgramOptions;
    private final ScheduledExecutorService mExecutorService;
    private final Logger mLogger;

    private final AtomicReference<MainWindow> mMainWindow;

    public FlashlibViewerFx(ProgramOptions programOptions, ScheduledExecutorService executorService, Logger logger) {
        mProgramOptions = programOptions;
        mExecutorService = executorService;
        mLogger = logger;
        mMainWindow = new AtomicReference<>();
    }

    public void run() throws InitializationException {
        mLogger.info("Starting GUI");
        Stage primaryStage = ApplicationGui.startGui(mExecutorService);
        mLogger.info("GUI launched");

        showMainWindow(primaryStage);
    }

    private void showMainWindow(Stage primaryStage) throws InitializationException {
        try {
            CountDownLatch runLatch = new CountDownLatch(1);

            Platform.runLater(()-> {
                final MainWindow mainWindow = new MainWindow(primaryStage, WINDOW_WIDTH, WINDOW_HEIGHT);
                mMainWindow.set(mainWindow);

                primaryStage.setScene(mainWindow.createScene());

                if (FORCE_FULL_SCREEN) {
                    primaryStage.setFullScreen(true);
                    primaryStage.setMaximized(true);
                }

                primaryStage.setOnCloseRequest((e)-> {
                    try {
                        mainWindow.close();
                    } catch (Exception ex) {}

                    runLatch.countDown();
                });
                primaryStage.show();

                try {
                    loadNatives();
                } catch (Throwable t) {
                    Dialogs.showError("Error", "Failed loading natives", t);
                    primaryStage.close();
                    Closeables.silentClose(mainWindow);
                    runLatch.countDown();
                    return;
                }

                mExecutorService.scheduleAtFixedRate(
                        mainWindow::updateClients,
                        100,
                        1000,
                        TimeUnit.MILLISECONDS
                );
            });

            runLatch.await();
        } catch (Exception e) {
            Platform.exit();
            throw new InitializationException(e);
        }
    }

    private void loadNatives() throws CodeLoadException {
        try {
            Natives.Loader loader = Natives.newLoader();
            loader.load("modbus_jni");
        } catch (FindException | IOException e) {
            throw new CodeLoadException(e);
        }
    }
}
