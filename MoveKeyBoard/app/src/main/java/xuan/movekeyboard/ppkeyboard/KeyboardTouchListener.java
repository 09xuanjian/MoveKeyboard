package xuan.movekeyboard.ppkeyboard;

import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * 自定义键盘 监听
 * @author xuanweijian
 * @create 2016/7/25 18:10.
 */
public class KeyboardTouchListener implements View.OnTouchListener {
    private KeyboardManage mKeyboardUtil;
    private int mKeyboardType = 1;
    private int mScrollTo = -1;
    private EditText mEditText;

    public KeyboardTouchListener(KeyboardManage util, EditText editText, int keyboardType, int scrollTo) {
        this.mKeyboardUtil = util;
        this.mKeyboardType = keyboardType;
        this.mScrollTo = scrollTo;
        this.mEditText = editText;
    }

    /**
     * 用于首次创建需要立刻显示
     */
    public KeyboardTouchListener show() {
        mKeyboardUtil.showKeyBoardLayout(mEditText, mKeyboardType, mScrollTo);
        mEditText.requestFocus();
        return this;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (mKeyboardUtil != null && mKeyboardUtil.getEd() != null && v.hashCode() != mKeyboardUtil.getEd().hashCode())
                mKeyboardUtil.showKeyBoardLayout((EditText) v, mKeyboardType, mScrollTo);
            else if (mKeyboardUtil != null && mKeyboardUtil.getEd() == null) {
                mKeyboardUtil.showKeyBoardLayout((EditText) v, mKeyboardType, mScrollTo);
            } else {
                if (mKeyboardUtil != null) {
                    mKeyboardUtil.setKeyBoardCursor((EditText) v);
                }
            }
        }
        return false;
    }
}
