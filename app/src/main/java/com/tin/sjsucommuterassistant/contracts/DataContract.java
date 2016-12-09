package com.tin.sjsucommuterassistant.contracts;

import android.provider.BaseColumns;

/**
 * Created by mbp on 11/27/16.
 */

public class DataContract
{
    private DataContract(){}

    public static class DataEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "data";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LONG = "long";
        public static final String COLUMN_NAME_TIME = "time";
    }
}
