package windylabs.com.vlcplayersample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

public class VideoVLCActivity extends Activity implements IVideoPlayer {
    private static final String TAG = VideoVLCActivity.class.getSimpleName();
    private static final String TAG_VLC = "VLC_TAG";
    private static final int pauseCode = 1;
    private static final int stopCode = 2;

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    private SurfaceView mSurfaceView;
    private FrameLayout mSurfaceFrame;
    private SurfaceHolder mSurfaceHolder;
    private Surface mSurface = null;

    private LibVLC mLibVLC;

    private String mMediaUrl;

    private BroadcastReceiver mIntentReceiver;
    private int action_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
        }
        Log.d(TAG, "VideoVLC -- onCreate -- START ------------");
        setContentView(R.layout.activity_video_vlc);

        mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);
        mMediaUrl = getIntent().getExtras().getString("videoUrl");

        try {
            mLibVLC = new LibVLC();
            mLibVLC.setAout(mLibVLC.AOUT_AUDIOTRACK);
            mLibVLC.setVout(mLibVLC.VOUT_ANDROID_SURFACE);
            mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);

            mLibVLC.init(getApplicationContext());
        } catch (LibVlcException e){
            Log.e(TAG, e.toString());
        }

        mSurface = mSurfaceHolder.getSurface();
        mLibVLC.attachSurface(mSurface, VideoVLCActivity.this);
        mLibVLC.playMRL(mMediaUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // MediaCodec opaque direct rendering should not be used anymore since there is no surface to attach.
        mLibVLC.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_vlc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void eventHardwareAccelerationError() {
        Log.e(TAG, "eventHardwareAccelerationError()!");
        return;
    }

    @Override
    public void setSurfaceLayout(final int width, final int height, int visible_width, int visible_height, final int sar_num, int sar_den){
        Log.d(TAG, "setSurfaceSize -- START");
        if (width * height == 0)
            return;

        // store video size
        mVideoHeight = height;
        mVideoWidth = width;
        mVideoVisibleHeight = visible_height;
        mVideoVisibleWidth = visible_width;
        mSarNum = sar_num;
        mSarDen = sar_den;

        Log.d(TAG, "setSurfaceSize -- mMediaUrl: " + mMediaUrl + " mVideoHeight: " + mVideoHeight + " mVideoWidth: " + mVideoWidth + " mVideoVisibleHeight: " + mVideoVisibleHeight + " mVideoVisibleWidth: " + mVideoVisibleWidth + " mSarNum: " + mSarNum + " mSarDen: " + mSarDen);
    }

    @Override
    public int configureSurface(android.view.Surface surface, int i, int i1, int i2){
        return -1;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //заморочиться, и посылать отсюда в ServerActivity код, который передается в Server
        // и оттуда отсылается в клиент, чтобы остановился стрим (ОЧЕНЬ ЗАМОРОЧИТЬСЯ)
        //пока просто при трансляции если нажать назад кнопку - ничего не произойдет. трансляция управляется только
        //командами с клиента
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter("ActionCode.intent.VLC");
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                action_code = intent.getIntExtra("action_code", 0);

                if (action_code == pauseCode) {
                    if (mLibVLC.getPlayerState() == LibVLC.MEDIA_PAUSED)
                        mLibVLC.play();
                    else if (mLibVLC.getPlayerState() != LibVLC.MEDIA_PAUSED)
                        mLibVLC.pause();
                    Log.e(TAG_VLC, "action_code получен");
                } else if (action_code == stopCode) {
                    mLibVLC.stop();
                    finish();
                    Log.e(TAG_VLC, "получен stopCode. Activity закрывается");
                } else {
                    Log.e(TAG_VLC, "action_code не опознан");
                }
            }
        };
        registerReceiver(mIntentReceiver, intentFilter);
    }
}
