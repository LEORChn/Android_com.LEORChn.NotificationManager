package appforms;
import leorchn.lib.*;
import android.content.*;
import android.view.*;
import java.io.*;
/*	需要布局文件（在本类定义后示例）
	- 需要权限
	<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
	- 需要定义
	<activity android:name="appforms.Perm_FloatWindow" />
	-- 使用方法
	- 检测
	FloatWindowTest.isPermissionGranted() // 只要这个也行
	if(!FloatWindowTest.isPermissionGranted()) startActivityForResult(FloatWindowTest.class,1);//数字任意指定
 	- 事件
	@Override protected void onActivityResult(int reqCode,int resCode,Intent data){
		if(reqCode==1)
			switch(resCode){
				case 0:break;//成功
				case 1://用户取消
			}
		super.onActivityResult(reqCode, resCode, data);
	}
*/
public class Perm_FloatWindow extends Activity1{
    @Override protected void oncreate() {
		this.setResult(RESULT_CANCELED);
    }
	@Override protected boolean onIdle(){
        setContentView(layout.activity_floatwindowtest);
		btnbind(id.fWindowTest_startTest,
				id.fWindowTest_remove,
				id.fWindowTest_help,
				id.fWindowTest_samplecheck);
		sample=fv(id.fWindowTest_samplemain);
		((ViewGroup)sample.getParent()).removeView(sample);
		sample.setVisibility(View.VISIBLE);
		if(isPermissionGranted()) fv(id.fWindowTest_remove).setVisibility(View.VISIBLE);
		return false;
	}
	View sample;
	@Override public void onClick(View v){
		switch(v.getId()){
			case id.fWindowTest_startTest:
				showFloatWindow(sample);
				break;
			case id.fWindowTest_remove:
				new Msgbox("取消悬浮窗授权","如要取消授权，一般是点击下方的帮助选项，然后执行反向行为即可。","ok");
				break;
			case id.fWindowTest_help:
				new Msgbox("如何开启悬浮窗","一般就是在系统设置或者手机自带的管家软件里调整设置。还不会的可以帮你百度一下。","百度一下","确定","系统设置"){
					@Override protected void onClick(int i){
						if(i==vbyes){
							openurl("https://www.baidu.com/s?wd=%e5%a6%82%e4%bd%95%e6%89%93%e5%bc%80%e6%82%ac%e6%b5%ae%e7%aa%97");
						}else if(i==vbmid){
							Sys.openAppSelfSetting();
						}
					}
				};
				break;
			case id.fWindowTest_samplecheck:
				this.setResult(RESULT_OK);
				tip("测试成功！\n\n看来本程序已经获得悬浮窗权限");
				setPermissionGranted();
				finish();
		}
	}
	@Override protected void onStop() {
		removeFloatWindow(sample);
		super.onStop();
	}
	static final String passfile="/module/floatwindow.key";
	void setPermissionGranted(){
		try{
			new File(string(getFilesDir().getPath(),passfile)).mkdirs();
		}catch(Throwable e){}
	}
	public static boolean isPermissionGranted(){
		return new File(string(leorchn.App.getContext().getFilesDir().getPath(),passfile)).exists();
	}
	static WindowManager.LayoutParams lp;
	static android.graphics.PixelFormat pf;
	protected boolean showFloatWindow(View v){return showFloatWindow(this,v,false);}
	public static boolean showFloatWindow(Context c,View v,boolean receiveBackKey){
		WindowManager m=(WindowManager)c.getSystemService(Context.WINDOW_SERVICE);
		int api=Sys.apiLevel(),
			wflag=0,
			wtype=0,
			w=lp.WRAP_CONTENT,
			h=lp.WRAP_CONTENT;
		wflag|=receiveBackKey?lp.FLAG_ALT_FOCUSABLE_IM:lp.FLAG_NOT_FOCUSABLE;
		wtype= false && api>=19 && api<=24? //强行阻止伪装Toast
			lp.TYPE_TOAST: //19-24
			lp.TYPE_SYSTEM_ALERT; //ROM种类繁多过于复杂，不管怎么样还是建议用这个
		WindowManager.LayoutParams wp=new WindowManager.LayoutParams(w,h,wtype,wflag,pf.TRANSLUCENT);
		try{
			m.addView(v,wp);
			return true;
		}catch(Throwable e){
			return false;
		}
	}
	/* TYPE_TOAST 建议api19到24使用
	 * TYPE_PHONE 建议api不足19或超过24使用
	 * TYPE_SYSTEM_ALERT 建议走投无路使用
	 */
	protected boolean removeFloatWindow(View v){return removeFloatWindow(this,v);}
	public static boolean removeFloatWindow(Context c,View v){
		WindowManager m=(WindowManager)c.getSystemService(Context.WINDOW_SERVICE);
		try{
			m.removeView(v);
			return true;
		}catch(Throwable e){
			return false;
		}
	}
}
/* res/layout/activity_floatwindowtest.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
	xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="fill_parent"
	android:layout_height="fill_parent"
	android:orientation="vertical"
	android:padding="15dp">

	<TextView
		android:layout_height="wrap_content"
		android:layout_width="wrap_content"
		android:text="悬浮窗测试"/>

	<View
		android:background="?android:attr/dividerVertical"
		android:layout_height="1dp"
		android:layout_width="match_parent"/>

	<LinearLayout
		android:layout_height="wrap_content"
		android:layout_width="wrap_content"
		android:orientation="vertical"
		android:padding="10dp">

		<TextView
			android:layout_height="wrap_content"
			android:textAppearance="?android:attr/textAppearanceMedium"
			android:layout_width="wrap_content"
			android:text="要使用相关功能，需要开启悬浮窗。"/>

		<TextView
			android:layout_height="wrap_content"
			android:textAppearance="?android:attr/textAppearanceMedium"
			android:layout_width="wrap_content"
			android:text="请点击下方 “测试悬浮窗” 尝试弹出悬浮窗，然后点击悬浮窗中的 “完成测试” 以继续使用相关功能。"/>

	</LinearLayout>

	<LinearLayout
		android:layout_height="wrap_content"
		android:layout_width="match_parent"
		android:orientation="vertical"
		android:layout_weight="1.0"
		android:gravity="bottom">

		<LinearLayout
			android:orientation="vertical"
			android:layout_width="wrap_content"
			android:layout_height="wrap_content"
			android:background="#000000"
			android:layout_margin="20dp"
			android:id="@+id/fWindowTest_samplemain"
			android:visibility="gone">

			<TextView
				android:layout_width="wrap_content"
				android:layout_height="wrap_content"
				android:text="悬浮窗"
				android:textAppearance="?android:attr/textAppearanceLarge"
				android:textColor="#33B5E5"
				android:layout_margin="10dp"/>

			<View
				android:layout_width="match_parent"
				android:layout_height="3dp"
				android:background="#33B5E5"/>

			<TextView
				android:layout_width="wrap_content"
				android:layout_height="wrap_content"
				android:text="正在测试悬浮窗。请点击“完成测试”。"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:layout_margin="10dp"/>

			<View
				android:background="?android:attr/dividerVertical"
				android:layout_width="match_parent"
				android:layout_height="1dp"
				android:layout_marginTop="10dp"/>

			<Button
				android:layout_width="match_parent"
				style="?android:attr/buttonBarButtonStyle"
				android:layout_height="wrap_content"
				android:text="完成测试"
				android:id="@+id/fWindowTest_samplecheck"/>

		</LinearLayout>

		<Button
			android:layout_height="wrap_content"
			android:layout_width="match_parent"
			android:text="测试悬浮窗"
			android:id="@+id/fWindowTest_startTest"/>

		<Button
			android:layout_height="wrap_content"
			android:layout_width="match_parent"
			android:text="我遇到问题，求帮助"
			android:id="@+id/fWindowTest_help"/>

	</LinearLayout>

</LinearLayout>
*/
