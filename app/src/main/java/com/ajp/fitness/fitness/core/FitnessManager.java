package com.ajp.fitness.fitness.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

/*
 * Created by akshay on 30/1/19.
 * Copyright © 2018, Huffy
 * Written under contract by Robosoft Technologies Pvt. Ltd.
 */
public class FitnessManager extends BaseManager
        implements IFitnessContract.FitnessService {

    private static IFitnessContract.FitnessService sService;
    private SensorsManager mSensorsManager;
    private HistoryManager mHistoryManager;
    private SubscriptionManager mSubscriptionManager;
    private SessionsManager mSessionManager;


    private static final int REQUEST_CODE = 20001;

    public static IFitnessContract.FitnessService getInstance() {
        if (sService == null) {
            //Fitness no activity instance
            Activity activity = null;
            sService = new FitnessManager(activity);
        }
        return sService;
    }

    private FitnessManager(Activity activity) {
        super(activity);
        mSensorsManager = SensorsManager.getInstance(activity);
        mSubscriptionManager = SubscriptionManager.getInstance(activity);
        mHistoryManager = HistoryManager.getInstance(activity);
        mSessionManager = SessionsManager.getInstance(activity);
    }

    /**
     * Request sign in with google account for accessing the history
     */
    @Override
    public void initClient() {
        FitnessOptions options = getFitnessOptions();
        requestAccessPermissions(options, REQUEST_CODE);
    }

    /**
     * Fitness Remove the data type which is not required
     *
     * @return fitness options with the data types
     */
    @NonNull
    private FitnessOptions getFitnessOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE)
                .addDataType(DataType.TYPE_HEART_RATE_BPM)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();
    }

    //Will get a call back if the permissions are already available or provided
    @Override
    void onAccessPermissionsAvailable(int requestCode) {
        Log.d(TAG, "onAccessPermissionsAvailable : " + requestCode);
    }

    @Override
    void onAccessPermissionsDenied(int requestCode) {
        Log.d(TAG, "onAccessPermissionsDenied : " + requestCode);
    }

    @Override
    boolean isFitnessResult(int requestCode) {
        return requestCode == REQUEST_CODE;
    }

    @Override
    public void readHistory(long startTime, long endTime, DataType... dataTypes) {
        if (mHistoryManager != null) {
            mHistoryManager.readHistory(dataTypes, startTime, endTime);
        }
    }

    @Override
    public void startRead(long startTime, long repeatInterval, DataType... dataTypes) {
        if (mHistoryManager != null) {
            mHistoryManager.startRead(dataTypes, startTime, repeatInterval);
        }
    }

    @Override
    public void cancelRead() {
        if (mHistoryManager != null) {
            mHistoryManager.cancelTimer();
        }
    }

    @Override
    public void insertData() {
        if (mHistoryManager != null) {
            mHistoryManager.insertData();
        }
    }

    @Override
    public void getSensorData() {
        if (mSensorsManager != null) {
            mSensorsManager.findFitnessDataSource();
        }
    }

    @Override
    public void subscribe(@NonNull DataType dataType) {
        if (mSubscriptionManager != null) {
            mSubscriptionManager.subscribe(dataType);
        }
    }

    @Override
    public void listSubscriptions() {
        if (mSubscriptionManager != null) {
            mSubscriptionManager.dumpSubscriptionsList();
        }
    }

    @Override
    public void unsubscribe(@NonNull DataType dataType) {
        if (mSubscriptionManager != null) {
            mSubscriptionManager.cancelSubscription(dataType);
        }
    }

    @Override
    public void subscriptionListener(IFitnessContract.SubscriptionListener listener) {
        if (mSubscriptionManager != null) {
            mSubscriptionManager.setListener(listener);
        }
    }

    @Override
    public void historyListener(IFitnessContract.HistoryListener listener) {
        if (mHistoryManager != null) {
            mHistoryManager.setListener(listener);
        }
    }

    @Override
    public void sessionListener(IFitnessContract.SessionListener listener) {
        if (mSessionManager != null) {
            mSessionManager.setListener(listener);
        }
    }

    @Override
    public void startSession(String id) {
        if (mSessionManager != null) {
            mSessionManager.startSession("New session", id, "No description");
        }
    }

    @Override
    public void endSession(String id) {
        if (mSessionManager != null) {
            mSessionManager.endSession(id);
        }
    }

    @Override
    public void insertSession() {
        if (mSessionManager != null) {
            mSessionManager.insertSession();
        }
    }

    @Override
    public void verifySession() {
        if (mSessionManager != null) {
            mSessionManager.verifySession();
        }
    }

    @Override
    public void deleteSession() {
        if (mSessionManager != null) {
            mSessionManager.deleteSession();
        }
    }

    @Override
    public void insertRideActivity(long startTime, long endTime) {
        if (mHistoryManager != null) {
            mHistoryManager.insertActivity(startTime, endTime);
        }
    }

    @Override
    public void trackActivity(long startTime, int repeatInterval) {
        if (mHistoryManager != null) {
            mHistoryManager.startActivityTrack(startTime, repeatInterval);
        }
    }

    @Override
    public void insertDistance(long startTime, long endTime, float distance) {
        if (mHistoryManager != null) {
            mHistoryManager.insertDistance(startTime, endTime, distance);
        }
    }

    @Override
    public void insertRideSession(String rideName, String rideId, long startTime, long endTime) {
        if (mSessionManager != null) {
            mSessionManager.insertSession(rideName, rideId, startTime, endTime);
        }
    }

    @Override
    public boolean hasAccessPermissions() {
        return hasAccessPermissions(getFitnessOptions());
    }

    @Override
    public void stopTrackActivity() {
        if (mHistoryManager != null) {
            mHistoryManager.stopActivityTrack();
        }
    }


    @Override
    public boolean onFitnessActivityResults(int requestCode, int resultCode) {
        return onFitnessResults(requestCode, resultCode);
    }

}
