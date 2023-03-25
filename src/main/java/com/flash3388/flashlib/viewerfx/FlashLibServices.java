package com.flash3388.flashlib.viewerfx;

import com.castle.exceptions.ServiceException;
import com.castle.util.closeables.Closer;
import com.flash3388.flashlib.net.hfcs.HfcsRegistry;
import com.flash3388.flashlib.net.hfcs.impl.HfcsBroadcastService;
import com.flash3388.flashlib.net.hfcs.impl.HfcsServiceBase;
import com.flash3388.flashlib.net.hfcs.impl.HfcsTightService;
import com.flash3388.flashlib.net.obsr.ObjectStorage;
import com.flash3388.flashlib.net.obsr.impl.ObsrSecondaryNodeService;
import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.util.unique.InstanceId;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class FlashLibServices {

    private final Clock mClock;
    private final HfcsServiceBase mHfcsService;
    private final ObsrSecondaryNodeService mObsrService;

    public FlashLibServices(InstanceId instanceId, Clock clock) {
        mClock = clock;
        mHfcsService = new HfcsTightService(
                Arrays.asList(
                        new InetSocketAddress("127.0.0.1", 5005)
                ),
                instanceId, clock,
                5000);
        mObsrService = new ObsrSecondaryNodeService(instanceId, "127.0.0.1", clock);
    }

    public Clock getClock() {
        return mClock;
    }

    public HfcsRegistry getHfcsService() {
        return mHfcsService;
    }

    public ObjectStorage getObsrService() {
        return mObsrService;
    }

    public void startAll() throws ServiceException {
        mHfcsService.start();
        mObsrService.start();
    }

    public void stopAll() {
        try {
            Closer.with(mHfcsService, mObsrService).close();
        } catch (Exception e) {}
    }
}
