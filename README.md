## Alpha Movie

Alpha Movie is an Android video player library with alpha channel support.

Video Player uses `OpenGL` to render video and apply *shader* that makes alpha compositing possible. The player encapsulates `MediaPlayer` and has its base functionality.

---

## Gradle Dependency

[ ![jCenter](https://api.bintray.com/packages/pavelsemak/alpha-movie/alpha-movie/images/download.svg) ](https://bintray.com/pavelsemak/alpha-movie/alpha-movie/_latestVersion)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

The easiest way to start using Alpha Movie is to add it as a *Gradle Dependency*. The Gradle dependency is available via [jCenter](https://bintray.com/pavelsemak/alpha-movie/alpha-movie/view). Please make sure that you have the jcenter repository included in the project's `build.gradle` file (*jCenter* is the default Maven repository used by Android Studio):

```gradle
repositories {
    jcenter()
}
```

Then add this dependency to your module's `build.gradle` file:

```gradle
dependencies {
    // ... other dependencies
    compile 'com.alphamovie.library:alpha-movie:1.0.0'
}
```

## Getting Started

Add `AlphaMovieView` into you activity layout:

```xml
<com.alphamovie.lib.AlphaMovieView
    android:id="@+id/video_player"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

Next you need to initialize the player. In your `Activity` class add:

```java
public class MainActivity extends AppCompatActivity {

    private AlphaMovieView alphaMovieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alphaMovieView = (AlphaMovieView) findViewById(R.id.video_player);
        alphaMovieView.setVideoFromAssets("video.mp4");
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
}
```

In this code snippet we load video from *assets* specifying filename `video.mp4`:

```java
alphaMovieView.setVideoFromAssets("video.mp4");
```

Video can also be set by *Url, FileDescriptor, MediaSource* and other sources.

You need to add `alphaMovieView.onPause()` and `alphaMovieView.onResume()` in activity's `onPause()` and `onResume()` callbacks. Calling these methods will pause and resume `OpenGL` rendering thread.

---

## How it works?

Alpha Movie player uses `OpenGL` to render video with a *shader* attached to gl renderer. This *shader* modifies each pixel of video frame. By default it converts *green* color to transparent.
So default alpha channel color is *green*. This color can be changed to *red* or *blue*.

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.alphamovie.lib.AlphaMovieView
        android:id="@+id/video_player"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        custom:shader="red"
        custom:accuracy="0.1"/>
</FrameLayout>
```

By default `accuracy="0.1"`. Accuracy is the value between **0** and **1**. Accuracy should be lower if you wish more shades of specified color be transparent and vice versa.

#### Custom shader

There is a possibility to apply your own *custom shader*. Add shader and `useCustomShader` attrs:

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.alphamovie.lib.AlphaMovieView
        android:id="@+id/video_player"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        custom:shader="@string/shader_custom"
        custom:useCustomShader="true"/>
</FrameLayout>
```

And define your custom shader in *string* values, for example:

```xml
<resources>
    <!-- ... other string resources -->
    <string name="shader_custom">#extension GL_OES_EGL_image_external : require\n
            precision mediump float;
            varying vec2 vTextureCoord;
            uniform samplerExternalOES sTexture;
            varying mediump float text_alpha_out;
            void main() {
              vec4 color = texture2D(sTexture, vTextureCoord);
              if (color.b - color.r >= 0.1 &amp;&amp; color.b - color.g >= 0.1) {
                  gl_FragColor = vec4(color.r, color.g, (color.r + color.g) / 2.0, 1.0 - color.b);
              } else {
                  gl_FragColor = vec4(color.r, color.g, color.b, color.a);
              }
            }
    </string>
</resources>
```

In this case accuracy attr is not affecting anything because it is used only with library-defined shader values such as *red*, *green*, *blue*.
