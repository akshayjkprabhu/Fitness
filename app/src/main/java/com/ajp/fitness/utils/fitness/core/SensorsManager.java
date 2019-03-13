package com.ajp.fitness.utils.fitness.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.*;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * Created by akshay on 1/2/19.
 * Copyright © 2018, Huffy
 * Written under contract by Robosoft Technologies Pvt. Ltd.
 */
class SensorsManager extends BaseManager {
    private static SensorsManager sManager;

    public static SensorsManager getInstance(Activity activity) {
        if (sManager == null) {
            sManager = new SensorsManager(activity);
        }
        return sManager;
    }

    private SensorsManager(Activity activity) {
        super(activity);
    }

    void findFitnessDataSource() {
        Activity activity = getActivity();
        GoogleSignInAccount account = getLastSignedInAccount();
        if (activity != null && account != null) {
            DataSourcesRequest request = getRequest();
            Fitness.getSensorsClient(activity, account)
                    .findDataSources(request)
                    .addOnSuccessListener(this::onDataSourceAvailable)
                    .addOnFailureListener(this::onFailed);
        }
    }

    private void onDataSourceAvailable(List<DataSource> dataSources) {
        for (DataSource dataSource : dataSources) {
            Log.i(TAG, "Data source found: " + dataSource.toString());
            Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

            // Let's register a listener to receive Activity data!
            if (dataSource.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE)
                    && mListener == null) {
                mListener = this::onDataPointsAvailable;
                Log.i(TAG, "Data source for LOCATION_SAMPLE found!  Registering.");
                registerFitnessDataListener(dataSource, DataType.TYPE_LOCATION_SAMPLE);
            }
        }
    }

    private void onFailed(Exception e) {
        Log.e(TAG, "onFailure", e);
    }

    @NonNull
    private DataSourcesRequest getRequest() {
        return new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();
    }


    private OnDataPointListener mListener;

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        Activity activity = getActivity();
        GoogleSignInAccount account = getLastSignedInAccount();
        if (activity != null && account != null) {
            SensorRequest request = new SensorRequest.Builder()
                    .setDataSource(dataSource) // Optional but recommended for custom data sets.
                    .setDataType(dataType) // Can't be omitted.
                    .setSamplingRate(10, TimeUnit.SECONDS)
                    .build();
            Fitness.getSensorsClient(activity, account)
                    .add(request, mListener)
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "Listener registered!"))
                    .addOnFailureListener(e -> Log.e(TAG, "Listener not registered.", e));
        }
    }

    private void onDataPointsAvailable(DataPoint dataPoint) {
        for (Field field : dataPoint.getDataType().getFields()) {
            Value val = dataPoint.getValue(field);
            Log.i(TAG, "Detected DataPoint field: " + field.getName());
            Log.i(TAG, "Detected DataPoint value: " + val);
        }
    }

    /**
     * Unregisters the listener with the Sensors API.
     */
    private void unregisterFitnessDataListener() {
        if (mListener == null) {
            return;
        }
        Activity activity = getActivity();
        GoogleSignInAccount account = getLastSignedInAccount();
        if (activity != null && account != null) {
            // [START unregister_data_listener]
            // Waiting isn't actually necessary as the unregister call will complete regardless,
            // even if called from within onStop, but a callback can still be added in order to
            // inspect the results.
            Fitness.getSensorsClient(activity, account)
                    .remove(mListener)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult()) {
                            Log.i(TAG, "Listener was removed!");
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    });
        }
        // [END unregister_data_listener]
    }

    @Override
    protected void onDestroy() {
        unregisterFitnessDataListener();
        mListener = null;
        super.onDestroy();
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
}
