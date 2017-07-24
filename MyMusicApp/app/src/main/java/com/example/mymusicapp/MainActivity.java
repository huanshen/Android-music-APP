package com.example.mymusicapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String ACTIVITY_TAG="LogDemo";

    public static final String UPDATE_ACTION = "com.example.mymusicapp.UPDATE_ACTION";
    public static final String PROGRESS_ACTION = "com.example.mymusicapp.PROGRESS_ACTION";
    public static final String PLAYING_ACTION = "com.example.mymusicapp.PLAYING";

    ListView mListView ;

    private ImageButton  start;

    Intent intentService;

    private SeekBar mSeekBar;

    private TextView tv;
    // 1播放 -1暂停
    private int status = -1;

    private boolean hadDestroy = false;

    private LinearLayout root;

    //播放音频的
    private MediaPlayer mediaPlayer = new MediaPlayer();

    // 表示当前播放的音乐索引, 播放位置
    private int currIndex = 0, duration, currposition, modeIndex = 1, preIndex, nextIndex;

    MyPlayingReceiver activityReceiver;
    MyUpdateReceiver myUpdateReceiver;

    List<Musiclist.MusicInfo> itemBeanList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemBeanList = Musiclist.instance(getContentResolver()).getMusicList();

        initView();
        setListener();

        activityReceiver = new MyPlayingReceiver();

        // 用于当歌曲完成的时候，更新标题
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver监听的Action
        filter.addAction(PLAYING_ACTION);
        // 注册BroadcastReceiver
        registerReceiver(activityReceiver, filter);

        // 用于更新操作，当在详情页操作时候，同时更新本地页面
        myUpdateReceiver = new MyUpdateReceiver();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(UPDATE_ACTION);
        registerReceiver(myUpdateReceiver, filter);


        //设置ListView的数据适配器
        mListView.setAdapter(new MyAdapter(this,itemBeanList));

        Intent intentService = new Intent(this, MyService.class);
        // 启动后台Service
        startService(intentService);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {

        menu.add(0, 1, 0, "设置");
        menu.add(0, 0, 0, "退出");

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    // 选项菜单的菜单项被单击后的回调方法
    public boolean onOptionsItemSelected(MenuItem mi)
    {
        //判断单击的是哪个菜单项，并有针对性地作出响应
        switch (mi.getItemId())
        {
            case 1:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);

                break;
            case 0:
                finish();
                //Intent intent1 = new Intent(this, MyService.class);
                stopService(intentService);
                break;
        }
        return true;
    }


    /**
     * 初始化界面
     */
    private void initView() {

        start = (ImageButton) findViewById(R.id.start);
        tv = (TextView) findViewById(R.id.tv);
        mListView = (ListView) findViewById(R.id.lv_main);
        root = (LinearLayout) findViewById(R.id.root);
        tv.setText(itemBeanList.get(currIndex).title);
    }

    /**
     * 设置各类点击事件
     */
    private void setListener() {
        // 播放按钮
        start.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if( status < 0){
                    // 按下的时候字符串是不变的
                    status = 1;
                    tv.setText(itemBeanList.get(currIndex).title);
                    start.setImageResource(R.drawable.playing);
                    Intent intent = new Intent("com.example.mymusicapp.UPDATE_ACTION");
                    intent.putExtra("status", status);
                    intent.putExtra("current", currIndex);
                    intent.putExtra("mode", modeIndex);
                    sendBroadcast(intent);
                }else{
                    // 按下的时候字符串是不变的
                    status = -1;
                    start.setImageResource(R.drawable.pause);
                    Intent intent = new Intent("com.example.mymusicapp.UPDATE_ACTION");
                    intent.putExtra("status", status);
                    intent.putExtra("current", currIndex);
                    intent.putExtra("mode", modeIndex);
                    sendBroadcast(intent);
                }
                //Toast.makeText(MainActivity.this, status +"  setOnClickListener", Toast.LENGTH_SHORT).show();
            }
        });

        // 给 mListView 中 item 创建点击事件, 点击就播放音乐
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                status = 1;
                currIndex = i;
                duration = itemBeanList.get(currIndex).duration;
                tv.setText(itemBeanList.get(currIndex).title);
                start.setImageResource(R.drawable.playing);
                // Toast.makeText(MainActivity.this, currIndex+"播放", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent("com.example.mymusicapp.UPDATE_ACTION");
                intent.putExtra("current", currIndex);
                intent.putExtra("status", status);
                intent.putExtra("mode", modeIndex);
                intent.putExtra("nextIndex", -1);
                intent.putExtra("preIndex", -1);
                sendBroadcast(intent);
            }
        });


        // 点击歌曲的名字，跳到详情页
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("status",status);
                intent.putExtra("current",currIndex);
                intent.putExtra("nextIndex", -1);
                intent.putExtra("preIndex", -1);
                intent.putExtra("mode", modeIndex);
                startActivityForResult(intent, 1000);

                Intent intent1 = new Intent("com.example.mymusicapp.UPDATE_ACTION");
                intent1.putExtra("current", currIndex);
                intent1.putExtra("status", status);
                intent1.putExtra("mode", modeIndex);
                intent1.putExtra("nextIndex", -1);
                intent1.putExtra("preIndex", -1);
                sendBroadcast(intent1);
                // Toast.makeText(MainActivity.this, modeIndex +"item", Toast.LENGTH_SHORT).show();

            }

        });
    }

    /**
     * Activity被销毁
     */
    @Override
    protected void onDestroy() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();//停止音频的播放
        }
        mediaPlayer.release();//释放资源
        super.onDestroy();
    }

    public class MyPlayingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent)
        {
            currIndex = intent.getIntExtra("current", -1);
            status = intent.getIntExtra("status", -1);
            tv.setText(itemBeanList.get(currIndex).title);
            //Toast.makeText(MainActivity.this, status + "  MyPlayingReceiver", Toast.LENGTH_SHORT).show();
            modeIndex = intent.getIntExtra("mode", modeIndex);
        }
    }

    public class MyUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent)
        {
            status = intent.getIntExtra("status", -1);
            currIndex = intent.getIntExtra("current", -1);
            tv.setText(itemBeanList.get(currIndex).title);

            status = intent.getIntExtra("status", -1);
            // 处理中间按钮
            if (status < 0){
                start.setImageResource(R.drawable.pause);
            }else{
                start.setImageResource(R.drawable.playing);
                //Toast.makeText(MainActivity.this, status +"   onReceive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_OK){
            currIndex = data.getIntExtra("current", -1);
            status = data.getIntExtra("status", -1);
            modeIndex = data.getIntExtra("mode", -1);

            tv.setText(itemBeanList.get(currIndex).title);
            if (status < 0){
                start.setImageResource(R.drawable.pause);
            }else{
                start.setImageResource(R.drawable.playing);
                //Toast.makeText(MainActivity.this, status +"   onActivityResult", Toast.LENGTH_SHORT).show();
            }
        }
    }
}