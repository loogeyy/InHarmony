package com.example.inharmony;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.inharmony.fragments.MatchingFragment;
import com.example.inharmony.tasks.AlgorithmTask;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    MatchingFragment matchingFragment = new MatchingFragment();
    AlgorithmTask algorithmTask = new AlgorithmTask();

    @Test
    public void calculateAverage() {
        List<Double> sameDoublesOne = Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        List<Double> sameDoublesZero = Arrays.asList(0.0, 0.0, 0.0, 0.0 ,0.0 ,0.0);
        assertEquals(1.0, algorithmTask.calculateAverage(sameDoublesOne), 0);
        assertEquals(0, algorithmTask.calculateAverage(sameDoublesZero), 0);

        List<Double> random = Arrays.asList(0.1, 0.25, 0.36, 0.47, 0.58, 0.69);
        assertEquals(0.40833, algorithmTask.calculateAverage(random), 0.001);
    }

    @Test
    public void calculateStandardDeviation() {
        List<Double> sameDoublesOne = Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        List<Double> sameDoublesZero = Arrays.asList(0.0, 0.0, 0.0, 0.0 ,0.0 ,0.0);
        List<Double> polar = Arrays.asList(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        List<Double> evenSpread = Arrays.asList(0.0, 0.2, 0.4, 0.6, 0.8, 1.0);

        assertEquals(0, algorithmTask.calculateStandardDeviation(sameDoublesOne), 0);
        assertEquals(0, algorithmTask.calculateStandardDeviation(sameDoublesZero), 0);
        assertEquals(0.5, algorithmTask.calculateStandardDeviation(polar), 0);
        assertEquals(0.34156, algorithmTask.calculateStandardDeviation(evenSpread), 0.001);
    }

    @Test
    public void calculateWeight() {
        // more concentrated distributions should have a greater weight
        List<Double> sameDoublesOne = Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        List<Double> sameDoublesZero = Arrays.asList(0.0, 0.0, 0.0, 0.0 ,0.0 ,0.0);
        List<Double> polar = Arrays.asList(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        List<Double> evenSpread = Arrays.asList(0.0, 0.2, 0.4, 0.6, 0.8, 1.0);
        List<Double> concentratedSpread = Arrays.asList(0.8, 0.93, 0.87, 0.91, 0.85, 0.82);
        List<Double> outlier = Arrays.asList(0.9, 0.93, 0.93, 0.91, 0.92, 0.04);

        assertEquals(28.3333, algorithmTask.calculateWeight(sameDoublesOne), 0.001);
        assertEquals(28.3333, algorithmTask.calculateWeight(sameDoublesZero), 0.001);
        assertEquals(7.5, algorithmTask.calculateWeight(polar), 0.001);
        assertEquals(10.5868, algorithmTask.calculateWeight(evenSpread), 0.001);
        assertEquals(23.8960, algorithmTask.calculateWeight(concentratedSpread), 0.001);
        assertEquals(10.9391, algorithmTask.calculateWeight(outlier), 0.001);
    }

    @Test
    public void calculateScore() {
        List<Double> myFeatureAvgs;
        List<Double> myFeatureWeights;
        List<Double> otherFeatureAvgs;
        List<Double> otherFeatureWeights;

        myFeatureAvgs = Arrays.asList(0.1, 0.1, 0.1, 0.1, 0.1, 0.1);
        myFeatureWeights = Arrays.asList(20.0, 20.0, 20.0, 20.0, 20.0, 20.0);
        otherFeatureAvgs = Arrays.asList(0.1, 0.1, 0.1, 0.1, 0.1, 0.1);
        otherFeatureWeights = Arrays.asList(20.0, 20.0, 20.0, 20.0, 20.0, 20.0);

        // difference score < 0.06 in order for it to be considered a potential match
        assertEquals(0, matchingFragment.calculateScore(myFeatureAvgs, myFeatureWeights, otherFeatureAvgs, otherFeatureWeights), 0.001);

        // change weights
        myFeatureWeights = Arrays.asList(10.0, 12.2, 14.4, 16.6, 18.8, 20.0);
        assertEquals(0, matchingFragment.calculateScore(myFeatureAvgs, myFeatureWeights, otherFeatureAvgs, otherFeatureWeights), 0.001);

        otherFeatureWeights = Arrays.asList(20.0, 18.2, 16.4, 14.6, 12.8, 10.0);
        assertEquals(0, matchingFragment.calculateScore(myFeatureAvgs, myFeatureWeights, otherFeatureAvgs, otherFeatureWeights), 0.001);

        // outlier with large weight
        myFeatureAvgs = Arrays.asList(0.1, 0.1, 0.1, 0.1, 0.1, 1.0);
        myFeatureWeights = Arrays.asList(5.0, 5.0, 5.0, 5.0, 5.0, 20.0);
        otherFeatureAvgs = Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0, 0.1);
        otherFeatureWeights = Arrays.asList(5.0, 5.0, 5.0, 5.0, 5.0, 20.0);
        assertEquals(0.9, matchingFragment.calculateScore(myFeatureAvgs, myFeatureWeights, otherFeatureAvgs, otherFeatureWeights), 0.001);

        otherFeatureAvgs = Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        assertEquals(0.5, matchingFragment.calculateScore(myFeatureAvgs, myFeatureWeights, otherFeatureAvgs, otherFeatureWeights), 0.001);

        // similar averages, varied weights
        myFeatureAvgs = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5, 0.6);
        myFeatureWeights = Arrays.asList(5.0, 10.0, 15.0, 5.0, 10.0, 20.0);
        otherFeatureAvgs = Arrays.asList(0.11, 0.18, 0.32, 0.41, 0.53, 0.59);
        otherFeatureWeights = Arrays.asList(5.0, 10.0, 15.0, 5.0, 10.0, 20.0);
        assertEquals(0.0169, matchingFragment.calculateScore(myFeatureAvgs, myFeatureWeights, otherFeatureAvgs, otherFeatureWeights), 0.001);


    }

}