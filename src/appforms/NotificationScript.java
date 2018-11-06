package appforms;
import android.app.Notification;
import android.content.pm.*;
import android.service.notification.StatusBarNotification;
import android.text.*;
import appforms.*;
import java.util.*;
import leorchn.App;
import leorchn.lib.*;
import static leorchn.lib.Activity1.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

public class NotificationScript implements SharedConsts{
	static String FILE_PATH=string(DIR_data,"/module/voice/script/*.lua"),
	FILE_DEFAULT="voice_script_default.lua";
	static PackageManager pm=App.getContext().getPackageManager();
	public static String genPath(String pkg){
		return FILE_PATH.replace("*", pkg);
	}
	public static void exec(StatusBarNotification sbn){
		String pkg=sbn.getPackageName(),
			path=genPath(pkg);
		boolean isConstomEnviron = new java.io.File(path).exists();
		Globals g = isConstomEnviron? getReuseable(sbn.getPackageName()): ENVIRONMENT_DEFAULT;
		try{
			if(g == null){
				g = JsePlatform.standardGlobals();
				g.load(Text.read(path)).call();
			}
			setReuseableUsed(pkg, g);

			Notification n=sbn.getNotification();
			LuaValue t=new LuaTable();
			CharSequence chr = pm.getApplicationInfo(sbn.getPackageName(),0).loadLabel(pm);
			if(!TextUtils.isEmpty(chr))
				t.set("name", chr.toString());
			if(!TextUtils.isEmpty(chr = n.tickerText))
				t.set("ticker", chr.toString());
			if(!TextUtils.isEmpty(chr = n.extras.getString(n.EXTRA_TITLE)))
				t.set("title", (String)chr);
			if(!TextUtils.isEmpty(chr = n.extras.getString(n.EXTRA_TEXT)))
				t.set("desc", (String)chr);
			t.set("pri", n.priority);

			String s=g.get("main").call(t).toString();

			TTS[]ts=TTS.getSupportedList();
			TTS.play(ts[(int)data.get(TYPE.VOICE_API_CHOICE)], s);
			Text.write(string(s),
				string(DIR_cache_log,System.currentTimeMillis(),"_voice_success.log"));
		}catch(Throwable e){
			Text.write(E.trace(e),string(DIR_cache_log,System.currentTimeMillis(),"_voice_fail.log"));
		}
	}
	/*	脚本环境管理区
	 通常内存只允许5个脚本环境，包含4个自定义环境和1个默认环境
	 */
	static LinkedHashMap<String,Globals>hm=new LinkedHashMap<>();
	static Globals ENVIRONMENT_DEFAULT = initDefaultEnvironment();
	static Globals initDefaultEnvironment(){
		Globals g = JsePlatform.standardGlobals();
		g.load(Text.read(FILE_DEFAULT)).call();
		return g;
	}
	static Globals getReuseable(String pkg){
		return hm.containsKey(pkg)? hm.get(pkg): null;
	}
	/*	setReuseable
	 添加自定脚本环境到内存，把使用频率较少的自定脚本环境挤出内存
	 */
	static void setReuseableUsed(String pkg, Globals g){
		if(hm.containsKey(pkg))
			hm.remove(pkg); // 删除然后重新添加，以防止经常复用的脚本被挤出内存队列
		while(hm.size()>=4) // 把排在队列底部的不经常用的脚本挤出内存
			hm.remove(
				new ArrayList<Map.Entry<String, Globals>>(hm.entrySet())
				.listIterator()
				.next()
				.getKey());
		hm.put(pkg, g); // 新添加，或者重新添加到队列顶部
	}
	static void removeEnvironment(String pkg){
		if(hm.containsKey(pkg))
			hm.remove(pkg);
	}
}
