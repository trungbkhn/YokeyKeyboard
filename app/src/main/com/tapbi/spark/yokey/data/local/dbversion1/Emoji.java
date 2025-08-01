package com.tapbi.spark.yokey.data.local.dbversion1;

import android.os.Parcel;
import android.os.Parcelable;


public class Emoji implements Parcelable {

    private String title;
    private String character;

    public Emoji(){

    }

    public Emoji(String title, String character) {
        this.title = title;
        this.character = character;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    protected Emoji(Parcel in) {
        title = in.readString();
        character = in.readString();
    }

    public static final Creator<Emoji> CREATOR = new Creator<Emoji>() {
        @Override
        public Emoji createFromParcel(Parcel in) {
            return new Emoji(in);
        }

        @Override
        public Emoji[] newArray(int size) {
            return new Emoji[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(character);
    }
}
