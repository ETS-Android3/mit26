package com.example.myapplication.Ultil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;


public class SessionManager {
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE=0;

    private static final String PREF_NAME="LOGIN";
    private static final String LOGIN="IS_LOGIN";
    //private static final String EMAIL="EMAIL";
    private static final String SIP_ID="SIP_ID";
    private static final String PASSWORD="PASSWORD";

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences=context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor=sharedPreferences.edit();

    }
    public void createSession(String Sip_id,String Password){
        editor.putBoolean(LOGIN,true);
        editor.putString(SIP_ID,Sip_id);
        editor.putString(PASSWORD,Password);
        editor.apply();
    }

    public boolean islogin(){
        return sharedPreferences.getBoolean(LOGIN,false);
    }

    public void checkLogin(){
        if(!this.islogin()){

        }
    }
    public void logout(){
        editor.clear();
        editor.commit();
        Toast.makeText(context,"Account is logout",Toast.LENGTH_SHORT).show();
    }
}
