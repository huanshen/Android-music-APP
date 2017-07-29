package com.example.mymusicapp;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

public class MyService extends Service {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int currIndex, duration = 0, status , modeIndex = 1, progress = 0;
    private String mTitle;

    private static final int updateProgress = 1;
    private static final int updateOnceMusic = 2;
    private static final int updateDuration = 3;
    private Binder musicBinder = new MusicBinder();
    private static ContentResolver contentResolver;
    private Cursor cursor = null;
    private int oncePlay = -1;

    List<Musiclist.MusicInfo> itemBeanList = new ArrayList<>();

    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            //Toast.makeText(MyService.this, msg.what+"  handleMessage", Toast.LENGTH_SHORT).show();
            switch(msg.what){
                case updateProgress:
                    if(mediaPlayer != null  ){
                        progress = mediaPlayer.getCurrentPosition();
                        Intent intent = new Intent(MainActivity.PROGRESS_ACTION);

                        //int currPosition = mediaPlayer.getCurrentPosition();
                        intent.putExtra("progress",progress);
                        intent.putExtra("currIndex",currIndex);
                        intent.putExtra("status",status);
                        intent.putExtra("duration",duration);
                        //intent.putExtra("currPosition",currPosition);
                        sendBroadcast(intent);
                        handler.sendEmptyMessageDelayed(updateProgress, 1000);
                        //Toast.makeText(MyService.this, progress+"  jindutiao", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case updateOnceMusic:
                    if(mediaPlayer != null  ){
                        progress = mediaPlayer.getCurrentPosition();
                        Intent intent = new Intent(MainActivity.PROGRESS_ACTION);

                        //int currPosition = mediaPlayer.getCurrentPosition();
                        intent.putExtra("progress",progress);
                        intent.putExtra("currIndex",-1);
                        intent.putExtra("status",status);
                        intent.putExtra("duration",duration);
                        //intent.putExtra("currPosition",currPosition);
                        sendBroadcast(intent);
                        handler.sendEmptyMessageDelayed(updateOnceMusic, 1000);
                        //Toast.makeText(MyService.this, progress+"  jindutiao", Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
        }
    };


    public void onCreate(){
        super.onCreate();

        itemBeanList = Musiclist.instance(getContentResolver()).getMusicList();



        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() // ①
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                // Toast.makeText(MyService.this, modeIndex+"  setOnCompletionListener", Toast.LENGTH_SHORT).show();
                if (oncePlay > 0){
                    mediaPlayer.stop();
                    oncePlay = -1;
                }else {
                    switch (modeIndex) {
                        case 1:
                            currIndex++;
                            if (currIndex > itemBeanList.size() - 1) {
                                mediaPlayer.pause();
                                status = -1;
                            }else {
                                play(itemBeanList.get(currIndex).url);
                            }
                            break;

                        case 2:
                            currIndex++;
                            if (currIndex >= itemBeanList.size() - 1) {
                                currIndex = 0;
                            }
                            break;

                        case 3:
                            play(itemBeanList.get(currIndex).url);
                            status = 1;
                            break;

                        case 4:
                            currIndex = getRandomPosition();
                            play(itemBeanList.get(currIndex).url);
                            status = 1;
                            break;
                    }
                }


            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return musicBinder;
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


    class MusicBinder extends Binder {

        public void startPlay(int index){
            currIndex = index;
            if (status == 0){
                play(itemBeanList.get(currIndex).url);
            }
            if (status == -1){
                mediaPlayer.start();
            }
            if(status > 0){
                play(itemBeanList.get(currIndex).url);
            }
            status = 1;
        }

        public void stopPlay(){
            mediaPlayer.pause();
            status = -1;
        }

        public void play1(Uri url){
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(MyService.this,url);
                mediaPlayer.prepare();
                mediaPlayer.start();
                oncePlay = 1;
                duration = mediaPlayer.getDuration();
                handler.sendEmptyMessage(updateOnceMusic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int toNext(){
            if (modeIndex == 4){
                currIndex = getRandomPosition();
                play(itemBeanList.get(currIndex).url);
            }

            if (modeIndex == 1 || modeIndex == 2 || modeIndex == 3){
                currIndex = currIndex+1;
                if(currIndex  >= itemBeanList.size()){
                    currIndex = 0;
                }
                play(itemBeanList.get(currIndex).url);
            }
            status = 1;
            return currIndex;
        }

        public int toPrevious(){
            if (modeIndex == 4){
                currIndex = getRandomPosition();
                play(itemBeanList.get(currIndex).url);
            }

            if (modeIndex == 1 || modeIndex == 2 || modeIndex == 3){
                currIndex = currIndex-1;
                if(currIndex  < 0){
                    currIndex = itemBeanList.size() - 1;
                }
                play(itemBeanList.get(currIndex).url);
                status = 1;
            }
            status = 1;
            return currIndex;
        }

        public  int isPlaying(){
            return status;
        }

        public void changeMode(int mode){
            modeIndex = mode;
        }


        public void changeProgress(int progress){

            int currPosition = progress * 1000;
            mediaPlayer.seekTo(currPosition);
        }
    }



}
