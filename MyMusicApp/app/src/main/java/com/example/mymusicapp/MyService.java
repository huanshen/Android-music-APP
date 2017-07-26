package com.example.mymusicapp;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

public class MyService extends Service {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int currIndex, duration, status , modeIndex = 1, nextIndex = -1, preIndex = -1, pro = -1;
    private String mTitle;

    private static final int updateProgress = 1;
    private static final int updateCurrentMusic = 2;
    private static final int updateDuration = 3;

    List<Musiclist.MusicInfo> itemBeanList = new ArrayList<>();

    MyReceiver activityReceiver;
    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            //Toast.makeText(MyService.this, msg.what+"  handleMessage", Toast.LENGTH_SHORT).show();
            switch(msg.what){
                case updateProgress:
                    if(mediaPlayer != null && (status > 0) ){
                        int progress = mediaPlayer.getCurrentPosition();
                        Intent intent = new Intent(MainActivity.PROGRESS_ACTION);

                        //int currPosition = mediaPlayer.getCurrentPosition();
                        intent.putExtra("progress",progress);
                        //intent.putExtra("currPosition",currPosition);
                        sendBroadcast(intent);
                        handler.sendEmptyMessageDelayed(updateProgress, 1000);
                        //Toast.makeText(MyService.this, progress+"  jindutiao", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    public void onCreate(){
        super.onCreate();

        itemBeanList = Musiclist.instance(getContentResolver()).getMusicList();

        activityReceiver = new MyReceiver();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver监听的Action
        filter.addAction(MainActivity.UPDATE_ACTION);
        // 注册BroadcastReceiver
        registerReceiver(activityReceiver, filter);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() // ①
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                // Toast.makeText(MyService.this, modeIndex+"  setOnCompletionListener", Toast.LENGTH_SHORT).show();
                switch (modeIndex){
                    case 1:
                        currIndex++;
                        if (currIndex > itemBeanList.size()-1)
                        {
                            mediaPlayer.pause();
                        }
                        break;

                    case 2:
                        currIndex++;
                        if (currIndex >= itemBeanList.size()-1)
                        {
                            currIndex = 0;
                        }
                        break;

                    case 3:
                        play(itemBeanList.get(currIndex).url);
                        break;

                    case 4:
                        currIndex = getRandomPosition();
                        play(itemBeanList.get(currIndex).url);
                        break;
                }

                status = 1;
                //发送广播通知Activity更改文本框

                mTitle = itemBeanList.get(currIndex).title;
                int currPosition = mediaPlayer.getCurrentPosition();

                Intent sendIntent = new Intent(MainActivity.PLAYING_ACTION);
                sendIntent.putExtra("current", currIndex);
                sendIntent.putExtra("status", status);
                sendIntent.putExtra("title", mTitle);
                    sendIntent.putExtra("mode", modeIndex);
                // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                sendBroadcast(sendIntent);
                // 准备并播放音乐
                play(itemBeanList.get(currIndex).url);
            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private int getRandomPosition(){
        int random = (int)(Math.random() * (itemBeanList.size() - 1));
        return random;
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
            //准备音频资源
            mediaPlayer.prepare();
            //开始音频
            mediaPlayer.start();
            handler.sendEmptyMessage(updateProgress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent)
        {
           int current = intent.getIntExtra("current", -1);
            status = intent.getIntExtra("status", -1);
            // 处理中间按钮
            if (status < 0){
                mediaPlayer.pause();
                handler.sendEmptyMessage(updateProgress);
            }else if(current == currIndex ) {
                mediaPlayer.start();
                handler.sendEmptyMessage(updateProgress);
                status = 1;
            }else {
                currIndex = current;
                play(itemBeanList.get(currIndex).url);
            }

            // 处理点击下一首的按钮
            nextIndex = intent.getIntExtra("nextIndex", -1);
            if (nextIndex > 0){
                currIndex = currIndex+1;
                if(currIndex  >= itemBeanList.size()){
                    currIndex = 0;
                }
                //
                // play(itemBeanList.get(currIndex).url);
                //currIndex = getRandomPosition();
                play(itemBeanList.get(currIndex).url);
                status = 1;
            }

            // 处理点击上一首的按钮
            preIndex = intent.getIntExtra("preIndex", -1);
            if (preIndex > 0){
                currIndex = currIndex-1;
                if(currIndex  < 0){
                    currIndex = itemBeanList.size() - 1;
                }
                play(itemBeanList.get(currIndex).url);
                status = 1;
            }

            modeIndex = intent.getIntExtra("mode", 1);

            pro = intent.getIntExtra("pro", -1);
            if ( pro > 0){
                mediaPlayer.seekTo(pro);
            }

            Intent intent1 = new Intent(MainActivity.PLAYING_ACTION);
            int currPosition = mediaPlayer.getCurrentPosition();
            intent1.putExtra("status", status);
            intent1.putExtra("current", currIndex);
            intent1.putExtra("currPosition", currPosition);
            intent1.putExtra("mode", modeIndex);
            sendBroadcast(intent1);

        }

        public void changeProgress(int progress){
            if(mediaPlayer != null){
                int currentPosition = progress * 1000;
                if(status > 0){
                    mediaPlayer.seekTo(currentPosition);
                }
            }
        }
    }



}
