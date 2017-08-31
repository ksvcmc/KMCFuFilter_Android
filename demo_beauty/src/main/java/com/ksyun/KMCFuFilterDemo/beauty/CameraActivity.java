package com.ksyun.KMCFuFilterDemo.beauty;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.kmcfilter.KMCFilter;
import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.capture.camera.CameraTouchHelper;
import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;
import com.xw.repo.BubbleSeekBar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sujia on 2017/8/16.
 */

public class CameraActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = CameraActivity.class.getSimpleName();

    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;

    private GLSurfaceView mCameraPreviewView;
    //private TextureView mCameraPreviewView;
    private CameraHintView mCameraHintView;

    private View mSwitchCameraView;
    private Chronometer mChronometer;
    private FrameLayout mChooseBeautyLayout;
    private RecyclerView mFilterChooseLayout;
    private LinearLayout mBeautyChooseLayout;
    private LinearLayout mShapeChooseLayout;

    private LinearLayout mFilterIconLayout;
    private LinearLayout mBeautyIconLayout;
    private LinearLayout mShapeIconLayout;
    private ImageView mFilterIconImg;
    private TextView mFilterIconTxt;
    private ImageView mBeautyIconImg;
    private TextView mBeautyIconTxt;
    private ImageView mShapeIconImg;
    private TextView mShapeIconTxt;

    private FilterSelectViewAdapter mFilterSelectViewAdapter;
    private BubbleSeekBar mBlurSeekBar;
    private BubbleSeekBar mColorSeekBar;
    private BubbleSeekBar mRedSeekBar;
    private Button mDefaultShapeButton;
    private Button mGodShapeButton;
    private Button mStarShapeButton;
    private Button mNatureShapeButton;
    private BubbleSeekBar mEyeBar;
    private BubbleSeekBar mCheekSeekBar;

    private ImageView mStartButton;
    private ImageView mBackOffButton;

    private ButtonObserver mObserverButton;

    private KSYStreamer mStreamer;
    private KMCFilter mFilter;
    private Handler mMainHandler;

    private boolean mPause = false;

    private String mURL;
    private boolean mSaveLocalFile = false;

    private boolean mIsStreaming = false;
    private boolean mIsFileRecording = false;
    private boolean mIsFlashOpened = false;
    private boolean mHWEncoderUnsupported;
    private boolean mSWEncoderUnsupported;

    private String mRecordUrl = "/sdcard/rec_test.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainHandler = new Handler();

        Intent intent = getIntent();
        mURL = intent.getStringExtra(MainActivity.STREAM_URL);
        mSaveLocalFile = intent.getBooleanExtra(MainActivity.SAVE_LOCAL_VIDEO, false);

        initUI();
        setController();

        initStreamer();

        hideChooseBeautyLayout(true);
        onDefaultShapeClicked();
    }

    private void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_camera);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCameraHintView = (CameraHintView) findViewById(R.id.kmc_camera_hint);
        mCameraPreviewView = (GLSurfaceView) findViewById(R.id.kmc_camera_preview);
        //mCameraPreviewView = (TextureView) findViewById(R.id.camera_preview);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setVisibility(mSaveLocalFile ? View.VISIBLE : View.GONE);

        mSwitchCameraView = findViewById(R.id.kmc_switch_cam);

        mChooseBeautyLayout = (FrameLayout) findViewById(R.id.beauty_choose_layout);
        mFilterChooseLayout = (RecyclerView) findViewById(R.id.filter_select_view);
        mBeautyChooseLayout = (LinearLayout) findViewById(R.id.beauty_layout);
        mShapeChooseLayout = (LinearLayout) findViewById(R.id.shape_layout);

        mFilterIconLayout = (LinearLayout) findViewById(R.id.filter_icon_layout);
        mBeautyIconLayout = (LinearLayout) findViewById(R.id.beauty_icon_layout);
        mShapeIconLayout = (LinearLayout) findViewById(R.id.shape_icon_layout);

        mFilterIconImg = (ImageView) findViewById(R.id.filter_img);
        mFilterIconTxt = (TextView) findViewById(R.id.filter_txt);
        mBeautyIconImg = (ImageView) findViewById(R.id.beauty_img);
        mBeautyIconTxt = (TextView) findViewById(R.id.beauty_txt);
        mShapeIconImg = (ImageView) findViewById(R.id.shape_img);
        mShapeIconTxt = (TextView) findViewById(R.id.shape_txt);

        mFilterChooseLayout = (RecyclerView) findViewById(R.id.filter_select_view);
        //创建默认的线性LayoutManager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterChooseLayout.setLayoutManager(mLayoutManager);
        mFilterChooseLayout.addItemDecoration(new SpacesItemDecoration(10));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mFilterChooseLayout.setHasFixedSize(true);

        mBlurSeekBar = (BubbleSeekBar) findViewById(R.id.beauty_blur);
        mColorSeekBar = (BubbleSeekBar) findViewById(R.id.beauty_color);
        mRedSeekBar = (BubbleSeekBar) findViewById(R.id.beauty_red);
        mDefaultShapeButton = (Button) findViewById(R.id.default_shape);
        mGodShapeButton = (Button) findViewById(R.id.god_shape);
        mStarShapeButton = (Button) findViewById(R.id.star_shape);
        mNatureShapeButton = (Button) findViewById(R.id.nature_shape);
        mEyeBar = (BubbleSeekBar) findViewById(R.id.beauty_eye);
        mCheekSeekBar = (BubbleSeekBar) findViewById(R.id.beauty_cheek);

        mStartButton = (ImageView) findViewById(R.id.start_button);
        mBackOffButton = (ImageView) findViewById(R.id.back_off);
    }

    private void setController() {
        mObserverButton = new ButtonObserver();

        mSwitchCameraView.setOnClickListener(mObserverButton);
        mFilterIconLayout.setOnClickListener(mObserverButton);
        mBeautyIconLayout.setOnClickListener(mObserverButton);
        mShapeIconLayout.setOnClickListener(mObserverButton);

        mStartButton.setOnClickListener(mObserverButton);
        mBackOffButton.setOnClickListener(mObserverButton);

        mFilterSelectViewAdapter = new FilterSelectViewAdapter(mFilterChooseLayout, getApplicationContext());
        mFilterChooseLayout.setAdapter(mFilterSelectViewAdapter);
        mFilterSelectViewAdapter.setOnItemSelectedListener(new FilterSelectViewAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemPosition) {
                onChangeFilter(itemPosition);
            }
        });

        mBlurSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onChangeBlur(progress);
            }

            @Override
            public void getProgressOnActionUp(int i, float v) {

            }

            @Override
            public void getProgressOnFinally(int i, float v) {

            }
        });
        mColorSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onChangeColor(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(int i, float v) {

            }

            @Override
            public void getProgressOnFinally(int i, float v) {

            }
        });
        mRedSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onChangeRed(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(int i, float v) {

            }

            @Override
            public void getProgressOnFinally(int i, float v) {

            }
        });

        mDefaultShapeButton.setOnClickListener(mObserverButton);
        mGodShapeButton.setOnClickListener(mObserverButton);
        mStarShapeButton.setOnClickListener(mObserverButton);
        mNatureShapeButton.setOnClickListener(mObserverButton);
        mEyeBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onChangeEye(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(int i, float v) {

            }

            @Override
            public void getProgressOnFinally(int i, float v) {

            }
        });
        mCheekSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onChangeCheek(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(int i, float v) {

            }

            @Override
            public void getProgressOnFinally(int i, float v) {

            }
        });
    }

    private void initStreamer() {
        //set para to  ksystreamer
        mStreamer = new KSYStreamer(this);
        mStreamer.setPreviewFps(15);
        mStreamer.setTargetFps(15);
        mStreamer.setVideoKBitrate(800);
        mStreamer.setAudioKBitrate(48);
        mStreamer.setPreviewResolution(StreamerConstants.VIDEO_RESOLUTION_720P);
        mStreamer.setTargetResolution(StreamerConstants.VIDEO_RESOLUTION_720P);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mStreamer.setAudioChannels(1);

        mStreamer.setUrl(mURL);
        mStreamer.setDisplayPreview(mCameraPreviewView);
        mStreamer.setEnableStreamStatModule(true);
        mStreamer.enableDebugLog(true);
        mStreamer.setFrontCameraMirror(true);

        mStreamer.setCameraFacing(CameraCapture.FACING_FRONT);
        mStreamer.setMuteAudio(false);
        mStreamer.setEnableAudioPreview(false);
        mStreamer.setOnInfoListener(mOnInfoListener);
        mStreamer.setOnErrorListener(mOnErrorListener);
        mStreamer.setOnLogEventListener(mOnLogEventListener);
        //设置为软编
        mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
        mStreamer.setVideoCodecId(StreamerConstants.CODEC_ID_AVC);
        mStreamer.setVideoEncodeScene(VideoEncodeFormat.ENCODE_SCENE_SHOWSELF);
        mStreamer.setVideoEncodeProfile(VideoEncodeFormat.ENCODE_PROFILE_LOW_POWER);

        // touch focus and zoom support
        CameraTouchHelper cameraTouchHelper = new CameraTouchHelper();
        cameraTouchHelper.setCameraCapture(mStreamer.getCameraCapture());
        mCameraPreviewView.setOnTouchListener(cameraTouchHelper);
        // set CameraHintView to show focus rect and zoom ratio
        cameraTouchHelper.setCameraHintView(mCameraHintView);
        cameraTouchHelper.addTouchListener(mTouchListener);

        mFilter = new KMCFilter(getApplicationContext(), mStreamer.getGLRender());
        mStreamer.getImgTexFilterMgt().setFilter(mFilter);
    }

    private CameraTouchHelper.OnTouchListener mTouchListener = new CameraTouchHelper.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getRawY() - mChooseBeautyLayout.getTop() < 0) {
                hideChooseBeautyLayout(true);
            }
            return false;
        }
    };

    private void hideChooseBeautyLayout(boolean hide) {
        mChooseBeautyLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraPreviewWithPermCheck();

        mStreamer.setDisplayPreview(mCameraPreviewView);
        mPause = false;
        mStreamer.onResume();
        mCameraHintView.hideAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPause = true;
        mStreamer.onPause();
        mStreamer.stopCameraPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }

        mStreamer.setOnLogEventListener(null);
        mStreamer.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackoffClick();
                break;
            default:
                break;
        }
        return true;
    }

    // Example to handle camera related operation
    private void setCameraAntiBanding50Hz() {
        Camera.Parameters parameters = mStreamer.getCameraCapture().getCameraParameters();
        if (parameters != null) {
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_50HZ);
            mStreamer.getCameraCapture().setCameraParameters(parameters);
        }
    }

    private void onSwitchCamera() {
        mStreamer.switchCamera();
        mCameraHintView.hideAll();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mStreamer.startCameraPreview();
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    Toast.makeText(this, "No CAMERA or AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void makeToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(CameraActivity.this, str, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    private void startCameraPreviewWithPermCheck() {
        int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int writePerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED ||
                audioPerm != PackageManager.PERMISSION_GRANTED ||
                writePerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "No CAMERA or AudioRecord permission, please check");
                Toast.makeText(this, "No CAMERA or AudioRecord permission, please check",
                        Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE };
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_CAMERA_AUDIOREC);
            }
        } else {
            mStreamer.startCameraPreview();
        }
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.kmc_switch_cam:
                    onSwitchCamera();
                    break;
                case R.id.start_button:
                    onStartClicked();
                    break;
                case R.id.back_off:
                    onBackoffClick();
                    break;
                case R.id.filter_icon_layout:
                    onFilterIconClicked();
                    break;
                case R.id.beauty_icon_layout:
                    onBeautyIconClicked();
                    break;
                case R.id.shape_icon_layout:
                    onShapeIconClicked();
                    break;
                case R.id.default_shape:
                    onDefaultShapeClicked();
                    break;
                case R.id.god_shape:
                    onGodShapeClicked();
                    break;
                case R.id.star_shape:
                    onStarShapeClicked();
                    break;
                case R.id.nature_shape:
                    onNatureShapeClicked();
                    break;
                default:
                    break;
            }
        }
    }

    private void onStartClicked() {
        onShootClicked();
    }

    private void updateStartButtonIcon() {
        if (mIsStreaming) {
            mStartButton.setImageResource(R.drawable.stop);
        } else {
            mStartButton.setImageResource(R.drawable.start);
        }
    }

    private boolean isNetworkConnectionAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    //start streaming
    private void startStream() {
        mStreamer.startStream();
        mIsStreaming = true;

        updateStartButtonIcon();
    }

    private void stopStream() {
        // stop stream
        mStreamer.stopStream();
        mIsStreaming = false;

        stopRecord();
        updateStartButtonIcon();
    }

    //start recording to a local file
    private void startRecord() {
        if(mIsFileRecording) {
            return;
        }
        //录制开始成功后会发送StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS消息
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss") ;
        try {
            mRecordUrl = Environment.getExternalStorageDirectory().getCanonicalPath() +
                    File.separator + dateFormat.format(date) + ".mp4";
        } catch (IOException e) {
            e.printStackTrace();
        }
        mStreamer.startRecord(mRecordUrl);
        mIsFileRecording = true;
    }


    private void stopRecord() {
        mStreamer.stopRecord();
        mIsFileRecording = false;
        stopChronometer();
        notifyMediaScanner(mRecordUrl);
    }

    private void startChronometer() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    private void stopChronometer() {
//        if (mIsStreaming || mIsFileRecording) {
//            return;
//        }
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
    }

    private KSYStreamer.OnInfoListener mOnInfoListener = new KSYStreamer.OnInfoListener() {
        @Override
        public void onInfo(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_INIT_DONE");
                    setCameraAntiBanding50Hz();
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_STREAM_SUCCESS");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(CameraActivity.this, "开始推流: " + mURL, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.show();
                        }
                    });

                    if(mSaveLocalFile) {
                        startRecord();
                    }
//                    startChronometer();
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_FILE_SUCCESS");
                    startChronometer();
                    break;
                case StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW:
                    Log.d(TAG, "KSY_STREAMER_FRAME_SEND_SLOW " + msg1 + "ms");
                    Toast.makeText(CameraActivity.this, "Network not good!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_RAISE:
                    Log.d(TAG, "BW raise to " + msg1 / 1000 + "kbps");
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_DROP:
                    Log.d(TAG, "BW drop to " + msg1 / 1000 + "kpbs");
                    break;
                default:
                    Log.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                    break;
            }
        }
    };

    private void handleEncodeError() {
        int encodeMethod = mStreamer.getVideoEncodeMethod();
        if (encodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mHWEncoderUnsupported = true;
            if (mSWEncoderUnsupported) {
                mStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE mode");
            }
        } else if (encodeMethod == StreamerConstants.ENCODE_METHOD_SOFTWARE) {
            mSWEncoderUnsupported = true;
            if (mHWEncoderUnsupported) {
                mStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got SW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_HARDWARE);
                Log.e(TAG, "Got SW encoder error, switch to HARDWARE mode");
            }
        }
    }

    private KSYStreamer.OnErrorListener mOnErrorListener = new KSYStreamer.OnErrorListener() {
        @Override
        public void onError(int what, int msg1, int msg2) {
            String errMsg;
            switch (what) {
                case StreamerConstants.KSY_STREAMER_ERROR_DNS_PARSE_FAILED:
                    errMsg = "KSY_STREAMER_ERROR_DNS_PARSE_FAILED";
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_FAILED:
                    errMsg = "KSY_STREAMER_ERROR_CONNECT_FAILED";
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_PUBLISH_FAILED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_PUBLISH_FAILED");
                    errMsg = "KSY_STREAMER_ERROR_PUBLISH_FAILED";
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_BREAKED:
                    errMsg = "KSY_STREAMER_ERROR_CONNECT_FAILED";
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                    errMsg = "KSY_STREAMER_ERROR_AV_ASYNC " + msg1 + "ms";
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                    errMsg = "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED";
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                    errMsg = "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN";
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED:
                    errMsg = "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED";
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN:
                    errMsg = "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN";
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                    errMsg = "KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED";
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    errMsg = "KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN";
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                    errMsg = "KSY_STREAMER_CAMERA_ERROR_UNKNOWN";
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                    errMsg = "KSY_STREAMER_CAMERA_ERROR_START_FAILED";
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    errMsg = "KSY_STREAMER_CAMERA_ERROR_SERVER_DIED";
                    break;
                //Camera was disconnected due to use by higher priority user.
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED:
                    errMsg = "KSY_STREAMER_CAMERA_ERROR_EVICTED";
                    break;
                default:
                    Log.d(TAG, "what=" + what + " msg1=" + msg1 + " msg2=" + msg2);
                    errMsg = "what=" + what + " msg1=" + msg1 + " msg2=" + msg2;
                    break;
            }

            if (!isNetworkConnectionAvailable(getApplicationContext())) {
                makeToast("网络连接失败，请检查您的网络设置");
            } else {
                makeToast("推流失败: " + errMsg + "， 请稍后重试");
            }

            switch (what) {
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    mStreamer.stopCameraPreview();
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_DNS_PARSE_FAILED:
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_FAILED:
                case StreamerConstants.KSY_STREAMER_ERROR_PUBLISH_FAILED:
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_BREAKED:
                case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                    stopStream();
                    break;
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_CLOSE_FAILED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_ERROR_UNKNOWN:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_OPEN_FAILED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_FORMAT_NOT_SUPPORTED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_WRITE_FAILED:
                    stopRecord();
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                    handleEncodeError();
                    if (mIsStreaming) {
                        stopStream();
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startStream();
                            }
                        }, 3000);
                    }
                    if (mIsFileRecording) {
                        stopRecord();
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startRecord();
                            }
                        }, 50);
                    }
                    break;
                default:
                    stopStream();
                    break;
            }
        }
    };


    private StatsLogReport.OnLogEventListener mOnLogEventListener =
            new StatsLogReport.OnLogEventListener() {
                @Override
                public void onLogEvent(StringBuilder singleLogContent) {
                    Log.i(TAG, "***onLogEvent : " + singleLogContent.toString());
                }
            };


    private void onBackoffClick() {
        new AlertDialog.Builder(CameraActivity.this).setCancelable(true)
                .setTitle("结束?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        CameraActivity.this.finish();
                    }
                }).show();
    }


    private void onShootClicked() {
        if (mIsStreaming) {
            stopStream();
        } else {
            startStream();
        }
    }

    private void onFilterIconClicked() {
        updateChooseLayout(1);
    }

    private void onBeautyIconClicked() {
        updateChooseLayout(2);
    }

    private void onShapeIconClicked() {
        updateChooseLayout(3);
    }

    private void updateChooseLayout(int index) {
        hideChooseBeautyLayout(false);

        mFilterChooseLayout.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        mBeautyChooseLayout.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        mShapeChooseLayout.setVisibility(index == 3 ? View.VISIBLE : View.GONE);

        updateFilterIcon(index == 1);
        updateBeautyIcon(index == 2);
        updateShapeIcon(index == 3);
    }

    private void updateFilterIcon(boolean enalbe) {
        mFilterIconImg.setImageResource(enalbe ? R.drawable.filter_on : R.drawable.filter_off);
        mFilterIconTxt.setTextColor(enalbe ? getResources().getColor(R.color.chosen) :
                getResources().getColor(R.color.white));
    }

    private void updateBeautyIcon(boolean enalbe) {
        mBeautyIconImg.setImageResource(enalbe ? R.drawable.beauty_on : R.drawable.beauty_off);
        mBeautyIconTxt.setTextColor(enalbe ? getResources().getColor(R.color.chosen) :
                getResources().getColor(R.color.white));
    }

    private void updateShapeIcon(boolean enable) {
        mShapeIconImg.setImageResource(enable ? R.drawable.shape_on : R.drawable.shape_off);
        mShapeIconTxt.setTextColor(enable ? getResources().getColor(R.color.chosen) :
                getResources().getColor(R.color.white));
    }

    private void onChangeFilter(int itemPosition) {
        mFilter.setFilterType(itemPosition);
    }

    private void onChangeBlur(int progress) {
        mFilter.setFaceBeautyBlurLevel(progress);
    }

    private void onChangeColor(float progress) {
        mFilter.setFaceBeautyColorLevel(progress);
    }

    private void onChangeRed(float progress) {
        mFilter.setFaceBeautyRedLevel(progress);
    }

    private void onChangeEye(float progress) {
        mFilter.setFaceBeautyEnlargeEye(progress);
    }

    private void onChangeCheek(float progress) {
        mFilter.setFaceBeautyCheeckThin(progress);
    }

    private void onNatureShapeClicked() {
        updateShapeButtonColor(4);

        mFilter.setFaceShape(2);
    }

    private void onStarShapeClicked() {
        updateShapeButtonColor(3);

        mFilter.setFaceShape(1);
    }

    private void onGodShapeClicked() {
        updateShapeButtonColor(2);

        mFilter.setFaceShape(0);
    }

    private void onDefaultShapeClicked() {
        updateShapeButtonColor(1);

        mFilter.setFaceShape(3);
    }

    private void updateShapeButtonColor(int index) {
        mDefaultShapeButton.setBackgroundResource(index == 1 ?
                R.drawable.round_button3:
                R.drawable.round_button2);
        mGodShapeButton.setBackgroundResource(index == 2 ?
                R.drawable.round_button3:
                R.drawable.round_button2);
        mStarShapeButton.setBackgroundResource(index == 3 ?
                R.drawable.round_button3:
                R.drawable.round_button2);
        mNatureShapeButton.setBackgroundResource(index == 4 ?
                R.drawable.round_button3:
                R.drawable.round_button2);
    }

    private void notifyMediaScanner(String path) {
        MediaScannerConnection.scanFile(this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, final Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                    }
                });
    }
}
