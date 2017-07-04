package com.android.game2048.bean;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.game2048.config.Config;


public class GameItem extends FrameLayout {//每个方格的类
    /*
    这里设置的是一个方格的属性，framelayout为父布局，背景颜色实质为游戏区域背景颜色，因为到时候要
    添加进gridlayout，就相当于是gridlayout的背景颜色，而实质方格是textview，所以textview背景颜色
    就是方格的颜色，而动画效果也是对textview设置
     */

    private int cardShowNum;//方格中数字
    private TextView tvNum;//方格中的子布局

    public GameItem(Context context, int cardShowNum) {
        super(context);
        this.cardShowNum = cardShowNum;
        initCardItem();
    }

    //初始化
    private void initCardItem() {
        setBackgroundColor(Color.GRAY);//设置方格的背景色，即整个游戏区域的背景色
        tvNum = new TextView(getContext());
        setNum(cardShowNum);//设置数字和子布局背景颜色，即方格背景颜色
        int gameLines = Config.sp.getInt(Config.KEY_GAME_LINES, 4);//取设置的行数
        if (gameLines == 4) {//四行时
            tvNum.setTextSize(35);
        } else if (gameLines == 5) {//五行时
            tvNum.setTextSize(25);
        } else {
            tvNum.setTextSize(20);//六行或以上时
        }
        TextPaint tp = tvNum.getPaint();
        tp.setFakeBoldText(true);//字体加粗
        tvNum.setGravity(Gravity.CENTER);//设置数字居方块中
        LayoutParams lp = new LayoutParams(//设置textview占满一个方格
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.setMargins(5, 5, 5, 5);//设置与指定方向控件之间的距离，即与父布局边界的距离（因只有一个控件）
        addView(tvNum, lp);//添加textview进framelayout
    }

    public View getItemView() {//获取文字控件
        return tvNum;
    }

    public int getNum() {
        return cardShowNum;//获取数字
    }

    //设置数字和背景颜色
    public void setNum(int num) {
        cardShowNum = num;//将其它地方传入的数字保存在对象里
        if (num == 0) {
            tvNum.setText("");//0则为空格
        } else {
            tvNum.setText("" + num);
        }
        switch (num) {
            case 0:
                tvNum.setBackgroundColor(0x00000000);
                break;
            case 2:
                tvNum.setBackgroundColor(0xffeee5db);
                break;
            case 4:
                tvNum.setBackgroundColor(0xffeee0ca);
                break;
            case 8:
                tvNum.setBackgroundColor(0xfff2c17a);
                break;
            case 16:
                tvNum.setBackgroundColor(0xfff59667);
                break;
            case 32:
                tvNum.setBackgroundColor(0xfff68c6f);
                break;
            case 64:
                tvNum.setBackgroundColor(0xfff66e3c);
                break;
            case 128:
                tvNum.setBackgroundColor(0xffedcf74);
                break;
            case 256:
                tvNum.setBackgroundColor(0xffedcc64);
                break;
            case 512:
                tvNum.setBackgroundColor(0xffedc854);
                break;
            case 1024:
                tvNum.setBackgroundColor(0xffedc54f);
                break;
            case 2048:
                tvNum.setBackgroundColor(0xffedc32e);
                break;
            default:
                tvNum.setBackgroundColor(0xff3c4a34);
                break;
        }
    }
}
