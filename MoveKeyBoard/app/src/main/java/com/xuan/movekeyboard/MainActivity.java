package com.xuan.movekeyboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {

    private LinearLayout rootView;
    private ScrollView scrollView;

    private EditText normalEd;
    private EditText specialEd;

    private KeyboardUtil keyboardUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = (LinearLayout) findViewById(R.id.root_view);
        scrollView = (ScrollView) findViewById(R.id.sv_main);

        normalEd = (EditText) findViewById(R.id.normal_ed);
        specialEd = (EditText) findViewById(R.id.special_ed);

        initMoveKeyBoard();

    }

    private void initMoveKeyBoard(){
        keyboardUtil = new KeyboardUtil(this,rootView,scrollView);
        keyboardUtil.setOtherEdittext(normalEd);
        // monitor the KeyBarod state
        keyboardUtil.setKeyBoardStateChangeListener(new KeyBoardStateListener());
        // monitor the finish or next Key
        keyboardUtil.setInputOverListener(new inputOverListener());
        specialEd.setOnTouchListener(new KeyboardTouchListener(keyboardUtil,KeyboardUtil.INPUTTYPE_ABC,-1));
    }

    class KeyBoardStateListener implements KeyboardUtil.KeyBoardStateChangeListener{

        @Override
        public void KeyBoardStateChange(int state, EditText editText) {

        }
    }

    class inputOverListener implements KeyboardUtil.InputFinishListener{

        @Override
        public void inputHasOver(int onclickType, EditText editText) {

        }
    }
}
