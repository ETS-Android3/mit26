package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activities.videoCall_Start;
import com.example.myapplication.Activities.videoCall_Start_waiting;
import com.example.myapplication.Activities.voiceCall_End;
import com.example.myapplication.Activities.voiceCall_Start;
import com.example.myapplication.Model.userChat;
import com.example.myapplication.R;
import com.example.myapplication.Ultil.LinphoneService;
import com.squareup.picasso.Picasso;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.util.ArrayList;

public class userCallAdapter extends RecyclerView.Adapter<userCallAdapter.ItemHolder> {
    Context context;
    ArrayList<userChat> arrayList;

    public userCallAdapter(Context context, ArrayList<userChat> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_call,null);
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
        public ImageView img_avatar;
        public TextView txt_userName;
        public ImageView btn_VoiceCall, btn_VideoCall;

        public ItemHolder(View itemView) {
            super(itemView);
            img_avatar= itemView.findViewById(R.id.itemUserCall_avatar1);
            txt_userName=itemView.findViewById(R.id.itemUserCall_userName);
            btn_VoiceCall= itemView.findViewById(R.id.itemUserCall_VoiceCall);
            btn_VideoCall=itemView.findViewById(R.id.itemUserCall_VideoCall);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
               // Toast.makeText(context,""+arrayList.get(getPosition()).getUserid(),Toast.LENGTH_SHORT).show();

                }
            });
            btn_VoiceCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Core core = LinphoneService.getCore();
                    Address addressToCall = core.interpretUrl(arrayList.get(getPosition()).getUserid());
                    CallParams params = core.createCallParams(null);
                    //addressToCall.setPort(5060);
                    params.enableVideo(false);
                    if (addressToCall != null) {
                        core.clearCallLogs();
                        core.inviteAddressWithParams(addressToCall, params);
                        Intent intent = new Intent(context, voiceCall_Start.class);
                        intent.putExtra("disPlayName",arrayList.get(getPosition()).getUserName());
                        intent.putExtra("avatar",arrayList.get(getPosition()).getAvatar());
                        intent.putExtra("idUser",arrayList.get(getPosition()).getUserid());
                        context.startActivity(intent);
                    }
                }
            });
            btn_VideoCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Core core = LinphoneService.getCore();
                    Address addressToCall = core.interpretUrl(arrayList.get(getPosition()).getUserid());

                    CallParams params = core.createCallParams(null);
                    params.enableVideo(true);

                    params.enableVideo(true);

                    if (addressToCall != null) {

                        core.inviteAddressWithParams(addressToCall, params);
                        Intent intent = new Intent(context, videoCall_Start_waiting.class);
                        intent.putExtra("disPlayName",arrayList.get(getPosition()).getUserName());
                        intent.putExtra("avatar",arrayList.get(getPosition()).getAvatar());
                        intent.putExtra("idUser",arrayList.get(getPosition()).getUserid());
                        context.startActivity(intent);

                    }
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
}
