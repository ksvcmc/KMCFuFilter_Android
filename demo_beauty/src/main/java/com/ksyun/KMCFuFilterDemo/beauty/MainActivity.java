package com.ksyun.KMCFuFilterDemo.beauty;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ksyun.media.kmcfilter.KMCAuthManager;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String STREAMING_URL = "rtmp://test.uplive.ks-cdn.com/live/fu_beauty_test";

    public final static String SAVE_LOCAL_VIDEO = "save_local_video";
    public final static String STREAM_URL = "stream_url";

    private EditText mStreamURLEditText;
    private ImageView mSaveFileSwitch;
    private Button mStartButton;
    private LinearLayout mShowNote;

    private ViewClickListener mListener;

    private boolean mSaveLocalFile = false;
    private boolean mAuthorized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStreamURLEditText = (EditText) findViewById(R.id.stream_url);
        mSaveFileSwitch = (ImageView) findViewById(R.id.save_file_switch);
        mStartButton = (Button) findViewById(R.id.start);
        mShowNote = (LinearLayout) findViewById(R.id.show_demo_note);

        mStreamURLEditText.setText(STREAMING_URL);

        mListener = new ViewClickListener();
        mSaveFileSwitch.setOnClickListener(mListener);
        mStartButton.setOnClickListener(mListener);
        mShowNote.setOnClickListener(mListener);

        doAuth();
    }

    private void doAuth() {
        String token = "40cc5de2e197678ea0f9da1ff70a2672";
        KMCAuthManager.getInstance().authorize(getApplicationContext(),
                token, mCheckAuthResultListener);
    }

    private KMCAuthManager.AuthResultListener mCheckAuthResultListener = new KMCAuthManager
            .AuthResultListener() {
        @Override
        public void onSuccess() {
            mAuthorized = true;

            KMCAuthManager.getInstance().removeAuthResultListener(mCheckAuthResultListener);
            makeToast("鉴权成功");
        }

        @Override
        public void onFailure(int errCode) {
            KMCAuthManager.getInstance().removeAuthResultListener(mCheckAuthResultListener);
            makeToast("鉴权失败! 错误码: " + errCode);
        }
    };

    private void makeToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    private class ViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.save_file_switch:
                    onSaveSwitchClicked();
                    break;
                case R.id.start:
                    onStartButtonClicked();
                    break;
                case R.id.show_demo_note:
                    onShowNoteClicked();
                    break;
                default:
                    break;
            }
        }
    }

    private void onShowNoteClicked() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.note_dialog);

        ImageView closeDialog = (ImageView) dialog.findViewById(R.id.close_dialog);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void onStartButtonClicked() {
        if (!mAuthorized) {
            makeToast("鉴权失败，请等待鉴权通过");
            return;
        }
        String url = mStreamURLEditText.getText().toString();
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra(SAVE_LOCAL_VIDEO, mSaveLocalFile);
        intent.putExtra(STREAM_URL, url);
        startActivity(intent);
    }

    private void onSaveSwitchClicked() {
        mSaveLocalFile = !mSaveLocalFile;
        mSaveFileSwitch.setImageResource(mSaveLocalFile ? R.drawable.save_file_on :
                R.drawable.save_file_off);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KMCAuthManager.getInstance().release();
    }
}
