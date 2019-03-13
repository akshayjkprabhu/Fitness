package com.ajp.fitness.utils.fitness.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.*;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/*
 * Created by akshay on 1/2/19.
 * Copyright © 2018, Huffy
 * Written under contract by Robosoft Technologies Pvt. Ltd.
 */
class HistoryManager extends BaseManager {

    private static HistoryManager sManager;
    private Timer mTimer;
    private Timer mActivityTimer;

    public static HistoryManager getInstance(Activity activity) {
        if (sManager == null) {
            sManager = new HistoryManager(activity);
        }
        return sManager;
    }

    private HistoryManager(Activity activity) {
        super(activity);
    }

    @Override
    void onAccessPermissionsAvailable(int requestCode) {

    }

    @Override
    void onAccessPermissionsDenied(int requestCode) {

    }

    @Override
    boolean isFitnessResult(int requestCode) {
        return false;
    }

    /**
     * Calls the history api ones be
     *
     * @param dataTypes are the type of values to be read
     * @param startTime is the start time
     * @param endTime   is the end time
     */
    void readHistory(@Nullable DataType[] dataTypes, long startTime, long endTime) {
        cancelTimer(); // to cancel the existing timer to read history
        DataReadRequest readRequest = buildReadRequest(dataTypes, startTime, endTime);
        if (readRequest != null) {
            readData(readRequest, getLastSignedInAccount());
        }
    }

    /**
     * Calls history API for every specified interval
     * Will return the total data from the startTime till the time history api is called
     *
     * @param dataTypes      are the type to read
     * @param startTime      is the start time from which the
     * @param repeatInterval is the time interval in milliseconds, the data should be read
     */
    void startRead(@Nullable DataType[] dataTypes, long startTime, long repeatInterval) {
        Calendar cal = Calendar.getInstance();
        if (startTime > cal.getTimeInMillis()) {
            return;
        }
        cancelTimer(); // Need to cancel timer if it is already running in any case otherwise mis function will happen
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long endTime = Calendar.getInstance().getTimeInMillis();
                DataReadRequest readRequest = buildReadRequest(dataTypes, startTime, endTime);
                if (readRequest != null) {
                    readData(readRequest, getLastSignedInAccount());
                } else {
                    cancelTimer();
                }
            }
        }, repeatInterval, repeatInterval);
    }

    /**
     * cancels the history read timer
     */
    void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private DataReadRequest buildReadRequest(@Nullable DataType[] dataTypes, long startTime, long endTime) {
        if (dataTypes != null && dataTypes.length > 0) {
            DataReadRequest.Builder builder = new DataReadRequest.Builder();
            for (DataType type : dataTypes) {
                builder.read(type);
            }
            // builder.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED);
            builder.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS);
            //  builder.bucketByActivitySegment(1, TimeUnit.MILLISECONDS);
            return builder.build();
        }
        return null;
    }

    private void readData(@NonNull DataReadRequest readRequest, @Nullable GoogleSignInAccount lastSignedInAccount) {
        if (lastSignedInAccount != null) {
            Fitness.getHistoryClient(getActivity(), lastSignedInAccount)
                    .readData(readRequest)
                    .addOnSuccessListener(dataReadResponse -> {
                        Log.d(TAG, "dataReadResponse");
                        printData(dataReadResponse);
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "failure");
                    })
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "complete");
                    });
        }
    }

    private void printData(DataReadResponse dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.d(
                    TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }

    // [START parse_dataset]
    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();
        float totalCalories = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                //showMessage("Field: " + field.getName() + " Value: " + dp.getValue(field));
                totalCalories += dp.getValue(field).asFloat();
            }
        }
        if (getListener() instanceof IFitnessContract.HistoryListener) {
            ((IFitnessContract.HistoryListener) getListener()).onCaloriesBurntAvailable(totalCalories);
        }
    }

    private void insertDataSet(@NonNull DataSet dataSet) {
        GoogleSignInAccount account = getLastSignedInAccount();
        Activity activity = getActivity();
        if (activity != null && account != null) {
            Fitness.getHistoryClient(activity, account)
                    .insertData(dataSet)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Success"))
                    .addOnFailureListener(e -> Log.d(TAG, "Failure"));
        }
    }

    void startActivityTrack(long startTime, long repeatInterval) {
        Calendar cal = Calendar.getInstance();
        if (startTime > cal.getTimeInMillis()) {
            return;
        }
        stopActivityTrack(); // Need to cancel timer if it is already running in any case otherwise mis function will happen
        mActivityTimer = new Timer();
        mActivityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long endTime = Calendar.getInstance().getTimeInMillis();
                DataSet activityData = getActivityData(startTime, endTime);
                if (activityData != null) {
                    insertDataSet(activityData);
                } else {
                    stopActivityTrack();
                }
            }
        }, repeatInterval, repeatInterval);
    }

    void insertActivity(long startTime, long endTime) {
        stopActivityTrack();
        DataSet activityData = getActivityData(startTime, endTime);
        if (activityData != null) {
            insertDataSet(activityData);
        }
    }

    void insertDistance(long startTime, long endTime, float distanceDelta) {
        DataSet speedData = getDistanceData(startTime, endTime, distanceDelta);
        if (speedData != null) {
            insertDataSet(speedData);
        }
    }

    void stopActivityTrack() {
        if (mActivityTimer != null) {
            mActivityTimer.cancel();
            mActivityTimer = null;
        }
    }


    //todo remove
    public void insertData() {
        GoogleSignInAccount lastSignedInAccount = getLastSignedInAccount();
        Activity activity = getActivity();
        if (activity != null && lastSignedInAccount != null) {
            DataSet dataSet = insertFitnessData();
            if (dataSet != null) {
                Fitness.getHistoryClient(activity, lastSignedInAccount)
                        .insertData(dataSet)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Success"))
                        .addOnFailureListener(e -> Log.d(TAG, "Failure"));
            }
        }
    }

    private DataSet getActivityData(long startTime, long endTime) {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }

        // Create a data source
        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(activity)
                        .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                        .setStreamName(" - riding")
                        .setType(DataSource.TYPE_RAW)
                        .build();
        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.BIKING);
        dataSet.add(dataPoint);
        return dataSet;
    }

    private DataSet getDistanceData(long startTime, long endTime, float distanceDelta) {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }

        // Create a data source
        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(activity)
                        .setDataType(DataType.TYPE_DISTANCE_DELTA)
                        .setStreamName(" - distance")
                        .setType(DataSource.TYPE_RAW)
                        .build();
        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_DISTANCE).setFloat(distanceDelta);
        dataSet.add(dataPoint);
        return dataSet;
    }


    //todo remove
    private DataSet insertFitnessData() {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        // [START build_insert_data_request]
        // Set a start and end time for our data, using a start time of 1 hour before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(activity)
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setStreamName(" - step count")
                        .setType(DataSource.TYPE_RAW)
                        .build();

        // Create a data set
        int stepCountDelta = 950;
        DataSet dataSet = DataSet.create(dataSource);
        // For each data point, specify a start time, end time, and the data value -- in this case,
        // the number of new steps.
        DataPoint dataPoint =
                dataSet.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
        dataSet.add(dataPoint);
        // [END build_insert_data_request]

        return dataSet;
    }
}
