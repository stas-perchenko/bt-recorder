package com.alperez.bt_microphone.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.ui.viewmodel.DataTransferViewModel;

import java.util.Stack;

/**
 * Created by stanislav.perchenko on 3/18/2017.
 */

public class DataTransferArrayAdapter extends ArrayAdapter<DataTransferViewModel> {

    private LayoutInflater inflater;

    private Stack<DataTransferViewModel> mItems;

    public DataTransferArrayAdapter(Context context, Stack<DataTransferViewModel> items) {
        super(context, R.layout.data_transfer_list_item, items);
        mItems = items;
        inflater = LayoutInflater.from(context);
    }



    @Override
    public void add(DataTransferViewModel item) {
        mItems.add(0, item);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        if (row == null) {
            row = inflater.inflate(R.layout.data_transfer_list_item, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        DataTransferViewModel item = getItem(position);
        holder.getTextDirecton().setText(item.getTransferDirection());
        holder.getTextTime().setText(item.getTimeText());
        holder.getTextData().setText(item.getData());
        return row;
    }

    private class ViewHolder {
        public TextView textDirecton, textTime, textData;
        public View vBase;

        public ViewHolder(View vBase) {
            this.vBase = vBase;
        }

        public TextView getTextDirecton() {
            if (textDirecton == null) {
                textDirecton = (TextView) vBase.findViewById(R.id.text_direction);
            }
            return textDirecton;
        }

        public TextView getTextTime() {
            if (textTime == null) {
                textTime = (TextView) vBase.findViewById(R.id.text_time);
            }
            return textTime;
        }

        public TextView getTextData() {
            if (textData == null) {
                textData = (TextView) vBase.findViewById(R.id.text_data);
            }
            return textData;
        }
    }
}
