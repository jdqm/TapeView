package com.jdqm.tapeview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.jdqm.tapelibrary.TapeView;

public class MainActivity extends Activity implements TapeView.OnValueChangeListener {

    private static final String TAG = "MainActivity";

    TextView textView;
    TapeView tapeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tvValue);
        tapeView = findViewById(R.id.tapeView);
        tapeView.setOnValueChangeListener(this);
        //tapeView.setValue(65, 40.7f, 200, 0.1f, 10);
        textView.setText(tapeView.getValue() + " 公斤");
    }

    @Override
    public void onChange(float value) {
        textView.setText(value + "公斤");
    }
}