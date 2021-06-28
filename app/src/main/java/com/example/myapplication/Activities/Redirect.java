package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;

import static org.linphone.mediastream.MediastreamerAndroidContext.getContext;


// athang - mit26: Activity Redirect
public class Redirect extends AppCompatActivity {
    EditText userRedirect, domainRedirect;
    Button btnDoneRedirect;
    ImageButton redirect_back;
    View redirect;
    static boolean toggleRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redirect);

        // mapping
        redirect = findViewById(R.id.redirectlayout);
        redirect_back = findViewById(R.id.redirect_back);
        userRedirect = findViewById(R.id.redirectUsername);
        domainRedirect = findViewById(R.id.redirectDomain);
        btnDoneRedirect = findViewById(R.id.doneRedirect);

        ToggleButton tgbtn_Redirect = findViewById(R.id.toggleRedirect);
        tgbtn_Redirect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleRedirect = isChecked;
            }
        });

        btnDoneRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userRedirect.length() > 0 && toggleRedirect) {
                    String userName = userRedirect.getText().toString();
                    String domain = domainRedirect.getText().toString();
                    LinphoneService.redirected_callee = userName + "@" + domain;
                    LinphoneService.enableRedirect = toggleRedirect;
                    Toast.makeText(Redirect.this, "Saved!", Toast.LENGTH_SHORT).show();
                }
                else if (toggleRedirect) {
                    Toast.makeText(Redirect.this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(Redirect.this, "Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        redirect_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirect.setVisibility(View.INVISIBLE);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LinphoneService.enableRedirect = false;
        toggleRedirect = false;
        redirect.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        //Do anything you want here,
        redirect.setVisibility(View.INVISIBLE);
        finish();
    }
}
