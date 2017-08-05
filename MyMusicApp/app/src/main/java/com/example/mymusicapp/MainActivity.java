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
import android.support.design.widget.NavigationView;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnCreateContextMenuListener {

    private static final String ACTIVITY_TAG="LogDemo";
    public static final String UPDATE_ACTION = "com.example.mymusicapp.UPDATE_ACTION";
    public static final String PROGRESS_ACTION = "com.example.mymusicapp.PROGRESS_ACTION";
    public static final String PLAYING_ACTION = "com.example.mymusicapp.PLAYING";
    private ListView mListView ;
    private ImageButton  start;
    private SeekBar mSeekBar;
    private TextView tv;
    // 1播放 -1暂停
    private int status = 0;
    private String title;
    private RelativeLayout root;
    private MyAdapter adapter;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Musiclist.MusicInfo newMusic;
    private int currIndex = 0, duration, currPosition = 0, modeIndex = 1, preIndex, nextIndex;
    private MyPlayingReceiver activityReceiver;
    private List<Musiclist.MusicInfo> itemBeanList = new ArrayList<>();
    private Musiclist.MusicInfo musicInfo;
    private MyService.MusicBinder musicBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemBeanList = Musiclist.instance(getContentResolver()).getMusicList();
        connectToNatureService();
        initView();
        registerReceiver();

        //设置ListView的数据适配器
        adapter = new MyAdapter(this,itemBeanList);
        mListView.setAdapter(adapter);
        if (savedInstanceState != null) {
            title = savedInstanceState.getString("title");
            tv.setText(title);
            Toast.makeText(MainActivity.this, title, Toast.LENGTH_SHORT).show();
        }
        // 若需要弹出菜单，这需要设置setOnCreateContextMenuListener
        mListView.setOnCreateContextMenuListener(this);
    }

    @Override
    // 播放  添加到播放列表  搜索
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfoIn) {
        menu.add(0, 1, 0, "sssss");
        AdapterView.AdapterContextMenuInfo menuinfo2 = (AdapterView.AdapterContextMenuInfo) menuInfoIn;
        String title = itemBeanList.get(menuinfo2.position).title;
        menu.setHeaderTitle(title);
        Toast.makeText(MainActivity.this, " 123 "+menuinfo2.position, Toast.LENGTH_SHORT).show();


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:

                Toast.makeText(MainActivity.this, " 123 "+item, Toast.LENGTH_SHORT).show();
                return true;

        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outcicle) {
        // need to store the selected item so we don't lose it in case
        // of an orientation switch. Otherwise we could lose it while
        // in the middle of specifying a playlist to add the item to.
        outcicle.putString("title", title);
        super.onSaveInstanceState(outcicle);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {}

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


    public  void registerReceiver(){
        activityReceiver = new MyPlayingReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PROGRESS_ACTION);
        registerReceiver(activityReceiver, filter);
    }



    public void onDestroy(){
        unregisterReceiver(activityReceiver);
        adapter =null;
        if(musicBinder != null){
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }


    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        menu.add(0, 1, 0, "设置");
        menu.add(0, 0, 0, "退出");

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    // 选项菜单的菜单项被单击后的回调方法
    public boolean onOptionsItemSelected(MenuItem mi)
    {
        switch (mi.getItemId()) {
            case 1:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case 0:
                unregisterReceiver(activityReceiver);
                adapter =null;
                if(musicBinder != null){
                    unbindService(serviceConnection);
                }
                finish();
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.settings:
                Toast.makeText(this, "You clicked Settings", Toast.LENGTH_SHORT).show();
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
        root = (RelativeLayout) findViewById(R.id.root);
        title = itemBeanList.get(currIndex).title;
        tv.setText(title);
        /*drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);*/

        /*ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu);
        }

       navView.setCheckedItem(R.id.nav_call);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawers();
                return true;
            }
        });*/

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFruits();
            }
        });
    }

    private void refreshFruits() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        newMusic = itemBeanList.get(2);
                        itemBeanList.add(newMusic);
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
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


    public class MyPlayingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent)
        {
            String action = intent.getAction();
            if(MainActivity.PROGRESS_ACTION.equals(action)){
                currPosition = intent.getIntExtra("progress", -1);
                int index = intent.getIntExtra("currIndex", -1);
                status = intent.getIntExtra("status", -1);
                if (index >= 0){
                    currIndex = index;
                    tv.setText(itemBeanList.get(currIndex).title);
                }

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

    @Override
    public void onBackPressed() {
        if(musicBinder != null){
            unbindService(serviceConnection);
        }
        finish();

    }

    /*@Override
    public void onPause(){
        super.onPause();
        musicBinder.stopPlay();
        //overridePendingTransition(R.anim.hold, R.anim.push_right_out);
    }

    public void onStop(){
        super.onStop();

    }
    public void onResume(){
        super.onResume();
        registerReceiver();
        if(musicBinder != null){
            if(musicBinder.isPlaying() > 0){
                start.setImageResource(R.drawable.pause);
            }else{
                start.setImageResource(R.drawable.playing);
            }
            // natureBinder.notifyActivity();
        }
    }
*/
}
