package com.example.myapplication.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.MessageAdapter1;
import com.example.myapplication.Model.Message;
import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;

import org.linphone.core.Address;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Chat1 extends AppCompatActivity {

    Toolbar toolbar;
    private CoreListenerStub mCoreListener;
    String strUserName;
    String idBoxChat;
    TextView txtUserName;
    ImageView btn_sendMessage;
    EditText textInput;
    String receiver;
    String typeOfBoxChat;
    RecyclerView recyclerView;
    ArrayList<Message> arrayList;
    MessageAdapter1 adapter;
    LinearLayoutManager linearLayoutManager;
    String idOfUserIsLoggingIn;
    private NotificationManagerCompat notificationManagerCompat;
    public static final String CHANNEL__ID = "channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        mapping();
        getInfo();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkExist();

        arrayList = new ArrayList<>();
        adapter = new MessageAdapter1(getApplicationContext(), arrayList);
        recyclerView = findViewById(R.id.listChat);
        linearLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        txtUserName.setText(strUserName);

        btn_sendMessage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                // get text
                String textToShow = textInput.getText().toString();
                String textToSend = idBoxChat + "-" + textToShow;

                if (textToShow.equals("")) {
                    Toast.makeText(getApplicationContext(), "Empty message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(textToShow, textToSend);
                }
            }
        });

        mCoreListener = new CoreListenerStub() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onMessageReceived(Core lc, ChatRoom room, ChatMessage message) {
                super.onMessageReceived(lc, room, message);
                receiveMessage(lc, room, message);
            }
        };

    }

    private void checkExist(){
        getBoxChatExist getBoxChatExist = new getBoxChatExist();
        getBoxChatExist.execute(idBoxChat, idOfUserIsLoggingIn);
        while (getBoxChatExist.getBoxChat().equals("undefined")){

        }
        if(getBoxChatExist.getBoxChat().equals("nonExist")){
            listUserChat listUserChat = new listUserChat();
            replaceFragment(listUserChat);
            Toast.makeText(getApplicationContext(),"Box chat was deleted", Toast.LENGTH_SHORT).show();
//            finish();

        } else{
            new getAllMessagesOfBoxChat().execute();
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
//        listUserChat listUserChat = new listUserChat();
//        replaceFragment(listUserChat);
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        LinphoneService.getCore().addListener(mCoreListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (typeOfBoxChat.equals("0")) {

        } else {
            getMenuInflater().inflate(R.menu.toolbar_chat, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addMember:
                Intent intent = new Intent(getApplicationContext(), addMembers.class);
                intent.putExtra("idBoxChat", idBoxChat);
                startActivity(intent);
                break;
            case R.id.deleteMember:
                Intent intent1 = new Intent(getApplicationContext(), deleteMembers.class);
                intent1.putExtra("idBoxChat", idBoxChat);
                startActivity(intent1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mapping() {
        toolbar = findViewById(R.id.toolbar_chat);
        txtUserName = findViewById(R.id.chat_userName);
        btn_sendMessage = findViewById(R.id.btn_sendMessage);
        textInput = findViewById(R.id.textInput);
        // recyclerView =view.findViewById(R.id.listChat);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL__ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("This is a channel");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        } else {
            Toast.makeText(getApplicationContext(), "Not support", Toast.LENGTH_SHORT).show();
        }
    }

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

    private void getInfo() {
        // get info of user is logging in
        ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
        Address userLoggingInAddress = cfg.getContact();
        idOfUserIsLoggingIn = "sip:" + userLoggingInAddress.getUsername() + "@bof-ims.dek.vn";

        Intent intent = getIntent();
        strUserName = intent.getStringExtra("userName");
        idBoxChat = intent.getStringExtra("idBoxChat");
        receiver = intent.getStringExtra("idUser");
        typeOfBoxChat = intent.getStringExtra("type");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendMessage(String textToShow, String textToSend) {
        // get timeCreate of Message
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDateTime = new Date();
        String timeCreate = formatter.format(currentDateTime);
        String timeToShow = formatTimeCreate(timeCreate);

        // 0: chat, 1: groupChat
        if (typeOfBoxChat.equals("0")) {
            sendToChat(idOfUserIsLoggingIn, textToSend, timeCreate, textToShow, timeToShow);
        } else {
            sendToGroupChat(idOfUserIsLoggingIn, textToSend, timeCreate, textToShow, timeToShow);
        }
    }

    private void sendToChat(String idOfUserIsLoggingIn, String textToSend, String timeCreate, String textToShow, String timeToShow) {
        // get userName of receiver
        int index = receiver.indexOf("@");
        String userName = receiver.substring(4, index);

        // create address of receiver
        Address peer_address = Factory.instance().createAddress(receiver);
        peer_address.setDomain("bof-ims.dek.vn");
        peer_address.setPort(5060);
        peer_address.setTransport(TransportType.Udp);
        peer_address.setUsername(userName);

        // create chatRoom with peer address and send message
        ChatRoom chatRoom = LinphoneService.getCore().createChatRoom(peer_address);
        ChatMessage chatMessage = chatRoom.createMessage(textToSend);
        chatRoom.sendChatMessage(chatMessage);

        // insert to db
        if (chatMessage.isOutgoing()) {
            insertMessageToDatabase insert = new insertMessageToDatabase();
            insert.execute(idOfUserIsLoggingIn, idBoxChat, "0", timeCreate, textToShow);
        }

        // add message to UI
        addMessage(textToShow, idOfUserIsLoggingIn, receiver, timeToShow, "https://cdn.hellobacsi.com/wp-content/uploads/2017/03/185460825-1024x683.jpg");
    }

    private void sendToGroupChat(String idOfUserIsLoggingIn, String textToSend, String timeCreate, String textToShow, String timeToShow) {
        // get all user in group chat
        getUsersInBoxChat getUsersInBoxChat = new getUsersInBoxChat();
        getUsersInBoxChat.execute(idBoxChat);

        // await to done
        while (getUsersInBoxChat.getIdUser().size() == 0) {

        }

        ArrayList<String> participants = new ArrayList<String>();
        participants = (ArrayList<String>) getUsersInBoxChat.getIdUser().clone();

        boolean isGoing = false;

        // create Address of all user
        for (int i = 0; i < participants.size(); i++) {
            // except user is logging in
            if (!idOfUserIsLoggingIn.equals(participants.get(i))) {
                Address peerAddress = Factory.instance().createAddress(participants.get(i));
                peerAddress.setPort(5060);
                peerAddress.setDomain("bof-ims.dek.vn");
                peerAddress.setTransport(TransportType.Udp);

                ChatRoom chatRoom = LinphoneService.getCore().createChatRoom(peerAddress);
                ChatMessage chatMessage = chatRoom.createMessage(textToSend);
                chatMessage.send();
                if (chatMessage.isOutgoing()) {
                    isGoing = true;
                }
            }
        }

        //
        if (isGoing) {
            insertMessageToDatabase insert = new insertMessageToDatabase();
            insert.execute(idOfUserIsLoggingIn, idBoxChat, "0", timeCreate, textToShow);
        }

        // add message to UI
        addMessage(textToShow, idOfUserIsLoggingIn, receiver, timeToShow, "https://cdn.hellobacsi.com/wp-content/uploads/2017/03/185460825-1024x683.jpg");
    }

    class getBoxChatExist extends AsyncTask<String, Void, String> {
        String error;
        String isExist = "undefined";

        @Override
        protected String doInBackground(String... params) {
            // get all idBoxChat of user
            String iE = "nonExist";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "select * from UsersInChat where idBoxChat = ? and idUser = ?;";
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
    class getAllMessagesOfBoxChat extends AsyncTask<Void, Void, Void> {
        String error;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                Statement stmt = con.createStatement();
                ResultSet resultSet = stmt.executeQuery("select m.idSender, u.moreInfo ,m.content, m.timeCreate from Messages m join Users u on u.idUser = m.idSender where m.idBoxChat = '" + idBoxChat + "' order by m.timeCreate;");

                while (resultSet.next()) {
                    // get time message and format it
                    String timeCreate = resultSet.getString("timeCreate");
                    String timeToShow = formatTimeCreate(timeCreate);
                    arrayList.add(new Message(resultSet.getString("idSender"), idOfUserIsLoggingIn, resultSet.getString("content"), timeToShow, resultSet.getString("moreInfo")));
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
            recyclerView.scrollToPosition(arrayList.size() - 1);
        }
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
               //Toast System.out.println(dayOfWeek);
            } catch (Exception e) {
                System.out.println(e);
            }
            timeCreate = dayOfWeek + ", " + time;
        } else {
            timeCreate = time;
        }

        return timeCreate;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void receiveMessage(Core lc, ChatRoom room, ChatMessage message) {

        // get info of peer user is clicked, sender
        Address peerAddress = room.getPeerAddress();
        String idPeerUser = "sip:" + peerAddress.getUsername() + "@bof-ims.dek.vn";

        //get idBoxChat and content from message
        String contentReceive = message.getTextContent();
        int index = contentReceive.indexOf("-");
        String idBoxChatLocal = contentReceive.substring(0, index);
        String contentMessage = contentReceive.substring(index + 1);

        // get timeCreate of Message
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDateTime = new Date();
        String timeCreate = formatter.format(currentDateTime);
        timeCreate = formatTimeCreate(timeCreate);

        getAvatar getAvatar = new getAvatar();
        getAvatar.execute(idPeerUser);
        while (getAvatar.getAvatar().equals("undefined")) {

        }
        String avatar = getAvatar.getAvatar();
        // compare receiver with currentBoxChat, if true, show message
        if (idBoxChatLocal.equals(idBoxChat)) {
            addMessage(contentMessage, idPeerUser, idOfUserIsLoggingIn, timeCreate, avatar);
            recyclerView.scrollToPosition(arrayList.size() - 1);

        } else {
            // create popup
            createNotificationChannels();

            getNameForPopUp getNameForPopUp = new getNameForPopUp();
            getNameForPopUp.execute(idBoxChatLocal, idOfUserIsLoggingIn);
            while (getNameForPopUp.getName().equals("undefined")) {

            }
            String nameOfBC = getNameForPopUp.getName();
            createNotification(nameOfBC, contentMessage);
        }
    }

    private void addMessage(String msg, String sender, String receiver, String time, String avatar) {
        arrayList.add(new Message(sender, receiver, msg, time, avatar));
        adapter.notifyDataSetChanged();
        textInput.setText("");
        recyclerView.scrollToPosition(arrayList.size() - 1);
    }

    class insertMessageToDatabase extends AsyncTask<String, Void, String> {
        String error;

        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "INSERT INTO Messages (idSender, idBoxChat, type, timeCreate, content) " +
                        "VALUES (?,?,?,?,?)";
                // create the mysql insert preparedStatement
                PreparedStatement preparedStmt = con.prepareStatement(query);
                preparedStmt.setString(1, params[0]);
                preparedStmt.setString(2, params[1]);
                preparedStmt.setString(3, params[2]);
                preparedStmt.setString(4, params[3]);
                preparedStmt.setString(5, params[4]);

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

    class getAvatar extends AsyncTask<String, Void, String> {
        String result = "undefined";
        String error;

        @Override
        protected String doInBackground(String... params) {
            String rs = "nonExist";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "Select moreInfo from Users where idUser = ?;";
                ResultSet resultSet;
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, params[0]);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    rs = resultSet.getString("moreInfo");
                }
                setAvatar(rs);
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

        public String getAvatar() {
            return result;
        }

        public void setAvatar(String rs) {
            result = rs;
        }

        @Override
        protected void onPostExecute(String result) {

        }
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

    class getUsersInBoxChat extends AsyncTask<String, Void, ArrayList<String>> {
        public ArrayList<String> result = new ArrayList<>();
        String error;

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> rs = new ArrayList<>();
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "Select idUser from UsersInChat where idBoxChat = ?;";
                ResultSet resultSet;
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, params[0]);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    rs.add(resultSet.getString("idUser"));
                }
                setIdUser(rs);
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

        public ArrayList<String> getIdUser() {
            return result;
        }

        public void setIdUser(ArrayList rs) {
            result = (ArrayList<String>) rs.clone();
        }

        @Override
        protected void onPostExecute(ArrayList result) {

        }
    }

    public void replaceFragment(Fragment newFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container1, newFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // transaction.addToBackStack(null);
        transaction.commit();
    }
    // athang - mit26
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LinphoneService.getCore().removeListener(mCoreListener);
        finish();
    }
    // athang - mit26
}