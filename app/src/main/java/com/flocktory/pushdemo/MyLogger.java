package com.flocktory.pushdemo;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

public class MyLogger extends BaseObservable {

    public static String text = "";

    @Bindable
    public String getText() {
        return text;
    }

    public void setText(String s) {
        text = s;
        notifyPropertyChanged(BR.text);
    }

    public void log(String m) {
        this.setText(text + m);
    }

    public void reset() {
        setText("");
    }
}
