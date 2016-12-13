package com.tin.sjsucommuterassistant.helpers;

import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by mbp on 12/9/16.
 */

public class JSONParserHelper
{

    public static int getDurationFromJSON(String body)
    {
        int duration = -1;
        try {
            JSONObject jsonObject = new JSONObject(body);
            System.out.println("getDurationFromJSON: " + jsonObject.getString("destination_addresses"));
            JSONArray jsonArrayRows = jsonObject.getJSONArray("rows");
            JSONObject jsonObjectElements = jsonArrayRows.getJSONObject(0);
            JSONArray jsonArrayEle = jsonObjectElements.getJSONArray("elements");
            JSONObject jsonObjectDuration = jsonArrayEle.getJSONObject(0);
            JSONObject jsonObjectInnerDuration = jsonObjectDuration.getJSONObject("duration");
            duration = jsonObjectInnerDuration.getInt("value");
            System.out.println("getDurationFromJSON: " + duration);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return duration;

    }

    public static int getDurationInTrafficFromJSON(String body)
    {
        int durationInTraffic = -1;


        try {
            JSONObject jsonObject = new JSONObject(body);
            System.out.println("getDurationFromJSON: " + jsonObject.getString("destination_addresses"));
            JSONArray jsonArrayRows = jsonObject.getJSONArray("rows");
            JSONObject jsonObjectElements = jsonArrayRows.getJSONObject(0);
            JSONArray jsonArrayEle = jsonObjectElements.getJSONArray("elements");
            JSONObject jsonObjectDuration = jsonArrayEle.getJSONObject(0);
            JSONObject jsonObjectInnerDuration = jsonObjectDuration.getJSONObject("duration_in_traffic");
            durationInTraffic = jsonObjectInnerDuration.getInt("value");
            System.out.println("getDurationFromJSON: " + durationInTraffic);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return durationInTraffic;
    }
}
