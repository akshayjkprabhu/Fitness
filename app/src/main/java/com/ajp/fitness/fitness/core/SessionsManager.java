package com.ajp.fitness.fitness.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.*;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * Created by akshay on 1/2/19.
 * Copyright © 2018, Huffy
 * Written under contract by Robosoft Technologies Pvt. Ltd.
 */
class SessionsManager extends BaseManager {
    private static SessionsManager sManager;

    public static SessionsManager getInstance(Activity activity) {
        if (sManager == null) {
            sManager = new SessionsManager(activity);
        }
        return sManager;
    }

    private SessionsManager(Activity activity) {
        super(activity);
    }

    private String SAMPLE_SESSION_NAME = "Riding 2";

    public Task<Void> insertSession() {
        //First, create a new session and an insertion request.
        SessionInsertRequest insertRequest = insertFitnessSession();

        // [START insert_session]
        // Then, invoke the Sessions API to insert the session and await the result,
        // which is possible here because of the AsyncTask. Always include a timeout when
        // calling await() to avoid hanging that can occur from the service being shutdown
        // because of low memory or other conditions.
        Log.i(TAG, "Inserting the session in the Sessions API");
        return Fitness.getSessionsClient(getActivity(), getLastSignedInAccount())
                .insertSession(insertRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // At this point, the session has been inserted and can be read.
                        Log.i(TAG, "Session insert was successful!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem inserting the session: " +
                                e.getLocalizedMessage());
                    }
                });
    }

    private SessionInsertRequest insertFitnessSession() {
        Log.i(TAG, "Creating a new session for an afternoon run");
        // Setting start and end times for our run.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        // Set a range of the run, using a start time of 30 minutes before this moment,
        // with a 10-minute walk in the middle.
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -10);
        long endWalkTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -10);
        long startWalkTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -10);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource speedDataSource = new DataSource.Builder()
                .setAppPackageName(getPackageName())
                .setDataType(DataType.TYPE_SPEED)
                .setName(SAMPLE_SESSION_NAME + "- speed")
                .setType(DataSource.TYPE_RAW)
                .build();

        float runSpeedMps = 10;
        float walkSpeedMps = 3;
        // Create a data set of the run speeds to include in the session.
        DataSet speedDataSet = DataSet.create(speedDataSource);

        DataPoint firstRunSpeed = speedDataSet.createDataPoint()
                .setTimeInterval(startTime, startWalkTime, TimeUnit.MILLISECONDS);
        firstRunSpeed.getValue(Field.FIELD_SPEED).setFloat(runSpeedMps);
        speedDataSet.add(firstRunSpeed);

        DataPoint walkSpeed = speedDataSet.createDataPoint()
                .setTimeInterval(startWalkTime, endWalkTime, TimeUnit.MILLISECONDS);
        walkSpeed.getValue(Field.FIELD_SPEED).setFloat(walkSpeedMps);
        speedDataSet.add(walkSpeed);

        DataPoint secondRunSpeed = speedDataSet.createDataPoint()
                .setTimeInterval(endWalkTime, endTime, TimeUnit.MILLISECONDS);
        secondRunSpeed.getValue(Field.FIELD_SPEED).setFloat(runSpeedMps);
        speedDataSet.add(secondRunSpeed);

        // [START build_insert_session_request_with_activity_segments]
        // Create a second DataSet of ActivitySegments to indicate the runner took a 10-minute walk
        // in the middle of the run.
        DataSource activitySegmentDataSource = new DataSource.Builder()
                .setAppPackageName(this.getPackageName())
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setName(SAMPLE_SESSION_NAME + "-activity segments")
                .setType(DataSource.TYPE_RAW)
                .build();
        DataSet activitySegments = DataSet.create(activitySegmentDataSource);

        DataPoint firstRunningDp = activitySegments.createDataPoint()
                .setTimeInterval(startTime, startWalkTime, TimeUnit.MILLISECONDS);
        firstRunningDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.BIKING);
        activitySegments.add(firstRunningDp);

        DataPoint walkingDp = activitySegments.createDataPoint()
                .setTimeInterval(startWalkTime, endWalkTime, TimeUnit.MILLISECONDS);
        walkingDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.BIKING_MOUNTAIN);
        //  activitySegments.add(walkingDp);

        DataPoint secondRunningDp = activitySegments.createDataPoint()
                .setTimeInterval(endWalkTime, endTime, TimeUnit.MILLISECONDS);
        secondRunningDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.BIKING);
        activitySegments.add(secondRunningDp);

        // [START build_insert_session_request]
        // Create a session with metadata about the activity.
        Session session = new Session.Builder()
                //.setName(SAMPLE_SESSION_NAME)
               // .setDescription("Long run around Shoreline Park")
                .setIdentifier("biking_id6")
                .setActivity(FitnessActivities.BIKING)
               // .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS)
                .build();

        // Build a session insert request
        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(session)
                //.addDataSet(speedDataSet)
                //.addDataSet(activitySegments)
                .build();
        // [END build_insert_session_request]
        // [END build_insert_session_request_with_activity_segments]

        return insertRequest;
    }

    /**
     * Current system time as start time
     *
     * @param name        name of the session
     * @param identifier  session identifier (Can be ride Id)
     * @param description description of session
     */
    void startSession(String name, String identifier, String description) {
        startSession(name, identifier, description, Calendar.getInstance().getTimeInMillis());
    }

    /**
     * @param name        name of the session
     * @param identifier  session identifier (Can be ride Id)
     * @param description description of session
     * @param startTime   is the start time in milli seconds
     */
    void startSession(String name, String identifier, String description, long startTime) {
        Session session = getSession(name, identifier, description, startTime);
        Task<Void> startSessionTask = startSession(session);
        if (startSessionTask != null) {
            startSessionTask.addOnSuccessListener(aVoid -> {
                if (getListener() instanceof IFitnessContract.SessionListener) {
                    ((IFitnessContract.SessionListener) getListener()).sessionStarted(identifier);
                }
            }).addOnFailureListener(e -> {
                if (getListener() != null) {
                   // getListener().onFailure(e.getMessage());
                }
            });
        }
    }

    /**
     * Starts a session with the session builder passed
     *
     * @return returns a Task which may/ may not be handled
     */
    private Task<Void> startSession(@NonNull Session session) {
        if (getActivity() != null && getLastSignedInAccount() != null) {
            return Fitness.getSessionsClient(getActivity(), getLastSignedInAccount())
                    .startSession(session);
        }
        return null;
    }

    void endSession(String identifier) {
        if (getActivity() != null && getLastSignedInAccount() != null) {
            Fitness.getSessionsClient(getActivity(), getLastSignedInAccount())
                    .stopSession(identifier)
                    .addOnSuccessListener(sessions -> {
                        if (getListener() instanceof IFitnessContract.SessionListener) {
                            ((IFitnessContract.SessionListener) getListener()).sessionEnded(identifier);
                        }
                    }).addOnFailureListener(e -> {
                if (getListener() != null) {
                    //getListener().onFailure(e.toString());
                }
            });
        }
    }

    private Session getSession(String name, String identifier, String description, long startTimeMillis) {
        return new Session.Builder()
                .setName(name)
                .setIdentifier(identifier)
                .setDescription(description)

                .setActivity(FitnessActivities.BIKING)
                .setStartTime(startTimeMillis,
                        TimeUnit.MILLISECONDS)
                .build();
    }

    public Task<SessionReadResponse> verifySession() {
        // Begin by creating the query.
        SessionReadRequest readRequest = readFitnessSession();

        // [START read_session]
        // Invoke the Sessions API to fetch the session with the query and wait for the result
        // of the read request. Note: Fitness.SessionsApi.readSession() requires the
        // ACCESS_FINE_LOCATION permission.
        return Fitness.getSessionsClient(getActivity(), getLastSignedInAccount())
                .readSession(readRequest)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @Override
                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                        // Get a list of the sessions that match the criteria to check the result.
                        List<Session> sessions = sessionReadResponse.getSessions();
                        Log.i(TAG, "Session read was successful. Number of returned sessions is: "
                                + sessions.size());

                        for (Session session : sessions) {
                            // Process the session
                            dumpSession(session);

                            // Process the data sets for this session
                            List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                            for (DataSet dataSet : dataSets) {
                                dumpDataSet(dataSet);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed to read session");
                    }
                });
        // [END read_session]
    }

    private SessionReadRequest readFitnessSession() {
        Log.i(TAG, "Reading History API results for session: " + SAMPLE_SESSION_NAME);
        // [START build_read_session_request]
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_SPEED)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_CYCLING_WHEEL_REVOLUTION)
                .read(DataType.TYPE_CYCLING_PEDALING_CUMULATIVE)
                .read(DataType.TYPE_CYCLING_PEDALING_CADENCE)
                .setSessionId("biking_id6")
                //.setSessionName(SAMPLE_SESSION_NAME)
                .build();
        // [END build_read_session_request]

        return readRequest;
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            DateFormat dateFormat = DateFormat.getTimeInstance();
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    private void dumpSession(Session session) {
        DateFormat dateFormat = DateFormat.getTimeInstance();
        Log.i(TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }

    public void deleteSession() {
        Log.i(TAG, "Deleting today's session data for speed");

        // Set a start and end time for our data, using a start time of 1 day before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Create a delete request object, providing a data type and a time interval
        DataDeleteRequest request = new DataDeleteRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .addDataType(DataType.TYPE_SPEED)
                .deleteAllSessions() // Or specify a particular session here
                .build();

        // Delete request using HistoryClient and specify listeners that will check the result.
        Fitness.getHistoryClient(getActivity(), getLastSignedInAccount())
                .deleteData(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully deleted today's sessions");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // The deletion will fail if the requesting app tries to delete data
                        // that it did not insert.
                        Log.i(TAG, "Failed to delete today's sessions");
                    }
                });
    }


    @Override
    protected void onDestroy() {
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

    void insertSession(String rideName, String rideId, long startTime, long endTime) {
        Log.d(TAG, "insertSession : " + rideName);
        GoogleSignInAccount account = getLastSignedInAccount();
        Activity activity = getActivity();
        if (account != null && activity != null) {
            SessionInsertRequest request = getRideSession(rideName, rideId, startTime, endTime);
            if (request != null) {
                Fitness.getSessionsClient(activity, account)
                        .insertSession(request)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, rideName + " inserted successfully"))
                        .addOnFailureListener(e -> Log.d(TAG, "Could not insert " + rideName));
            }
        }
    }

    @Nullable
    private SessionInsertRequest getRideSession(String rideName, String rideId, long startTime, long endTime) {
        if (startTime >= endTime) {
            return null;
        }
        Session session = new Session.Builder()
                .setName(rideName)
                .setIdentifier(rideId)
                .setActivity(FitnessActivities.BIKING)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS)
                .build();
        return new SessionInsertRequest.Builder()
                .setSession(session)
                .build();
    }
}
