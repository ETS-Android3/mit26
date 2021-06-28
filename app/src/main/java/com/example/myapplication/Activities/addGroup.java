package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.Adapter.addMemberAdapter;
import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class addGroup extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<userChat> arrayList;
    addMemberAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    String idBoxChat, groupChatName;
    Button btn_done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        getInfo();

        btn_done = findViewById(R.id.btnCreateDone);
        recyclerView = findViewById(R.id.recyclerViewListCall);

        linearLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(linearLayoutManager);
        arrayList = new ArrayList<>();
        adapter = new addMemberAdapter(getApplicationContext(), arrayList);
        recyclerView.setAdapter(adapter);

        new getAllUsers().execute(idBoxChat);

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), Chat1.class);
                intent.putExtra("idBoxChat", idBoxChat);
                intent.putExtra("userName", groupChatName);
                intent.putExtra("type", "1");
                startActivity(intent);
            }
        });
    }

    private void getInfo() {
        Intent intent = getIntent();
        idBoxChat = intent.getStringExtra("idBoxChat");
        groupChatName = intent.getStringExtra("groupChatName");
    }

    class getAllUsers extends AsyncTask<String, Void, String> {
        String error;

        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "select idUser, displayName, moreInfo from Users where idUser not in (select idUser from UsersInChat where idBoxChat = ?);";
                ResultSet resultSet;
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, params[0]);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    // athang - mit26
                    arrayList.add(new userChat(idBoxChat, resultSet.getString("idUser"),
                            resultSet.getString("moreInfo"),
                            resultSet.getString("displayName"),
                            "", "", ""));
                }
                // athang - mit26
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
            adapter.notifyDataSetChanged();
        }
    }
}
