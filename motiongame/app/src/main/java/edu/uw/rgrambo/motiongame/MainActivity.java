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
import android.view.MotionEvent;

public class MainActivity extends AppCompatActivity implements
        SensorEventListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private SensorManager mSensorManager;

    // Accelermeter handling loosely based on
    // http://examples.javacodegeeks.com/android/core/hardware/sensor/android-accelerometer-example/
    private Sensor mAccelerometer;

    private float lastX;
    private float lastY;
    private float lastZ;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    // Gestures based off of http://developer.android.com/training/gestures/detector.html
    private String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;

    SoundPool soundPool;
    int music;

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
                soundPool.play(music, 1, 1, 0, 0, 1);
            }
        });

        music = soundPool.load(getBaseContext(), R.raw.sandstorm, 1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
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
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        float x = event.getRawX();
        float y = event.getRawY();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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

        // Sets the deltas of the new values
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        // A change below 2 is white noise
        if (deltaX < 2) {
            deltaX = 0;
        }
        if (deltaY < 2) {
            deltaY = 0;
        }

        // Sets the last known values
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];


    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
