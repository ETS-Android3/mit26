package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import java.text.ParseException;

public class Login extends AppCompatActivity {
    EditText edit_username,edit_domain,edit_password;
    Button btn_login;

    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
//        startService(new Intent(this, LinphoneService.class));
        mAccountCreator = LinphoneService.getCore().createAccountCreator(null);
        mapping();
//        final ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidation()){
                    configureAccount();


                }else {
                    Toast.makeText(getApplicationContext(),"Please enter all of fields",Toast.LENGTH_LONG).show();
                }
            }
        });
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                if (state == RegistrationState.Ok) {
                    //finish();
                    //Toast.makeText(Login.this, "Success: " + message, Toast.LENGTH_LONG).show();

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
        //System.out.println("pppppppppppppppppppppppppppppppppppppp");
        LinphoneService.getCore().addListener(mCoreListener);
    }

    @Override
    protected void onPause() {
        LinphoneService.getCore().removeListener(mCoreListener);

        super.onPause();
        //System.out.println("ppppppppppppppppppppp");
    }

    @Override
    protected void onDestroy() {

        LinphoneService.getCore().clearProxyConfig();
        super.onDestroy();

    }

    private void configureAccount() {
        // At least the 3 below values are required
        mAccountCreator.setUsername(edit_username.getText().toString());
        mAccountCreator.setDomain(edit_domain.getText().toString());
        mAccountCreator.setPassword(edit_password.getText().toString());
     //   Integer Expire = Integer.parseInt(expire_time.getText().toString());



        ProxyConfig cfg = mAccountCreator.createProxyConfig();
        cfg.edit();
        Address proxy = Factory.instance().createAddress("sip:192.168.122.40");
        cfg.setServerAddr(proxy.asString());
        cfg.enableRegister(true);
        cfg.setExpires(3600);
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
        if(edit_username.length()<=0 || edit_password.length()<=0 || edit_password.length()<=0 ){
            return false;
        }
        return true;
    }

    private void mapping() {
        edit_username=findViewById(R.id.login_edit_username);
        edit_domain=findViewById(R.id.login_edit_domain);
        edit_password=findViewById(R.id.login_edit_password);
       // expire_time =findViewById((R.id.expire_time));
        btn_login=findViewById(R.id.login_btn_login);
    }
}
