package com.ajp.fitness.utils.fitness.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/*
 * Created by akshay on 1/2/19.
 * Copyright © 2018, Huffy
 * Written under contract by Robosoft Technologies Pvt. Ltd.
 */
class SubscriptionManager extends BaseManager {

    private static SubscriptionManager sManager;

    public static SubscriptionManager getInstance(Activity activity) {
        if (sManager == null) {
            sManager = new SubscriptionManager(activity);
        }
        return sManager;
    }

    private SubscriptionManager(Activity activity) {
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
     * Subscribes to an available {@link DataType}. Subscriptions can exist across application
     * instances (so data is recorded even after the application closes down).  When creating
     * a new subscription, it may already exist from a previous invocation of this app.  If
     * the subscription already exists, the method is a no-op.  However, you can check this with
     * a special success code.
     *
     * @param dataType is the type to be subscribed specific to the logged in account
     */
    void subscribe(@NonNull DataType dataType) {
        Activity activity = getActivity();
        GoogleSignInAccount account = getLastSignedInAccount();
        if (account != null && activity != null) {
            Fitness.getRecordingClient(activity, account)
                    .subscribe(dataType)
                    .addOnSuccessListener(aVoid -> subscriptionSuccess(dataType))
                    .addOnFailureListener(e -> subscriptionFailed(dataType));
        }
    }

    private void subscriptionSuccess(DataType dataType) {
        Log.i(TAG, "Successfully subscribed!");
        if (getListener() instanceof IFitnessContract.SubscriptionListener) {
            ((IFitnessContract.SubscriptionListener) getListener()).subscribeSuccess(dataType);
        }
    }

    private void subscriptionFailed(DataType dataType) {
        Log.i(TAG, "There was a problem subscribing.");
        if (getListener() instanceof IFitnessContract.SubscriptionListener) {
            ((IFitnessContract.SubscriptionListener) getListener()).subscribeFailed(dataType);
        }
    }

    /**
     * Fetches a list of all active subscriptions and log it. Since the logger for this sample
     * also prints to the screen, we can see what is happening in this way.
     */
    void dumpSubscriptionsList() {
        Activity activity = getActivity();
        GoogleSignInAccount account = getLastSignedInAccount();
        if (account != null && activity != null) {
            // [START list_current_subscriptions]
            Fitness.getRecordingClient(activity, account)
                    .listSubscriptions()
                    .addOnSuccessListener(new OnSuccessListener<List<Subscription>>() {
                        @Override
                        public void onSuccess(List<Subscription> subscriptions) {
                            for (Subscription sc : subscriptions) {
                                DataType dt = sc.getDataType();
                                Log.i(TAG, "Active subscription for data type: " + dt.getName());
                            }
                        }
                    });
        }
        // [END list_current_subscriptions]
    }

    /**
     * Cancels the ACTIVITY_SAMPLE subscription by calling unsubscribe on that {@link DataType}.
     */
    void cancelSubscription(@NonNull DataType dataType) {
        Activity activity = getActivity();
        GoogleSignInAccount account = getLastSignedInAccount();
        if (account != null && activity != null) {
            Fitness.getRecordingClient(activity, account)
                    .unsubscribe(dataType)
                    .addOnSuccessListener(aVoid -> unSubscribeSuccess(dataType))
                    .addOnFailureListener(e -> unSubscribeFailed(dataType));
        }
    }

    private void unSubscribeSuccess(DataType dataType) {
        Log.i(TAG, "Successfully unsubscribed!");
        if (getListener() instanceof IFitnessContract.SubscriptionListener) {
            ((IFitnessContract.SubscriptionListener) getListener()).unSubscribeSuccess(dataType);
        }
    }

    private void unSubscribeFailed(DataType dataType) {
        Log.i(TAG, "There was a problem un subscribing.");
        if (getListener() instanceof IFitnessContract.SubscriptionListener) {
            ((IFitnessContract.SubscriptionListener) getListener()).unSubscribeFailed(dataType);
        }
    }
}
