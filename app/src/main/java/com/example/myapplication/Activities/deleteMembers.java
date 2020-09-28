package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.myapplication.Adapter.addMemberAdapter;
import com.example.myapplication.Adapter.deleteMemberAdapter;
import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class deleteMembers extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<userChat> arrayList;
    deleteMemberAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    String idBoxChat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_members);
        recyclerView = findViewById(R.id.recyclerViewListCall);

        linearLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(linearLayoutManager);
        arrayList = new ArrayList<>();
        adapter = new deleteMemberAdapter(getApplicationContext(), arrayList);
        recyclerView.setAdapter(adapter);
        Intent intent = getIntent();
        idBoxChat = intent.getStringExtra("idBoxChat");
        new Task().execute(idBoxChat);
    }

    class Task extends AsyncTask<String, Void, String> {
        String result = "undefined", error;

        @Override
        protected String doInBackground(String... params) {
            String rs = "nonExist";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "select idUser, displayName, moreInfo from Users where idUser in (select idUser from UsersInChat where idBoxChat = ?);";
                ResultSet resultSet;
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, params[0]);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    arrayList.add(new userChat(params[0], resultSet.getString("idUser"), resultSet.getString("moreInfo"), resultSet.getString("displayName"), "", "", "0"));
                }

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
            //Toast.makeText(getContext(),result+"",Toast.LENGTH_SHORT).show();
//            arrayList.add(new userChat("",result,"",""));
//            adapter.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
        }
    }
}
