package appforms;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.service.notification.*;
import android.view.*;
import leorchn.lib.*;

import static leorchn.lib.Activity1.*;

public class Listener extends NotificationListenerService implements Consts, SharedConsts{
	@Override public void onCreate() {
		super.onCreate();
	}
	@Override public int onStartCommand(Intent intent, int flags, int startId) {
		tip("小瑞通知管理 已启动");
		Main.initGlobalConfig();
		return super.onStartCommand(intent, flags, startId);
	}

	public void onListenerConnected(){ // api 21
		tip("监听器已连接");
	}
	public void onListenerDisconnected(){ // api 24
		tip("监听器已断开");
	}
	
	int total;
	@Override public void onNotificationPosted(StatusBarNotification sbn){
		if((boolean)data.get(TYPE.PAUSE)) return; // 全局暂停使用
		String pkg=sbn.getPackageName();
		//PowerManager pow=(PowerManager)getSystemService(POWER_SERVICE);
		if(!LISTENING_APPS.contains(pkg)) return; // 发送新通知的包名不在监听范围内
		
		do{ // 语音功能块
			if(!(boolean)data.get(TYPE.VOICE_ON_SCREEN_OFF)) break; // 未开启锁屏播报，退出
			if(!DEBUG) if(isScreenOn()) break; // 如果在非调试模式，检测到未锁屏则不播报
			if((boolean)data.get(TYPE.VOICE_STAT_DEFAULT)){ // 检测语音功能对所有包的默认状态
				if(DISABLED_VOICE_APPS.contains(pkg)) break; // 虽然默认开启，但是是强制禁用播报的成员，退出
			}else if(!ENABLED_VOICE_APPS.contains(pkg)) break; // 默认关闭，而且不是强制启用播报的成员，退出
			NotificationScript.exec(sbn);
		}while(false);
		
		if(!DEBUG) return;
		FSON j=new FSON("{}");
		Notification n=sbn.getNotification();
		j.set("ticker",n.tickerText);
		j.set("flags",n.flags);
		j.set("priority",n.priority);
		j.set("defaults",n.defaults);
		//j.set("category",n.category);
		j.set("when",n.when);
		j.set("number",n.number);
		j.set("iconLevel",n.iconLevel);
		j.set("sound",n.sound==null?null:n.sound.toString());
		j.set("audioStreamType",n.audioStreamType);
		j.set("extras",generateExtras(n.extras));
		PackageManager pm=getPackageManager();
		CharSequence appname=null;
		try{
			appname=pm.getPackageInfo(sbn.getPackageName(),0).applicationInfo.loadLabel(pm);
		}catch(Throwable e){}
		Text.write(j.toString(),string(DIR_cache_log,System.currentTimeMillis(),"_",appname==null?sbn.getPackageName():appname,".log"));

		total++;
		String title=(String)n.extras.get(b.EXTRA_TITLE);
		if(title==null || !title.contains("选择输入法"))
		if(DEBUG) multip("收到新通知 累计 "+total);
		//super.onNotificationPosted(sbn); // 父类的该方法似乎是抽象的，并未实现
	}
	int remTotal;
	@Override public void onNotificationRemoved(StatusBarNotification sbn) {
		remTotal++;
		if(DEBUG) multip("通知被移除 累计 "+remTotal);
		//super.onNotificationRemoved(sbn); // 父类的该方法似乎是抽象的，并未实现
	}
	static Notification b;
	FSON generateExtras(Bundle n){
		FSON j=new FSON("{}");
		j.set("title",n.get(b.EXTRA_TITLE));
		j.set("title_big",n.get(b.EXTRA_BIG_TEXT));
		j.set("text",n.get(b.EXTRA_TEXT));
		j.set("text_sub",n.get(b.EXTRA_SUB_TEXT));
		j.set("text_info",n.get(b.EXTRA_INFO_TEXT));
		j.set("text_summary",n.get(b.EXTRA_SUMMARY_TEXT));
		j.set("text_lines",n.get(b.EXTRA_TEXT_LINES));
		j.set("progress",n.get(b.EXTRA_PROGRESS));
		j.set("progress_max",n.get(b.EXTRA_PROGRESS_MAX));
		j.set("progress_unsure",n.get(b.EXTRA_PROGRESS_INDETERMINATE));
		j.set("people",n.get(b.EXTRA_PEOPLE));
		return j;
	}
//----- 绑定系统服务相关
	NotiBinder nb=new NotiBinder();
	@Override public IBinder onBind(Intent intent) {
		//tip("服务已绑定");
		//startService(new Intent(this,Listener.class));
		/*int i=0;
		StatusBarNotification[]sbns=getActiveNotifications();
		if(sbns != null)
			for(StatusBarNotification sbn:sbns){
				Notification n=sbn.getNotification();
				tip("[",i,"]=",n==null?"null":n.extras.get("title"));
				i++;
			}*/
		return super.onBind(intent);
	}
	class NotiBinder extends Binder{
		
	}
	public static ServiceConnection getServiceConnection(){
		return sctn;
	}
	static ServiceConnection sctn=new ServiceConnection(){
		@Override public void onServiceConnected(ComponentName p1, IBinder p2) {
			//tip("服务已连接");
		}
		@Override public void onServiceDisconnected(ComponentName p1) {
			//tip("服务断开连接");
		}
	};
	boolean isScreenOn(){
		if(Sys.apiLevel() >= 20){
			WindowManager wm=(WindowManager) getSystemService(WINDOW_SERVICE);
			Display d = wm.getDefaultDisplay();
			switch(d.getState()){
				case Display.STATE_OFF: // 1 api 20
				case Display.STATE_DOZE: // 3 api 21
				case Display.STATE_DOZE_SUSPEND: // 4 api 21
				case 6: // STATE_ON_SUSPEND api 28
					return false;
				case Display.STATE_ON: // 2 api 20
					return true;
				default:
					return false;
			}
		}else{
			PowerManager pow=(PowerManager)getSystemService(POWER_SERVICE);
			return pow.isScreenOn();
		}
	}
}
