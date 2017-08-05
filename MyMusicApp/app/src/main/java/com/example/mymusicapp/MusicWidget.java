package com.example.mymusicapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by shenjiaqi on 2017/8/4.
 */

public class MusicWidget extends AppWidgetProvider {
    private static MusicWidget sInstance;

    // 单例模式，当你点击一个播放时所有的AppWidget都播放
    static synchronized MusicWidget getInstance() {
        if (sInstance == null) {
            sInstance = new MusicWidget();
        }
        return sInstance;
    }

    @Override
    // 每次在创建AppWiget的时候或者到了更新频率的时候执行此函数
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 设置AppWidget的一些默认设置，并且传递一些相关参数
        defaultAppWidget(context, appWidgetManager, appWidgetIds);

        // Send broadcast intent to any running MediaPlaybackService so it can
        // wrap around with an immediate update.
        // 发送广播消息通知正在运行的MediaPlaybackService以响应AppWidget
       // Intent updateIntent = new Intent(MusicWidget.SERVICECMD);
        // 发送广播
      //  context.sendBroadcast(updateIntent);
    }

    private void defaultAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final Resources res = context.getResources();
        // Create a new RemoteViews object that will display the views contained in the specified layout file.
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
        Intent intent = new Intent();
        intent.setClass(context, MusicWidget.class); //通过intent把广播发给TestWidget本身，TestWidget接受到广播之后，会调用。。进而刷新借鉴       // 。
        intent.setAction("widget");
        //views.setViewVisibility(R.id.title, View.GONE);
        views.setTextViewText(R.id.name, "widget");
        views.setTextViewText(R.id.artist, "artist");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.next, pendingIntent);//控件btn_widget的点击事件：点击按钮时，会发一个带action的广播。

        appWidgetManager.updateAppWidget(appWidgetIds, views); //点击完了之后，记得更新一下。

        views.setOnClickPendingIntent(R.id.pause, pendingIntent);//控件btn_widget的点击事件：点击按钮时，会发一个带action的广播。

        //appWidgetManager.updateAppWidget(appWidgetIds, views);
        // 开始创建Widget的时候默认是不播放音乐的
        //linkButtons(context, views, false /* not playing */);
        //pushUpdate(context, appWidgetIds, views);
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent != null && TextUtils.equals(intent.getAction(), "widget")) { //当intent不为空，且action匹配成功时，就接收广播，然后点击事件成功
            Log.i("widget", "is clicked");
            //接下来开始做点击事件里面的内容
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);//注意：需要【重新】构造一个RemoteViews
            remoteViews.setTextViewText(R.id.name, "be clicked");
            remoteViews.setTextColor(R.id.name, Color.RED);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);// 单例模式
            ComponentName componentName = new ComponentName(context, MusicWidget.class);
            appWidgetManager.updateAppWidget(componentName, remoteViews);//setText之后，记得更新一下
        }
    }
}
