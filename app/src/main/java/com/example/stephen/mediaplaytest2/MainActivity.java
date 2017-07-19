package com.example.stephen.mediaplaytest2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView button;
    private TextView endTime;
    private TextView startTime;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private int position=0;
    private String TEXT_CHANGE="com.example.stephen.mediaplayertest2.TEXT_CHASNGE";

    private IntentFilter intentFilter;
    private TextChangeReceiver textChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter=new IntentFilter();
        intentFilter.addAction(TEXT_CHANGE);
        textChangeReceiver=new TextChangeReceiver();
        registerReceiver(textChangeReceiver,intentFilter);

        endTime=(TextView)findViewById(R.id.tv_time_total_play);
        startTime=(TextView)findViewById(R.id.tv_time_current_play);
        startTime.setText("00:00");
        seekBar=(SeekBar)findViewById(R.id.progress_voice_play);

        mediaPlayer=new MediaPlayer();
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("MainActivity","Play Error!!!!!!!!!");
                return false;
            }
        });
        final File file=new File(Environment.getExternalStorageDirectory(),"music.mp3");
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepareAsync();
        } catch (IOException e){
            e.printStackTrace();
        }
        seekBar.setMax(mediaPlayer.getDuration());
        button=(ImageView)findViewById(R.id.button_voice_play);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                    endTime.setText(calTime(mediaPlayer.getDuration()));
                    if (position>0){
                        mediaPlayer.seekTo(position);
                    }
                    new MyThread().start();
                    button.setImageResource(R.drawable.ic_voice_pause);
                }else {
                    mediaPlayer.pause();
                    button.setImageResource(R.drawable.ic_voice_play);
                }
            }
        });
    }

    public String calTime(int druation){
        int min,sec,allSec;
        allSec=druation/1000+1;
        min=allSec/60;
        sec=allSec%60;
        String minStr,secStr;
        if (min==0){
            minStr="00";
        }else minStr=""+min;
        if (sec<10){
            secStr="0"+sec;
        }else secStr=""+sec;
        return minStr+":"+secStr;
    }

    private class MyThread extends Thread{
        @Override
        public void run() {
            while (mediaPlayer.isPlaying())
            {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                position=mediaPlayer.getCurrentPosition();
                Intent intent=new Intent(TEXT_CHANGE);
                sendBroadcast(intent);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class TextChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action==TEXT_CHANGE){
                startTime.setText(calTime(mediaPlayer.getCurrentPosition()));
            }
        }
    }
}
