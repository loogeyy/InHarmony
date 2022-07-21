package com.example.inharmony;

import org.json.JSONArray;

import java.util.List;

public interface AsyncResponse {
    public void processFinishAvg(JSONArray featureAvgs);
    public void processFinishWeight(JSONArray featureWeights);
}
