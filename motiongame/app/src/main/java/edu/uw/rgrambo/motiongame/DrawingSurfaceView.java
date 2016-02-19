package edu.uw.rgrambo.motiongame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

/**
 * An example SurfaceView for generating graphics on
 * @author Joel Ross
 * Editted by Ross Grambo
 * @version Winter 2016
 */
public class DrawingSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "SurfaceView";

    private int viewWidth, viewHeight; //size of the view

    private Bitmap bmp; //image to draw on

    private SurfaceHolder mHolder; //the holder we're going to post updates to
    private DrawingRunnable mRunnable; //the code htat we'll want to run on a background thread
    private Thread mThread; //the background thread

    public List<Float> UpHits;
    public List<Float> LeftHits;
    public List<Float> DownHits;
    public List<Float> RightHits;

    private Paint blackPaint;
    private Paint whitePaintLeft;
    private Paint whitePaintRight;
    private Paint redPaint;
    private Paint bluePaint;
    private Paint lightBluePaint;

    private boolean LeftEnabled;
    private boolean RightEnabled;
    private boolean DownEnabled;

    private int Rate;
    private int Cooldown;
    private int Level;
    private int NextLevelRate;
    private int NextLevelCooldown;

    public int Health;
    public int Points;

    public boolean Playing;

    public int ShakeCount;
    private int ShakeCooldown;
    private int ShakeCooldownRate;


    /**
     * We need to override all the constructors, since we don't know which will be called
     */
    public DrawingSurfaceView(Context context) {
        this(context, null);
    }

    public DrawingSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingSurfaceView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);

        viewWidth = 1; viewHeight = 1; //positive defaults; will be replaced when #surfaceChanged() is called

        // register our interest in hearing about changes to our surface
        mHolder = getHolder();
        mHolder.addCallback(this);

        mRunnable = new DrawingRunnable();

        //set up drawing variables ahead of time
        whitePaintLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaintLeft.setColor(Color.WHITE);
        whitePaintRight = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaintRight.setColor(Color.WHITE);

        whitePaintLeft.setTextAlign(Paint.Align.LEFT);
        whitePaintRight.setTextAlign(Paint.Align.RIGHT);

        float textSize = whitePaintLeft.getTextSize();
        whitePaintLeft.setTextSize(textSize * 2);
        whitePaintRight.setTextSize(textSize * 2);

        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPaint.setColor(Color.BLACK);
        redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redPaint.setColor(Color.RED);
        bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluePaint.setColor(Color.BLUE);
        lightBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightBluePaint.setColor(Color.argb(50, 0, 0, 255));

        NewGame();
    }


    /**
     * Helper method for the "game loop"
     */
    public void update(){
        if (Playing) {
            Cooldown--;

            if (ShakeCooldown > 0) {
                ShakeCooldown --;
            }

            if (LeftEnabled && Cooldown == 150) {
                SpawnLeft();
            }

            if (DownEnabled && Cooldown == 50) {
                SpawnDown();
            }

            if (RightEnabled && Cooldown == 100) {
                SpawnRight();
            }

            if (Cooldown == 25 || Cooldown == 75 || Cooldown == 125 || Cooldown == 175) {
                SpawnUp();
            }

            // add to counter sound offset
            if ((Cooldown + 16) % 25 == 0) {
                ((MainActivity)getContext()).playTick();
            }

            if (Cooldown <= 0) {
                Cooldown = Rate;
                NextLevelCooldown--;
                if (NextLevelCooldown <= 0) {
                    Level++;
                    NextLevelCooldown = NextLevelRate;
                    if (Level == 2) {
                        LeftEnabled = true;
                    } else if (Level == 3) {
                        RightEnabled = true;
                    } else if (Level == 4) {
                        DownEnabled = true;
                    }
                }
            }

            for (int i = 0; i < UpHits.size(); i++) {
                UpHits.set(i, UpHits.get(i) - 2f);
                if (UpHits.get(i) <= 30) {
                    UpHits.remove(i);
                    LoseHealth();
                }
            }

            for (int i = 0; i < DownHits.size(); i++) {
                DownHits.set(i, DownHits.get(i) - 2f);
                if (DownHits.get(i) <= 30) {
                    DownHits.remove(i);
                    LoseHealth();
                }
            }

            for (int i = 0; i < LeftHits.size(); i++) {
                LeftHits.set(i, LeftHits.get(i) - 2f);
                if (LeftHits.get(i) <= 30) {
                    LeftHits.remove(i);
                    LoseHealth();
                }
            }

            for (int i = 0; i < RightHits.size(); i++) {
                RightHits.set(i, RightHits.get(i) - 2f);
                if (RightHits.get(i) <= 30) {
                    RightHits.remove(i);
                    LoseHealth();
                }
            }
        }
    }

    private void LoseHealth() {
        Health--;
        if (Health <= 0) {
            GameOver();
        }
    }

    private void GameOver() {
        Playing = false;
        ClearAll();

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler((getContext()).getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                ((MainActivity)getContext()).MenuButtons();
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    private void SpawnUp() {
        UpHits.add(UpHits.size(), 1000f);
    }

    private void SpawnLeft() {
        LeftHits.add(LeftHits.size(), 1000f);
    }

    private void SpawnRight() {
        RightHits.add(RightHits.size(), 1000f);
    }

    private void SpawnDown() {
        DownHits.add(DownHits.size(), 1000f);
    }

    /**
     * Helper method for the "render loop"
     * @param canvas The canvas to draw on
     */
    public void render(Canvas canvas){
        if(canvas == null) return; //if we didn't get a valid canvas for whatever reason
        canvas.drawColor(Color.BLACK); //black out the background

        canvas.drawText("Health: " + Health, 100, 100, whitePaintLeft);
        canvas.drawText("Points: " + Points, viewWidth - 100, 100, whitePaintRight);
        canvas.drawText("Shake Events: " + ShakeCount, viewWidth - 100, viewHeight - 100, whitePaintRight);

        canvas.drawCircle(viewWidth / 2.0f, viewHeight / 2.0f, 110.0f, lightBluePaint);
        canvas.drawCircle(viewWidth / 2.0f, viewHeight / 2.0f, 65.0f, blackPaint);

        try {
            for (float f : UpHits) {
                canvas.drawCircle(viewWidth / 2.0f, viewHeight / 2.0f - f, 15.0f, redPaint);
            }

            for (float f : DownHits) {
                canvas.drawCircle(viewWidth / 2.0f, viewHeight / 2.0f + f, 15.0f, redPaint);
            }

            for (float f : LeftHits) {
                canvas.drawCircle(viewWidth / 2.0f - f, viewHeight / 2.0f, 15.0f, redPaint);
            }

            for (float f : RightHits) {
                canvas.drawCircle(viewWidth / 2.0f + f, viewHeight / 2.0f, 15.0f, redPaint);
            }
        } catch (ConcurrentModificationException e) {
            // Ignore it
        }

        canvas.drawCircle(viewWidth/2.0f, viewHeight/2.0f, 30.0f, bluePaint);

//        for(int x=50; x<viewWidth-50; x++) { //most of the width
//            for(int y=100; y<110; y++) { //10 pixels high
//                bmp.setPixel(x, y, Color.BLUE); //we can also set individual pixels in a Bitmap (like a BufferedImage)
//            }
//        }
//        //Canvas bmc = new Canvas(bmp); //we can also make a canvas out of a Bitmap to draw on that (like fetching g2d from a BufferedImage)
//
//        canvas.drawBitmap(bmp, 0, 0, null); //and then draw the BitMap onto the canvas.
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // create thread only; it's started in surfaceCreated()
        Log.v(TAG, "making new thread");
        mThread = new Thread(mRunnable);
        mRunnable.setRunning(true); //turn on the runner
        mThread.start(); //start up the thread when surface is created

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        synchronized (mHolder) { //synchronized to keep this stuff atomic
            viewWidth = width;
            viewHeight = height;
            bmp = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888); //new buffer to draw on
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        mRunnable.setRunning(false); //turn off
        boolean retry = true;
        while(retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //will try again...
            }
        }
        Log.d(TAG, "Drawing thread shut down.");
    }




    /**
     * An inner class representing a runnable that does the drawing. Animation timing could go in here.
     * http://obviam.net/index.php/the-android-game-loop/ has some nice details about using timers to specify animation
     */
    public class DrawingRunnable implements Runnable {

        private boolean isRunning; //whether we're running or not (so we can "stop" the thread)

        public void setRunning(boolean running){
            this.isRunning = running;
        }

        public void run() {
            Canvas canvas;
            while(isRunning)
            {
                canvas = null;
                try {
                    canvas = mHolder.lockCanvas(); //grab the current canvas
                    synchronized (mHolder) {
                        update(); //update the game
                        render(canvas); //redraw the screen
                    }
                }
                finally { //no matter what (even if something goes wrong), make sure to push the drawing so isn't inconsistent
                    if (canvas != null) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    public void ShakeEvent() {
        if (ShakeCount > 0) {
            if (ShakeCooldown <= 0) {
                ShakeCooldown = ShakeCooldownRate;
                ShakeCount--;
                ClearAll();
            }
        }
    }

    public void ClearAll() {
        UpHits = new LinkedList<Float>();
        LeftHits = new LinkedList<Float>();
        RightHits = new LinkedList<Float>();
        DownHits = new LinkedList<Float>();
    }

    public void NewGame() {
        // Set up hits
        UpHits = new LinkedList<Float>();
        DownHits = new LinkedList<Float>();
        LeftHits = new LinkedList<Float>();
        RightHits = new LinkedList<Float>();

        Rate = 200;
        Cooldown = 500;
        NextLevelRate = 4;
        NextLevelCooldown = 5;
        Level = 1;

        Health = 10;

        ShakeCount = 3;
        ShakeCooldownRate = 200;
        ShakeCooldown = ShakeCooldownRate;

        LeftEnabled = false;
        RightEnabled = false;
        DownEnabled = false;

        Playing = false;
    }
}