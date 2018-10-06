package appforms;
import leorchn.lib.*;
import static leorchn.lib.Activity1.*;
import android.service.notification.*;
import android.app.*;
import android.os.*;
import android.content.pm.*;
import android.content.*;

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
		PowerManager pow=(PowerManager)getSystemService(POWER_SERVICE);
		if(!LISTENING_APPS.contains(pkg)) return; // 发送新通知的包名不在监听范围内
		do{
			if(!(boolean)data.get(TYPE.VOICE_ON_SCREEN_OFF)) break; // 未开启锁屏播报，退出
			if(!DEBUG && pow.isScreenOn()) break; // 检测未锁屏，退出。（测试阶段暂不开启）
			if(DISABLED_VOICE_APPS.contains(pkg)) break; // 包名是排除播报的成员之一，退出
			
		}while(false);
		
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
}
