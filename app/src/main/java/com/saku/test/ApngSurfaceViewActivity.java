package com.saku.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.apng.entity.AnimParams;
import com.apng.utils.FileUtils;
import com.apng.view.ApngSurfaceView;

import java.io.File;

public class ApngSurfaceViewActivity extends Activity{
    private ApngSurfaceView mApngSurfaceView;
    private static final  String COLOR_BALL_IMAGE_PATH = "assets://color_ball.png";
    private static final  String CAR_IMAGE_PATH = "assets://car.png";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);
        mApngSurfaceView = (ApngSurfaceView)findViewById(R.id.apng_surface_view);
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
        File file1 = FileUtils.processApngFile(CAR_IMAGE_PATH, this);

      /*  if(file == null) return;
        AnimParams animItem = new AnimParams();
        animItem.align = 2;
        animItem.imagePath = file.getAbsolutePath();
        animItem.isHasBackground = true;
        animItem.percent = 0.5f;
        mApngSurfaceView.addApngForPlay(animItem);*/


        AnimParams animItem1 = new AnimParams();
        animItem1.align = 2;
        animItem1.imagePath = file1.getAbsolutePath();
        animItem1.isHasBackground = true;
        animItem1.percent = 0.5f;
        animItem1.loopCount = AnimParams.PLAY_4_LOOP;
        mApngSurfaceView.addApngForPlay(animItem1);
    }
}
