package com.saku.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.apng.entity.AnimParams;
import com.apng.utils.FileUtils;
import com.apng.view.ApngImageView;
import com.apng.view.ApngLoader;

import java.io.File;

public class ApngImageViewActivity extends Activity{
    private ApngImageView mApngImageView;
    private static final  String COLOR_BALL_IMAGE_PATH = "assets://color_ball.png";
    private static final  String CAR_IMAGE_PATH = "assets://car.png";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        mApngImageView = (ApngImageView) findViewById(R.id.apng_image_view);
        Button startPlay = (Button) findViewById(R.id.start_play);
        startPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAnim();
            }
        });
    }


    private void playAnim(){
        //File file = FileUtils.processApngFile(COLOR_BALL_IMAGE_PATH, this);
        File file1 = FileUtils.processApngFile(COLOR_BALL_IMAGE_PATH, this);

        ApngLoader.getInstance().loadApng(file1.getAbsolutePath(), mApngImageView);

    }
}
