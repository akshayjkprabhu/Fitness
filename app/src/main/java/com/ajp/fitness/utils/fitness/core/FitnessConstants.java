package com.ajp.fitness.utils.fitness.core;

/*
 * Created by akshay on 5/3/19.
 * Copyright © 2018, Huffy
 * Written under contract by Robosoft Technologies Pvt. Ltd.
 */
public class FitnessConstants {
    public interface RequestTypes {
        String SUBSCRIBE = "subscription";
        String PERMISSION = "permission";
        String INSERT_SEGMENT = "insert_segment";
        String READ_HISTORY = "read_history";
    }

    public interface Keys {
        String START_TIME = "start_time";
        String LAST_PAUSED_TIME = "pause_time";
        String LAST_RESUMED_TIME = "resume_time";
        String LAST_LOCATION_TIME = "last_known_location_time";
        String LAST_SAVED_LATITUDE = "last_known_latitude";
        String LAST_SAVED_LONGITUDE = "last_known_longitude";
    }
}
