# MoveKeyboard
android 自定义键盘，在页面嵌入的。使用还算简单
## 使用简介
###1.布局需要
布局需要参考以下形式进行布局，主要因为自定义的键盘是在页面的最底下占位置，模拟键盘弹出来的，不同于输入法的做法。
``` xml
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
###2.AndroidMainfest.xml 声明需要
一般来说，如果页面只有一个EditText, 而这个EditText需要使用自定义键盘，我们就需要设置默认系统键盘隐藏。
其他情况，按照需要弹系统键盘与否。
``` xml
       <activity
            ...
            android:windowSoftInputMode="adjustResize|stateHidden" >
        </activity>
```
###3.代码调用
* **必要设置方法,在Activity 或者 View 创建的时候，进行设置**
     
     A. 构造方法（必要）
                
              //Activity,相关界面都是使用这个
            keyboardUtil = new KeyboardUtil(testActivity.this, rootView, sv_main);
              //弹框类型的
            keyboardUtil = new KeyboardUtil(view,context,rootView,sv_main);
  
    B. 设置同一个页面，不使用自定义键盘EditText（不完全必要）
        主要为了解决来回切换系统键盘还有自定义键盘的问题而定，如果这个页面没有使用系统键盘的EditText,  这个就不是必要的。
          
           keyboardUtil.setOtherEdittext(et_mobile,et_code,...);//有多少个设置多少个EditText

    C. 设置OnTouchListener,通过监听，来弹出键盘（必要）
    
        
         /** 有多少个EditText使用到自定义键盘的，就需要设置多少个监听
         *   KeyboardTouchListener(KeyboardUtil util,int keyboardType,int scrollTo)  ,需要三个参数
         *   第一个是刚创建的KeyBoardUtil,  第二个是键盘的类型，第三个参数是是否滑动都某个位置
         *  /
         your_editText . setOnTouchListener(new KeyboardTouchListener(keyboardUtil, KeyboardUtil.INPUTTYPE_ABC, -1));


* **按需要设置方法,都不是必要的**
    
    A. 监听数字键盘又下角的变化（完成，下一项），监听变化，做出相应页面需要的处理

        //设置监听事件，主要是只有数字键盘时候需要。
		keyboardUtil.setInputOverListener(new InputListener());
			
			// 监听变化需要,用于监听点击下一项，完成监听
	       class InputListener implements KeyboardUtil.InputFinishListener {
		        @Override
	 	            public void inputHasOver(int onClickType, EditText editText) {
	 	                 //监听操作
		         }
		}

   B. 弹出键盘。如果页面一进来就需要，把自定义键盘弹出来，或者从某个页面返回来，需要弹出键盘，可以自己手动把键盘显示出来，可以用以下方法：（参数和必要中的C点，OnTouch一样）

        keyboardUtil.showKeyBoardLayout(cash_out_et_amount
				,KeyboardUtil.INPUTTYPE_NUM_POINT,-1));

   
   C. 监听键盘的状态（弹出，隐藏）
   
        keyboardUtil1.setKeyBoardStateChangeListener(new keyboardChangeLister());

        	//监听键盘开关
	    class keyboardChangeLister implements KeyboardUtil.KeyBoardStateChangeListener{
		@Override
		public void KeyBoardStateChange(int state,EditText editText) {
			switch (state){
				case KeyboardUtil.KEYBOARD_SHOW:
					Log.i("keyboradState",KeyboardUtil.KEYBOARD_SHOW+"");
					break;
				case KeyboardUtil.KEYBOARD_HIDE:
					Log.i("keyboradState",KeyboardUtil.KEYBOARD_HIDE+"");
					break;
			}
		}}

    D. 隐藏键盘。一般可以在onPause(),还有按手机的实体BackKey的时候，隐藏自定义键盘使用。

           //必要时候需要调用隐藏键盘的
        keyboardUtil.hideKeyboardLayout();
