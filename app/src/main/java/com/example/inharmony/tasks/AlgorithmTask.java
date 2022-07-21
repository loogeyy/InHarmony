package com.example.inharmony.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.inharmony.AsyncResponse;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.AudioFeaturesTracks;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;

public class AlgorithmTask extends AsyncTask<Void, Void, List<JSONArray>> {

    private static final String TAG = "AlgorithmTask";
    public AsyncResponse delegate = null;
    private SpotifyService service;

    public AlgorithmTask(SpotifyService service) {
        this.service = service;
    }

    @Override
    protected List<JSONArray> doInBackground(Void... voids) {
        List<Float> acousticness = new ArrayList<>();
        List<Float> danceability = new ArrayList<>();
        List<Float> energy = new ArrayList<>();
        List<Float> instrumentalness = new ArrayList<>();
        List<Float> speechiness = new ArrayList<>();
        List<Float> valence = new ArrayList<>();
        Pager<Track> topTracks = service.getTopTracks();
        if (topTracks.items.size() == 0) {
            Log.i("song id", "no size");
        }
        int i = 1;
        for (Track track : topTracks.items) {
            Log.i(TAG, "#" + i + " song: " + track.name + " " + track.artists.get(0).name);
            i++;
            AudioFeaturesTracks audioFeaturesTracks = service.getTracksAudioFeatures(track.id);
            List<AudioFeaturesTrack> list = audioFeaturesTracks.audio_features;
            int j = 1;
            for (AudioFeaturesTrack feature : list) {
                Log.i("acousticness", j + ":" + String.valueOf(feature.acousticness));
                acousticness.add(feature.acousticness);

                Log.i("danceability", j + ":" + String.valueOf(feature.danceability));
                danceability.add(feature.danceability);

                Log.i("energy", j + ":" + String.valueOf(feature.energy));
                energy.add(feature.energy);

                Log.i("instrumentalness", j + ":" + String.valueOf(feature.instrumentalness));
                instrumentalness.add(feature.instrumentalness);

                Log.i("speechiness", j + ":" + String.valueOf(feature.speechiness));
                speechiness.add(feature.speechiness);

                Log.i("valence", j + ":" + String.valueOf(feature.valence));
                valence.add(feature.valence);
                j++;
            }
        }

        float avgAcousticness = calculateAverage(acousticness);
        float avgValence = calculateAverage(valence);
        float avgDanceability = calculateAverage(danceability);
        float avgEnergy = calculateAverage(energy);
        float avgInstrumentalness = calculateAverage(instrumentalness);
        float avgSpeechiness = calculateAverage(speechiness);

        try {
            JSONArray featureAvgs = new JSONArray();
            JSONArray featureWeights = new JSONArray();

            featureAvgs.put(avgAcousticness);
            featureAvgs.put(avgValence);
            featureAvgs.put(avgDanceability);
            featureAvgs.put(avgEnergy);
            featureAvgs.put(avgInstrumentalness);
            featureAvgs.put(avgSpeechiness);

            featureWeights.put(1);
            List<JSONArray> data = new ArrayList<>();
            data.add(featureAvgs);
            data.add(featureWeights);

            if (ParseUser.getCurrentUser() != null) {
                ParseUser.getCurrentUser().put("featureAvgs", featureAvgs);
                ParseUser.getCurrentUser().put("featureWeights", featureWeights);
                try {
                    ParseUser.getCurrentUser().save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private float calculateAverage(List<Float> list) {
        float total = 0;
        for (Float number : list) {
            total += number;
        }
        return total / list.size();
    }

    @Override
    protected void onPostExecute(List<JSONArray> jsonArray) {
        delegate.processFinishAvg(jsonArray.get(0));
        delegate.processFinishWeight(jsonArray.get(1));
    }
}
