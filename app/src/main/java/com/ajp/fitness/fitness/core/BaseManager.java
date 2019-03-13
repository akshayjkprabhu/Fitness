package com.ajp.fitness.fitness.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;

import java.lang.ref.WeakReference;

/*
 * Created by akshay on 1/2/19.
 * Copyright © 2018, Huffy
 * Written under contract by Robosoft Technologies Pvt. Ltd.
 */
abstract class BaseManager {
    protected final String TAG = this.getClass().getSimpleName();
    private WeakReference<Activity> mActivity;

    protected WeakReference<IFitnessContract.FitnessListener> mListener;

    BaseManager(Activity activity) {
        this.mActivity = new WeakReference<>(activity);
    }

    protected Activity getActivity() {
        return mActivity != null ? mActivity.get() : null;
    }

    @Nullable
    GoogleSignInAccount getLastSignedInAccount() {
        Activity activity = getActivity();
        if (activity != null) {
            return GoogleSignIn.getLastSignedInAccount(activity);
        }
        return null;
    }

    protected void onDestroy() {
        mActivity = null;
    }

    String getPackageName() {
        String packageName = "";
        Activity activity = getActivity();
        if (activity != null) {
            packageName = activity.getPackageName();
        }
        return packageName;
    }

    /**
     * checks if the signed in account has the required permissions defined by FitnessOptions
     */
    boolean hasAccessPermissions(@NonNull FitnessOptions options) {
        GoogleSignInAccount account = getLastSignedInAccount();
        return GoogleSignIn.hasPermissions(account, options);
    }

    /**
     * @param options     set of permissions defined
     * @param requestCode for activity result
     */
    void requestAccessPermissions(@NonNull FitnessOptions options, int requestCode) {
        boolean hasPermissions = hasAccessPermissions(options);
        if (hasPermissions) {
            onAccessPermissionsAvailable(requestCode);
        } else {
            requestPermissions(options, requestCode);
        }
    }

    private void requestPermissions(@NonNull FitnessOptions options, int requestCode) {
        Activity activity = getActivity();
        if (activity != null) {
            GoogleSignInAccount account = getLastSignedInAccount();
            GoogleSignIn.requestPermissions(activity, requestCode, account, options);
        }
    }

    abstract void onAccessPermissionsAvailable(int requestCode);

    abstract void onAccessPermissionsDenied(int requestCode);

    boolean onFitnessResults(int requestCode, int resultCode) {
        if (isFitnessResult(requestCode)) {
            if (resultCode == Activity.RESULT_OK) {
                onAccessPermissionsAvailable(requestCode);
            } else {
                onAccessPermissionsDenied(requestCode);
            }
            return true;
        }
        return false;
    }

    abstract boolean isFitnessResult(int requestCode);

    protected void setListener(IFitnessContract.FitnessListener listener) {
        if (listener != null) {
            mListener = new WeakReference<>(listener);
        } else {
            mListener = null;
        }
    }

    protected IFitnessContract.FitnessListener getListener() {
        if (mListener != null) {
            return mListener.get();
        }
        return null;
    }
}
