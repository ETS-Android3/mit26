package com.example.myapplication.Activities;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.example.myapplication.Adapter.addMemberAdapter;
import com.example.myapplication.Adapter.userCallAdapter;
import com.example.myapplication.Adapter.userChatAdapter;
import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;

import org.linphone.core.Address;
import org.linphone.core.ProxyConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class listUserCall extends Fragment {

    RecyclerView recyclerView;
    ArrayList<userChat> arrayList;
    userCallAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.list_user_call,container, false);
        recyclerView =view.findViewById(R.id.recyclerViewListCall);

        linearLayoutManager = new GridLayoutManager(getContext(),1);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        new Task().execute();
        
        
        return view;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<>();
        adapter = new userCallAdapter(getContext(),arrayList);
    }
    class Task extends AsyncTask<Void,Void,Void> {
        String result="", error;
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con= DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase","bof","bof");
                Statement stmt=con.createStatement();
                ResultSet resultSet = stmt.executeQuery("select * from Users;");

                while(resultSet.next()){
                    ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
                    Address userLoggingInAddress = cfg.getContact();
                    String idOfUserIsLoggingIn = "sip:" + userLoggingInAddress.getUsername() + "@bof-ims.dek.vn";

                    if(!idOfUserIsLoggingIn.equals(resultSet.getString("idUser"))){
                        arrayList.add(new userChat("",resultSet.getString("idUser"),resultSet.getString("moreInfo"),resultSet.getString("displayName"),"","", ""));
                    }
                }

                con.close();

            } catch (SQLException e) {
                e.printStackTrace();
                error =e.toString();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                error =e.toString();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
           //Toast.makeText(getContext(),result+"",Toast.LENGTH_SHORT).show();
//            arrayList.add(new userChat("",result,"",""));
//            adapter.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
        }
    }
}
