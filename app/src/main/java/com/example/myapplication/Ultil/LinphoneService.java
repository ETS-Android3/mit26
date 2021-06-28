package com.example.myapplication.Ultil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.Activities.videoCall_End;
import com.example.myapplication.Activities.videoCall_Start;
import com.example.myapplication.Activities.voiceCall_End;
import com.example.myapplication.Activities.voiceCall_Start;
import com.example.myapplication.R;

import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;
import org.linphone.core.tools.Log;
import org.linphone.mediastream.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Build.VERSION_CODES.R;

public class LinphoneService extends Service {
    private static final String START_LINPHONE_LOGS = " ==== Device information dump ====";
    // Keep a static reference to the Service so we can access it from anywhere in the app
    private static LinphoneService sInstance;

    private Handler mHandler;
    private Timer mTimer;

    private Core mCore;
    private CoreListenerStub mCoreListener;

    public static boolean enableRedirect = true; // set redirect chỗ này nè <----------------------

    public static boolean isReady() {
        return sInstance != null;
    }

    public static LinphoneService getInstance() {
        return sInstance;
    }

    public static Core getCore() {
        return sInstance.mCore;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        // The first call to liblinphone SDK MUST BE to a Factory method
        // So let's enable the library debug logs & log collection
        String basePath = getFilesDir().getAbsolutePath();
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, "Be On Fresh");

        // Dump some useful information about the device we're running on
        Log.i(START_LINPHONE_LOGS);
        dumpDeviceInformation();
        dumpInstalledLinphoneInformation();

        mHandler = new Handler();
        // This will be our main Core listener, it will change activities depending on events
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {


                //  Toast.makeText(LinphoneService.this, message, Toast.LENGTH_SHORT).show();
                if (state == Call.State.IncomingReceived) {
                    // Call preparation
                    CallParams params = getCore().createCallParams(call);
                    String caller = call.getRemoteAddress().getUsername() + "@bof-ims.dek.vn";
                    String redirected_callee = "toan@bof-ims.dek.vn"; // cho t cái biến nhận input của data vào chỗ này nha

                    // Redirect call to redirected_callee if the caller
                    // is not the redirected_callee
                    if (LinphoneService.enableRedirect && !caller.equals(redirected_callee)) {
                        call.redirect(redirected_callee);
                        call.terminate();
                    } else {
                        boolean checkVideo = call.getRemoteParams().videoEnabled();
                        String idUser = "sip:" + call.getRemoteAddress().getUsername() + "@bof-ims.dek.vn";
                        //System.out.println(idUser + " idid");
                        getAvatar getAvatar = new getAvatar();
                        getAvatar.execute(idUser);
                        while (getAvatar.getAvatar().equals("undefined")) {

                        }
                        String avatar = getAvatar.getAvatar();
                        String displayName = getAvatar.getDisplayName();

                        if (!checkVideo) {
                            params.enableVideo(false);
                            //System.out.println("doan11");
                            Intent intent = new Intent(LinphoneService.this, voiceCall_End.class);
                            intent.putExtra("disPlayName", displayName);
                            intent.putExtra("avatar", avatar);
                            intent.putExtra("idUser", idUser);
                            // As it is the Service that is starting the activity, we have to give this flag
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        if (checkVideo) {
                            params.enableVideo(true);
                            //System.out.println("doan12");
                            Intent intent = new Intent(LinphoneService.this, videoCall_End.class);
                            intent.putExtra("disPlayName", displayName);
                            intent.putExtra("avatar", avatar);
                            intent.putExtra("idUser", idUser);
                            // As it is the Service that is starting the activity, we have to give this flag
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                } else if (state == Call.State.Connected && call.getRemoteParams().videoEnabled()) {
                    // This stats means the call has been established, let's start the call activity
                    Intent intent = new Intent(LinphoneService.this, videoCall_Start.class);
                    // As it is the Service that is starting the activity, we have to give this flag
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        };

//        try {
//            // Let's copy some RAW resources to the device
//            // The default config file must only be installed once (the first time)
//            //copyIfNotExist(R.raw.linphonerc_default, basePath + "/.linphonerc");
//            // The factory config is used to override any other setting, let's copy it each time
//           // copyFromPackage(R.raw.linphonerc_factory, "linphonerc");
//        } catch (IOException ioe) {

//            Log.e(ioe);
//        }

        // Create the Core and add our listener
        mCore = Factory.instance()
                .createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);
        mCore.addListener(mCoreListener);
        // Core is ready to be configured
        configureCore();
    }

    class getAvatar extends AsyncTask<String, Void, String> {
        String avatar = "undefined", displayName = "undefined";
        String error;

        @Override
        protected String doInBackground(String... params) {
            String rsA = "nonExist", rsD = "nonExist";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "Select displayName, moreInfo from Users where idUser = ?;";
                ResultSet resultSet;
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, params[0]);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    rsA = resultSet.getString("moreInfo");
                    rsD = resultSet.getString("displayName");
                }
                setAvatar(rsA);
                setDisplayName(rsD);
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

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String rs) {
            avatar = rs;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String rs) {
            displayName = rs;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // If our Service is already running, no need to continue
        if (sInstance != null) {
            return START_STICKY;
        }

        // Our Service has been started, we can keep our reference on it
        // From now one the Launcher will be able to call onServiceReady()
        sInstance = this;

        // Core must be started after being created and configured
        mCore.start();
        // We also MUST call the iterate() method of the Core on a regular basis
        TimerTask lTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCore != null) {
                                            mCore.iterate();
                                        }
                                    }
                                });
                    }
                };
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mCore.removeListener(mCoreListener);
        mTimer.cancel();
        mCore.stop();
        // A stopped Core can be started again
        // To ensure resources are freed, we must ensure it will be garbage collected
        mCore = null;
        // Don't forget to free the singleton as well
        sInstance = null;

        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // For this sample we will kill the Service at the same time we kill the app
        stopSelf();

        super.onTaskRemoved(rootIntent);
    }

    private void configureCore() {
        // We will create a directory for user signed certificates if needed
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.e(userCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(userCerts);
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Supported ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        sb.append("\n");
        Log.i(sb.toString());
    }

    private void dumpInstalledLinphoneInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.e(nnfe);
        }

        if (info != null) {
            Log.i(
                    "[Service] Linphone version is ",
                    info.versionName + " (" + info.versionCode + ")");
        } else {
            Log.i("[Service] Linphone version is unknown");
        }
    }

    private void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }


}
