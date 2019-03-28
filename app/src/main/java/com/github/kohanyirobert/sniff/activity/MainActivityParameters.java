package com.github.kohanyirobert.sniff.activity;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import static java.lang.String.format;

public final class MainActivityParameters implements Parcelable {

    public static final String YOUTUBE_URL_FORMAT = "https://www.youtube.com/watch?v=%s";

    public static final Creator<MainActivityParameters> CREATOR = new Creator<MainActivityParameters>() {

        @Override
        public MainActivityParameters createFromParcel(Parcel in) {
            return new MainActivityParameters(in);
        }

        @Override
        public MainActivityParameters[] newArray(int size) {
            return new MainActivityParameters[size];
        }
    };

    public static MainActivityParameters create(Intent intent) {
        return new MainActivityParameters(
                MainActivityMode.valueOf(intent.getStringExtra(MainActivity.EXTRA_MODE), MainActivityMode.PRODUCTION),
                intent.getStringExtra(Intent.EXTRA_TEXT),
                intent.getStringExtra(Intent.EXTRA_SUBJECT));
    }

    private MainActivityMode mMode;
    private String mText;
    private String mSubject;
    private String mVideoTitle;
    private String mNormalizedUrl;
    private String mArtist;
    private String mTitle;

    public MainActivityParameters(Parcel in) {
        mMode = MainActivityMode.valueOf(in.readString(), MainActivityMode.PRODUCTION);
        mText = in.readString();
        mSubject = in.readString();
        mNormalizedUrl = in.readString();
        mVideoTitle = in.readString();
        mArtist = in.readString();
        mTitle = in.readString();
    }

    private MainActivityParameters(@NonNull MainActivityMode mode, String text, String subject) {
        mMode = mode;
        mText = text;
        mSubject = subject;
        init();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMode.name());
        dest.writeString(mText);
        dest.writeString(mSubject);
        dest.writeString(mNormalizedUrl);
        dest.writeString(mVideoTitle);
        dest.writeString(mArtist);
        dest.writeString(mTitle);
    }

    public MainActivityMode getMode() {
        return mMode;
    }

    public String getText() {
        return mText;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getVideoTitle() {
        return mVideoTitle;
    }

    public String getNormalizedUrl() {
        return mNormalizedUrl;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getTitle() {
        return mTitle;
    }

    private void init() {
        initNormalizedUrl();
        initVideoTitle();
        initArtistAndTitle();
    }

    private void initNormalizedUrl() {
        int index = mText.lastIndexOf('/');
        mNormalizedUrl = format(YOUTUBE_URL_FORMAT, mText.substring(index + 1));
    }

    private void initVideoTitle() {
        int startIndex = mSubject.indexOf('"');
        int endIndex = mSubject.lastIndexOf('"');
        if (startIndex != -1 && endIndex != -1 && startIndex != endIndex) {
            mVideoTitle = mSubject.substring(startIndex + 1, endIndex);
        } else {
            mVideoTitle = mSubject;
        }
    }

    private void initArtistAndTitle() {
        String[] parts = mVideoTitle.split("-");
        if (parts.length == 2) {
            mArtist = parts[0].trim();
            mTitle = parts[1].trim();
        }
    }
}
