package com.example.inharmony;

import org.json.JSONArray;

import java.util.List;

public interface AsyncResponse {
    public void processFinish(List<JSONArray> featureList);
}
