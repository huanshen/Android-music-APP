package com.example.musicapp.mymusic;

import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.media.MediaPlayer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {

    private static final String ACTIVITY_TAG="LogDemo";

    ListView mListView ;
    private Button pre , start, pause, next;
    private SeekBar mSeekBar;
    private TextView textView, textView2, textView3;
    private boolean hadDestroy = false;
    private MediaPlayer mediaPlayer = new MediaPlayer();//播放音频的
    private int time;   // 记录播放位置
    private int currIndex = -1, duration, currposition;// 表示当前播放的音乐索引

    List<Musiclist> itemBeanList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readContacts();

        initView();

        setListener();

        //设置ListView的数据适配器
        mListView.setAdapter(new MyAdapter(this,itemBeanList));
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case 0x01:
                    break;
                default:
                    break;
            }
        };
    };

    Runnable runnable = new Runnable() {

        @Override
        public void run() {

            if (!hadDestroy) {

                mHandler.postDelayed(this, 1000);
                int currentTime = Math.round(mediaPlayer.getCurrentPosition() / 1000);

                String currentStr = String.format("%s%02d:%02d", "当前时间 ",
                        currentTime / 60, currentTime+60 % 60);

                // 不断更新进度条和下面的时间显示
                mSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                int totalTime = Math.round(mediaPlayer.getCurrentPosition() / 1000);
                String str = String.format("%02d:%02d", totalTime / 60, totalTime % 60);
                textView2.setText(str);

                // 判断是否要进行下一首播放
                if (mediaPlayer.getCurrentPosition() +300 >= mediaPlayer.getDuration()){
                    if(currIndex < itemBeanList.size()-1 ){
                        currIndex = currIndex + 1;
                        play(itemBeanList.get(currIndex).url);
                    }else {
                        play(itemBeanList.get(0).url);
                    }
                }
            }
        }
    };

    /**
     * 初始化界面
     */
    private void initView() {

        pre = (Button) findViewById(R.id.pre);
        next = (Button) findViewById(R.id.next);
        pause = (Button) findViewById(R.id.pause);
        start = (Button) findViewById(R.id.start);
        mListView = (ListView) findViewById(R.id.lv_main);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
    }

    /**
     * 设置各类点击事件
     */
    private void setListener() {
        // 暂停按钮
        pause.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                time = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();

                int totalTime = Math.round(mediaPlayer.getCurrentPosition() / 1000);
                String str = String.format("%02d:%02d", totalTime / 60, totalTime % 60);
                textView2.setText(str);
            }
        });

        // 播放按钮
        start.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(currIndex  < 0 ){
                    currIndex = currIndex + 1;
                    play(itemBeanList.get(currIndex+1).url);
                }else{
                    mediaPlayer.start();
                }
            }
        });

        // 下一首按钮
        next.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(currIndex < itemBeanList.size()-1 ){
                    currIndex = currIndex + 1;
                    play(itemBeanList.get(currIndex).url);
                }else {
                    play(itemBeanList.get(0).url);
                }
            }
        });

        // 上一首按钮
        pre.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(currIndex  <= 0 ){
                    play(itemBeanList.get(itemBeanList.size()-1).url);
                }else {
                    currIndex = currIndex - 1;
                    play(itemBeanList.get(currIndex).url);
                }
            }
        });

        // 给 mListView 中 item 创建点击事件, 点击就播放音乐
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //创建一个播放音频的方法，把点击到的地址传过去
                //list.get(i).path这个就是歌曲的地址
                currIndex = i;
                play(itemBeanList.get(currIndex).url);

            }
        });

        // 点击歌曲的名字，跳到详情页
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, detailActivity.class);
                startActivity(intent);
            }

        });

        // 进度条事件
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 拖动条停止拖动的时候调用
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //description.setText("拖动停止");
            }
            /**
             * 拖动条开始拖动的时候调用
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //description.setText("开始拖动");
            }
            /**
             * 拖动条进度改变的时候调用
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

    }


    /**
     * 扫面本地文件夹，获取mp3
     */
    private void readContacts() {
        Cursor cursor = null;
        try {
            // 查询联系人数据
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // 获取每一首歌的基本信息
                    String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    int album = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    itemBeanList.add(new Musiclist(R.mipmap.ic_launcher, displayName, duration, id, album, size, artist, url));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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

    /**
     * 音乐播放
     * @param url
     */
    private void play(String url) {
        //播放之前要先把音频文件重置
            if (url == null ){
                url = itemBeanList.get(currIndex).url;
            }
            mediaPlayer.reset();
            //调用方法传进去要播放的音频路url
        try {
            mediaPlayer.setDataSource(url);
            //异步准备音频资源
            mediaPlayer.prepareAsync();
            //调用mediaPlayer的监听方法，音频准备完毕会响应此方法
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();//开始音频

                    // 新开一个线程，用来改变seekbar
                    mHandler.postDelayed(runnable, 1000);

                    // 因为这里是异步播放，所以需要放在这里设置时间
                    int totalTime = Math.round(mediaPlayer.getDuration() / 1000);
                    String str = String.format("%02d:%02d", totalTime / 60, totalTime % 60);
                    textView.setText(str);
                    textView3.setText(itemBeanList.get(currIndex).title);
                    mSeekBar.setMax(mediaPlayer.getDuration());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}