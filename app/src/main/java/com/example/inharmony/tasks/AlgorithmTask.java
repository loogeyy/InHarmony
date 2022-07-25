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

    public AlgorithmTask() {}

    public AlgorithmTask(SpotifyService service) {
        this.service = service;
    }

    @Override
    protected List<JSONArray> doInBackground(Void... voids) {
        List<Double> acousticness = new ArrayList<>();
        List<Double> danceability = new ArrayList<>();
        List<Double> energy = new ArrayList<>();
        List<Double> instrumentalness = new ArrayList<>();
        List<Double> speechiness = new ArrayList<>();
        List<Double> valence = new ArrayList<>();
        Pager<Track> topTracks = service.getTopTracks();

        int i = 1;
        for (Track track : topTracks.items) {
           // Log.i(TAG, "#" + i + " song: " + track.name + " " + track.artists.get(0).name);
            i++;
            AudioFeaturesTracks audioFeaturesTracks = service.getTracksAudioFeatures(track.id);
            List<AudioFeaturesTrack> list = audioFeaturesTracks.audio_features;
            int j = 1;
            for (AudioFeaturesTrack feature : list) {
               // Log.i("acousticness", j + ":" + String.valueOf(feature.acousticness));
                acousticness.add((double) feature.acousticness);

               // Log.i("danceability", j + ":" + String.valueOf(feature.danceability));
                danceability.add((double) feature.danceability);

               // Log.i("energy", j + ":" + String.valueOf(feature.energy));
                energy.add((double) feature.energy);

               // Log.i("instrumentalness", j + ":" + String.valueOf(feature.instrumentalness));
                instrumentalness.add((double) feature.instrumentalness);

                //Log.i("speechiness", j + ":" + String.valueOf(feature.speechiness));
                speechiness.add((double) feature.speechiness);

               // Log.i("valence", j + ":" + String.valueOf(feature.valence));
                valence.add((double) feature.valence);
                j++;
            }
        }

        double avgAcousticness = calculateAverage(acousticness);
        double avgValence = calculateAverage(valence);
        double avgDanceability = calculateAverage(danceability);
        double avgEnergy = calculateAverage(energy);
        double avgInstrumentalness = calculateAverage(instrumentalness);
        double avgSpeechiness = calculateAverage(speechiness);

        double acousticnessWeight = calculateWeight(acousticness);
        double valenceWeight = calculateWeight(valence);
        double danceabilityWeight = calculateWeight(danceability);
        double energyWeight = calculateWeight(energy);
        double instrumentalnessWeight = calculateWeight(instrumentalness);
        double speechinessWeight = calculateWeight(speechiness);

        try {
            JSONArray featureAvgs = new JSONArray();
            JSONArray featureWeights = new JSONArray();

            featureAvgs.put(avgAcousticness);
            featureAvgs.put(avgValence);
            featureAvgs.put(avgDanceability);
            featureAvgs.put(avgEnergy);
            featureAvgs.put(avgInstrumentalness);
            featureAvgs.put(avgSpeechiness);

            featureWeights.put(acousticnessWeight);
            featureWeights.put(valenceWeight);
            featureWeights.put(danceabilityWeight);
            featureWeights.put(energyWeight);
            featureWeights.put(instrumentalnessWeight);
            featureWeights.put(speechinessWeight);

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
            //Log.i(TAG, "featureavgs: " + featureAvgs.toString());
            //Log.i(TAG, "featureweight" + featureWeights.toString());
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Double calculateAverage(List<Double> list) {
        double total = 0;
        for (Double number : list) {
            total += number;
        }
        return total / list.size();
    }

    public Double calculateStandardDeviation(List<Double> list) {
        double sum = 0;
        double mean = 0;
        double sd = 0;
        double sq = 0;
        double result = 0;

        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }

        mean = sum / list.size();

        for (int i = 0; i < list.size(); i++) {
            sd += (double) (Math.pow((list.get(i) - mean), 2));
        }

        sq = sd / list.size();
        result = (double) Math.sqrt(sq);
        //Log.i("Standard Deviation", String.valueOf(result));
        return result;
    }

    public double calculateWeight(List<Double> list) {
        double standardDeviation = calculateStandardDeviation(list);
        double result = (double) (10/(standardDeviation + 0.3) - 5);
        return result;
    }

    @Override
    protected void onPostExecute(List<JSONArray> jsonArray) {
        //Log.i(TAG, "post execute");
        delegate.processFinish(jsonArray);
    }
}
