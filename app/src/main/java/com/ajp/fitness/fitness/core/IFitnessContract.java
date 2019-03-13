package com.ajp.fitness.fitness.core;

import android.support.annotation.NonNull;

import com.google.android.gms.fitness.data.DataType;

/*
 * Created by akshay on 12/2/19.
 * Copyright © 2018, Huffy
 * Written under contract by Robosoft Technologies Pvt. Ltd.
 */
public interface IFitnessContract {
    interface FitnessService {

        void initClient();

        void readHistory(long startTime, long endTime, DataType... dataTypes);

        void startRead(long startTime, long repeatInterval, DataType... dataTypes);

        void cancelRead();

        void insertData();

        void getSensorData();

        void subscribe(@NonNull DataType dataType);

        void listSubscriptions();

        void unsubscribe(@NonNull DataType dataType);

        void subscriptionListener(SubscriptionListener listener);

        void historyListener(HistoryListener listener);

        void sessionListener(SessionListener listener);

        void startSession(String id);

        void endSession(String id);

        void insertSession();

        void verifySession();

        void deleteSession();

        void insertRideActivity(long startTime, long endTime);

        void stopTrackActivity();

        boolean onFitnessActivityResults(int requestCode, int resultCode);

        void trackActivity(long startTime, int repeatInterval);

        void insertDistance(long startTime, long endTime, float distance);

        void insertRideSession(String rideName, String rideId, long startTime, long endTime);

        boolean hasAccessPermissions();
    }

    interface FitnessListener {
        //todo common interface to be extended by all the result listener

        void onFailure(String message, String type);

        void onSuccess(String message, String type);
    }

    interface SubscriptionListener extends FitnessListener {
        void subscribeSuccess(DataType dataType);

        void subscribeFailed(DataType dataType);

        void unSubscribeSuccess(DataType dataType);

        void unSubscribeFailed(DataType dataType);
    }

    interface HistoryListener extends FitnessListener {
        void onCaloriesBurntAvailable(float calories);
    }

    interface SessionListener extends FitnessListener {
        void sessionStarted(String sessionId);

        void sessionEnded(String sessionId);
    }
}
