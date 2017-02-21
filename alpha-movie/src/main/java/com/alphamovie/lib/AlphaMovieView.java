package com.alphamovie.lib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;

@SuppressLint("ViewConstructor")
public class AlphaMovieView extends GLTextureView {

    private static final String TAG = "VideoSurfaceView";

    private static final float VIEW_ASPECT_RATIO = 4f / 5f;
    private float videoAspectRatio = VIEW_ASPECT_RATIO;

    VideoRenderer renderer;
    private MediaPlayer mediaPlayer;

    private OnVideoStartedListener onVideoStartedListener;
    private OnVideoEndedListener onVideoEndedListener;

    private boolean isSurfaceCreated;
    private boolean isDataSourceSet;

    private PlayerState state = PlayerState.NOT_PREPARED;

    public AlphaMovieView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        initMediaPlayer();

        renderer = new VideoRenderer();
        this.addOnSurfacePrepareListener();
        setRenderer(renderer);

        bringToFront();
        setPreserveEGLContextOnPause(true);
        setOpaque(false);
    }

    public void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setLooping(true);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                state = PlayerState.PAUSED;
                if (onVideoEndedListener != null) {
                    onVideoEndedListener.onVideoEnded();
                }
            }
        });
        /*queueEvent(new Runnable() {
            public void run() {
                renderer.prepareSurface();
            }
        });*/
    }

    private void addOnSurfacePrepareListener() {
        if (renderer != null) {
            renderer.setOnSurfacePrepareListener(new VideoRenderer.OnSurfacePrepareListener() {
                @Override
                public void surfacePrepared(Surface surface) {
                    isSurfaceCreated = true;
                    mediaPlayer.setSurface(surface);
                    surface.release();
                    if (isDataSourceSet) {
                        prepareAndStartMediaPlayer();
                    }
                }
            });
        }
    }

    private void prepareAndStartMediaPlayer() {
        prepareAsync(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                start();
            }
        });
    }

    private void calculateVideoAspectRatio(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            videoAspectRatio = (float) videoWidth / videoHeight;
        }

        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        double currentAspectRatio = (double) widthSize / heightSize;
        if (currentAspectRatio > videoAspectRatio) {
            widthSize = (int) (heightSize * videoAspectRatio);
        } else {
            heightSize = (int) (widthSize / videoAspectRatio);
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, widthMode),
                MeasureSpec.makeMeasureSpec(heightSize, heightMode));
    }

    private void onDataSourceSet(MediaMetadataRetriever retriever) {
        int videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        calculateVideoAspectRatio(videoWidth, videoHeight);
        isDataSourceSet = true;

        if (isSurfaceCreated) {
            prepareAndStartMediaPlayer();
        }
    }

    public void setVideoFromAssets(String assetsFileName) {
        reset();

        try {
            AssetFileDescriptor assetFileDescriptor = getContext().getAssets().openFd(assetsFileName);
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());

            onDataSourceSet(retriever);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void setVideoByUrl(String url) {
        reset();

        try {
            mediaPlayer.setDataSource(url);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(url, new HashMap<String, String>());

            onDataSourceSet(retriever);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void setVideoFromFile(FileDescriptor fileDescriptor) {
        reset();

        try {
            mediaPlayer.setDataSource(fileDescriptor);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(fileDescriptor);

            onDataSourceSet(retriever);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void setVideoFromFile(FileDescriptor fileDescriptor, int startOffset, int endOffset) {
        reset();

        try {
            mediaPlayer.setDataSource(fileDescriptor, startOffset, endOffset);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(fileDescriptor, startOffset, endOffset);

            onDataSourceSet(retriever);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @TargetApi(23)
    public void setVideoFromMediaDataSource(MediaDataSource mediaDataSource) {
        reset();

        mediaPlayer.setDataSource(mediaDataSource);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mediaDataSource);

        onDataSourceSet(retriever);
    }

    @Override
    public void onResume() {
        //start();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    private void prepareAsync(final MediaPlayer.OnPreparedListener onPreparedListener) {
        if (mediaPlayer != null && state == PlayerState.NOT_PREPARED
                || state == PlayerState.STOPPED) {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    state = PlayerState.PREPARED;
                    onPreparedListener.onPrepared(mp);
                }
            });
            mediaPlayer.prepareAsync();
        }
    }

    public void start() {
        if (mediaPlayer != null) {
            switch (state) {
                case PREPARED:
                    mediaPlayer.start();
                    state = PlayerState.STARTED;
                    if (onVideoStartedListener != null) {
                        onVideoStartedListener.onVideoStarted();
                    }
                    break;
                case PAUSED:
                    mediaPlayer.start();
                    state = PlayerState.STARTED;
                    break;
                case STOPPED:
                    prepareAsync(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                            state = PlayerState.STARTED;
                            if (onVideoStartedListener != null) {
                                onVideoStartedListener.onVideoStarted();
                            }
                        }
                    });
                    break;
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null && state == PlayerState.STARTED) {
            mediaPlayer.pause();
            state = PlayerState.PAUSED;
        }
    }

    public void stop() {
        if (mediaPlayer != null && (state == PlayerState.STARTED || state == PlayerState.PAUSED)) {
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    mediaPlayer.stop();
                    state = PlayerState.STOPPED;
                }
            });
            mediaPlayer.seekTo(0);
        }
    }

    public void reset() {
        if (mediaPlayer != null && (state == PlayerState.STARTED || state == PlayerState.PAUSED ||
                state == PlayerState.STOPPED)) {
            mediaPlayer.reset();
            state = PlayerState.NOT_PREPARED;
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            state = PlayerState.RELEASE;
        }
    }

    public void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    public void setLooping(boolean looping) {
        mediaPlayer.setLooping(looping);
    }

    public void setOnVideoStartedListener(OnVideoStartedListener onVideoStartedListener) {
        this.onVideoStartedListener = onVideoStartedListener;
    }

    public void setOnVideoEndedListener(OnVideoEndedListener onVideoEndedListener) {
        this.onVideoEndedListener = onVideoEndedListener;
    }

    public void setTransparentShader() {
        renderer.setTransparentShader();
    }

    public interface OnVideoStartedListener {
        void onVideoStarted();
    }

    public interface OnVideoEndedListener {
        void onVideoEnded();
    }

    private enum PlayerState {
        NOT_PREPARED, PREPARED, STARTED, PAUSED, STOPPED, RELEASE
    }
}