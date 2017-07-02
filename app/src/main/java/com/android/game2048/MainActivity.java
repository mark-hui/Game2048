package com.android.game2048;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.game2048.config.Config;
import com.android.game2048.view.GameView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static MainActivity mMainActivity;//此类的引用，为了使用其中方法

    private TextView mTvScore;//分数的view
    private TextView mTvHighScore;//最高分的view
    private TextView mTvGoal;//左上角目标分数view

    private int mHighScore;//最高分的数字
    private int mGoal;//目标分数的数字

    private Button mBtnRestart;
    private Button mBtnRevert;
    private Button mBtnOptions;

    private GameView mGameView;


    public MainActivity() {//构造函数
        mMainActivity = this;
    }

    public static MainActivity getMainActivity() {
        return mMainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mGameView = new GameView(this);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.game_panel_rl);
        relativeLayout.addView(mGameView);
    }

    private void initView() {
        mTvScore = (TextView) findViewById(R.id.score);
        mTvGoal = (TextView) findViewById(R.id.tv_goal);
        mTvHighScore = (TextView) findViewById(R.id.record);
        mBtnRestart = (Button) findViewById(R.id.btn_restart);
        mBtnRevert = (Button) findViewById(R.id.btn_revert);
        mBtnOptions = (Button) findViewById(R.id.btn_option);
        mBtnRestart.setOnClickListener(this);
        mBtnRevert.setOnClickListener(this);
        mBtnOptions.setOnClickListener(this);
        mHighScore = Config.sp.getInt(Config.KEY_HIGH_SCORE, 0);
        mGoal = Config.sp.getInt(Config.KEY_GAME_GOAL, 2048);
        mTvHighScore.setText("" + mHighScore);
        mTvGoal.setText("" + mGoal);
        mTvScore.setText("0");
        setScore(0, 0);
    }

    public void setGoal(int num) {
        mTvGoal.setText(String.valueOf(num));
    }

    public void setScore(int score, int flag) {
        switch (flag) {
            case 0:
                mTvScore.setText("" + score);
                break;
            case 1:
                mTvHighScore.setText("" + score);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_restart:
                mGameView.startGame();
                setScore(0, 0);
                break;
            case R.id.btn_revert:
                mGameView.revertGame();
                break;
            case R.id.btn_option:
                Intent intent = new Intent(MainActivity.this, ConfigPreference.class);
                startActivityForResult(intent, 0);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mGoal = Config.sp.getInt(Config.KEY_GAME_GOAL, 2048);
            mTvGoal.setText("" + mGoal);
            getHighScore();
            mGameView.startGame();
        }
    }

    private void getHighScore() {
        int score = Config.sp.getInt(Config.KEY_HIGH_SCORE, 0);
        setScore(score, 1);
    }
}
