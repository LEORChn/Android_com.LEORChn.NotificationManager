package leorchn.lib;

import com.LEORChn.NotificationManager.*;

public interface Consts{
	public static boolean DEBUG=BuildConfig.DEBUG || new java.io.File("/sdcard/Android/NM_DEBUG").exists();
	//protected static Icon icon;
	public static R.id id;
	public static R.layout layout;
	public static R.drawable drawable,draw;//可以给 R 类定义快捷方式和多个别名而且不用额外声明，超爽
	public static R.color color;
	//public static R.menu menu;
	public static R.string string;
	//public static R.array array;
	
	public static final String
		PERMISSION_NAME_INSTALL_SHORTCUT="com.android.launcher.permission.INSTALL_SHORTCUT",
		ACTION_IMAGE_CROP="com.android.camera.action.CROP";
}
