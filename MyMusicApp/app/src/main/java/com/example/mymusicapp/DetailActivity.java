package com.example.mymusicapp;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.example.mymusicapp.MyService.MusicBinder;

public class DetailActivity extends AppCompatActivity {

    private TextView tv_title, tv_artist, tv_currPosition, tv_duration;
    private ListView tv_lrc;
    private ImageButton pause, pre, next, mode;
    private SeekBar mSeekBar;
    private Uri mUri;
    // modeIndex 1 seq , 2 loop ,3 rep, 4 random
    private int status = 0, currIndex, i = 0, modeIndex, nextIndex = -1, preIndex = -1, duration = 0, currPosition;
    MyPlayingReceiver myPlayingReceiver;
    private String title, artist;
    List<Musiclist.MusicInfo> itemBeanList = new ArrayList<>();
    private Musiclist.MusicInfo musicInfo;
    private MusicBinder musicBinder;
    private MediaPlayer mediaPlayer1 = new MediaPlayer();
    private static ContentResolver contentResolver;
    private Cursor cursor = null;
    private ArrayList<LrcContent> list = new ArrayList<>();
    private LrcAdapter lrcAdapter;
    private ImageView mImage;


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {}

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicBinder) service;
            if (mUri != null){
                Uri url=Uri.parse((mUri+"").replace("file://",""));
                musicBinder.play1(url);
            }
        }
    };

    private void connectToNatureService(){
        Intent intent = new Intent(DetailActivity.this, MyService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }


    private void readLrc (){
        LrcProcess mLrcProcess = new LrcProcess();
        InputStream is = getResources().openRawResource(R.raw.yellow);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        list = mLrcProcess.readLRC(br);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView();

        itemBeanList = Musiclist.instance(getContentResolver()).getMusicList();

        setListener();

        initReceiver();
        connectToNatureService();
        getIntentData ();
        readLrc ();

        lrcAdapter = new LrcAdapter(this, list);
        tv_lrc.setAdapter(lrcAdapter);


        AsyncQueryHandler mAsyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                if (cursor != null && cursor.moveToFirst()) {

                    title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000;
                    artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    /*String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    int album = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));*/

                    tv_title.setText(title);
                    tv_artist.setText(artist);
                    mSeekBar.setMax(duration );
                    tv_duration.setText(timeFormat(duration));
                }

                if (cursor != null) {
                    cursor.close();
                }
                //setNames();
            }
        };

        try {
            String path = mUri.getPath();
            if (path != null ) {
                mAsyncQueryHandler.startQuery(0, null, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DURATION,
                                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM},
                        MediaStore.Audio.Media.DATA + "=?", new String[]{path}, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


/*    public void onResume(){
        super.onResume();
        initReceiver();
        if(musicBinder != null){
            if(musicBinder.isPlaying() > 0){
                pause.setImageResource(R.drawable.pause);
            }else{
                pause.setImageResource(R.drawable.playing);
            }
            // natureBinder.notifyActivity();
        }
    }

    public void onPause(){
        super.onPause();
    }

    public void onStop(){
        super.onStop();
    }

    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(myPlayingReceiver);
        if(musicBinder != null){
            unbindService(serviceConnection);
        }
    }*/


    private  void getIntentData (){
        //getIntent将该项目中包含的原始intent检索出来，将检索出来的intent赋值给一个Intent类型的变量intent
        Intent intent=getIntent();
        //getExtras()得到intent所附带的额外数据

        mUri = intent.getData();
        //setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //
        // mediaPlayer.start();
        if (mUri == null) {

            Bundle bundle = intent.getExtras();

            modeIndex = bundle.getInt("mode");
            currIndex = bundle.getInt("currIndex");
            status = bundle.getInt("status");
            currPosition = bundle.getInt("progress");
            musicInfo = itemBeanList.get(currIndex);
            tv_title.setText(musicInfo.title);
            tv_artist.setText(musicInfo.artist);
            duration = musicInfo.getDuration() / 1000;
            mSeekBar.setMax(duration);

            currPosition = currPosition / 1000; // Remember the current position

            tv_currPosition.setText(timeFormat(currPosition));
            mSeekBar.setProgress(currPosition);
            tv_duration.setText(timeFormat(duration));
            setMode(modeIndex);
        }


        if ( status  > 0){
            pause.setImageResource(R.drawable.playing);
        }else{
            pause.setImageResource(R.drawable.pause);
        }
        int id = musicInfo.id;
        int albumid = musicInfo.album_id;
        Bitmap bm = MusicUtils.getArtworkFromFile(this, id, albumid);

        if(bm != null){
            mImage.setImageBitmap(bm);
        }
        // Log.i("11111111111",bm +"");

        //time
        //Toast.makeText(DetailActivity.this, intent.getExtras()+" aa", Toast.LENGTH_SHORT).show();
    }

    public String timeFormat(int currPosition){
        return String.format("%02d:%02d", currPosition / 60, currPosition % 60);
    }
    public void initView(){
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_artist = (TextView) findViewById(R.id.tv_artist);
        tv_lrc = (ListView) findViewById(R.id.tv_lrc);
        pause = (ImageButton) findViewById(R.id.pause);
        mode = (ImageButton) findViewById(R.id.mode);
        next = (ImageButton) findViewById(R.id.next);
        pre = (ImageButton) findViewById(R.id.pre);
        mImage = (ImageView) findViewById(R.id.mImage);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        tv_currPosition = (TextView) findViewById(R.id.tv_currPosition);
        tv_duration = (TextView) findViewById(R.id.tv_duration);
    }

    private void initReceiver(){
        myPlayingReceiver = new MyPlayingReceiver();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(MainActivity.PLAYING_ACTION);
        filter1.addAction(MainActivity.PROGRESS_ACTION);
        registerReceiver(myPlayingReceiver, filter1);
    }

    /**
     * 设置各类点击事件
     */
    private void setListener() {

        // 暂停按钮
        pause.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (status <= 0){
                    pause.setImageResource(R.drawable.playing);
                    musicBinder.startPlay(currIndex);
                    status = 1;
                }else{
                    status = -1;
                    pause.setImageResource(R.drawable.pause);
                    musicBinder.stopPlay();
                }
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
                musicBinder.changeMode(modeIndex);
            }
        });

        // 下一首按钮
        next.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                currIndex = musicBinder.toNext();
                status = 1;
                pause.setImageResource(R.drawable.playing);
            }
        });

        // 上一首按钮
        pre.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                currIndex = musicBinder.toPrevious();
                pause.setImageResource(R.drawable.playing);
                status =1 ;
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImage.setVisibility(View.INVISIBLE);
            }
        });

        tv_lrc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mImage.setVisibility(View.VISIBLE);
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
                if(fromUser){
                    musicBinder.changeProgress(progress);
                }

            }
        });

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
                int index = intent.getIntExtra("currIndex", -1);

                if ( index >= 0){

                    if (list.get(i+3).lrcTime <= progress  ){
                        i = i + 1;
                        tv_lrc.setSelection(i);
                        if(i >= list.size()-6){
                            i = i - 1;
                        }
                    }
                   Log.i("111111111", progress+"     "  +list.get(i+5).lrcTime);
                    // Toast.makeText(DetailActivity.this, list.get(i).lrcTime + "   "+ progress, Toast.LENGTH_SHORT).show();
                    // Toast.makeText(DetailActivity.this, list.get(i).lrcTime + "   "+ progress, Toast.LENGTH_SHORT).show();
                    currIndex = index;
                    title = itemBeanList.get(currIndex).title;
                    artist = itemBeanList.get(currIndex).artist;
                    tv_title.setText(title);
                    tv_artist.setText(artist);
                    duration = itemBeanList.get(currIndex).getDuration() / 1000;
                    mSeekBar.setMax(duration);
                    tv_duration.setText(timeFormat(duration));
                }else {
                    duration = intent.getIntExtra("duration", -1) / 1000;
                    mSeekBar.setMax(duration);
                    tv_duration.setText(timeFormat(duration));
                    pause.setImageResource(R.drawable.playing);
                }
                musicInfo = itemBeanList.get(currIndex);
                int id = musicInfo.id;
                int albumid = musicInfo.album_id;
                Bitmap bm = MusicUtils.getArtwork(DetailActivity.this, id, albumid, true);

                if(bm != null){
                    mImage.setImageBitmap(bm);
                }
                Log.i("11111111111",bm +"");


                currPosition = progress / 1000;
                mSeekBar.setProgress(currPosition);
                tv_currPosition.setText(timeFormat(currPosition));



            }else {

                status = intent.getIntExtra("status", -1);
                currIndex = intent.getIntExtra("currIndex", -1);
                modeIndex = intent.getIntExtra("mode", modeIndex);
                if (status > 0) {
                    pause.setImageResource(R.drawable.playing);
                } else {
                    pause.setImageResource(R.drawable.pause);
                }

                duration = itemBeanList.get(currIndex).getDuration() / 1000;
                mSeekBar.setMax(duration);
                tv_duration.setText(timeFormat(duration));

                title = itemBeanList.get(currIndex).title;
                artist = itemBeanList.get(currIndex).artist;
                tv_title.setText(title);
                tv_artist.setText(artist);
            }

        }
    }


    @Override
    public void onBackPressed() {
        // Toast.makeText(DetailActivity.this, currIndex+" pressed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("currIndex", currIndex);
        setResult(RESULT_OK, intent);
        finish();
    }
}
