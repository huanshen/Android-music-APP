package com.example.mymusicapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private TextView tv_title, tv_artist, tv_currPosition, tv_duration;
    private ImageButton pause, pre, next, mode;
    private SeekBar mSeekBar;
    // modeIndex 1 seq , 2 loop ,3 rep, 4 random
    private int status, currIndex, modeIndex, nextIndex = -1, preIndex = -1, duration = 0, currPosition;
    MyPlayingReceiver myPlayingReceiver;
    private String title;
    List<Musiclist.MusicInfo> itemBeanList = new ArrayList<>();
    Musiclist.MusicInfo musicInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView();

        itemBeanList = Musiclist.instance(getContentResolver()).getMusicList();

        myPlayingReceiver = new MyPlayingReceiver();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(MainActivity.PLAYING_ACTION);
        filter1.addAction(MainActivity.PROGRESS_ACTION);
        registerReceiver(myPlayingReceiver, filter1);

        //getIntent将该项目中包含的原始intent检索出来，将检索出来的intent赋值给一个Intent类型的变量intent
        Intent intent=getIntent();
        //getExtras()得到intent所附带的额外数据
        Bundle bundle=intent.getExtras();
        //getString()返回指定key的值
        modeIndex = bundle.getInt("mode");
        currIndex = bundle.getInt("current");
        status = bundle.getInt("status");
        currPosition = bundle.getInt("currPosition");

        musicInfo = itemBeanList.get(currIndex);

        tv_title.setText(musicInfo.title);
        tv_artist.setText(musicInfo.artist);

        duration = musicInfo.getDuration() / 1000;
        mSeekBar.setMax(duration);
        String str = String.format("%02d:%02d", duration / 60, duration % 60);

        currPosition = currPosition / 1000; // Remember the current position
        String str1 = String.format("%02d:%02d", currPosition / 60, currPosition % 60);

        tv_currPosition.setText(str1);
        mSeekBar.setProgress(currPosition);
        tv_duration.setText(str);
        setMode(modeIndex);

        if ( status  > 0){
            pause.setImageResource(R.drawable.playing);

        }else{
            pause.setImageResource(R.drawable.pause);
        }
        setListener();
    }

    public void initView(){
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_artist = (TextView) findViewById(R.id.tv_artist);
        pause = (ImageButton) findViewById(R.id.pause);
        mode = (ImageButton) findViewById(R.id.mode);
        next = (ImageButton) findViewById(R.id.next);
        pre = (ImageButton) findViewById(R.id.pre);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        tv_currPosition = (TextView) findViewById(R.id.tv_currPosition);
        tv_duration = (TextView) findViewById(R.id.tv_duration);
    }

    /**
     * 设置各类点击事件
     */
    private void setListener() {

        // 暂停按钮
        pause.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (status < 0){
                    pause.setImageResource(R.drawable.playing);
                    status = 1;
                }else{
                    status = -1;
                    pause.setImageResource(R.drawable.pause);
                }
                sendUpdateBroadcast(status, currIndex, modeIndex, -1, -1);
            }
        });

        // 播放按钮
        mode.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 每次点击之后加1,到4之后回退到1；
                modeIndex = modeIndex + 1;
                if(modeIndex > 4){
                    modeIndex = 1;
                }

                setMode(modeIndex);
                sendUpdateBroadcast(status, currIndex, modeIndex, -1, -1);
            }
        });

        // 下一首按钮
        next.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendUpdateBroadcast(status, currIndex, modeIndex, 1, -1);
            }
        });

        // 上一首按钮
        pre.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendUpdateBroadcast(status, currIndex, modeIndex, -1, 1);
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
                int pro = progress * 1000;

               /* Intent intent = new Intent(MainActivity.UPDATE_ACTION);
                intent.putExtra("status", status);
                intent.putExtra("current", currIndex);
                intent.putExtra("nextIndex", -1);
                intent.putExtra("preIndex", -1);
                intent.putExtra("mode", modeIndex);
                intent.putExtra("pro", pro);
                sendBroadcast(intent);*/
               Toast.makeText(DetailActivity.this, pro+" proDong le", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void  sendUpdateBroadcast(int status, int currIndex, int modeIndex, int nextIndex, int preIndex){
        Intent intent = new Intent(MainActivity.UPDATE_ACTION);
        intent.putExtra("current", currIndex);
        intent.putExtra("status", status);
        intent.putExtra("mode", modeIndex);
        intent.putExtra("nextIndex", nextIndex);
        intent.putExtra("preIndex", preIndex);
        sendBroadcast(intent);
    }

    private void setMode(int modeIndex){
        switch (modeIndex){
            case 1:
                mode.setImageResource(R.drawable.seq);
                break;
            case 2:
                mode.setImageResource(R.drawable.loop);
                break;
            case 3:
                mode.setImageResource(R.drawable.rep);
                break;
            case 4:
                mode.setImageResource(R.drawable.random);
                break;
        }
    }


    public class MyPlayingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent)
        {
            String action = intent.getAction();
            //Toast.makeText(DetailActivity.this, action, Toast.LENGTH_SHORT).show();
            if(MainActivity.PROGRESS_ACTION.equals(action)){
                int progress = intent.getIntExtra("progress", -1);
                if(progress > 0){
                    currPosition = progress / 1000; // Remember the current position
                    String str = String.format("%02d:%02d", currPosition / 60, currPosition % 60);

                    mSeekBar.setProgress(currPosition);
                    tv_currPosition.setText(str);
                    Toast.makeText(DetailActivity.this, str, Toast.LENGTH_SHORT).show();
                }
            }else {

                status = intent.getIntExtra("status", -1);
                currIndex = intent.getIntExtra("current", -1);
                modeIndex = intent.getIntExtra("mode", modeIndex);
                if (status > 0) {
                    pause.setImageResource(R.drawable.playing);
                } else {
                    pause.setImageResource(R.drawable.pause);
                }

                duration = itemBeanList.get(currIndex).getDuration() / 1000;
                String str = String.format("%02d:%02d", duration / 60, duration % 60);

                mSeekBar.setMax(duration);
                tv_duration.setText(str);
                tv_title.setText(itemBeanList.get(currIndex).title);
                tv_artist.setText(itemBeanList.get(currIndex).artist);
            }

        }
    }


    @Override
    public void onBackPressed() {
        // Toast.makeText(DetailActivity.this, currIndex+" pressed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("status", status);
        intent.putExtra("current", currIndex);
        intent.putExtra("nextIndex", -1);
        intent.putExtra("preIndex", -1);
        intent.putExtra("mode", modeIndex);
        setResult(RESULT_OK, intent);
        finish();
    }
}
