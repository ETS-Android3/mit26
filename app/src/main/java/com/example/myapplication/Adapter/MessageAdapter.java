package com.example.myapplication.Adapter;

import android.content.Context;
import android.os.Bundle;
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


import com.example.myapplication.Model.Message;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;

    private Context mcontext;
    private ArrayList<Message> mchatList;


    public MessageAdapter(Context mcontext, ArrayList<Message> mchatList) {
        this.mcontext = mcontext;
        this.mchatList = mchatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mcontext).inflate(R.layout.item_message_right,parent,false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(mcontext).inflate(R.layout.item_message_left,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = mchatList.get(position);
        holder.show_message.setText(""+message.getMessage());
        //holder.profile_image.setImageResource(R.drawable.ic_send_black_24dp);

        //if(imageurl.equals("default")){
        holder.profile_image.setImageResource(R.mipmap.ic_launcher);
//        }else {
//            Glide.with(mcontext).load(imageurl).into(holder.profile_image);
//        }
    }
    @Override
    public int getItemCount() {
        return mchatList.size();
    }

    public  class  ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            //profile_image = itemView.findViewById(R.id.profile_img);
        }
    }

//    public void pushFragment(Fragment newFragment, Context context){
//        FragmentTransaction transaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.full_screen, newFragment);
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        //transaction.addToBackStack(null);
//        transaction.commit();
//    }

    @Override
    public int getItemViewType(int position) {
        //Toast.makeText(mcontext,""+mchatList.get(position).getSender(),Toast.LENGTH_SHORT).show();
        //System.out.println(mchatList.get(position).getSender());
        if(mchatList.get(position).getSender().equals("User_002")){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }
}

