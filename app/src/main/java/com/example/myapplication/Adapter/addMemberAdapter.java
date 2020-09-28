package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activities.videoCall_Start_waiting;
import com.example.myapplication.Activities.voiceCall_Start;
import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;
import com.squareup.picasso.Picasso;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class addMemberAdapter extends RecyclerView.Adapter<addMemberAdapter.ItemHolder> {
    Context context;
    ArrayList<userChat> arrayList;
    public Button button;

    public addMemberAdapter(Context context, ArrayList<userChat> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_add,null);
        ItemHolder itemHolder = new ItemHolder(v);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        userChat userchat = arrayList.get(position);
        holder.txt_userName.setMaxLines(1);
        holder.txt_userName.setEllipsize(TextUtils.TruncateAt.END);
        holder.txt_userName.setText(userchat.getUserName());
        if(userchat.getAvatar().length()<=0){

        }else {
            Picasso.with(context).load(userchat.getAvatar())
                    .placeholder(R.drawable.loader)
                    .error(R.drawable.broken)
                    .into(holder.img_avatar);
        }

    }
    public class ItemHolder extends RecyclerView.ViewHolder{
        public ImageView img_avatar,img_correct;
        public TextView txt_userName;
        public ImageView addMember;

        public ItemHolder(View itemView) {
            super(itemView);
            img_avatar= itemView.findViewById(R.id.itemUserCall_avatar1);
            img_correct =itemView.findViewById(R.id.correct);
            txt_userName=itemView.findViewById(R.id.itemUserCall_userName);
            addMember =itemView.findViewById(R.id.addMember);

            addMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String idBoxChat = arrayList.get(getPosition()).getIdboxchat();
                    String idUser = arrayList.get(getPosition()).getUserid();

                    insertIntoUsersInChat insertIntoUsersInChat = new insertIntoUsersInChat();
                    insertIntoUsersInChat.execute(idBoxChat,idUser);
                   //
                    // Toast.makeText(context,idBoxChat + " and " + idUser,Toast.LENGTH_LONG).show();
                    addMember.setVisibility(View.GONE);
                    img_correct.setVisibility(View.VISIBLE);
                }
            });

        }
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void pushFragment(Fragment newFragment, Context context){
        FragmentTransaction transaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container2, newFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    class insertIntoUsersInChat extends AsyncTask<String, Void, String> {
        String error;

        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "INSERT INTO UsersInChat (idBoxChat, idUser) " +
                        "VALUES (?,?)";
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
}
