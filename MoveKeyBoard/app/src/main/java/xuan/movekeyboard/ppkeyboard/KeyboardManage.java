package xuan.movekeyboard.ppkeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import xuan.movekeyboard.R;

/**
 * 自定义键盘
 *
 * @author xuanweijian
 * @create 2016/7/25 18:10.
 */

public class KeyboardManage {

    private Context mContext;
    private Activity mActivity;
    private int mWidthPixels;

    private boolean mIsUpper = false;// 是否大写
    private boolean mIsShow = false;
    private InputFinishListener mInputOver;
    private KeyBoardStateChangeListener mKeyBoardStateChangeListener;

    private PpKeyBoardView mKeyboardView;
    private Keyboard mAbcKeyboard;// 字母键盘
    private View mKeyBoardLayout;
    private TextView mKeyboardTipsTextView;
    private ScrollView mMainScrollView;
    private View mRootView;
    private EditText mEditText;
    private View mInflaterView;

    // 开始输入的键盘状态设置
    private int mInputType = KeyBoardDefinition.INPUTTYPE_NUM;// 默认

    private Handler mHandler;
    private Handler mShowHandler; //TODO 需要检查是否泄漏
    private int mScrollTo = 0;

    private boolean mSafeTipsVisible = false;

    private static final float KEYBOARD_H = 0.391f;//键盘高度
    private static final float KEYBOARD_TOP_H = 0.053f;//顶部tips高度
    private static final float TIPS_MARGIN_W = 0.0407f;
    private HashMap<Integer, KeyBoardMap> mKeyBoardMapHashMap;

    /**
     * 构造
     *
     * @param ctx
     * @param rootView rootView 需要是LinearLayout,以适应键盘
     */
    public KeyboardManage(Context ctx, LinearLayout rootView, ScrollView scrollView) {
        this.mContext = ctx;
        this.mActivity = (Activity) mContext;
        mWidthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
        mKeyBoardMapHashMap = new HashMap<>();
        initKeyBoardView(rootView);
        if (null != scrollView) {
            initScrollHandler(rootView, scrollView);
        }
    }

    public KeyboardManage(Context ctx, LinearLayout rootView) {
        this(ctx, rootView, null);
    }

    /**
     * 弹框类，用这个
     *
     * @param view 是弹框的inflaterView
     */
    public KeyboardManage(View view, Context ctx, LinearLayout root_View, ScrollView scrollView) {
        this(ctx, root_View, scrollView);
        this.mInflaterView = view;
    }


    /**
     * 隐藏所有keyBoard(自定义，系统)
     */
    public void hideAllKeyBoard() {
        hideSystemKeyBoard();
        hideKeyboardLayout();
    }

    /**
     * 获取现在键盘的状态
     *
     * @return
     */
    public boolean getKeyboardState() {
        return this.mIsShow;
    }

    /**
     * 获取当前EditText
     *
     * @return
     */
    EditText getEd() {
        return mEditText;
    }


    /**
     * 设置需要使用这个键盘的editText
     *
     * @param keyboardType    键盘类型
     * @param scrollTo        屏幕滑动距离
     * @param editText        editText
     * @param showImmediately 只有最后设置的有效，其他无效
     */
    public KeyboardManage addEditText(EditText editText, int keyboardType, int scrollTo, boolean showImmediately) {
        KeyBoardMap keyBoard = new KeyBoardMap();
        keyBoard.editText = editText;
        keyBoard.keyType = keyboardType;
        keyBoard.scrollTo = scrollTo;
        mKeyBoardMapHashMap.put(editText.hashCode(), keyBoard);

        if (showImmediately) {
            editText.setOnTouchListener(new KeyboardTouchListener(this, editText, keyboardType, scrollTo).show());
        } else {
            editText.setOnTouchListener(new KeyboardTouchListener(this, editText, keyboardType, scrollTo));
        }
        return this;
    }

    /**
     * 不设置 scrollTo 默认不滑动到具体位置，默认不立刻show出来
     */
    public KeyboardManage addEditText(EditText editText, int keyboardType) {
        addEditText(editText, keyboardType, 0, false);
        return this;
    }

    /**
     * 默认不立刻show出来, 可以自动滑动到具体位置
     */
    public KeyboardManage addEditText(EditText editText, int keyboardType, int scrollTo) {
        addEditText(editText, keyboardType, scrollTo, false);
        return this;
    }

    /**
     * 默认不滑动，可以判断是否立刻显示
     */
    public KeyboardManage addEditText(EditText editText, int keyboardType, boolean showImmediately) {
        addEditText(editText, keyboardType, 0, showImmediately);
        return this;
    }


    /**
     * 设置一些不需要使用这个键盘的editText,解决切换问题
     *
     * @param editTexts editTexts
     */
    public KeyboardManage setOtherEditText(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //防止没有隐藏键盘的情况出现
                    new Handler().postDelayed(() -> hideKeyboardLayout(), 300);
                    mEditText = (EditText) v;
                    hideKeyboardLayout();
                }
                return false;
            });
        }
        return this;
    }

    /**
     * 设置是否显示安全icon
     *
     * @param safeTipsVisible true 显示，false 不显示
     */
    public KeyboardManage setSafeTipsVisible(boolean safeTipsVisible) {
        this.mSafeTipsVisible = safeTipsVisible;
        return this;
    }

    /**
     * 手动隐藏键盘方法
     */
    public void hideKeyboardLayout() {
        if (getKeyboardState()) {
            if (mKeyBoardLayout != null) {
                mKeyBoardLayout.setVisibility(View.GONE);
            }

            if (mKeyboardView != null) {
                int visibility = mKeyboardView.getVisibility();
                if (visibility == View.VISIBLE) {
                    mKeyboardView.setVisibility(View.INVISIBLE);
                }
            }

            if (mKeyBoardStateChangeListener != null) {
                mKeyBoardStateChangeListener.KeyBoardStateChange(KeyBoardDefinition.KEYBOARD_HIDE, mEditText);
            }

            mEditText = null;
        }
    }

    /**
     *  在addEditText 之后，如果某个EditText 需要显示键盘。调用这个
     */
    public void showKeyBoard(EditText editText) {

        if (editText.getVisibility() == View.INVISIBLE) {
            return;
        }

        KeyBoardMap map = mKeyBoardMapHashMap.get(editText.hashCode());
        if (null != map) {
            showKeyBoardLayout(map.editText, map.keyType, map.scrollTo);
        }
    }

    /**
     * @param editText
     * @param keyBoardType 类型
     * @param scrollTo     滑动到某个位置,可以是大于等于0的数，其他数不滑动
     */
    public void showKeyBoardLayout(final EditText editText, int keyBoardType, int scrollTo) {
        if (mEditText != null
                && editText.equals(mEditText)
                && getKeyboardState()
                && this.mInputType == keyBoardType)
            return;
        this.mEditText = editText;
        this.mInputType = keyBoardType;
        this.mScrollTo = scrollTo;


        mEditText.requestFocus();

        if (setKeyBoardCursor(editText)) {
            //需要系统键盘收了再显示，让UI切换流畅，添加延时
            mShowHandler = new Handler();
            mShowHandler.postDelayed(() -> show(editText), 300);
        } else {
            //直接显示
            show(editText);
        }
    }

    /**
     * @description: 数字键盘完成按键输入监听
     */
    public KeyboardManage setInputOverListener(InputFinishListener listener) {
        this.mInputOver = listener;
        return this;
    }


    public interface InputFinishListener {
        void inputHasOver(int onclickType, EditText editText);
    }

    /**
     * 监听键盘变化
     */
    public KeyboardManage setKeyBoardStateChangeListener(KeyBoardStateChangeListener listener) {
        this.mKeyBoardStateChangeListener = listener;
        return this;
    }

    public interface KeyBoardStateChangeListener {
        void KeyBoardStateChange(int state, EditText editText);
    }


    private void initKeyBoardView(LinearLayout rootView) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mKeyBoardLayout = inflater.inflate(R.layout.keyboard_layout, null);

        mKeyBoardLayout.setVisibility(View.GONE);
        initLayoutHeight((LinearLayout) mKeyBoardLayout);
        rootView.addView(mKeyBoardLayout);

    }

    private void initLayoutHeight(LinearLayout layoutView) {
        LinearLayout.LayoutParams keyboardLayoutLayoutParams = (LinearLayout.LayoutParams) layoutView
                .getLayoutParams();
        RelativeLayout TopLayout = (RelativeLayout) layoutView.findViewById(R.id.keyboard_view_top_rLayout);
        mKeyboardTipsTextView = (TextView) layoutView.findViewById(R.id.keyboard_tips_textView);
        TextView keyboard_view_finish = (TextView) layoutView.findViewById(R.id.keyboard_finish_top_textView);
        int tips_size = pxTextSize(34);
        int finish_size = pxTextSize(40);
        mKeyboardTipsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tips_size);
        keyboard_view_finish.setTextSize(TypedValue.COMPLEX_UNIT_PX, finish_size);
        setMargins(mKeyboardTipsTextView, (int) (mWidthPixels * TIPS_MARGIN_W), 0, 0, 0);
        mKeyboardTipsTextView.setVisibility(View.GONE);
        setMargins(keyboard_view_finish, 0, 0, (int) (mWidthPixels * TIPS_MARGIN_W), 0);
        keyboard_view_finish.setOnClickListener(new finishListener());
        if (keyboardLayoutLayoutParams == null) {
            int height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * KEYBOARD_H);
            layoutView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        } else {
            keyboardLayoutLayoutParams.height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * KEYBOARD_H);
        }

        LinearLayout.LayoutParams TopLayoutParams = (LinearLayout.LayoutParams) TopLayout
                .getLayoutParams();

        if (TopLayoutParams == null) {
            int height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * KEYBOARD_TOP_H);
            TopLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        } else {
            TopLayoutParams.height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * KEYBOARD_TOP_H);
        }
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                    .getLayoutParams();
            layoutParams.setMargins(left, top, right, bottom);
        } else if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view
                    .getLayoutParams();
            layoutParams.setMargins(left, top, right, bottom);
        }
    }

    /**
     * 外部不需要设置这个,提供给KeyboardTouchListener 使用
     *
     * @param edit
     * @return
     */
    boolean setKeyBoardCursor(@NonNull EditText edit) {
        this.mEditText = edit;
        boolean flag = false;

        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
        if (isOpen) {
            if (imm.hideSoftInputFromWindow(edit.getWindowToken(), 0)) {
                flag = true;
            }
        }
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus";
        }

        if (methodName == null) {
            edit.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            try {
                setShowSoftInputOnFocus = cls.getMethod(methodName,
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(edit, false);
            } catch (NoSuchMethodException e) {
                edit.setInputType(InputType.TYPE_NULL);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 隐藏系统键盘
     */
    public void hideSystemKeyBoard() {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mKeyBoardLayout.getWindowToken(), 0);
    }


    //滑动监听
    private void keyBoardScroll(final EditText editText, int scrollTo) {
        this.mScrollTo = scrollTo;
        ViewTreeObserver viewObserver = mRootView.getViewTreeObserver();
        viewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Message msg = new Message();
                msg.what = editText.getId();
                mHandler.sendMessageDelayed(msg, 500);
                // 防止多次促发
                mRootView.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
            }
        });
    }

    private int pxTextSize(int original) {
        int px = (original + 5) * mWidthPixels / 1080;
        if (px < 1) {
            px = 1;
        }
        return px;
    }

    private class finishListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mInputOver != null) {
                mInputOver.inputHasOver(Keyboard.KEYCODE_CANCEL, mEditText);
            }
            hideKeyboardLayout();
        }
    }


    private OnKeyboardActionListener listener = new OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void onText(CharSequence text) {
            if (mEditText == null)
                return;
            Editable editable = mEditText.getText();
            if (editable.length() >= 20)
                return;
            int start = mEditText.getSelectionStart();
            int end = mEditText.getSelectionEnd();

            String temp = editable.subSequence(0, start) + text.toString() + editable.subSequence(start, editable.length());
            mEditText.setText(temp);
            Editable eText = mEditText.getText();
            Selection.setSelection(eText, start + 1);
        }

        @Override
        public void onRelease(int primaryCode) {
            if (mInputType != KeyBoardDefinition.INPUTTYPE_NUM_ABC
                    && (primaryCode == Keyboard.KEYCODE_SHIFT)) {
                mKeyboardView.setPreviewEnabled(true);
            }
        }

        @Override
        public void onPress(int primaryCode) {
            //数字键盘不要preview
            if (mInputType == KeyBoardDefinition.INPUTTYPE_NUM_ABC ||
                    mInputType == KeyBoardDefinition.INPUTTYPE_NUM ||
                    mInputType == KeyBoardDefinition.INPUTTYPE_NUM_POINT ||
                    mInputType == KeyBoardDefinition.INPUTTYPE_NUM_FINISH ||
                    mInputType == KeyBoardDefinition.INPUTTYPE_NUM_NEXT ||
                    mInputType == KeyBoardDefinition.INPUTTYPE_NUM_X) {
                mKeyboardView.setPreviewEnabled(false);
                return;
            }
            //特殊输入不要preview
            if (primaryCode == Keyboard.KEYCODE_SHIFT
                    || primaryCode == Keyboard.KEYCODE_DELETE
                    || primaryCode == KeyBoardDefinition.CODE_CHANGE_123
                    || primaryCode == KeyBoardDefinition.CODE_CHANGE_ABC
                    || primaryCode == KeyBoardDefinition.CODE_CHANGE_SYMBOL
                    || primaryCode == KeyBoardDefinition.CODE_SPACE) {
                mKeyboardView.setPreviewEnabled(false);
                return;
            }
            mKeyboardView.setPreviewEnabled(true);
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            int start = 0;
            Editable editable = null;
            if (mEditText != null) {
                start = mEditText.getSelectionStart();
                editable = mEditText.getText();
            }

            switch (primaryCode) {
                case Keyboard.KEYCODE_CANCEL:
                    hideKeyboardLayout();
                    if (mInputOver != null) {
                        mInputOver.inputHasOver(primaryCode, mEditText);
                    }
                    break;

                case Keyboard.KEYCODE_DELETE:
                    if (mEditText != null && editable != null && editable.length() > 0) {
                        if (start > 0) {
                            editable.delete(start - 1, start);
                        }
                    }
                    break;

                case Keyboard.KEYCODE_SHIFT:
                    downUpperChangeKey();
                    mKeyboardView.setKeyboard(mAbcKeyboard);
                    break;

                case Keyboard.KEYCODE_DONE:
                    if (mKeyboardView.getRightType() == KeyBoardDefinition.NUM_KEYBOARD_RIGHT_TYPE_FINISH) {
                        hideKeyboardLayout();
                        if (mInputOver != null) {
                            mInputOver.inputHasOver(mKeyboardView.getRightType(), mEditText);
                        }
                    } else if (mKeyboardView.getRightType() == KeyBoardDefinition.NUM_KEYBOARD_RIGHT_TYPE_NEXT) {
                        // 下一个监听

                        if (mInputOver != null) {
                            mInputOver.inputHasOver(mKeyboardView.getRightType(), mEditText);
                        }
                    }
                    break;

                case KeyBoardDefinition.CODE_EMPTY:
                    // 空白键
                    break;

                case KeyBoardDefinition.CODE_CHANGE_123:
                    mIsUpper = false;
                    showKeyBoardLayout(mEditText, KeyBoardDefinition.INPUTTYPE_NUM_ABC, -1);
                    break;

                case KeyBoardDefinition.CODE_CHANGE_ABC:
                case KeyBoardDefinition.CODE_ABC_2:
                    mIsUpper = false;
                    showKeyBoardLayout(mEditText, KeyBoardDefinition.INPUTTYPE_ABC, -1);
                    break;

                case KeyBoardDefinition.CODE_CHANGE_SYMBOL:
                    showKeyBoardLayout(mEditText, KeyBoardDefinition.INPUTTYPE_SYMBOL, -1);
                    break;

                default:
                    if (editable != null) {
                        editable.insert(start, Character.toString((char) primaryCode));
                    }
                    break;
            }
        }
    };

    /**
     * 键盘大小写切换
     */
    private void downUpperChangeKey() {
        List<Key> keyList = mAbcKeyboard.getKeys();
        if (mIsUpper) {// 大写切小写
            mIsUpper = false;
            for (Key key : keyList) {
                if (key.label != null && isWord(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
            }
        } else {// 小写切大写
            mIsUpper = true;
            for (Key key : keyList) {
                if (key.label != null && isWord(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
            }
        }
    }


    private void initKeyBoard(int keyBoardViewID) {
        mActivity = (Activity) mContext;
        if (mInflaterView != null) {
            mKeyboardView = (PpKeyBoardView) mInflaterView.findViewById(keyBoardViewID);
        } else {
            mKeyboardView = (PpKeyBoardView) mActivity
                    .findViewById(keyBoardViewID);
        }
        mKeyboardView.setEnabled(true);
        mKeyboardView.setOnKeyboardActionListener(listener);
        mKeyboardView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                return true;
            }
            return false;
        });
    }

    private void initInputType() {
        switch (mInputType) {
            case KeyBoardDefinition.INPUTTYPE_NUM:
                initKeyBoard(R.id.keyboard_view);
                mKeyboardView.setPreviewEnabled(false);
                if (mSafeTipsVisible) {
                    mKeyboardTipsTextView.setVisibility(View.VISIBLE);
                } else {
                    mKeyboardTipsTextView.setVisibility(View.GONE);
                }

                Keyboard numKeyboard = new Keyboard(mContext, R.xml.symbols);
                setPPKeyBoard(numKeyboard, KeyBoardDefinition.KEYBOARD_TYPE_NUM);
                break;

            case KeyBoardDefinition.INPUTTYPE_NUM_FINISH:
                initKeyBoard(R.id.keyboard_view);
                mKeyboardView.setPreviewEnabled(false);
                mKeyboardTipsTextView.setVisibility(View.GONE);
                Keyboard numKeyboardFinish = new Keyboard(mContext, R.xml.symbols_finish);
                setPPKeyBoard(numKeyboardFinish, KeyBoardDefinition.KEYBOARD_TYPE_NUM);
                break;

            case KeyBoardDefinition.INPUTTYPE_NUM_POINT:
                initKeyBoard(R.id.keyboard_view);
                mKeyboardView.setPreviewEnabled(false);
                mKeyboardTipsTextView.setVisibility(View.GONE);
                Keyboard numKeyboardPoint = new Keyboard(mContext, R.xml.symbols_point);
                setPPKeyBoard(numKeyboardPoint, KeyBoardDefinition.KEYBOARD_TYPE_NUM);
                break;

            case KeyBoardDefinition.INPUTTYPE_NUM_X:
                initKeyBoard(R.id.keyboard_view);
                mKeyboardView.setPreviewEnabled(false);
                mKeyboardTipsTextView.setVisibility(View.VISIBLE);
                Keyboard numKeyboardX = new Keyboard(mContext, R.xml.symbols_x);
                setPPKeyBoard(numKeyboardX, KeyBoardDefinition.KEYBOARD_TYPE_NUM);
                break;

            case KeyBoardDefinition.INPUTTYPE_NUM_NEXT:
                initKeyBoard(R.id.keyboard_view);
                mKeyboardView.setPreviewEnabled(false);
                mKeyboardTipsTextView.setVisibility(View.GONE);
                Keyboard numKeyboardNext = new Keyboard(mContext, R.xml.symbols_next);
                setPPKeyBoard(numKeyboardNext, KeyBoardDefinition.KEYBOARD_TYPE_NUM);
                break;

            case KeyBoardDefinition.INPUTTYPE_NUM_ABC:
                initKeyBoard(R.id.keyboard_view);
                mKeyboardView.setPreviewEnabled(false);
                mKeyboardTipsTextView.setVisibility(View.VISIBLE);
                Keyboard numKeyboardAbc = new Keyboard(mContext, R.xml.symbols_num_abc);
                setPPKeyBoard(numKeyboardAbc, KeyBoardDefinition.KEYBOARD_TYPE_NUM);
                break;

            case KeyBoardDefinition.INPUTTYPE_ABC:
                initKeyBoard(R.id.abc_sym_keyboardView);
                mKeyboardView.setPreviewEnabled(true);
                mKeyboardTipsTextView.setVisibility(View.VISIBLE);
                mAbcKeyboard = new Keyboard(mContext, R.xml.symbols_abc);
                setPPKeyBoard(mAbcKeyboard, KeyBoardDefinition.KEYBOARD_TYPE_ABC);
                break;

            case KeyBoardDefinition.INPUTTYPE_SYMBOL:
                initKeyBoard(R.id.abc_sym_keyboardView);
                mKeyboardView.setPreviewEnabled(true);
                mKeyboardTipsTextView.setVisibility(View.VISIBLE);
                Keyboard symbolKeyboard = new Keyboard(mContext, R.xml.symbols_symbol);
                setPPKeyBoard(symbolKeyboard, KeyBoardDefinition.KEYBOARD_TYPE_SYMBOL);
                break;

        }
    }


    private void setPPKeyBoard(Keyboard newKeyboard, int type) {
        mKeyboardView.setPPKeyBoardType(newKeyboard, type);
        mKeyboardView.setKeyboard(newKeyboard);
    }

    //初始化滑动handler
    @SuppressLint("HandlerLeak")
    private void initScrollHandler(View rootView, ScrollView scrollView) {
        this.mMainScrollView = scrollView;
        this.mRootView = rootView;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mEditText == null)
                    return;
                if (msg.what == mEditText.getId()) {
                    if (mMainScrollView != null)
                        mMainScrollView.smoothScrollTo(0, mScrollTo);
                }
            }
        };
    }


    private boolean isWord(String str) {
        String wordStr = "abcdefghijklmnopqrstuvwxyz";
        return wordStr.contains(str.toLowerCase());
    }

    private void show(EditText editText) {
        this.mEditText = editText;
        if (mKeyBoardLayout != null) {
            mKeyBoardLayout.setVisibility(View.VISIBLE);
        }

        if (mKeyboardView != null) {
            mKeyboardView.setVisibility(View.GONE);
        }

        initInputType();
        mIsShow = true;
        mKeyboardView.setVisibility(View.VISIBLE);

        if (mKeyBoardStateChangeListener != null) {
            mKeyBoardStateChangeListener.KeyBoardStateChange(KeyBoardDefinition.KEYBOARD_SHOW, editText);
        }
        //用于滑动
        if (mScrollTo >= 0 && null != mMainScrollView) {
            keyBoardScroll(editText, mScrollTo);
        }
    }

    private class KeyBoardMap {
        public EditText editText;
        public int keyType;
        public int scrollTo;
    }

}
