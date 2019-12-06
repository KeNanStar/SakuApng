package com.saku.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.apng.view.ApngSurfaceView;
import com.apng.entity.AnimParams;
import com.apng.utils.FileUtils;

import java.io.File;

public class MainActivity extends Activity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.image_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ApngImageViewActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.surface_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ApngSurfaceViewActivity.class);
                startActivity(intent);
            }
        });
    }



}
