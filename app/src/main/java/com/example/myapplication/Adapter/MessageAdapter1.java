package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
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

import com.example.myapplication.Activities.videoCall_Start;
import com.example.myapplication.Activities.voiceCall_Start;
import com.example.myapplication.Model.Message;
import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;
import com.squareup.picasso.Picasso;

import org.linphone.core.Address;
import org.linphone.core.ProxyConfig;

import java.util.ArrayList;

public class MessageAdapter1 extends RecyclerView.Adapter<MessageAdapter1.ItemHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    Context context;
    ArrayList<Message> arrayList;

    public MessageAdapter1(Context context, ArrayList<Message> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, null);
            ItemHolder itemHolder = new ItemHolder(v);
            return itemHolder;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, null);
            ItemHolder itemHolder = new ItemHolder(v);
            return itemHolder;
        }
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        Message message = arrayList.get(position);
        holder.txt_message.setText(message.getMessage());
        holder.time_message.setText(message.getTime());
        if (message.getAvatar().length() <= 0) {

        } else {
            Picasso.with(context).load(message.getAvatar())
                    .placeholder(R.drawable.loader)
                    .error(R.drawable.broken)
                    .into(holder.img_avatar);
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        public TextView txt_message;
        private ImageView img_avatar;
        public TextView time_message;

        public ItemHolder(View itemView) {
            super(itemView);
            txt_message = itemView.findViewById(R.id.show_message);
            img_avatar = itemView.findViewById(R.id.avatar_chat);
            time_message = itemView.findViewById(R.id.time_message);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ProxyConfig cfg = LinphoneService.getCore().getDefaultProxyConfig();
        Address address = cfg.getContact();
        String name = "sip:" + address.getUsername() + "@bof-ims.dek.vn";
        //System.out.println(name);
        if (arrayList.get(position).getSender().equals(name)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
