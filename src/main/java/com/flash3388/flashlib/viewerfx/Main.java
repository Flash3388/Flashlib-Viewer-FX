package com.flash3388.flashlib.viewerfx;

import com.castle.util.closeables.Closer;
import com.castle.util.logging.LoggerBuilder;
import javafx.application.Platform;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {

    public static void main(String[] args) throws Exception {
        Logger logger = new LoggerBuilder("modbusFX")
                .enableConsoleLogging(true)
                .build();

        ProgramOptions programOptions = handleArguments(args);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

        Closer closer = Closer.empty();
        closer.add(executorService::shutdownNow);
        try {
            FlashlibViewerFx flashlibViewerFx = new FlashlibViewerFx(programOptions, executorService, logger);
            flashlibViewerFx.run();
        } finally {
            closer.close();
        }

        Platform.exit();
    }

    private static ProgramOptions handleArguments(String[] args) throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor("ModbusFx")
                .build()
                .defaultHelp(true)
                .description("");

        Namespace namespace = parser.parseArgs(args);
        return new ProgramOptions();
    }
}
