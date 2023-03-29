package com.flash3388.flashlib.viewerfx.services;

import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.util.unique.InstanceId;
import com.flash3388.flashlib.viewerfx.services.hfcs.HfcsService;
import com.flash3388.flashlib.viewerfx.services.obsr.ObsrService;

public class FlashLibServices {

    private final Clock mClock;
    private final HfcsService mHfcsService;
    private final ObsrService mObsrService;

    public FlashLibServices(InstanceId instanceId, Clock clock) {
        mClock = clock;
        mHfcsService = new HfcsService(instanceId, clock);
        mObsrService = new ObsrService(instanceId, clock);
    }

    public Clock getClock() {
        return mClock;
    }

    public HfcsService getHfcsService() {
        return mHfcsService;
    }

    public ObsrService getObsrService() {
        return mObsrService;
    }

    public void startAll() {
        mHfcsService.start();
        mObsrService.start();
    }

    public void stopAll() {
        try {
            mHfcsService.stop();
        } catch (Exception e) {}
        try {
            mObsrService.stop();
        } catch (Exception e) {}
    }
}
