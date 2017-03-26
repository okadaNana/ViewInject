package com.okada.viewinject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.okada.library.annotation.InjectView;
import com.okada.library.api.Views;


public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.tv)
    public TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Views.inject(this);
        mTextView.setText("TextView");
    }
}
