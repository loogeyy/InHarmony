package com.example.inharmony;



public interface Player {

    void play(String url);

    void pause();

    void resume();

    boolean isPlaying();


    String getCurrentTrack();

    void release();
}
