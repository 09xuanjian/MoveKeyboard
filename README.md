# MoveKeyboard
>更新了一版，主要优化了一下，使用键盘更加方便。


页面接键盘，主要注意，重点是布局要求，
键盘可以嵌入在activity里面，也可以嵌入到某个View里面
#### 1.布局要求：
```xml
<LinearLayout>
    <头部Title的Layout></>
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"   重点
        android:layout_weight="1"     重点
        android:scrollbars="none">
        < 你的主要布局 ></>
    </ScrollView>
</LinearLayout>
```

#### 2.AndroidManifest.xml 声明需要
一般来说，如果第一个EditText就需要使用自定义键盘，我们就需要设置默认系统键盘隐藏。 其他情况，按照需要弹系统键盘与否。
```xml
        <activity
            ...
            android:windowSoftInputMode="stateHidden" >
        </activity>
```
#### 3.代码中使用
###### 1.初始化
```java
mKeyBoardManage = new KeyboardManage(this, rootLLayout, scrollView)
        .setOtherEditText(smsCodeEditText, smsCodeHasPhoneEditText) 
        .setKeyBoardStateChangeListener(new keyboardChangeLister())
        .setInputOverListener(new InputListener())
        .addEditText(loginPasswordEditText, KeyBoardDefinition.INPUTTYPE_ABC)
        .addEditText(loginPhoneEditText, KeyBoardDefinition.INPUTTYPE_NUM, 0, false);
        
        
/**1.参数：this(上下文,必要)，rootLLayout(外层布局Linearlayout，必要), scrollView（滑动的布局，非必要）
*  2.setOtherEditText  设置页面中不使用自定义键盘的edittext。（页面有使用键盘才是必要的。）
*  3.setKeyBoardStateChangeListener  监听键盘关和开（非必要）
*  4.setInputOverListener  监听完成按键或下一项（非必要）
*  5.addEditText （使用自定义键盘的EditText（必要）, 键盘类型（必要）， 滑动距离（非必要），是否进页面就显示（非必要）） ，必要
*/
```
##### 2.手动显示键盘（必要时候调用，一般不需要）
```java
//如果已经已经addEditText,在需要时候直接显示
mLoginKeyBoardManage.showKeyBoard(loginPhoneEditText);

//如果前面没有进行过 addEditText的操作，可以用这个
mLoginKeyBoardManage.showKeyBoardLayout(loginPhoneEditText, KeyBoardDefinition.INPUTTYPE_NUM, 0);
```
##### 3.隐藏键盘（必要时候调用，一般不需要）
```java
mLoginKeyBoardManage.hideKeyboardLayout(); //隐藏自定义键盘
mLoginKeyBoardManage.hideAllKeyBoard(); //可以同时隐藏系统键盘还有自定义键盘
```