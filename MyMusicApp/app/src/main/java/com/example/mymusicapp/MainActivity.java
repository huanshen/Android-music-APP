package com.example.mymusicapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
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

    private SeekBar mSeekBar;

    private TextView tv;
    // 1播放 -1暂停
    private int status = 0;

    private boolean hadDestroy = false;

    private LinearLayout root;

    //播放音频的
    private MediaPlayer mediaPlayer = new MediaPlayer();

    // 表示当前播放的音乐索引, 播放位置
    private int currIndex = 0, duration, currPosition = 0, modeIndex = 1, preIndex, nextIndex;

    MyPlayingReceiver activityReceiver;

    List<Musiclist.MusicInfo> itemBeanList = new ArrayList<>();
    Musiclist.MusicInfo musicInfo;

    private MyService.MusicBinder musicBinder;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MyService.MusicBinder) service;
            setListener();
        }
    };

    private void connectToNatureService(){
        Intent intent = new Intent(MainActivity.this, MyService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemBeanList = Musiclist.instance(getContentResolver()).getMusicList();
        connectToNatureService();
        initView();

        activityReceiver = new MyPlayingReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PROGRESS_ACTION);
        registerReceiver(activityReceiver, filter);

        //设置ListView的数据适配器
        mListView.setAdapter(new MyAdapter(this,itemBeanList));


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

                if( status <=0){
                    // 按下的时候字符串是不变的
                    tv.setText(itemBeanList.get(currIndex).title);
                    start.setImageResource(R.drawable.playing);
                    musicBinder.startPlay(currIndex);
                    status = 1;
                }else{
                    // 按下的时候字符串是不变的
                    status = -1;
                    start.setImageResource(R.drawable.pause);
                    musicBinder.stopPlay();
                }
            }
        });

        // 给 mListView 中 item 创建点击事件, 点击就播放音乐
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currIndex = i;
                musicBinder.startPlay(currIndex);
                status = 1;
                tv.setText(itemBeanList.get(currIndex).title);
                start.setImageResource(R.drawable.playing);

            }
        });


        // 点击歌曲的名字，跳到详情页
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("status",status);
                intent.putExtra("currIndex",currIndex);
                intent.putExtra("progress",currPosition);
                startActivityForResult(intent, 1000);
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
            String action = intent.getAction();
            if(MainActivity.PROGRESS_ACTION.equals(action)){
                currPosition = intent.getIntExtra("progress", -1);
                currIndex = intent.getIntExtra("currIndex", -1);
                status = intent.getIntExtra("status", -1);
                tv.setText(itemBeanList.get(currIndex).title);
                if (status < 0){
                    start.setImageResource(R.drawable.pause);
                }else{
                    start.setImageResource(R.drawable.playing);
                }
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_OK){
            currIndex = data.getIntExtra("currIndex", -1);
            tv.setText(itemBeanList.get(currIndex).title);
            if (status < 0){
                start.setImageResource(R.drawable.pause);
            }else{
                start.setImageResource(R.drawable.playing);
            }
        }
    }
}
