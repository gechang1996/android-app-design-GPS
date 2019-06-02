package edu.msu.gechang1.cse476map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EndGameActivity extends AppCompatActivity {

    private TextView text_result;
    private ImageView image_result;
    private String status;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endgame);

        text_result = (TextView) findViewById(R.id.textWinOrLose);
        image_result = (ImageView) findViewById(R.id.imageWinOrLose);
        status = getIntent().getStringExtra("status");
        if(status.equals("1")) {
            image_result.setImageResource(R.mipmap.yellow_star);
            text_result.setText(R.string.YouWin);
        }else if(status.equals("2")){
            image_result.setImageResource(R.mipmap.black_star);
            text_result.setText(R.string.YouLose);
        }
        else
        {
            image_result.setImageResource(R.mipmap.black_star);
            text_result.setText(R.string.YouSurrender);
        }

    }

    public void onClickEndGame(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud();

                if(status.equals("1") || status.equals("2"))
                {
                    cloud.clearTables();
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(EndGameActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                });
            }


        }).start();
    }
}
