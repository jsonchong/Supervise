package com.jason.supervise;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SuperVise.getInstance().trackPageCreateEvent(getRenderKey());
        int layoutResID = R.layout.activity_main;
        setContentView(SuperVise.getInstance().wrapWithTrackView(getRenderKey(), getLayoutInflater(), layoutResID));
    }

    private String getRenderKey() {
        return getClass().getSimpleName();
    }

}
