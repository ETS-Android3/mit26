package com.example.myapplication.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;
import com.squareup.picasso.Picasso;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.VideoDefinition;
import org.linphone.core.tools.Log;
import org.linphone.mediastream.Version;

public class videoCall_Start_waiting extends Activity {
    private TextureView mVideoView;
    private TextureView mCaptureView;
    private TextView displayName;
    String name,avatar;
    ImageView img_avatar;

    private CoreListenerStub mCoreListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.videocall_start_init);
        mVideoView = findViewById(R.id.videoSurface);
        mCaptureView = findViewById(R.id.videoCaptureSurface);
        displayName=findViewById(R.id.disPlayName);
        img_avatar=findViewById(R.id.avatar);
        Intent intent = getIntent();
        name=intent.getStringExtra("disPlayName");
        avatar=intent.getStringExtra("avatar");
        displayName.setText(name);
        Picasso.with(getApplicationContext()).load(avatar)
                .placeholder(R.drawable.loader)
                .error(R.drawable.broken)
                .into(img_avatar);



        Core core = LinphoneService.getCore();
        // We need to tell the core in which to display what

        core.setNativePreviewWindowId(mCaptureView);
        //resizePreview();

        // Listen for call state changes
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                if (state == Call.State.Connected || state == Call.State.Released || state == Call.State.End){

                    finish();
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
                }
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LinphoneService.getCore().addListener(mCoreListener);

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
                    screenHeight; // Let's take at most 1/4 of the screen for the camera preview

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

}
