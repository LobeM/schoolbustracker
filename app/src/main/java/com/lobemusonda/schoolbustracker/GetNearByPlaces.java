package com.lobemusonda.schoolbustracker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by lobemusonda on 10/1/18.
 */

public class GetNearByPlaces extends AsyncTask<Object, String, String> {
    private static final String TAG = "GetNearByPlaces";

    private Spinner mSpinnerPickUp;
    private Spinner mSpinnerDropOff;
    private String mURL;
    private InputStream is;
    private BufferedReader mBufferedReader;
    private StringBuilder mStringBuilder;
    private String data;

    private SpinAdapter mSpinAdapter;
    private ArrayList<BusStation> mStations;

    private Context mContext;

    public GetNearByPlaces (Context context){
        mContext = context;
    }

    @Override
    protected String doInBackground(Object... objects) {
        mURL = (String) objects[0];
        mSpinnerPickUp = (Spinner) objects[1];
        mSpinnerDropOff = (Spinner) objects[2];

        mStations = new ArrayList<>();

        try {
            URL myurl = new URL(mURL);
            Log.d(TAG, "doInBackground: url = " + mURL);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)myurl.openConnection();
            httpsURLConnection.connect();
            is = httpsURLConnection.getInputStream();
            mBufferedReader = new BufferedReader(new InputStreamReader(is));

            String line = "";
            mStringBuilder = new StringBuilder();

            while ((line = mBufferedReader.readLine()) != null) {
                mStringBuilder.append(line);
            }

            data = mStringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: json = " + s);
        try {
            JSONObject parentObject = new JSONObject(s);
            JSONArray resultsArray = parentObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject jsonObject = resultsArray.getJSONObject(i);
                JSONObject locationObject = jsonObject.getJSONObject("geometry").getJSONObject("location");

                String latitude = locationObject.getString("lat");
                String longitude = locationObject.getString("lng");

                JSONObject nameObject = resultsArray.getJSONObject(i);

                String stationName = nameObject.getString("name");

                BusStation busStation = new BusStation();
                busStation.setName(stationName);
                busStation.setLatitude(Double.parseDouble(latitude));
                busStation.setLongitude(Double.parseDouble(longitude));
                mStations.add(busStation);
            }

//            Initialize the adapter sending the current context
            mSpinAdapter = new SpinAdapter(mContext, R.layout.support_simple_spinner_dropdown_item, mStations);
            mSpinnerPickUp.setAdapter(mSpinAdapter);
            mSpinnerDropOff.setAdapter(mSpinAdapter);

            mSpinnerPickUp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                    Here you get the current item that is selected by its position
                    BusStation station = mSpinAdapter.getItem(i);
//                    Here perform desired action
                    Toast.makeText(mContext, "Station is located at" + station.getLatitude() + "," + station.getLongitude(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
