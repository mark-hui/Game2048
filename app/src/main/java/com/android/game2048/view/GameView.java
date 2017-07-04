package com.android.game2048.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.GridLayout;
import android.view.View;

import com.android.game2048.MainActivity;
import com.android.game2048.bean.GameItem;
import com.android.game2048.config.Config;

import java.util.ArrayList;
import java.util.List;


public class GameView extends GridLayout implements View.OnTouchListener {
    //此为游戏界面中各种事件和算法的类
    //此类为游戏区域容器的类，即gridlayout中可设置行列空格数量，添加的framelayout即gameitem即方格
    //添加进布局中即形成游戏界面，在这里设置相关滑动和算法都是对游戏区域的计算

    private int mTarget;//目标分数
    private int mGameLines;//行数
    private int mHighScore;//最高分数
    private int mStartX, mStartY, mEndX, mEndY;//记录滑动前后的坐标

    private int mScoreHistory;//历史记录的分数
    private int[][] mGameMatrixHistory;//历史记录中的二维数组矩阵,只记录数字

    private GameItem[][] mGameMatrix;//二维数组矩阵
    private List<Integer> mCalList;//记录一行的数字集合，用于计算滑动合并等算法
    //空格的集合，利用point类型记录空格所在位置坐标XY，并没使用point类相关方法
    private List<Point> mBlanks;
    private int mKeyItemNum = -1;//记录是否有合并数字的标记


    //构造函数
    public GameView(Context context) {
        super(context);
        mTarget = Config.sp.getInt(Config.KEY_GAME_GOAL, 2048);//获取设定的目标分数
        initGameMatrix();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameMatrix();
    }

    //初始化，新建各种对象，载入设置等
    private void initGameMatrix() {
        removeAllViews();//布局方法，移除所有子布局
        mScoreHistory = 0;//清空历史记录，即上一步的分数
        Config.SCORE = 0;//面板上的实时分数清空
        Config.gameLines = Config.sp.getInt(Config.KEY_GAME_LINES, 4);//获取设置行数
        mGameLines = Config.gameLines;
        mGameMatrix = new GameItem[mGameLines][mGameLines];
        mGameMatrixHistory = new int[mGameLines][mGameLines];
        mCalList = new ArrayList<>();
        mBlanks = new ArrayList<>();
        mHighScore = Config.sp.getInt(Config.KEY_HIGH_SCORE, 0);//各种新建对象
        setColumnCount(mGameLines);//布局方法，设置列数
        setRowCount(mGameLines);//布局方法，设置行数
        setOnTouchListener(this);//在界面内触碰事件
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        Config.itemSize = metrics.widthPixels / Config.gameLines;
        initGameView(Config.itemSize);
    }

    //触碰屏幕事件
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                saveHistoryMatrix();
                mStartX = (int) event.getX();//获取坐标的int值，就是大概的值
                mStartY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                mEndX = (int) event.getX();
                mEndY = (int) event.getY();
                judgeDirection(mEndX - mStartX, mEndY - mStartY);
                if (isMoved()) {
                    addRandomNum();
                    MainActivity.getMainActivity().setScore(Config.SCORE, 0);
                }
                checkCompleted();
                break;
            default:
                break;
        }
        return true;
    }

    private boolean isMoved() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrixHistory[i][j] != mGameMatrix[i][j].getNum()) {
                    return true;
                }
            }
        }
        return false;
    }

    //判断是否有移动
    private void judgeDirection(int offsetX, int offsetY) {
        int density = getDeviceDensity();
        int slideDis = 5 * density;//最小滑动距离，为了证明有滑动而不是点击
        int maxDis = 200 * density;//最大滑动距离，为了区分普通滑动和作弊滑动
        //普通滑动的标记，XY任一方向滑动大于最小距离代表有滑动，且XY两个方向的滑动距离都不大于最大距离
        boolean flagNormal = (Math.abs(offsetX) > slideDis || Math.abs(offsetY) > slideDis) &&
                (Math.abs(offsetX) < maxDis) && (Math.abs(offsetY) < maxDis);
        //作弊滑动的标记，XY任一方向滑动大于最大距离就相当于是作弊滑动
        boolean flagSuper = Math.abs(offsetX) > maxDis || Math.abs(offsetY) > maxDis;
        if (flagNormal && !flagSuper) {
            if (Math.abs(offsetX) > Math.abs(offsetY)) {
                if (offsetX > slideDis) {
                    swipeRight();
                } else {
                    swipeLeft();
                }
            } else {
                if (offsetY > slideDis) {
                    swipeDown();
                } else {
                    swipeUp();
                }
            }
        } else if (flagSuper) {//如果是作弊滑动
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final EditText et = new EditText(getContext());
            builder.setTitle("Back Door")
                    .setView(et)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!TextUtils.isEmpty(et.getText())) {
                                //弹出提示框添加自定义数字到随机空格中
                                addSuperNum(Integer.parseInt(et.getText().toString()));
                                checkCompleted();
                            }
                        }
                    })
                    .setNegativeButton("ByeBye", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
    }

    //判断是否结束游戏了
    private void checkCompleted() {
        int result = checkNums();//获取判断结果类型
        if (result == 0) {//game over了
            if (Config.SCORE > mHighScore) {//如果大于历史高分
                SharedPreferences.Editor editor = Config.sp.edit();
                editor.putInt(Config.KEY_HIGH_SCORE, Config.SCORE);//记录最高分到文件中
                editor.apply();
                MainActivity.getMainActivity().setScore(Config.SCORE, 1);//将分数替换历史最高分
                Config.SCORE = 0;//实时记录的分数清零
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Game Over")//弹出对话框
                    .setPositiveButton("Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startGame();
                        }
                    }).create().show();
            Config.SCORE = 0;
        } else if (result == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Mission Accomplished")
                    .setPositiveButton("Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startGame();
                        }
                    })
                    .setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = Config.sp.edit();
                            if (mTarget == 1024) {
                                editor.putInt(Config.KEY_GAME_GOAL, 2048);
                                mTarget = 2048;
                                MainActivity.getMainActivity().setGoal(2048);
                            } else if (mTarget == 2048) {
                                editor.putInt(Config.KEY_GAME_GOAL, 4096);
                                mTarget = 4096;
                                MainActivity.getMainActivity().setGoal(4096);
                            } else {//这里如果继续用4096，会导致出现4096后每一步都会弹窗
                                editor.putInt(Config.KEY_GAME_GOAL, 8192);
                                mTarget = 8192;
                                MainActivity.getMainActivity().setGoal(8192);
                            }
                            editor.apply();
                        }
                    }).create().show();
            Config.SCORE = 0;
        }
    }

    //开始游戏
    public void startGame() {
        initGameMatrix();
        initGameView(Config.itemSize);
    }

    private void initGameView(int cardSize) {
        removeAllViews();//移除所有子布局
        GameItem card;
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                card = new GameItem(getContext(), 0);
                addView(card, cardSize, cardSize);//gridlayout加子布局，自动从左上角开始
                mGameMatrix[i][j] = card;//先左到右再从上到下
                mBlanks.add(new Point(i, j));//开始全部为空格
            }
        }
        addRandomNum();
        addRandomNum();
    }

    //撤销
    public void revertGame() {
        int sum = 0;
        for (int[] element : mGameMatrixHistory) {
            for (int i : element) {
                sum += i;
            }
        }
        if (sum != 0) {//不等于0说明已经有历史记录，所以可以撤销
            MainActivity.getMainActivity().setScore(mScoreHistory, 0);
            Config.SCORE = mScoreHistory;
            for (int i = 0; i < mGameLines; i++) {
                for (int j = 0; j < mGameLines; j++) {
                    mGameMatrix[i][j].setNum(mGameMatrixHistory[i][j]);
                }
            }
        }
    }

    private void addRandomNum() {
        getBlanks();//获取空格
        if (mBlanks.size() > 0) {//有空格
            int randomNum = (int) (Math.random() * mBlanks.size());
            Point randomPoint = mBlanks.get(randomNum);//随机取一个位置的空格加数字
            //添加2或者4，出现比例4：1
            mGameMatrix[randomPoint.x][randomPoint.y].setNum(Math.random() > 0.2d ? 2 : 4);
            animCreate(mGameMatrix[randomPoint.x][randomPoint.y]);
        }
    }

    //检查所有数字，看符合什么条件
    //0：为结束游戏，game over
    //1：为正常状态，可以继续游戏
    //2：为成功，达到目标分数，可选择继续或重新游戏
    private int checkNums() {
        getBlanks();//获取空格坐标的数组
        if (mBlanks.size() == 0) {//如果无空格
            for (int i = 0; i < mGameLines; i++) {//行
                for (int j = 0; j < mGameLines; j++) {//列
                    if (j < mGameLines - 1) {//除最后一个数外其余数与下面的数比较
                        if (mGameMatrix[i][j].getNum() == mGameMatrix[i][j + 1].getNum()) {
                            return 1;//相等则还可以游戏
                        }
                    }
                    if (i < mGameLines - 1) {//除最后一个数外其余数与右面的数比较
                        if (mGameMatrix[i][j].getNum() == mGameMatrix[i + 1][j].getNum()) {
                            return 1;//有相等则还可以游戏
                        }
                    }
                }
            }
            return 0;//循环结束两两相邻数都没有相等说明游戏结束
        }
        //有空格
        for (int i = 0; i < mGameLines; i++) {//行
            for (int j = 0; j < mGameLines; j++) {//列
                if (mGameMatrix[i][j].getNum() == mTarget) {//有空格数字等于目标分数
                    return 2;//说明成功了
                }
            }
        }
        return 1;//没成功有空格说明游戏正常还可以继续
    }

    //作弊模式下在随机空白地方生成一个指定数字
    private void addSuperNum(int num) {
        if (checkSuperNum(num)) {
            getBlanks();
            if (mBlanks.size() > 0) {
                int randomNum = (int) (Math.random() * mBlanks.size());
                Point randomPoint = mBlanks.get(randomNum);
                mGameMatrix[randomPoint.x][randomPoint.y].setNum(num);
                animCreate(mGameMatrix[randomPoint.x][randomPoint.y]);
            }
        }
    }

    //小方块弹出的动画
    private void animCreate(GameItem target) {
        ScaleAnimation sa = new ScaleAnimation(0.1f, 1, 0.1f, 1,//动画为中心向四周展开
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(100);//动画时长
        target.setAnimation(null);//为该视图设置下一个动画，null为无动画，target为整个游戏区域
        target.getItemView().startAnimation(sa);//设置方块中textview的动画并立刻执行动画
    }

    //检查数字是不是2048的数字
    private boolean checkSuperNum(int num) {
        boolean flag = (num == 2 || num == 4 || num == 8 || num == 16 ||
                num == 32 || num == 64 || num == 128 || num == 256 ||
                num == 512 || num == 1024);
        return flag;
    }

    //获取此时界面中空格坐标的集合
    private void getBlanks() {
        mBlanks.clear();
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrix[i][j].getNum() == 0) {
                    mBlanks.add(new Point(i, j));
                }
            }
        }
    }

    //获取屏幕密度块
    private int getDeviceDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return (int) metrics.density;//强转成int型省去小数点方便计算
    }

    //保存历史记录
    private void saveHistoryMatrix() {
        mScoreHistory = Config.SCORE;
        for (int i = 0; i < mGameLines; i++) {
            for (int j =0; j < mGameLines; j++) {
                mGameMatrixHistory[i][j] = mGameMatrix[i][j].getNum();
            }
        }
    }

    //向上滑动时的合并方块方法
    private void swipeUp() {
        for (int i = 0; i < mGameLines; i++) {//列
            for (int j = 0; j < mGameLines; j++) {//行
                int currentNum = mGameMatrix[j][i].getNum();//跟每列下一个比较
                if (currentNum != 0) {//有数字
                    if (mKeyItemNum == -1) {//是第一个比较数
                        mKeyItemNum = currentNum;//记下第一个比较数
                    } else {//是第二个比较数
                        if (mKeyItemNum == currentNum) {//跟第一相等说明可以合并
                            mCalList.add(mKeyItemNum * 2);//保存合并后的数到临时集合中
                            Config.SCORE += mKeyItemNum * 2;//总分加上合并的分数
                            mKeyItemNum = -1;//合并过一次后标记清零
                        } else {//跟第一个数不相等说明不能合并
                            mCalList.add(mKeyItemNum);//将第一个数存到临时集合中
                            mKeyItemNum = currentNum;//第二个数作为第一个比较数
                        }
                    }
                } else {
                    continue;//等于0说明是空格不能比较所以跳到下一个循环找非零数比较
                }
            }
            if (mKeyItemNum != -1) {//过完一列后如果最后一个数字没有合并则加到临时数组中
                mCalList.add(mKeyItemNum);
            }
            //将临时数组中保存的合并后一列的数字存到显示界面的数组中
            for (int j = 0; j < mCalList.size(); j++) {
                mGameMatrix[j][i].setNum(mCalList.get(j));
            }
            //存完临时数组中的数字还有剩下的填0代表空格
            for (int m = mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[m][i].setNum(0);
            }
            mKeyItemNum = -1;//将临时保存比较数清空
            mCalList.clear();//临时数组清空
        }
    }

    //向下滑动时的合并方块方法
    private void swipeDown() {
        for (int i = mGameLines - 1; i >= 0; i--) {//列
            for (int j = mGameLines - 1; j >= 0; j--) {//行
                int currentNum = mGameMatrix[j][i].getNum();//最后一列最后一个与上面一个比较
                if (currentNum != 0) {//不是空格
                    if (mKeyItemNum == -1) {//是第一个比较数
                        mKeyItemNum = currentNum;
                    } else {//是第二个比较数
                        if (mKeyItemNum == currentNum) {//等于第一个比较数
                            mCalList.add(mKeyItemNum * 2);//临时数组记录的是倒序
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;//清空
                        } else {//不等于
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {//是空格
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);//过完一行把最后一个（第一个）存到数组里
            }
            //先从上面剩下的位置填0
            for (int j = 0; j < mGameLines - mCalList.size(); j++) {
                mGameMatrix[j][i].setNum(0);
            }
            int index = mCalList.size() - 1;
            for (int m = mGameLines - mCalList.size();m < mGameLines; m++) {//从有数字的空格开始
                mGameMatrix[m][i].setNum(mCalList.get(index));//从临时数组最后一个开始反向加载
                index--;//临时数组向前加载
            }
            mKeyItemNum = -1;
            mCalList.clear();
        }
    }

    //向左滑动时的合并方块方法
    private void swipeLeft() {
        for (int i = 0; i < mGameLines; i++) {//行
            for (int j = 0; j < mGameLines; j++) {//列
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;//空格
                }
            }
            if (mKeyItemNum != -1) {//过完一行最后一个
                mCalList.add(mKeyItemNum);
            }
            for (int j = 0; j < mCalList.size(); j++) {
                mGameMatrix[i][j].setNum(mCalList.get(j));//将记录的单行数值加入
            }
            for (int m = mCalList.size(); m < mGameLines; m++) {//剩余空格设0
                mGameMatrix[i][m].setNum(0);
            }
            mKeyItemNum = -1;//清空临时容器里的数
            mCalList.clear();
        }
    }

    //向右滑动时的合并方块方法
    private void swipeRight() {
        for (int i = mGameLines - 1; i >= 0; i--) {
            for (int j = mGameLines - 1; j >= 0; j--) {
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            for (int j = 0; j < mGameLines - mCalList.size(); j++) {
                mGameMatrix[i][j].setNum(0);
            }
            int index = mCalList.size() - 1;
            for (int m = mGameLines - mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[i][m].setNum(mCalList.get(index));
                index--;
            }
            mKeyItemNum = -1;
            mCalList.clear();
        }
    }
}
