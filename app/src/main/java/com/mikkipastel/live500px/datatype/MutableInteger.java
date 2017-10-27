package com.mikkipastel.live500px.datatype;

import android.os.Bundle;

public class MutableInteger {
    private int value;

    public MutableInteger(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Bundle onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putInt("value", value);
        return bundle;
    }

    public void onRestoreSaveInstanceState(Bundle savedInstanceState) {
        value = savedInstanceState.getInt("value");
    }
}
