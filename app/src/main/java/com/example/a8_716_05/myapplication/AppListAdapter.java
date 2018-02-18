package com.example.a8_716_05.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Shin on 2016-12-03.
 */

public class AppListAdapter extends BaseAdapter {
    private static final String TAG = AppListAdapter.class.getSimpleName();
    private DataBaseManager dbManager = null;
    private Cursor cursor = null;
    private Context mContext;
    private ArrayList<AppListItem> mData;
    private int mLayout;
    private LayoutInflater inflater;

    private ImageView imgView;
    private TextView textView;
    private CheckBox chBox;



    public AppListAdapter(Context context, int layout, ArrayList<AppListItem> data) {
        mContext = context;
        mLayout = layout;
        mData = data;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position).getPackName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(mLayout, parent, false);
        }

        imgView = (ImageView)convertView.findViewById(R.id.iconView);
        textView = (TextView)convertView.findViewById(R.id.packnameView);
        imgView.setImageDrawable(mData.get(position).getIcon());
        textView.setText(mData.get(position).getAppName());
        chBox = (CheckBox)convertView.findViewById(R.id.listCheck);

        if(mData.get(position).getEnable()){
            chBox.setChecked(true);
        }else{
            chBox.setChecked(false);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbManager = new DataBaseManager(mContext);
                dbManager.open();
                ContentValues cv = new ContentValues();

                // True -> False
                // 비사용 설정
                if(mData.get(position).getEnable()){
                    cv.put("en",0);
                    mData.get(position).setEnable(false);
                    chBox.setChecked(false);
                }else{  // 사용 설정
                    cv.put("en",1);
                    chBox.setChecked(true);
                    mData.get(position).setEnable(true);
                }
                dbManager.updateRecord(mData.get(position).getPackName(), cv);
                dbManager.close();
                // getView 때문에 다시 불러와야 제대로 체크박스 구성됨.
                notifyDataSetChanged();

            }
        });

        return convertView;
    }



}