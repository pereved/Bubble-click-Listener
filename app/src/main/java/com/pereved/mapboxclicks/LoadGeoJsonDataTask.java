package com.pereved.mapboxclicks;

import android.content.Context;
import android.os.AsyncTask;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

//Created by Squirty on 19.03.2019.
public class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

    private final WeakReference<MainActivity> activityRef;
    private static final String PROPERTY_SELECTED = "selected";

    LoadGeoJsonDataTask(MainActivity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    public FeatureCollection doInBackground(Void... params) {
        MainActivity activity = activityRef.get();

        if(activity == null) {
            return null;
        }

        String geoJson = loadGeoJsonFromAsset(activity);
        return FeatureCollection.fromJson(geoJson);
    }

    @Override
    public void onPostExecute(FeatureCollection featureCollection) {
        super.onPostExecute(featureCollection);
        MainActivity activity = activityRef.get();
        if(featureCollection == null || activity == null) {
            return;
        }

        for(Feature singleFeature : Objects.requireNonNull(featureCollection.features())) {
            singleFeature.addBooleanProperty(PROPERTY_SELECTED, false);
        }

        activity.setUpData(featureCollection);
        new GenerateViewIconTask(activity).execute(featureCollection);
    }

    private static String loadGeoJsonFromAsset(Context context) {
        try {
            InputStream is = context.getAssets().open("us_west_coast.geojson");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
