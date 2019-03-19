package com.pereved.mapboxclicks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

//Created by Squirty on 19.03.2019.
public class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

    private final boolean refreshSource;
    private static final String PROPERTY_NAME = "name";
    private final WeakReference<MainActivity> activityRef;
    private static final String PROPERTY_CAPITAL = "capital";
    private final HashMap<String, View> viewMap = new HashMap<>();

    private GenerateViewIconTask(MainActivity activity, boolean refreshSource) {
        this.activityRef = new WeakReference<>(activity);
        this.refreshSource = refreshSource;
    }

    GenerateViewIconTask(MainActivity activity) {
        this(activity, false);
    }

    @SuppressWarnings("WrongThread")
    @Override
    protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
        MainActivity activity = activityRef.get();
        if(activity != null) {
            HashMap<String, Bitmap> imagesMap = new HashMap<>();
            LayoutInflater inflater = LayoutInflater.from(activity);

            FeatureCollection featureCollection = params[0];

            for(Feature feature : Objects.requireNonNull(featureCollection.features())) {

                BubbleLayout bubbleLayout = (BubbleLayout)
                        inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);


                //TODO запрос заголовка
                String name = feature.getStringProperty(PROPERTY_NAME);
                TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
                titleTextView.setText(name);

                //TODO по идее должно работать, но нет :)
                bubbleLayout.findViewById(R.id.bubble).setOnClickListener(v -> {
                    Timber.d(String.format("Click bubble of %s", name));
                });

                //TODO запрос субтайтла
                String style = feature.getStringProperty(PROPERTY_CAPITAL);
                TextView descriptionTextView = bubbleLayout.findViewById(R.id.info_window_description);
                descriptionTextView.setText("capital");

                int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                bubbleLayout.measure(measureSpec, measureSpec);

                int measuredWidth = bubbleLayout.getMeasuredWidth();

                bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

                Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                imagesMap.put(name, bitmap);
                viewMap.put(name, bubbleLayout);
            }

            return imagesMap;
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
        super.onPostExecute(bitmapHashMap);
        MainActivity activity = activityRef.get();
        if(activity != null && bitmapHashMap != null) {
            activity.setImageGenResults(bitmapHashMap);
            if(refreshSource) {
                activity.refreshSource();
            }
        }
    }
}