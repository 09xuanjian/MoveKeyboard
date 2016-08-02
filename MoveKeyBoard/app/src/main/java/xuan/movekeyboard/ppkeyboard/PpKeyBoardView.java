package xuan.movekeyboard.ppkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import java.util.List;

import xuan.movekeyboard.R;

/**
 * 自定义键盘 View
 * @author xuanweijian
 * @create 2016/7/25 18:10.
 */

public class PpKeyBoardView extends KeyboardView {

    private Context mContext;
    private Keyboard mKeyBoard;
    private int mWidthPixels;

    private int rightType = KeyBoardDefinition.NUM_KEYBOARD_RIGHT_TYPE_ZERO;// 数字键盘右下角类型
    private int mKeyBoardType = KeyBoardDefinition.KEYBOARD_TYPE_ABC;

    public PpKeyBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mWidthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
    }

    public PpKeyBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        this.mWidthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 重新画一些按键
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mKeyBoard != null) {
            List<Key> keys = mKeyBoard.getKeys();

            for (Key key : keys) {
                // 数字键盘的处理
                switch (mKeyBoardType) {
                    case KeyBoardDefinition.KEYBOARD_TYPE_ABC:
                        drawABCSpecialKey(key, canvas);
                        break;
                    case KeyBoardDefinition.KEYBOARD_TYPE_NUM:
                        initRightType(key);
                        drawNumSpecialKey(key, canvas);
                        break;
                    case KeyBoardDefinition.KEYBOARD_TYPE_SYMBOL:
                        drawSymbolSpecialKey(key, canvas);
                        break;
                }
            }
        }
    }

    /**
     * 设置类型
     * @param keyBoard
     * @param type
     */
    void setPPKeyBoardType(Keyboard keyBoard, int type) {
        this.mKeyBoard = keyBoard;
        this.mKeyBoardType = type;
    }

    //数字键盘
    private void drawNumSpecialKey(Key key, Canvas canvas) {
        switch (key.codes[0]) {
            case KeyBoardDefinition.CODE_PULL: // 顶部按键
                drawKeyBackground(R.drawable.btn_keyboard_key_pull, canvas, key);
                drawText(canvas, key);
                break;
            case KeyBoardDefinition.CODE_DELETE: //删除
            case KeyBoardDefinition.CODE_EMPTY: // 右下角的按键
            case KeyBoardDefinition.CODE_ABC_2:
            case KeyBoardDefinition.CODE_X:
            case KeyBoardDefinition.CODE_FINISH:
            case KeyBoardDefinition.CODE_POINT:
                if (KeyBoardDefinition.CODE_FINISH == key.codes[0] && null == key.label) {
                    break;
                }
                drawKeyBackground(R.drawable.btn_keyboard_key2, canvas, key);
                drawText(canvas, key);

                break;
        }
    }

    //字母键盘特殊处理背景
    private void drawABCSpecialKey(Key key, Canvas canvas) {
        switch (key.codes[0]) {
            case KeyBoardDefinition.CODE_DELETE:
                drawKeyBackground(R.drawable.btn_keyboard_key_delete, canvas, key);
                drawText(canvas, key);
                break;
            case KeyBoardDefinition.CODE_SHIFT:
                drawKeyBackground(R.drawable.btn_keyboard_key_shift, canvas, key);
                drawText(canvas, key);
                break;
            case KeyBoardDefinition.CODE_CHANGE_123:
            case KeyBoardDefinition.CODE_CHANGE_SYMBOL:
                drawKeyBackground(R.drawable.btn_keyboard_key_123, canvas, key);
                drawText(canvas, key);
                break;
            case KeyBoardDefinition.CODE_SPACE:
                drawKeyBackground(R.drawable.btn_keyboard_key_space, canvas, key);
                break;
        }
    }

    //标点键盘特殊处理背景
    private void drawSymbolSpecialKey(Key key, Canvas canvas) {
        switch (key.codes[0]) {
            case KeyBoardDefinition.CODE_CHANGE_123:
            case KeyBoardDefinition.CODE_CHANGE_ABC:
                drawKeyBackground(R.drawable.btn_keyboard_key_change, canvas, key);
                drawText(canvas, key);
                break;
            case KeyBoardDefinition.CODE_DELETE:
                drawKeyBackground(R.drawable.btn_keyboard_key_delete, canvas, key);
                break;
        }
    }

    private void drawKeyBackground(int drawableId, Canvas canvas, Key key) {
        Drawable npd = mContext.getResources().getDrawable(
                drawableId);
        int[] drawableState = key.getCurrentDrawableState();
        if (key.codes[0] != 0) {
            npd.setState(drawableState);
        }
        npd.setBounds(key.x, key.y, key.x + key.width, key.y
                + key.height);
        npd.draw(canvas);
    }

    private void initRightType(Key key) {
        switch (key.codes[0]) {
            case KeyBoardDefinition.CODE_EMPTY:
                rightType = KeyBoardDefinition.NUM_KEYBOARD_RIGHT_TYPE_ZERO;
                break;
            case KeyBoardDefinition.CODE_X:
                rightType = KeyBoardDefinition.NUM_KEYBOARD_RIGHT_TYPE_X;
                break;
            case KeyBoardDefinition.CODE_POINT:
                rightType = KeyBoardDefinition.NUM_KEYBOARD_RIGHT_TYPE_POINT;
                break;
            case KeyBoardDefinition.CODE_FINISH:
                if (key.label.equals("完成")) {
                    rightType = KeyBoardDefinition.NUM_KEYBOARD_RIGHT_TYPE_FINISH;
                } else if (key.label.equals("下一项")) {
                    rightType = KeyBoardDefinition.NUM_KEYBOARD_RIGHT_TYPE_NEXT;
                }
                break;
        }
    }

    int getRightType() {
        return this.rightType;
    }

    private int pxTextSize(int original) {
        int px = (original + 5) * mWidthPixels / 1080;
        if (px < 1) {
            px = 1;
        }
        return px;
    }

    private void drawText(Canvas canvas, Key key) {
        Rect bounds = new Rect();
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        if (KeyBoardDefinition.CODE_POINT == key.codes[0]) {
            paint.setTextSize(70);
        } else {
            paint.setTextSize(pxTextSize(55));
        }
        paint.setAntiAlias(true);
        // paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.BLACK);

        switch (mKeyBoardType) {
            case KeyBoardDefinition.KEYBOARD_TYPE_NUM:
                if (key.label != null) {
                    paint.getTextBounds(key.label.toString(), 0, key.label.toString()
                            .length(), bounds);
                    canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                            (key.y + key.height / 2) + bounds.height() / 2, paint);
                } else if (KeyBoardDefinition.CODE_PULL == key.codes[0]) {
                    key.icon.setBounds(key.x + 9 * key.width / 20, key.y + 3
                            * key.height / 8, key.x + 11 * key.width / 20, key.y + 5
                            * key.height / 8);
                    key.icon.draw(canvas);
                } else if (KeyBoardDefinition.CODE_DELETE == key.codes[0]) {
                    key.icon.setBounds(key.x + (int) (0.4 * key.width), key.y + (int) (0.328
                            * key.height), key.x + (int) (0.6 * key.width), key.y + (int) (0.672
                            * key.height));
                    key.icon.draw(canvas);
                }
                break;
            case KeyBoardDefinition.KEYBOARD_TYPE_ABC:
                if (key.label != null) {
                    paint.setColor(mContext.getResources().getColor(R.color.keyboard_text_color));
                    paint.getTextBounds(key.label.toString(), 0, key.label.toString()
                            .length(), bounds);
                    canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                            (key.y + key.height / 2) + bounds.height() / 2, paint);
                }
                break;
            case KeyBoardDefinition.KEYBOARD_TYPE_SYMBOL:
                paint.setColor(mContext.getResources().getColor(R.color.keyboard_text_color));
                paint.getTextBounds(key.label.toString(), 0, key.label.toString()
                        .length(), bounds);
                canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                        (key.y + key.height / 2) + bounds.height() / 2, paint);
                break;
        }
    }
}
