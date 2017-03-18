/*
 * Copyright 2017 Pavel Semak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alphamovie.example;

import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.alphamovie.lib.AlphaMovieView;

public class MainActivity extends AppCompatActivity {
    public static final String FILENAME = "ball.mp4";

    public static final int FIRST_BG_INDEX = 0;
    public static final int BG_ARRAY_LENGTH = 3;

    public static final int[] backgroundResources = new int[]{R.drawable.court_blue,
            R.drawable.court_green, R.drawable.court_red};

    private AlphaMovieView alphaMovieView;

    private ImageView imageViewBackground;
    private int bgIndex = FIRST_BG_INDEX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_main);

        imageViewBackground = (ImageView) findViewById(R.id.image_background);

        alphaMovieView = (AlphaMovieView) findViewById(R.id.video_player);
        alphaMovieView.setVideoFromAssets(FILENAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        alphaMovieView.onResume();
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
        bgIndex = ++bgIndex % BG_ARRAY_LENGTH;
        imageViewBackground.setImageResource(backgroundResources[bgIndex]);
    }
}
