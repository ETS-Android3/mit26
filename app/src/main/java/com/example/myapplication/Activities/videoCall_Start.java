package com.example.myapplication.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;

import org.linphone.core.AudioRoute;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.VideoDefinition;
import org.linphone.core.tools.Log;
import org.linphone.mediastream.Version;

public class videoCall_Start extends Activity {
    private TextureView mVideoView;
    private TextureView mCaptureView;
    private CoreListenerStub mCoreListener;
    private ImageView speaker;
    AudioManager manager;
    String userID;
    //---------------------------------------------
    private Chronometer time;
    private long pauseOffset;
    private boolean running;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.videocall_start);

        mVideoView = findViewById(R.id.videoSurface);
        mCaptureView = findViewById(R.id.videoCaptureSurface);
        time = findViewById(R.id.time);
        speaker = findViewById(R.id.speaker);

        Core core = LinphoneService.getCore();
        // We need to tell the core in which to display what
        core.setNativeVideoWindowId(mVideoView);
        core.setNativePreviewWindowId(mCaptureView);

        // Listen for call state changes
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                if (state == Call.State.IncomingReceived) {
                    acceptVideoCall(call);

                } else if (state == Call.State.End || state == Call.State.Released) {
                    call.terminate();
                    pauseTimer();
                    finish();
                    Toast.makeText(getApplicationContext(), "Video calls duration: " + time.getText(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        findViewById(R.id.terminate_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Core core = LinphoneService.getCore();
                if (core.getCallsNb() > 0) {
                    Call call = core.getCurrentCall();
                    if (call == null) {
                        // Current call can be null if paused for example
                        call = core.getCalls()[0];
                    }
                    call.terminate();
                    //insert into db
                }
                finish();
            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (!manager.isSpeakerphoneOn()) {
                    speaker.setImageResource(R.drawable.speaker_on);
                    Toast.makeText(getApplicationContext(), "Speaker on", Toast.LENGTH_SHORT).show();
                    manager.setSpeakerphoneOn(true);
                } else {
                    speaker.setImageResource(R.drawable.speaker_off);
                    Toast.makeText(getApplicationContext(), "Speaker off", Toast.LENGTH_SHORT).show();
                    manager.setSpeakerphoneOn(false);
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        manager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);
        manager.setSpeakerphoneOn(false);
        startTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinphoneService.getCore().addListener(mCoreListener);
        resizePreview();
    }

    @Override
    protected void onPause() {
        LinphoneService.getCore().removeListener(mCoreListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @TargetApi(24)
    @Override
    public void onUserLeaveHint() {
        // If the device supports Picture in Picture let's use it
        boolean supportsPip =
                getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
        Log.i("[Call] Is picture in picture supported: " + supportsPip);
        if (supportsPip && Version.sdkAboveOrEqual(24)) {
            // enterPictureInPictureMode();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(
            boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            // Currently nothing to do has we only display video
            // But if we had controls or other UI elements we should hide them
        } else {
            // If we did hide something, let's make them visible again
        }
    }

    private void resizePreview() {
        Core core = LinphoneService.getCore();
        if (core.getCallsNb() > 0) {
            Call call = core.getCurrentCall();
            if (call == null) {
                call = core.getCalls()[0];
            }
            if (call == null) return;

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int screenHeight = metrics.heightPixels;
            int maxHeight =
                    screenHeight / 4; // Let's take at most 1/4 of the screen for the camera preview

            VideoDefinition videoSize =
                    call.getCurrentParams()
                            .getSentVideoDefinition(); // It already takes care of rotation
            if (videoSize.getWidth() == 0 || videoSize.getHeight() == 0) {
                Log.w(
                        "[Video] Couldn't get sent video definition, using default video definition");
                videoSize = core.getPreferredVideoDefinition();
            }
            int width = videoSize.getWidth();
            int height = videoSize.getHeight();

            Log.d("[Video] Video height is " + height + ", width is " + width);
            width = width * maxHeight / height;
            height = maxHeight;

            if (mCaptureView == null) {
                Log.e("[Video] mCaptureView is null !");
                return;
            }

            RelativeLayout.LayoutParams newLp = new RelativeLayout.LayoutParams(width, height);
            newLp.addRule(
                    RelativeLayout.ALIGN_PARENT_BOTTOM,
                    1); // Clears the rule, as there is no removeRule until API 17.
            newLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            mCaptureView.setLayoutParams(newLp);
            Log.d("[Video] Video preview size set to " + width + "x" + height);
        }
    }

    private void acceptVideoCall(Call call) {
        //Toast.makeText(getApplicationContext(),"accept a call", Toast.LENGTH_LONG).show();
        CallParams params = LinphoneService.getCore().createCallParams(call);
        params.enableVideo(true);
        params.enableAudio(true);
        call.acceptWithParams(params);
        startTimer();
    }

    public void startTimer() {
        if (!running) {
            time.setBase(SystemClock.elapsedRealtime() - pauseOffset); // Returns milliseconds since boot, including time spent in sleep.
            time.start();
            running = true;
        }
    }

    public void pauseTimer() {
        if (running) {
            time.stop();
            pauseOffset = SystemClock.elapsedRealtime() - time.getBase();
            running = false;
        }
    }

}
