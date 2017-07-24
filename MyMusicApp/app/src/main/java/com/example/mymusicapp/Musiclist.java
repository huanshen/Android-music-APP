package com.example.mymusicapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenjiaqi on 2017/7/18.
 */

public class Musiclist {

    private static ContentResolver contentResolver;

    private static Musiclist musiclist;

    List<MusicInfo> itemBeanList = new ArrayList<>();

    public static Musiclist instance(ContentResolver pContentResolver){
        if(musiclist == null){
            contentResolver = pContentResolver;
            musiclist = new Musiclist();
        }
        return musiclist;
    }

    public List<MusicInfo> getMusicList(){
        return itemBeanList;
    }

    class MusicInfo {
        public int id;
        public int itemImageResId;
        public String title;
        public int album;
        public int duration;
        public long size;
        public String artist;
        public String url;

        public MusicInfo(int itemImageResId, String title, int duration, int id, int album, long size, String artist, String url) {
            this.itemImageResId = itemImageResId;
            this.title = title;
            this.duration = duration;
            this.id = id;
            this.album = album;
            this.size = size;
            this.artist = artist;
            this.url = url;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getAlbum() {
            return album;
        }

        public void setAlbum(int album) {
            this.album = album;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }


    private Musiclist (){
        Cursor cursor = null;
        try {
            // 查询联系人数据

            cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
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
                    itemBeanList.add(new MusicInfo(R.mipmap.ic_launcher, displayName, duration, id, album, size, artist, url));
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
}


