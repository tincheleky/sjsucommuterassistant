package com.tin.sjsucommuterassistant.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by mbp on 12/9/16.
 */
// Fetches all places from GooglePlaces AutoComplete Web Service
public class DistanceMatrixTask extends AsyncTask<String, Void, String>{

    @Override
    protected String doInBackground(String... params) {
        // For storing data from web service
        String data = "";

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyA7qp9aVHzsRTtNs4TT99bSYQYt0lUdPn4";

        //https://maps.googleapis.com/maps/api/distancematrix/json?
        // origins=37.359289,-121.966124
        // &destinations=37.335143,-121.881276
        // &mode=driving&departure_time=now
        // &traffic_mode=best_guess
        // &key=AIzaSyA7qp9aVHzsRTtNs4TT99bSYQYt0lUdPn4

        // Current Location
        String origins = "origins=" + params[0];

        // SJSU coordinate
        String destinations = "destinations=37.335143,-121.881276";

        // Mode
        String mode = "mode=driving";

        /// Departure_time
        String departure_time = "departure_time=now";

        //traffic_mode
        String traffic_mode = "traffic_mode=best_guess";



        // Building the parameters to the web service
        String parameters = origins + "&" + destinations + "&" + mode + "&" + departure_time + "&" + traffic_mode + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/distancematrix/" + output + "?" + parameters;

        try{
            // Fetching the data from we service
            data = downloadUrl(url);
            System.out.println(url);
        }catch(Exception e){
            Log.d("Background Task",e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

//        // Creating ParserTask
//        parserTask = new ParserTask();
//
//        // Starting Parsing the JSON string returned by Web Service
//        parserTask.execute(result);
        System.out.println("API CALL FOR DISTANCE MATRIX");
        System.out.println(result);

    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
