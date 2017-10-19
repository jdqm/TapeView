package com.jdqm.tapeview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.jdqm.tapelibrary.TapeView;

public class MainActivity extends Activity implements TapeView.OnValueChangeListener {

    private static final String TAG = "MainActivity";

    private TextView tvWeight;
    private TextView tvHeight;
    private TapeView tapeWeight;
    private TapeView tapeHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        tapeWeight.setOnValueChangeListener(this);
        tapeHeight.setOnValueChangeListener(new TapeView.OnValueChangeListener() {
            @Override
            public void onChange(float value) {
                tvHeight.setText(value + " " + getString(R.string.cm));
            }
        });

        //tapeView.setValue(65, 40.7f, 200, 0.1f, 10);
        tvWeight.setText(tapeWeight.getValue() + " " + getString(R.string.kg));
        tvHeight.setText(tapeHeight.getValue() + " " + getString(R.string.cm));
    }

    private void initViews() {
        tvWeight = findViewById(R.id.tvWeight);
        tvHeight = findViewById(R.id.tvHeight);
        tapeWeight = findViewById(R.id.tapeWeight);
        tapeHeight = findViewById(R.id.tapeHeight);
    }

    @Override
    public void onChange(float value) {
        tvWeight.setText(value + " " + getString(R.string.kg));
    }
}