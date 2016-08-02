package xuan.movekeyboard.ppkeyboard;

/**
 * 自定义键盘类型
 *
 * @author xuanweijian
 * @create 2016/7/25 18:10.
 */

public class KeyBoardDefinition {

    //键盘类型
    public static final int INPUTTYPE_NUM = 1; // 数字，右下角 为空
    public static final int INPUTTYPE_NUM_FINISH = 2;// 数字，右下角 完成
    public static final int INPUTTYPE_NUM_POINT = 3; // 数字，右下角 为点
    public static final int INPUTTYPE_NUM_X = 4; // 数字，右下角 为X
    public static final int INPUTTYPE_NUM_NEXT = 5; // 数字，右下角 为下一个

    public static final int INPUTTYPE_ABC = 6;// 一般的abc
    public static final int INPUTTYPE_SYMBOL = 7;// 标点键盘
    public static final int INPUTTYPE_NUM_ABC = 8; // 数字，右下角 为下一个

    //数字键盘右键类型类型
    public static final int NUM_KEYBOARD_RIGHT_TYPE_ZERO = 9; //0 数字键盘，右下角类型
    public static final int NUM_KEYBOARD_RIGHT_TYPE_X = 10; //X
    public static final int NUM_KEYBOARD_RIGHT_TYPE_POINT = 11; //点
    public static final int NUM_KEYBOARD_RIGHT_TYPE_FINISH = 12; //完成
    public static final int NUM_KEYBOARD_RIGHT_TYPE_NEXT = 13; //下一项

    //键盘一下特殊 CODE 代码
    public static final int CODE_DELETE = -5;
    public static final int CODE_PULL = -3;
    public static final int CODE_SHIFT = -1;
    public static final int CODE_CHANGE_123 = 123123;
    public static final int CODE_CHANGE_SYMBOL = 789789;
    public static final int CODE_SPACE = 32;
    public static final int CODE_EMPTY = 0;
    public static final int CODE_CHANGE_ABC = 456456;
    public static final int CODE_ABC_2 = 741741;
    public static final int CODE_X = 88;
    public static final int CODE_POINT = 46;
    public static final int CODE_FINISH = -4;

    //键盘整体类型，数字，abc,标点
    public static final int KEYBOARD_TYPE_ABC = 100; //abc类型
    public static final int KEYBOARD_TYPE_SYMBOL = 101; //标点类型
    public static final int KEYBOARD_TYPE_NUM = 102; //数字类型

    //键盘状态
    public static final int KEYBOARD_SHOW = 103;//键盘显示状态
    public static final int KEYBOARD_HIDE = 104;

}
