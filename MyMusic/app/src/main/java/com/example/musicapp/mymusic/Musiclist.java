package com.example.musicapp.mymusic;

/**
 * Created by shenjiaqi on 2017/7/18.
 */

public class Musiclist {

    public int id;
    public int itemImageResId;
    public String title;
    public int album;
    public int duration;
    public long size;
    public String artist;
    public String url;

    public Musiclist(int itemImageResId, String title, int duration, int id, int album, long size,  String artist, String url) {
        this.itemImageResId = itemImageResId;
        this.title = title;
        this.duration = duration;
        this.id = id;
        this.album = album;
        this.size = size;
        this.artist = artist;
        this.url = url;
    }
}
