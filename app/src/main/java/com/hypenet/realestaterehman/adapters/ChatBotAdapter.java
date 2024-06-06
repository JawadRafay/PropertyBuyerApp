package com.hypenet.realestaterehman.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.model.Chat;
import com.hypenet.realestaterehman.model.ChatModels;
import com.hypenet.realestaterehman.utils.PrefManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatBotAdapter extends RecyclerView.Adapter<ChatBotAdapter.ViewHolder> {

    private static final String TAG = "ChatListAdapter";
    private List<Chat> mData;
    private LayoutInflater mInflater;
    private ClipboardManager clipboard;
    private Context context;
    SimpleDateFormat simpleDateFormat;
    PrefManager prefManager;
    Integer today_day = 0;

    public ChatBotAdapter(Context context, List<Chat> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.context = context;
        simpleDateFormat = new SimpleDateFormat("hh:mm a");
        prefManager = new PrefManager(context);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        today_day = cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Chat chat = mData.get(position);

        holder.tvMsgMe.setText(chat.getContent());
        holder.tvMsg.setText(chat.getContent());

        if(chat.getRole().equals("user")){
            holder.tvMsg.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);
            holder.tvMsgMe.setVisibility(View.VISIBLE);
            holder.tvTimeMe.setVisibility(View.GONE);
        }else{
            holder.tvMsgMe.setVisibility(View.GONE);
            holder.tvTimeMe.setVisibility(View.GONE);
            holder.tvMsg.setVisibility(View.VISIBLE);
            holder.tvTime.setVisibility(View.GONE);
        }

        holder.tvMsgMe.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clip = ClipData.newPlainText("message", chat.getContent());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "text copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        holder.tvMsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clip = ClipData.newPlainText("message", chat.getContent());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "text copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    public void notify(int position){
        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMsg, tvTime,tvMsgMe, tvTimeMe, datetxt;

        ViewHolder(View itemView) {
            super(itemView);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMsgMe = itemView.findViewById(R.id.tvMsgMe);
            tvTimeMe = itemView.findViewById(R.id.tvTimeMe);
            datetxt = itemView.findViewById(R.id.datetxt);
        }
    }
}
