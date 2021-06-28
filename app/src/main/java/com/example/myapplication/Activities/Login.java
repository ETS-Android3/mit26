package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

public class Login extends AppCompatActivity {
    EditText edit_username,edit_domain,edit_password;
    Button btn_login;
    private static Toast status_message;
    EditText edit_qvalue;

    private long backPressedTime;
    private Toast backToast;


    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mAccountCreator = LinphoneService.getCore().createAccountCreator(null);
        mapping();
//        final ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValidation()) {
                    configureAccount();
                }
                // athang - mit26
                else {
                    status_message.show();
                }
                // athang - mit26
            }
        });
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                if (state == RegistrationState.Ok) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else if (state == RegistrationState.Failed) {
                    Toast.makeText(Login.this, "Failure: " + message, Toast.LENGTH_LONG).show();
                }
            }
        };
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
        LinphoneService.getCore().clearProxyConfig();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Close app when press back 2 times
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            LinphoneService.getCore().removeListener(mCoreListener);
            LinphoneService.getCore().clearProxyConfig();
            finishAffinity();
        }
        else {
            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    private void configureAccount() {
        // At least the 3 below values are required
        mAccountCreator.setUsername(edit_username.getText().toString());
        mAccountCreator.setDomain(edit_domain.getText().toString());
        mAccountCreator.setPassword(edit_password.getText().toString());
        // athang - mit26
        // set q-value khi dang nhap & config cau hinh khi ng dung dang nhap
        float qvalue = Float.parseFloat(edit_qvalue.getText().toString());

        ProxyConfig cfg = mAccountCreator.createProxyConfig();
        cfg.edit();
        Address proxy = Factory.instance().createAddress("sip:192.168.122.40");
        cfg.setServerAddr(proxy.asString());
        cfg.enableRegister(true);
        // athang - mit26

        cfg.setContactParameters("q=" + qvalue);

        cfg.getContactParameters();
        // athang - mit26
        cfg.setExpires(180);
        //cfg.setExpires(Expire + (Expire/10));
        cfg.done();
        // Make sure the newly created one is the default
        LinphoneService.getCore().setDefaultProxyConfig(cfg);
    }

    private void updateLed(RegistrationState state) {
        switch (state) {
            case Ok: // This state means you are connected, to can make and receive calls & messages
                //mLed.setImageResource(R.drawable.led_connected);
                Toast.makeText(getApplicationContext(),"OK",Toast.LENGTH_LONG).show();
                break;
            case None: // This state is the default state
            case Cleared: // This state is when you disconnected
                //mLed.setImageResource(R.drawable.led_disconnected);
                Toast.makeText(getApplicationContext(),"Dis",Toast.LENGTH_LONG).show();
                break;
            case Failed: // This one means an error happened, for example a bad password
                Toast.makeText(getApplicationContext(),"Fail",Toast.LENGTH_LONG).show();
                //mLed.setImageResource(R.drawable.led_error);
                break;
            case Progress: // Connection is in progress, next state will be either Ok or Failed
                // mLed.setImageResource(R.drawable.led_inprogress);
                Toast.makeText(getApplicationContext(),"Progress",Toast.LENGTH_LONG).show();
                break;
        }
    }

    private boolean checkValidation() {
        if (edit_username.length() > 0 && edit_password.length() > 0) {
            if (edit_qvalue.length() > 0) {
                float qvalue = Float.parseFloat(edit_qvalue.getText().toString());
                if (qvalue >= 0.0 && qvalue <= 1.0) return true;
                else status_message = Toast.makeText(getBaseContext(), "Q value must be between 0.0 to 1.0!", Toast.LENGTH_SHORT);
            }
            else status_message = Toast.makeText(getBaseContext(), "Please enter q value between 0.0 to 1.0!", Toast.LENGTH_SHORT);
        }
        else status_message = Toast.makeText(getBaseContext(), "Please enter all fields!", Toast.LENGTH_SHORT);
        return false;
    }

    private void mapping() {
        edit_username=findViewById(R.id.login_edit_username);
        edit_domain=findViewById(R.id.login_edit_domain);
        edit_password=findViewById(R.id.login_edit_password);
        btn_login=findViewById(R.id.login_btn_login);
        edit_qvalue=findViewById(R.id.login_edit_qvalue);
    }
}
