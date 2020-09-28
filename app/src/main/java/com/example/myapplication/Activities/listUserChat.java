package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.userChatAdapter;
import com.example.myapplication.Adapter.userOnlineAdapter;
import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;

import org.linphone.core.Address;
import org.linphone.core.ProxyConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class listUserChat extends Fragment {
    RecyclerView recyclerView;
    ArrayList<userChat> arrayList;
    userChatAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    ImageView btn_createChat, btn_addGroup;
    TextView textViewSearch;

    RecyclerView recyclerViewListOnline;
    ArrayList<userChat> arrayListListOnline;
    userOnlineAdapter adapterListOnline;
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_user_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewListChat);
        linearLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        new getAllBoxChatsOfUserIsLoggingIn().execute();

        //Get data for list online
        recyclerViewListOnline = view.findViewById(R.id.online_User);
        recyclerViewListOnline.setLayoutManager(layoutManager);
        recyclerViewListOnline.setAdapter(adapterListOnline);
        //getUserOnline();

        //
        btn_createChat = view.findViewById(R.id.createChat);
        btn_addGroup = view.findViewById(R.id.addGroup);
        textViewSearch = view.findViewById(R.id.search_username);
        btn_createChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewChat();
            }
        });
        btn_addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get name of boxChat
                String groupChatName = textViewSearch.getText().toString().trim();
                if (groupChatName.equals("")) {
                    Toast.makeText(getContext(), "Please enter group name!", Toast.LENGTH_SHORT).show();
                } else {
                    //insert into group chat
                    insertGroupChatIntoBoxChat insertGroupChatIntoBoxChat = new insertGroupChatIntoBoxChat();
                    insertGroupChatIntoBoxChat.execute(groupChatName);


                    // get max id Box Chat
                    getMaxIdBoxChat getMaxIdBoxChat = new getMaxIdBoxChat();
                    getMaxIdBoxChat.execute();
                    while (getMaxIdBoxChat.getMaxIdBoxChat().equals("undefined")) {

                    }
                    String idBoxChat = getMaxIdBoxChat.getMaxIdBoxChat();
                    // get info of user is logging in
                    ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
                    Address userIsLoggingIn = cfg.getContact();
                    String idOfUserIsLoggingIn = "sip:" + userIsLoggingIn.getUsername() + "@bof-ims.dek.vn";
                    insertIntoUsersInChat insertIntoUsersInChat_1 = new insertIntoUsersInChat();
                    insertIntoUsersInChat_1.execute(idBoxChat, idOfUserIsLoggingIn);
                    Intent intent = new Intent(getContext(), addGroup.class);
                    intent.putExtra("idBoxChat", idBoxChat);
                    intent.putExtra("groupChatName", groupChatName);
                    startActivity(intent);
                }

            }
        });
        return view;
    }



    public void replaceFragment(Fragment newFragment) {
        FragmentTransaction transaction = ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container1, newFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<>();
        adapter = new userChatAdapter(getContext(), arrayList);

        // define for list user online
        arrayListListOnline = new ArrayList<>();
        adapterListOnline = new userOnlineAdapter(getContext(), arrayListListOnline);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatTimeCreate(String timeCreate) {
        //
        String currentDate = java.time.LocalDate.now().toString();

        String day = timeCreate.substring(0, 10);
        String time = timeCreate.substring(11, 16);

        String dayOfWeek = "";

        if (!day.equals(currentDate)) {
            try {
                Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(day);
                //System.out.println(date1);
                dayOfWeek = new SimpleDateFormat("EEEE").format(date1);
                //ToastSystem.out.println(dayOfWeek);
            } catch (Exception e) {
                System.out.println(e);
            }
            timeCreate = dayOfWeek + " " + time;
        } else {
            timeCreate = time;
        }

        return timeCreate;
    }

    private void createNewChat() {
        // get info of user is logging in
        ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
        Address userIsLoggingIn = cfg.getContact();
        String idOfUserIsLoggingIn = "sip:" + userIsLoggingIn.getUsername() + "@bof-ims.dek.vn";

        // user input
        String userName = textViewSearch.getText().toString().trim().toLowerCase();
        if (userName.equals("")) {
            Toast.makeText(getContext(), "Please enter user name!", Toast.LENGTH_LONG).show();
        } else if (userIsLoggingIn.getUsername().equals(userName)) {
            Toast.makeText(getContext(), "It's you", Toast.LENGTH_LONG).show();
        } else {
            //
            String idReceiver = "sip:" + userName + "@bof-ims.dek.vn";
            // from idUser above, get displayName, if nonExist: notify user not found, else do something
            getDisplayName getDisplayName = new getDisplayName();
            getDisplayName.execute(idReceiver);

            while (getDisplayName.getDisplayName().equals("undefined")) {

            }

            String displayName = getDisplayName.getDisplayName();
            if (displayName.equals("nonExist")) {
                Toast.makeText(getContext(), "User not found!", Toast.LENGTH_LONG).show();
            } else {
                // do something

                // get boxChat of two user, if nonExist: create, else load boxChat
                getBoxChat getBoxChat = new getBoxChat();
                getBoxChat.execute(idOfUserIsLoggingIn, idReceiver);
                while (getBoxChat.getBoxChat().equals("undefined")) {

                }
                // if boxChat is nonExisted: create
                if (getBoxChat.getBoxChat().equals("nonExist")) {

                    insertIntoBoxChat insertIntoBoxChat = new insertIntoBoxChat();
                    insertIntoBoxChat.execute();
                    // get max id Box Chat
                    getMaxIdBoxChat getMaxIdBoxChat = new getMaxIdBoxChat();
                    getMaxIdBoxChat.execute();
                    while (getMaxIdBoxChat.getMaxIdBoxChat().equals("undefined")) {

                    }
                    String idBoxChat = getMaxIdBoxChat.getMaxIdBoxChat();

                    // insert into UsersInChat
                    insertIntoUsersInChat insertIntoUsersInChat_1 = new insertIntoUsersInChat();
                    insertIntoUsersInChat_1.execute(idBoxChat, idOfUserIsLoggingIn);

                    insertIntoUsersInChat insertIntoUsersInChat_2 = new insertIntoUsersInChat();
                    insertIntoUsersInChat_2.execute(idBoxChat, idReceiver);

                    Intent intent = new Intent(getContext(), Chat1.class);
                    intent.putExtra("userName", displayName);
                    intent.putExtra("idBoxChat", idBoxChat);
                    intent.putExtra("idUser", idReceiver);
                    intent.putExtra("type", "0");
                    startActivity(intent);

                    // get
                } else {
                    Intent intent = new Intent(getContext(), Chat1.class);
                    intent.putExtra("userName", displayName);
                    intent.putExtra("idBoxChat", getBoxChat.getBoxChat());
                    intent.putExtra("idUser", idReceiver);
                    intent.putExtra("type", "0");
                    startActivity(intent);
                }
            }

        }
    }

    class insertIntoBoxChat extends AsyncTask<String, Void, String> {
        String error;

        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "INSERT INTO BoxChats (type) VALUES (0);";
                // create the mysql insert preparedStatement
                PreparedStatement preparedStmt = con.prepareStatement(query);

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

    class insertGroupChatIntoBoxChat extends AsyncTask<String, Void, String> {
        String error;

        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "INSERT INTO BoxChats (boxChatName, type) VALUES (?, 1);";
                // create the mysql insert preparedStatement
                PreparedStatement preparedStmt = con.prepareStatement(query);
                preparedStmt.setString(1, params[0]);
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

    class insertIntoUsersInChat extends AsyncTask<String, Void, String> {
        String error;

        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "INSERT INTO UsersInChat (idBoxChat, idUser) VALUES (?,?);";
                // create the mysql insert preparedStatement
                PreparedStatement preparedStmt = con.prepareStatement(query);
                preparedStmt.setString(1, params[0]);
                preparedStmt.setString(2, params[1]);

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

    class getMaxIdBoxChat extends AsyncTask<String, Void, String> {
        String error;
        String idBoxChat = "undefined";

        @Override
        protected String doInBackground(String... params) {
            // get all idBoxChat of user
            String temp = "nonExist";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "select max(idBoxChat) from BoxChats";
                PreparedStatement preparedStatement = con.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    temp = resultSet.getString("max(idBoxChat)");
                }
                setMaxIdBoxChat(temp);
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

        public String getMaxIdBoxChat() {
            return idBoxChat;
        }

        public void setMaxIdBoxChat(String id) {
            idBoxChat = id;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    class getBoxChat extends AsyncTask<String, Void, String> {
        String error;
        String isExist = "undefined";

        @Override
        protected String doInBackground(String... params) {
            // get all idBoxChat of user
            String iE = "nonExist";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "select a.idBoxChat from (select u.idBoxChat from UsersInChat u join BoxChats b on b.idBoxChat=u.idBoxChat WHERE u.idUser = ? and b.type='0') as a " +
                        "INNER JOIN (select u.idBoxChat from UsersInChat u join BoxChats b on b.idBoxChat=u.idBoxChat WHERE u.idUser = ? and b.type='0') as b USING(idBoxChat);";
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, params[0]);
                preparedStatement.setString(2, params[1]);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    iE = resultSet.getString("idBoxChat");
                }
                setBoxChat(iE);
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

        public String getBoxChat() {
            return isExist;
        }

        public void setBoxChat(String ist) {
            isExist = ist;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    class getDisplayName extends AsyncTask<String, Void, String> {
        String error;
        String displayName = "undefined";

        @Override
        protected String doInBackground(String... params) {
            // get all idBoxChat of user
            String subDisplayName = "nonExist";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "select * from Users where idUser = ?;";
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, params[0]);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    subDisplayName = resultSet.getString("displayName");
                }
                setDisplayName(subDisplayName);
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

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String subDisplayName) {
            displayName = subDisplayName;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    class getAllBoxChatsOfUserIsLoggingIn extends AsyncTask<Void, Void, Void> {
        String error;
        // id of user is logging in
        ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
        Address address = cfg.getContact();
        String strIdUser = "sip:" + address.getUsername() + "@bof-ims.dek.vn";

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids) {
            // get all idBoxChat of user
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                Statement stmt = con.createStatement();
                String query = "select idBoxChat, type, timeCreate from " +
                        "(select b.idBoxChat, b.type, max(timeCreate) as timeCreate from Messages m JOIN BoxChats b on b.idBoxChat = m.idBoxChat group by idBoxChat) as source " +
                        "where idBoxChat in " +
                        "(select b.idBoxChat from BoxChats b JOIN UsersInChat u ON u.idBoxChat =b.idBoxChat where u.idUser = '" + strIdUser + "') order by timeCreate DESC;";


                ResultSet resultSet = stmt.executeQuery(query);

                // with each idBoxChat: get idBoxChat, boxChatName/displayName, contentMessage, timeCreate of last message
                while (resultSet.next()) {
                    // type = 1: groupChat
                    if (resultSet.getString("type").equals("1")) {
                        Statement stmt_2 = con.createStatement();
                        ResultSet resultSet_2 = stmt_2.executeQuery("SELECT b.idBoxChat, b.boxChatName, m.content, m.timeCreate " +
                                "from Messages m JOIN BoxChats b ON b.idBoxChat = m.idBoxChat where b.idBoxChat = '" + resultSet.getString("idBoxChat") + "' " +
                                "and timeCreate in (select max(timeCreate) From Messages as c where idBoxChat = '" + resultSet.getString("idBoxChat") + "');");
                        while (resultSet_2.next()) {
                            String timeCreate = resultSet_2.getString("timeCreate");
                            timeCreate = formatTimeCreate(timeCreate);
                            arrayList.add(new userChat(resultSet_2.getString("idBoxChat"), "", "", resultSet_2.getString("boxChatName"), timeCreate, resultSet_2.getString("content"), resultSet.getString("type")));
                        }

                        // else chat
                    } else {
                        Statement stmt_3 = con.createStatement();
                        ResultSet resultSet_3 = stmt_3.executeQuery("select idBoxChat, displayName, idUser,moreInfo, content, timeCreate from " +
                                "((SELECT content, idBoxChat, timeCreate FROM Messages where idBoxChat = '" + resultSet.getString("idBoxChat") + "')  as allChat " +
                                "CROSS JOIN (select u.displayName,u.moreInfo, u.idUser from Users u Join UsersInChat us ON us.idUser = u.idUser where us.idBoxChat = '" + resultSet.getString("idBoxChat") + "') as a) " +
                                "where timeCreate in " +
                                "(select max(timeCreate) From Messages as c where idBoxChat = '" + resultSet.getString("idBoxChat") + "') " +
                                ";");
                        while (resultSet_3.next()) {
                            if (!resultSet_3.getString("idUser").equals(strIdUser)) {

                                System.out.println(resultSet_3.getString("idBoxChat"));
                                String timeCreate = resultSet_3.getString("timeCreate");
                                timeCreate = formatTimeCreate(timeCreate);
                                arrayList.add(new userChat(resultSet_3.getString("idBoxChat"), resultSet_3.getString("idUser"), resultSet_3.getString("moreInfo"), resultSet_3.getString("displayName"), timeCreate, resultSet_3.getString("content"), resultSet.getString("type")));

                            }
                        }
                    }
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
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
        }

    }

    private void getUserOnline() {

        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        arrayListListOnline.add(new userChat("0", "0", "", "0", "0", "", ""));
        adapterListOnline.notifyDataSetChanged();
    }

}
