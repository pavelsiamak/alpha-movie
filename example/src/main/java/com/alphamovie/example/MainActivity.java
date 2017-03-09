package com.alphamovie.example;

import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.alphamovie.lib.AlphaMovieView;

public class MainActivity extends AppCompatActivity {
    public static final int[] backgroundResources = new int[]{R.drawable.court_blue,
            R.drawable.court_green, R.drawable.court_red};

    private AlphaMovieView alphaMovieView;

    private View container;
    private int bgIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);

        alphaMovieView = (AlphaMovieView) findViewById(R.id.video_player);
        alphaMovieView.setVideoFromAssets("ball.mp4");
        alphaMovieView.setLooping(false);
    }

    @Override
    public void onResume() {
        alphaMovieView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        alphaMovieView.onPause();
    }

    public void play(View view) {
        alphaMovieView.start();
    }

    public void pause(View view) {
        alphaMovieView.pause();
    }

    public void stop(View view) {
        alphaMovieView.stop();
    }

    public void changeBackground(View view) {
        bgIndex = ++bgIndex % 3;
        container.setBackgroundResource(backgroundResources[bgIndex]);
    }

    public void addNew(View view) {
        alphaMovieView.setVideoFromAssets("ball.mp4");
    }
}
