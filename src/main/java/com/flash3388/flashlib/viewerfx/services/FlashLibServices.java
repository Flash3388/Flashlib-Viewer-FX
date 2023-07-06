package com.flash3388.flashlib.viewerfx.services;

import com.flash3388.flashlib.time.Clock;
import com.flash3388.flashlib.util.unique.InstanceId;
import com.flash3388.flashlib.viewerfx.services.hfcs.HfcsService;
import com.flash3388.flashlib.viewerfx.services.nt.NtService;
import com.flash3388.flashlib.viewerfx.services.obsr.ObsrService;
import edu.wpi.first.networktables.NetworkTableInstance;

public class FlashLibServices {

    private final Clock mClock;
    private final HfcsService mHfcsService;
    private final ObsrService mObsrService;
    private final NtService mNtService;

    public FlashLibServices(InstanceId instanceId, Clock clock) {
        mClock = clock;
        mHfcsService = new HfcsService(instanceId, clock);
        mObsrService = new ObsrService(instanceId, clock);
        mNtService = new NtService();
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

    public NtService getNtService() {
        return mNtService;
    }

    public void startAll() {
        mHfcsService.start();
        mObsrService.start();
        mNtService.start();
    }

    public void stopAll() {
        try {
            mHfcsService.stop();
        } catch (Exception e) {}
        try {
            mObsrService.stop();
        } catch (Exception e) {}
        try {
            mNtService.stop();
        } catch (Exception e) {}
    }
}
