package com.example.mymusicapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenjiaqi on 2017/7/18.
 */

public class LrcAdapter extends BaseAdapter {
    private ArrayList <LrcContent> mList;//数据源
    private LayoutInflater mInflater;//布局装载器对象

    // 通过构造方法将数据源与数据适配器关联起来
    // context:要使用当前的Adapter的界面对象
    public LrcAdapter(Context context, ArrayList list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    //ListView需要显示的数据数量
    public int getCount() {
        return mList.size();
    }

    @Override
    //指定的索引对应的数据项
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    //指定的索引对应的数据项ID
    public long getItemId(int position) {
        return position;
    }

    @Override
    //返回每一项的显示内容
    public View getView(int position, View convertView, ViewGroup parent) {
        //将布局文件转化为View对象
        View view = mInflater.inflate(R.layout.item1,null);

        /**
         * 找到item布局文件中对应的控件
         */

        TextView titleTextView =  view.findViewById(R.id.tv_title);

        //获取相应索引的ItemBean对象
        LrcContent music = mList.get(position);

        /**
         * 设置控件的对应属性值
         */

        titleTextView.setText(music.lrcStr);

        return view;
    }
}
