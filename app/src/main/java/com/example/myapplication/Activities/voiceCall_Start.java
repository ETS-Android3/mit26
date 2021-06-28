package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;
import com.squareup.picasso.Picasso;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class voiceCall_Start extends AppCompatActivity {

    ImageView btn_reject, speaker;
    TextView txt_userName, txt_connecting;
    String strUserName;
    private CoreListenerStub mCoreListener;
    String avatar;
    ImageView img_avatar;

    AudioManager manager;
    String userID;

    //-----------------------------------------------
    private Chronometer time;
    private long pauseOffset;
    private boolean running;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicecall_start);
        mapping();
        getInfor();
        Core core = LinphoneService.getCore();
        // We need to tell the core in which to display what
        // Listen for call state changes
        manager = (AudioManager)getSystemService(getApplicationContext().AUDIO_SERVICE);
        manager.setSpeakerphoneOn(false);
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {


                if (state == Call.State.Connected) {
                    speaker.setVisibility(View.VISIBLE);
                    time.setVisibility(View.VISIBLE);
                    manager.setSpeakerphoneOn(false);
                    startTimer();
                }
                if (state == Call.State.End || state == Call.State.Released) {
                    pauseTimer();
                    String callLength = time.getText().toString();

                    if (state == Call.State.Released) {

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
                pauseTimer();
                //Insert into database

                // get info of user is logging in
                ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
                Address userLoggingInAddress = cfg.getContact();
                String idOfUserIsLoggingIn = "sip:" + userLoggingInAddress.getUsername() + "@bof-ims.dek.vn";

                // get info of receiver

                // get timeCreate of Message
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date currentDateTime = new Date();
                String timeCreate = formatter.format(currentDateTime);

                // callLength
                String callLength = time.getText().toString();
                Toast.makeText(getApplicationContext(), "Voice calls duration: " + callLength, Toast.LENGTH_LONG).show();
//                insertCallToDatabase insertCallToDatabase = new insertCallToDatabase();
//                insertCallToDatabase.execute(idOfUserIsLoggingIn, userID, timeCreate, callLength, "0");
            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
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

    private void getInfor() {
        txt_connecting.setText("Voice call ");
        Intent intent = getIntent();

        avatar = intent.getStringExtra("avatar");
        userID = intent.getStringExtra("idUser");
        txt_userName.setText(intent.getStringExtra("disPlayName"));

        Picasso.with(getApplicationContext()).load(avatar)
                .placeholder(R.drawable.loader)
                .error(R.drawable.broken)
                .into(img_avatar);
    }

    private void rejectVoiceCall() {
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

    private void mapping() {
        btn_reject = findViewById(R.id.voicecall_start_img_reject);
        txt_userName = findViewById(R.id.voice_call_start);
        txt_connecting = findViewById(R.id.connecting_voice);
        img_avatar = findViewById(R.id.avatar);
        time = findViewById(R.id.time);
        speaker = findViewById(R.id.speaker);
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

    private String formatTimeCreate(String timeCreate) {
        String day = timeCreate.substring(0, 10);
        String time = timeCreate.substring(11, 16);
        String dayOfWeek = "";

        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(day);
            //System.out.println(date1);
            dayOfWeek = new SimpleDateFormat("EEEE").format(date1);
           // System.out.println(dayOfWeek);
        } catch (Exception e) {
            System.out.println(e);
        }
        timeCreate = dayOfWeek + ", " + time;
        return timeCreate;
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
