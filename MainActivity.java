package com.example.myapplication.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.airbnb.lottie.L;
import com.example.myapplication.Model.Message;
import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;

    private CoreListenerStub mCoreListener;
    private NotificationManagerCompat notificationManagerCompat;
    public static final String CHANNEL__ID = "channel";
    Timer timer = new Timer();
    private boolean flag = true;
    View redirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mapping();
        notificationManagerCompat = NotificationManagerCompat.from(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("Message");

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_chat:
                        listUserChat listUserChat = new listUserChat();
                        replaceFragment(listUserChat);
                        toolbar.setTitle("Message");
                        flag = true;
                        break;
                    case R.id.navigation_call:
                        listUserCall listUserCall = new listUserCall();
                        replaceFragment(listUserCall);
                        toolbar.setTitle("Call");
                        flag = false;
                        break;
                }
                return true;
            }
        });
        mCoreListener = new CoreListenerStub() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onMessageReceived(Core lc, ChatRoom room, ChatMessage message) {
                super.onMessageReceived(lc, room, message);
                receiveMessage(lc, room, message);


            }
        };
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                listUserChat listUserChat = new listUserChat();
//                replaceFragment(listUserChat);
//            }
//        },0,5000);

    }


    public void mapping() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        redirect = findViewById(R.id.Redirect);
    }



    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL__ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("this is a channel");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        } else {
            Toast.makeText(getApplicationContext(), "Not support", Toast.LENGTH_SHORT).show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void createNotification(String name, String message) {
        android.app.Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL__ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(name)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManagerCompat.notify(1, notification);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                finish();
                Intent intentLogout = new Intent(getApplicationContext(), Login.class);
                startActivity(intentLogout);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                configureLogout();
                updateLed(LinphoneService.getCore().getDefaultProxyConfig().getState());
                break;
            case R.id.Redirect:
                Intent intentRedirect = new Intent(MainActivity.this, Redirect.class);
                startActivity(intentRedirect);
                redirect.setVisibility(View.VISIBLE);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void addFragment(Fragment newFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container1, newFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // transaction.addToBackStack(null);
        transaction.commit();
    }

    public void replaceFragment(Fragment newFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container1, newFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Ask runtime permissions, such as record audio and camera
        // We don't need them here but once the user has granted them we won't have to ask again

        checkAndRequestCallPermissions();
//        listUserChat listUserChat = new listUserChat();
//        replaceFragment(listUserChat);
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        LinphoneService.getCore().addListener(mCoreListener);
        // Manually update the LED registration state, in case it has been registered before
        // we add a chance to register the above listener
        ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
        if (proxyConfig != null) {
            updateLed(proxyConfig.getState());
            if (flag) {
                listUserChat listUserChat = new listUserChat();
                // addFragment(listUserChat);

                replaceFragment(listUserChat);
            } else {
                listUserCall listUserCall = new listUserCall();
                replaceFragment(listUserCall);
            }

        } else {
            // No account configured, we display the configuration activity
            startActivity(new Intent(this, Login.class));
        }

    }

    private void updateLed(RegistrationState state) {
        switch (state) {
            case Ok: // This state means you are connected, to can make and receive calls & messages
                //mLed.setImageResource(R.drawable.led_connected);
                //Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_LONG).show();
                break;
            case None: // This state is the default state
            case Cleared: // This state is when you disconnected
                //mLed.setImageResource(R.drawable.led_disconnected);
                //Toast.makeText(getApplicationContext(), "dis", Toast.LENGTH_LONG).show();
                break;
            case Failed: // This one means an error happened, for example a bad password
                //Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_LONG).show();
                //mLed.setImageResource(R.drawable.led_error);
                break;
            case Progress: // Connection is in progress, next state will be either Ok or Failed
                // mLed.setImageResource(R.drawable.led_inprogress);
                //Toast.makeText(getApplicationContext(), "Progress", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onPause() {
        // Like I said above, remove unused Core listeners in onPause
        LinphoneService.getCore().removeListener(mCoreListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LinphoneService.getCore().clearAllAuthInfo();
        LinphoneService.getCore().clearProxyConfig();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        // Callback for when permissions are asked to the user
        for (int i = 0; i < permissions.length; i++) {
            Log.i(
                    "[Permission] "
                            + permissions[i]
                            + " is "
                            + (grantResults[i] == PackageManager.PERMISSION_GRANTED
                            ? "granted"
                            : "denied"));
        }
    }

    private void checkAndRequestCallPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();

        // Some required permissions needs to be validated manually by the user
        // Here we ask for record audio and camera to be able to make video calls with sound
        // Once granted we don't have to ask them again, but if denied we can
        int recordAudio =
                getPackageManager()
                        .checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName());
        Log.i(
                "[Permission] Record audio permission is "
                        + (recordAudio == PackageManager.PERMISSION_GRANTED
                        ? "granted"
                        : "denied"));
        int camera =
                getPackageManager().checkPermission(Manifest.permission.CAMERA, getPackageName());
        Log.i(
                "[Permission] Camera permission is "
                        + (camera == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            Log.i("[Permission] Asking for record audio");
            permissionsList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (camera != PackageManager.PERMISSION_GRANTED) {
            Log.i("[Permission] Asking for camera");
            permissionsList.add(Manifest.permission.CAMERA);
        }

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    private void configureLogout() {
        // At least the 3 below values are required


        // By default it will be UDP if not set, but TLS is strongly recommended
//        switch (mTransport.getCheckedRadioButtonId()) {
//            case R.id.transport_udp:
        //mAccountCreator = LinphoneService.getCore().createAccountCreator(null);
//                break;
//            case R.id.transport_tcp:
//                mAccountCreator.setTransport(TransportType.Tcp);
//                break;
//            case R.id.transport_tls:
//                mAccountCreator.setTransport(TransportType.Tls);
//                break;
        //       }

        // This will automatically create the proxy config and auth info and add them to the Core
        ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
        cfg.edit();
        cfg.setExpires(0);
        cfg.done();

        // Make sure the newly created one is the default
        LinphoneService.getCore().setDefaultProxyConfig(cfg);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void receiveMessage(Core lc, ChatRoom room, ChatMessage message) {
        // create popup
        createNotificationChannels();
        String contentReceive = message.getTextContent();
        int index = contentReceive.indexOf("-");
        String contentMessage = contentReceive.substring(index + 1);
        String idBC = contentReceive.substring(0, index);

        // get info of user is logging in
        ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
        Address userLoggingInAddress = cfg.getContact();
        String idOfUserIsLoggingIn = "sip:" + userLoggingInAddress.getUsername() + "@bof-ims.dek.vn";

        getNameForPopUp getNameForPopUp = new getNameForPopUp();
        getNameForPopUp.execute(idBC, idOfUserIsLoggingIn);
        while (getNameForPopUp.getName().equals("undefined")) {

        }
        String nameOfBC = getNameForPopUp.getName();
        createNotification(nameOfBC, contentMessage);


        listUserChat listUserChat = new listUserChat();
        replaceFragment(listUserChat);
    }

    class getNameForPopUp extends AsyncTask<String, Void, String> {
        String result = "undefined";
        String error;

        @Override
        protected String doInBackground(String... params) {
            String rs = "nonExist";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "select b.type, b.boxChatName, us.idUser, us.displayName from (UsersInChat u JOIN BoxChats b on b.idBoxChat=u.idBoxChat)JOIN Users us on us.idUser = u.idUser where u.idBoxChat = ?;";
                ResultSet resultSet;
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, params[0]);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {

                    if (resultSet.getString("type").equals("1")) {
                        rs = resultSet.getString("boxChatName");
                        break;
                    } else {
                        if (!resultSet.getString("idUser").equals(params[1])) {
                            rs = resultSet.getString("displayName");
                            break;
                        } else {
                            rs = "nonExist";
                        }
                    }
                }
                setName(rs);
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                error = e.toString();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                error = e.toString();
            }
            return rs;
        }

        public String getName() {
            return result;
        }

        public void setName(String rs) {
            result = rs;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
