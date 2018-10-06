package leorchn.lib;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.widget.*;
import leorchn.App;
/**	适用于 8 以上版本的 桌面快捷方式相关 功能兼容包
	能够在 8 版本进行编译。
	
	仍然需要改进的程序行为：
	1.在 send() 中，即使 BroadcastReceiver 参数是正确的，也可能无法达到预期效果
	
	所用的全部高版本类：
	25 android.content.pm.ShortcutInfo.Builder
	25 android.content.pm.ShortcutManager
	26 以及该类的方法 isRequestPinShortcutSupported
	23 android.content.Context 的方法 getSystemService(java.lang.Class)
	25 以及该类的常量 SHORTCUT
	23 android.graphics.drawable.Icon
	
	通常建议的操作步骤：
	1.使用 check() 进行初始化，通常只有返回 STAT_OK 时，才建议继续操作
	2.在创建快捷方式之前，需要准备一个 Bitmap 参数作为图标，如果你只有一个 ImageView，需要先使用 ImageView2Bitmap() 进行转换
	3.使用 send() 创建固定的快捷方式。参数有 String 的快捷方式名称、Bitmap 的快捷方式图标，以及 Intent 的延时意图
	4.如果是由桌面程序进行启动的，使用 generate() 返回给桌面程序来进行创建。参数同上，但是新增的 boolean 参数必须设为 true
*/
public class ShortcutManagerCompat {
	static Context c=App.getContext();
	static Invoker shortcutManager, builder;
	static boolean isSystemServiceInstanced;
	public static final int STAT_OK=0,
		STAT_LAUNCHER_NOT_SUPPORT=1,
		STAT_PERMISSION_NOT_GRANT=2,
		STAT_SECURETY_EXCEPTION=4;
	public static Throwable LASTEST_EXCEPTION;
	public static boolean isNewApiEnable(){
		return Build.VERSION.SDK_INT>=26;
	}
	public static boolean isPermissionGranted(){
		PackageManager pm=c.getPackageManager();
		return pm.checkPermission(App.PERMISSION_NAME_INSTALL_SHORTCUT,c.getPackageName())==PackageManager.PERMISSION_GRANTED;
	}
	public static int check(){
		if(!isPermissionGranted()) return STAT_PERMISSION_NOT_GRANT;
		if(isNewApiEnable()){
			try{
				if(!isSystemServiceInstanced){
					shortcutManager=new Invoker("android.content.pm.ShortcutManager"); // api 25
					builder=new Invoker("android.content.pm.ShortcutInfo$Builder"); // api 25
					Invoker android_content_Context=new Invoker(Context.class);
					android_content_Context.setCompatInstance(c);
					shortcutManager.setInstance(
						android_content_Context
							.findMethod("getSystemService",Class.class)
							.invoke(shortcutManager.Class()));
				}
				boolean isLauncherSupported=shortcutManager.invoke("isRequestPinShortcutSupported"); // api 26
				isSystemServiceInstanced=true;
				if(!isLauncherSupported)
					return STAT_LAUNCHER_NOT_SUPPORT;
			}catch(Throwable e){
				LASTEST_EXCEPTION=e;
				return STAT_SECURETY_EXCEPTION;
			}
		}
		return STAT_OK;
	}
	public static boolean send(String name,Bitmap icon,Intent pending){
		return send(name, icon, pending, null);
	}
	public static boolean send(String name,Bitmap icon,Intent pending,BroadcastReceiver newApiNeeds){
		if(!isPermissionGranted()) return false;
		if(isNewApiEnable()){
			Parcelable si=generate(name,icon,pending).getParcelableExtra(EXTRA_NEW_API_OBJECT_SHORTCUTINFO);
			PendingIntent callback=PendingIntent.getBroadcast(c,0,
				new Intent(c,newApiNeeds.getClass()),
				PendingIntent.FLAG_UPDATE_CURRENT);
			return (boolean)shortcutManager.invoke("requestPinShortcut",si,callback.getIntentSender()); // api 26
		}else{
			c.sendBroadcast(generate(name,icon,pending,true));
			return true;
		}
	}
	static final String EXTRA_NEW_API_OBJECT_SHORTCUTINFO="object_shortcutinfo";
	public static Bitmap ImageView2Bitmap(ImageView v){
		return ((BitmapDrawable)v.getDrawable()).getBitmap();
	}
	public static Intent generate(String name,Bitmap icon,Intent pending){
		return generate(name,icon,pending,false);
	}
	public static Intent generate(String name,Bitmap icon,Intent pending,boolean createrIsDesktop){
		Intent i=null;
		if(isNewApiEnable() && !createrIsDesktop){
			try{
				Invoker android_graphics_drawable_Icon=null;
				android_graphics_drawable_Icon=new Invoker("android.graphics.drawable.Icon"); // api 23
				builder.newInstance(Context.class,String.class)
					.invoke(c,"id");
				builder.findMethod("setIcon",android_graphics_drawable_Icon.Class())
					.invoke(
						android_graphics_drawable_Icon
							.findMethod("createWithBitmap",Bitmap.class)
							.invoke(icon));
				builder.findMethod("setShortLabel",CharSequence.class)
					.invoke(name);
				builder.findMethod("setIntent",Intent.class)
					.invoke(pending);
				Object inf=builder.invoke("build");
				i=new Intent().putExtra(EXTRA_NEW_API_OBJECT_SHORTCUTINFO,(Parcelable)inf);
			}catch(Throwable e){
				LASTEST_EXCEPTION=e;
			}
		}else{
			i=new Intent("com.android.launcher.action.INSTALL_SHORTCUT")
				.putExtra(Intent.EXTRA_SHORTCUT_NAME,name)
				.putExtra(Intent.EXTRA_SHORTCUT_ICON,icon)
				.putExtra(Intent.EXTRA_SHORTCUT_INTENT,pending);
		}
		return i;
	}
}
