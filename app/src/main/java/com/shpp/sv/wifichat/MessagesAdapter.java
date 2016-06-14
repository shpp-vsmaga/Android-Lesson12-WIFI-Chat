package com.shpp.sv.wifichat;

import android.content.Context;
import android.util.LayoutDirection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by SV on 09.06.2016.
 */
public class MessagesAdapter extends BaseAdapter {

    private ArrayList<Message> messagesList;
    private LayoutInflater inflater;
    private static final String DATE_FORMAT = "HH:mm";
    private static final int ITEM_TYPE_COUNT = 2;
    private static final int ITEM_OUT = 0;
    private static final int ITEM_IN = 1;


    public MessagesAdapter(Context context, ArrayList<Message> messagesList){
        this.messagesList = messagesList;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messagesList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return messagesList.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_TYPE_COUNT;
    }



    @Override
    public int getItemViewType(int position) {
        int type = messagesList.get(position).isOwn() ? ITEM_OUT : ITEM_IN;

        return type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = messagesList.get(position);
        int itemType = getItemViewType(position);
        if (convertView == null){
            if (itemType == ITEM_OUT) {
                convertView = inflater.inflate(R.layout.msg_list_item_out, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.msg_list_item_in, parent, false);
            }
        }


        TextView tvUsername = (TextView)convertView.findViewById(R.id.tvUserName);
        tvUsername.setText(message.getAuthor().getName());
        tvUsername.setTextColor(message.getAuthor().getColor());

        ((TextView)convertView.findViewById(R.id.tvMessageText)).setText(message.getText());

        ((TextView)convertView.findViewById(R.id.tvTime)).setText(convertTimeLongToString(message.getTime()));

        return convertView;
    }

    public static String convertTimeLongToString(long milliSeconds){
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
