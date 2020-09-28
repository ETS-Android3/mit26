package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;
import com.squareup.picasso.Picasso;


import org.linphone.core.AudioRoute;

import org.linphone.core.Address;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class voiceCall_End extends AppCompatActivity {

    ImageView btn_accept, btn_reject, btn_reject1, speaker;
    LinearLayout layout_waiting_accept, layout_accepted;
    private CoreListenerStub mCoreListener;
    ImageView img_avatar;
    TextView txt_disPlayName;
    String avatar, disPlayName;
    AudioManager manager;
    String userID;
    //----------------------------------------------
    private Chronometer time;
    private long pauseOffset;
    private boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicecall_end);
        mapping();
        getInfor();
        Core core = LinphoneService.getCore();
        // We need to tell the core in which to display what
        // Listen for call state changes
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {

                if(state == Call.State.Connected){
                    time.setVisibility(View.VISIBLE);
                    startTimer();
                }
                if (state == Call.State.End || state == Call.State.Released) {
                    pauseTimer();
                    String callLength = time.getText().toString();

                    if (state == Call.State.End) {

                        Toast.makeText(getApplicationContext(), "Voice calls duration: " + callLength, Toast.LENGTH_LONG).show();
                        LinphoneService.getCore().clearCallLogs();
                        LinphoneService.getCore().removeListener(mCoreListener);

                    }

                    finish();

                }
            }
        };
        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectVoiceCall();
            }
        });
        btn_reject1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectVoiceCall();
                pauseTimer();
                String callLength = time.getText().toString();

                // get info of user is logging in
                ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
                Address userLoggingInAddress = cfg.getContact();
                String idOfUserIsLoggingIn = "sip:" + userLoggingInAddress.getUsername() + "@bof-ims.dek.vn";
                // get timeCreate of Message
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date currentDateTime = new Date();
                String timeCreate = formatter.format(currentDateTime);
                // callLength

                Toast.makeText(getApplicationContext(), "Voice calls duration: " + callLength, Toast.LENGTH_LONG).show();
                //Insert into database
//                insertCallToDatabase insertCallToDatabase = new insertCallToDatabase();
//                insertCallToDatabase.execute(userID, idOfUserIsLoggingIn, timeCreate, callLength, "0");
            }
        });
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutAccept();
                acceptVoiceCall();
            }
        });
        manager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);
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
    protected void onResume() {
        super.onResume();
        LinphoneService.getCore().addListener(mCoreListener);
    }

    private void mapping() {
        btn_accept = findViewById(R.id.voicecall_end_img_accept);
        btn_reject = findViewById(R.id.voicecall_end_img_reject);
        btn_reject1 = findViewById(R.id.voicecall_end_img_reject1);
        layout_waiting_accept = findViewById(R.id.layout_waiting_accept);
        layout_accepted = findViewById(R.id.layout_accepted);
        img_avatar = findViewById(R.id.avatar);
        txt_disPlayName = findViewById(R.id.disPlayName);
        time = findViewById(R.id.time);
        speaker = findViewById(R.id.speaker);
    }

    private void rejectVoiceCall() {
        stopVoiceCall();
        //Toast.makeText(getApplicationContext(),"Voice call has been rejected", Toast.LENGTH_LONG).show();
    }

    private void acceptVoiceCall() {

        Call call = LinphoneService.getCore().getCurrentCall();
        CallParams params = LinphoneService.getCore().createCallParams(call);
        params.enableAudio(true);
        call.acceptWithParams(params);
        AudioRoute.fromInt(1);
        manager.setSpeakerphoneOn(false);

    }

    private void setLayoutAccept() {
        layout_waiting_accept.setVisibility(View.GONE);
        layout_accepted.setVisibility(View.VISIBLE);

    }

    private void stopVoiceCall() {
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

    void getInfor() {
        Intent intent = getIntent();
        disPlayName = intent.getStringExtra("disPlayName");
        avatar = intent.getStringExtra("avatar");
        txt_disPlayName.setText(disPlayName);
        userID = intent.getStringExtra("idUser");
        Picasso.with(getApplicationContext()).load(avatar)
                .placeholder(R.drawable.loader)
                .error(R.drawable.broken)
                .into(img_avatar);
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


    class insertCallToDatabase extends AsyncTask<String, Void, String> {
        String error;

        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "INSERT INTO CallsOrVideos (idCaller, idCallee, timeCreate, callLength, type) " +
                        "VALUES (?,?,?,?,?)";
                // create the mysql insert preparedStatement
                PreparedStatement preparedStmt = con.prepareStatement(query);
                preparedStmt.setString(1, params[0]);
                preparedStmt.setString(2, params[1]);
                preparedStmt.setString(3, params[2]);
                preparedStmt.setString(4, params[3]);
                preparedStmt.setString(5, params[4]);
                // execute the preparedStatement
                preparedStmt.execute();
                con.close();

            } catch (SQLException e) {
                e.printStackTrace();
                error = e.toString();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                error = e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
