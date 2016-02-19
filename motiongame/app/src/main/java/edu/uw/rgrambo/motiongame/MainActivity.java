package edu.uw.rgrambo.motiongame;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        SensorEventListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private SensorManager mSensorManager;

    // Accelermeter handling loosely based on
    // http://examples.javacodegeeks.com/android/core/hardware/sensor/android-accelerometer-example/
    private Sensor mAccelerometer;
    private Sensor mSensor;

    private float lastX;
    private float lastY;
    private float lastZ;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float shakeThreshold = 4;

    private float soundRate = 1;

    private DrawingSurfaceView drawingSurfaceView;

    // Gestures based off of http://developer.android.com/training/gestures/detector.html
    private String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;

    SoundPool soundPool;

    float[] mGravity;
    float[] mGeomagnetic;

    int Tick;
    int Tap;
    int SwipeLeft;
    int SwipeRight;
    int SwipeDown;
    int Bomb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get accelerometer
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Get Gesture Detector
        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        // Set the hardware buttons to control the music
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Set up music
        if((android.os.Build.VERSION.SDK_INT) == 21){
            soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .build();
        }
        else{
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

            }
        });

        Tick = soundPool.load(getBaseContext(), R.raw.tick, 1);
        Tap = soundPool.load(getBaseContext(), R.raw.drum, 1);
        SwipeRight = soundPool.load(getBaseContext(), R.raw.clap, 1);
        SwipeLeft = soundPool.load(getBaseContext(), R.raw.cowbell, 1);
        SwipeDown = soundPool.load(getBaseContext(), R.raw.crash, 1);
        Bomb = soundPool.load(getBaseContext(), R.raw.bomb, 1);

        drawingSurfaceView = ((DrawingSurfaceView)findViewById(R.id.surfaceView));

        MenuButtons();
        ((Button)findViewById(R.id.start)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingSurfaceView.NewGame();
                drawingSurfaceView.Playing = true;
                PlayingButtons();
            }
        });
        ((Button)findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingSurfaceView.Playing = false;
                drawingSurfaceView.ClearAll();
                MenuButtons();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void PlayingButtons() {
        ((Button)findViewById(R.id.start)).setEnabled(false);
        ((Button)findViewById(R.id.stop)).setEnabled(true);
    }

    public void MenuButtons() {
        ((Button)findViewById(R.id.start)).setEnabled(true);
        ((Button)findViewById(R.id.stop)).setEnabled(false);
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
        float dx = event1.getX() - event2.getX();
        float dy = event1.getY() - event2.getY();
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                List<Float> LeftHits = drawingSurfaceView.LeftHits;
                DeleteBeat(LeftHits, SwipeLeft);
            } else {
                List<Float> RightHits = drawingSurfaceView.RightHits;
                DeleteBeat(RightHits, SwipeRight);
            }
        } else {
            if (dy < 0) {
                List<Float> DownHits = drawingSurfaceView.DownHits;
                DeleteBeat(DownHits, SwipeDown);
            } else {
                // do nothing for up
            }
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        List<Float> UpHits = drawingSurfaceView.UpHits;
        DeleteBeat(UpHits, Tap);
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    public void playTick() {
        soundPool.play(Tick, 1, 1, 0, 0, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;

            // Sets the deltas of the new values
            deltaX = Math.abs(lastX - event.values[0]);
            deltaY = Math.abs(lastY - event.values[1]);
            deltaZ = Math.abs(lastZ - event.values[2]);

            // Sets the last known values
            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];

            if (deltaX > shakeThreshold || deltaY > shakeThreshold || deltaZ > shakeThreshold) {
                drawingSurfaceView.ShakeEvent();
                if (drawingSurfaceView.Playing && drawingSurfaceView.ShakeCount > 0) {
                    soundPool.play(Bomb, 1, 1, 0, 0, soundRate);
                }
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                Log.e("test", orientation[0] + "");
            }
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {return false;}

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    private void DeleteBeat(List<Float> Hits, int sound) {
        for (int i = 0; i < Hits.size(); i++) {
            Hits.set(i, Hits.get(i) - 2f);
            if (Hits.get(i) <= 100 && Hits.get(i) > 65) {
                Hits.remove(i);
                drawingSurfaceView.Points++;
                soundPool.play(sound, 1, 1, 0, 0, soundRate);
            }
        }
    }
}
