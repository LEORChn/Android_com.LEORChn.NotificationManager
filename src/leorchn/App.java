package leorchn;

import android.app.*;
import android.content.*;
import leorchn.lib.*;
import static leorchn.lib.Activity1.*;

public class App extends Application implements Consts{
	public App(){
		super();
		c=getContext=this;
	}
	public void onCreate(){
		super.onCreate();
		pl("==== Application loaded");
		if(DEBUG) tip(string("当前正在调试模式运行\n",getApplicationInfo().loadLabel(getPackageManager())));
	}
	static Context c, getContext; //支持多种写法
	public static Context getContext(){return c;}
}

