package leorchn.lib;

import android.content.*;
import android.net.*;
import android.os.Build;
import leorchn.App;

public class Sys{
	public static int apiLevel(){ return Build.VERSION.SDK_INT; }
	public static void openAppSelfSetting(){
		openAppSetting(App.getContext().getPackageName());
	}
	public static void openAppSetting(String packageName) {
		Context c=App.getContext();
        Intent i = new Intent();
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(apiLevel() >= 9){
            i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
            	.setData(Uri.fromParts("package", packageName, null));
		}else{
			i.setAction(Intent.ACTION_VIEW)
				.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
				.putExtra("com.android.settings.ApplicationPkgName", packageName);
		}
		c.startActivity(i);
	}
}
