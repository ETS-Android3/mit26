package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;
import com.squareup.picasso.Picasso;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

public class videoCall_End extends AppCompatActivity {

    ImageView btn_accept, btn_reject,img_avatar;
    private CoreListenerStub mCoreListener;
    TextView txt_disPlayName;
    String avatar,disPlayName;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videocall_end);
        mapping();
        getInfor();

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                if (state == Call.State.End || state == Call.State.Released) {
                    finish();
                }
            }
        };
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptVideoCall();
            }
        });
        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectVideoCall();
                // insert database
            }
        });
    }

    private void getInfor() {
        Intent intent = getIntent();
        disPlayName=intent.getStringExtra("disPlayName");
        avatar=intent.getStringExtra("avatar");
        txt_disPlayName.setText(disPlayName);
        userID=intent.getStringExtra("idUser");
        Picasso.with(getApplicationContext()).load(avatar)
                .placeholder(R.drawable.loader)
                .error(R.drawable.broken)
                .into(img_avatar);
    }

    private void rejectVideoCall() {
        //Toast.makeText(getApplicationContext(),"reject a video call", Toast.LENGTH_LONG).show();
        finish();
        Core core = LinphoneService.getCore();
        if (core.getCallsNb() > 0) {
            Call call = core.getCurrentCall();
            if (call == null) {
                // Current call can be null if paused for example
                call = core.getCalls()[0];
            }
            call.terminate();
        }

    }

    private void acceptVideoCall() {
        //Toast.makeText(getApplicationContext(),"accept a video call", Toast.LENGTH_LONG).show();
        Call call = LinphoneService.getCore().getCurrentCall();
        CallParams params = LinphoneService.getCore().createCallParams(call);
        params.enableVideo(true);
        params.enableAudio(true);
        call.acceptWithParams(params);
        finish();
    }

    private void mapping() {
        btn_accept=findViewById(R.id.videocall_end_img_accept);
        btn_reject=findViewById(R.id.videocall_end_img_reject);
        img_avatar=findViewById(R.id.avatar);
        txt_disPlayName=findViewById(R.id.disPlayName);
    }
    @Override
    protected void onResume() {
        super.onResume();
        LinphoneService.getCore().addListener(mCoreListener);
    }

}
