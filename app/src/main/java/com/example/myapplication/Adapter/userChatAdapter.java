package com.example.myapplication.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activities.Chat1;
import com.example.myapplication.Activities.listUserChat;
import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class userChatAdapter extends RecyclerView.Adapter<userChatAdapter.ItemHolder> {
    Context context;
    ArrayList<userChat> arrayList;

    public userChatAdapter(Context context, ArrayList<userChat> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_chat, null);
        ItemHolder itemHolder = new ItemHolder(v);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        userChat userchat = arrayList.get(position);
        holder.txt_userName.setMaxLines(1);
        holder.txt_userName.setEllipsize(TextUtils.TruncateAt.END);
        holder.txt_userName.setText(userchat.getUserName());
        holder.txt_lastMessage.setMaxLines(1);
        holder.txt_lastMessage.setEllipsize(TextUtils.TruncateAt.END);
        holder.txt_lastMessage.setText(userchat.getLastMessage());
        holder.txt_time.setText(userchat.getTime());
        if (userchat.getAvatar().length() <= 0) {

        } else {
            Picasso.with(context).load(userchat.getAvatar())
                    .placeholder(R.drawable.loader)
                    .error(R.drawable.broken)
                    .into(holder.img_avatar);
        }


    }
    public void replaceFragment(Fragment newFragment) {
        FragmentTransaction transaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container1, newFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // transaction.addToBackStack(null);
        transaction.commit();
    }
    public class ItemHolder extends RecyclerView.ViewHolder {
        public ImageView img_avatar;
        public TextView txt_userName, txt_lastMessage, txt_time;

        public ItemHolder(View itemView) {
            super(itemView);
            img_avatar = itemView.findViewById(R.id.itemUserChat_avatar);
            txt_userName = itemView.findViewById(R.id.itemUserChar_userName);
            txt_lastMessage = itemView.findViewById(R.id.itemUserChat_lastMessage);
            txt_time = itemView.findViewById(R.id.itemUserChat_time);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, Chat1.class);
                    intent.putExtra("userName", arrayList.get(getPosition()).getUserName());  // Truyền một String
                    intent.putExtra("idBoxChat", arrayList.get(getPosition()).getIdboxchat());                    // Truyền một Int
                    intent.putExtra("idUser", arrayList.get(getPosition()).getUserid());                 // Truyền một Boolean
                    intent.putExtra("type", arrayList.get(getPosition()).getType());
                    context.startActivity(intent);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Do you want to delete this box chat?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!
                                    deleteBoxChat deleteBoxChat = new deleteBoxChat();
                                    deleteBoxChat.execute(arrayList.get(getPosition()).getIdboxchat());
                                    listUserChat listUserChat = new listUserChat();
                                    replaceFragment(listUserChat);
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.dismiss();
                                }
                            });
                    // Create the AlertDialog object and return it
                    builder.create();
                    builder.show();

                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void pushFragment(Fragment newFragment, Context context) {
        FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container2, newFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    class deleteBoxChat extends AsyncTask<String, Void, String> {
        String error;

        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://192.168.122.40:3306/BoFDatabase", "bof", "bof");
                String query = "DELETE FROM BoxChats WHERE idBoxChat = ?;";
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
}
