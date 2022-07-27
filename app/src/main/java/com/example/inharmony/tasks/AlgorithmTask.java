package com.example.inharmony.tasks;

import android.os.AsyncTask;

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

        for (Track track : topTracks.items) {
            AudioFeaturesTracks audioFeaturesTracks = service.getTracksAudioFeatures(track.id);
            List<AudioFeaturesTrack> list = audioFeaturesTracks.audio_features;
            for (AudioFeaturesTrack feature : list) {
                acousticness.add((double) feature.acousticness);
                danceability.add((double) feature.danceability);
                energy.add((double) feature.energy);
                instrumentalness.add((double) feature.instrumentalness);
                speechiness.add((double) feature.speechiness);
                valence.add((double) feature.valence);
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
            sd += Math.pow((list.get(i) - mean), 2);
        }

        sq = sd / list.size();
        result = Math.sqrt(sq);
        return result;
    }

    public double calculateWeight(List<Double> list) {
        double standardDeviation = calculateStandardDeviation(list);
        double result = 10/(standardDeviation + 0.3) - 5;
        return result;
    }

    @Override
    protected void onPostExecute(List<JSONArray> jsonArray) {
        delegate.processFinish(jsonArray);
    }
}
