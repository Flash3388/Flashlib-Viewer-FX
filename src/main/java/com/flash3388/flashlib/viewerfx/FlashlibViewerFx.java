package com.flash3388.flashlib.viewerfx;

import com.castle.exceptions.CodeLoadException;
import com.castle.exceptions.ServiceException;
import com.castle.util.closeables.Closeables;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.time.SystemMillisClock;
import com.flash3388.flashlib.util.logging.Logging;
import com.flash3388.flashlib.util.unique.InstanceId;
import com.flash3388.flashlib.util.unique.InstanceIdGenerator;
import com.flash3388.flashlib.viewerfx.gui.ApplicationGui;
import com.flash3388.flashlib.viewerfx.gui.Dialogs;
import com.flash3388.flashlib.viewerfx.gui.MainWindow;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FlashlibViewerFx {

    private static final Logger LOGGER = Logging.getLogger("Viewer");

    private static final boolean FORCE_FULL_SCREEN = false;

    private static final double WINDOW_WIDTH = 850;
    private static final double WINDOW_HEIGHT = 650;

    private final ProgramOptions mProgramOptions;
    private final ScheduledExecutorService mExecutorService;
    private final FlashLibServices mServices;

    private final AtomicReference<MainWindow> mMainWindow;

    public FlashlibViewerFx(ProgramOptions programOptions,
                            ScheduledExecutorService executorService) {
        mProgramOptions = programOptions;
        mExecutorService = executorService;

        InstanceId instanceId = InstanceIdGenerator.generate();
        Clock clock = new SystemMillisClock();

        mServices = new FlashLibServices(instanceId, clock);
        mMainWindow = new AtomicReference<>();
    }

    public void run() throws InitializationException {
        LOGGER.info("Starting GUI");
        Stage primaryStage = ApplicationGui.startGui(mExecutorService);
        LOGGER.info("GUI launched");

        showMainWindow(primaryStage);
    }

    private void showMainWindow(Stage primaryStage) throws InitializationException {
        try {
            CountDownLatch runLatch = new CountDownLatch(1);

            Platform.runLater(()-> {
                final MainWindow mainWindow;
                try {
                    mainWindow = new MainWindow(primaryStage, WINDOW_WIDTH, WINDOW_HEIGHT, mServices);
                } catch (ServiceException e) {
                    Dialogs.showError("Error", "Failed loading window", e);
                    primaryStage.close();
                    runLatch.countDown();
                    return;
                }
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

                try {
                    mServices.startAll();
                } catch (Throwable t) {
                    Dialogs.showError("Error", "Failed loading services", t);
                    primaryStage.close();
                    Closeables.silentClose(mainWindow);
                    runLatch.countDown();
                    return;
                }

                mExecutorService.scheduleAtFixedRate(
                        mainWindow::updateView,
                        100,
                        100,
                        TimeUnit.MILLISECONDS
                );
            });

            runLatch.await();
        } catch (Exception e) {
            Platform.exit();
            throw new InitializationException(e);
        } finally {
            mServices.stopAll();
        }
    }

    private void loadNatives() throws CodeLoadException {

    }
}
