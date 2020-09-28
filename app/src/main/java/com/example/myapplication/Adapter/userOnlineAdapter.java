package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;

import java.util.ArrayList;

public class userOnlineAdapter extends RecyclerView.Adapter<userOnlineAdapter.ItemHolder> {
    Context context;
    ArrayList<userChat> arrayList;

    public userOnlineAdapter(Context context, ArrayList<userChat> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_online,null);
        ItemHolder itemHolder = new ItemHolder(v);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        userChat userchat = arrayList.get(position);
//        Picasso.with(context).load(product.getImage())
//                .placeholder(R.drawable.loader)
//                .error(R.drawable.photo)
//                .into(holder.img_product);

    }
    public class ItemHolder extends RecyclerView.ViewHolder{
        public ImageView img_avatar;
        //public TextView txt_userName,txt_lastMessage, txt_time;

        public ItemHolder(View itemView) {
            super(itemView);
            img_avatar= itemView.findViewById(R.id.itemUserChat_avatar);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Chat chat = new Chat();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("userName", arrayList.get(getPosition()).getUserName());
//                    bundle.putString("idBoxChat", arrayList.get(getPosition()).getIdboxchat());
//                    bundle.putString("idUser", arrayList.get(getPosition()).getUserid());
//
//                    chat.setArguments(bundle);
//                    pushFragment(chat,context);
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }


}
