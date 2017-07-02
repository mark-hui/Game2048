package com.android.game2048.config;

import android.app.Application;
import android.content.SharedPreferences;


public class Config extends Application {//用于保存设置和记录最高分等要记录数据的类
    //此类继承Application就说明程序一启动，系统会自动初始化此类，sp对象和文件也会一起生成

    public static SharedPreferences sp;//sp对象，可以给外面
    public static int gameGoal;//设置的目标分数
    public static int gameLines;//矩阵行列数
    public static int itemSize;//每个小方格宽高

    public static int SCORE = 0;//记录分数
    //取出用字符表示为了方便其他地方引用，不用重复打键名
    public static String SP_HIGH_SCORE = "SP_HIGHSCORE";//sp文件名
    public static String KEY_HIGH_SCORE = "KEY_HIGHSCORE";//记录最高分的键
    public static String KEY_GAME_LINES = "KEY_GAMELINES";//记录设置行数的键
    public static String KEY_GAME_GOAL = "KEY_GAMEGOAL";//记录设置目标分数的键

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(SP_HIGH_SCORE, MODE_PRIVATE);//获取sp对象
        gameLines = sp.getInt(KEY_GAME_LINES, 4);//取设置行数，没有返回默认4
        gameGoal = sp.getInt(KEY_GAME_GOAL, 2048);//取设置目标分数，没有返回默认2048
        itemSize = 0;//小方格宽高清零
    }
}
