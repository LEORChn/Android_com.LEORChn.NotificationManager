package appforms;
import android.content.*;
import android.provider.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;
import leorchn.lib.*;
/*	猜测到该窗口有一个面向低 sdk level 的隐患
	在二次加载这个窗口的时候，hasinit 可能不是 0
	这会导致 onResume 时回到 onIdle 并从阶段 1 开始，而不是从阶段 0 开始
	然后跳过 sdk level 的检查，并在低 sdk 上发生错误
	但是真的会这么发生吗？
	还是说仅仅只是低概率事件？
	修复该问题的方法是把 onResume 里的 hasinit=1 改为 0
	但是我想验证这个问题是否真的会发生
*/
public class Main extends BottomActivity implements SharedConsts, CompoundButton.OnCheckedChangeListener{
	@Override protected void oncreate() {

	}
	int hasinit;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0: // 检测环境
				if(Sys.apiLevel()<18){
					new Msgbox("系统初始化失败","由于系统限制，本软件目前只能在 安卓4.3（sdk18）以上系统运行。\n若忽略本提示而强制运行，可能造成系统不稳定或崩溃。","退出运行","忽略"){
						@Override protected void onClick(int i){
							if(i==vbyes) finish();
							else addIdleHandler();
						}
					};
					hasinit++;
					return false;
				}
				//new Msgbox("测试版本 2 任务","启用语音开关，测试在锁屏时是否还能念出消息。","ok");
				break;
			case 1: // 加载组件
				setContentView(layout.activity_main_set);
				break;
			case 2: // 绑定组件事件
				perm_read=(CheckBox)fv(id.main_perm_read);
				perm_window=(CheckBox)fv(id.main_perm_window);
				btnbind(perm_read, perm_window);
				btnbind(id.main_app_sets, id.main_extra_voice_sets, id.main_save,
					id.main_donate, id.main_report,
					id.main_launch_monitor, id.main_launch_log, id.main_get_entry, id.main_launch_console);
				app_mms=(CheckBox)fv(id.main_app_sms);
				app_wechat=(CheckBox)fv(id.main_app_wechat);
				app_qq=(CheckBox)fv(id.main_app_qq);
				extra_pause=(CheckBox)fv(id.main_extra_pause);
				extra_voice=(CheckBox)fv(id.main_extra_voice);
				debugmode=(CheckBox)fv(id.main_debug_mode);
				chkboxbind(app_mms, app_wechat, app_qq, extra_pause, extra_voice, debugmode);
				break;
			case 3: // 加载数据到组件
				initGlobalConfig();
				
				onResume(); // 可能在切换窗口后更新配置，重新把内存中的配置载入到界面
				
				loadVersionOnTitle();
				loadDonateList();
				break;
			case 4: // 检查开发者模式
				if(new File(PATH_FLAG_DEVELOPER).exists()){
					visible(fv(id.main_get_entry), false);
					visible(fv(id.main_launch_console), true);
				}
		}
		return hasinit++<9;
	}
	CheckBox perm_read = null, perm_window = null,
		app_mms, app_wechat, app_qq,
		extra_pause, extra_voice,
		debugmode;
	boolean isNotificationListenerGranted(){ // 返回 true 如果通知监听器已授权
		String s=Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
		return !TextUtils.isEmpty(s) && s.contains(Listener.class.getName());
	}
	@Override protected void onResume(){ // 确保反复进出窗口时能刷新 选择框的 授权信息
		super.onResume();
		if(hasinit==0)return;
		if(perm_read == null){ // 有时候这个是空的，试试重新加载界面
			hasinit=1;
			addIdleHandler();
			return;
		}
		debugmode.setChecked(DEBUG);
		boolean b=false;
		int retryTms=3;
		do{
			try{
				b=isNotificationListenerGranted(); // 这个不知道为啥老是返回给空指针
				if(b == true || b == false); else throw new NullPointerException();
				retryTms=0;
			}catch(Throwable e){
				pl("detected return null from isNotificationListenerGranted");
				retryTms--;
			}
		}while(retryTms>0);
		pl("is perm_read null? ",perm_read==null); // 有时候这个是空的
		perm_read.setChecked(b);
		if(b) startService(new Intent(this,Listener.class));
		perm_window.setChecked(Perm_FloatWindow.isPermissionGranted());
		loadMemoryToUi(); // 内存中已有配置，载入到界面
	}
	@Override public void onClick(View v){
		switch(v.getId()){
			case id.main_perm_read: // 每一次点击选择框都跳转到 系统授权页面
				bindService(new Intent(this,Listener.class),Listener.getServiceConnection(),BIND_AUTO_CREATE);
				startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
				break;
			case id.main_perm_window: // 每一次点击选择框都跳转到 悬浮窗测试页面
				startActivity(Perm_FloatWindow.class);
				break;
			case id.main_app_sets:
				startActivity(SettingAppChoose.class);
				break;
			case id.main_extra_voice_sets:
				startActivity(SettingVoice.class);
				break;
			case id.main_save:
				loadMemoryToFile();
				tip("保存成功");
				break;
			case id.main_donate:
				openurl("https://leorchn.github.io/#home/about.html");
				break;
			case id.main_report:
				try{
					getPackageManager().getPackageInfo("com.tencent.mobileqq",543).activities.toString();
					openurl("mqqwpa://im/chat?chat_type=wpa&uin=522237296");
				}catch(Throwable e){
					new Msgbox("找不到程序","需要以下程序才能运行：\nQQ","OK");
				}
				break;
			case id.main_launch_monitor:
				startService(new Intent(this, MemMonitor.class));
				break;
			case id.main_launch_log:
				startActivity(Logcat.class);
				break;
			case id.main_get_entry:
				tryEnterDeveloperMode();
				break;
			case id.main_launch_console:
				startActivity(ScriptConsole.class);
				break;
		}
	}
	@Override public void onCheckedChanged(CompoundButton v, boolean b){
		String pkg=null;
		switch(v.getId()){
			case id.main_app_sms:
				pkg = pkg == null? PKG.mms: pkg;
			case id.main_app_wechat:
				pkg = pkg == null? PKG.wechat: pkg;
			case id.main_app_qq:
				pkg = pkg == null? PKG.qq: pkg;
				if(b) list.addListeningApp(pkg);
				else list.removeListeningApp(pkg);
				break;
			case id.main_extra_pause:
				data.set(TYPE.PAUSE, b);
				break;
			case id.main_extra_voice:
				data.set(TYPE.VOICE_ON_SCREEN_OFF, b);
				break;
			case id.main_debug_mode:
				File f = new File(DEBUG_MODE_FILE_TAG);
				if(DEBUG=b)
					f.mkdirs();
				else
					f.delete();
				break;
		}
	}
	static String SAVE_FILE=string(DIR_data,"/app.setting");
	/*	initGlobalConfig
		通常由 服务启动时 或者 窗体启动时 进入
		完全执行时，将默认文件全部覆盖到数据存储区
		这会在载入初始配置失败时被完全执行
		载入初始配置失败的原因通常是，该配置文件不存在
	*/
	public static void initGlobalConfig(){
		if(!loadFileToMemory()){
			loadDefaultFile();
			loadFileToMemory();
		}
	}
	static void loadDefaultFile(){ // 加载默认
		Files.copy("default.setting",SAVE_FILE);
		Files.copy(string(PKG.mms,".lua"), NotificationScript.genPath(PKG.mms));
		Files.copy(string(PKG.qq,".lua"), NotificationScript.genPath(PKG.qq));
	}
	void loadMemoryToUi(){
		app_mms.setChecked(LISTENING_APPS.contains(PKG.mms));
		app_wechat.setChecked(LISTENING_APPS.contains(PKG.wechat));
		app_qq.setChecked(LISTENING_APPS.contains(PKG.qq));
		extra_pause.setChecked(data.get(TYPE.PAUSE));
		extra_voice.setChecked(data.get(TYPE.VOICE_ON_SCREEN_OFF));
		// 以下 当已选择复选框以外的 app 时，在按钮中显示附加多少个 app
		int selectAppsExcept=0;
		if(LISTENING_APPS.contains(PKG.mms)) selectAppsExcept++;
		if(LISTENING_APPS.contains(PKG.wechat)) selectAppsExcept++;
		if(LISTENING_APPS.contains(PKG.qq)) selectAppsExcept++;
		selectAppsExcept= LISTENING_APPS.size() - selectAppsExcept;
		setText(fv(id.main_app_sets),
			selectAppsExcept > 0?
				string("+",selectAppsExcept," 额外..."):
				"额外...");
	}
	void loadMemoryToFile(){
		FSON j=new FSON("{}"); // 暂停使用
		j.set("ver", CONFIG_VERSION);
		j.set("pause", data.get(TYPE.PAUSE));
		FSON t=new FSON("[]"); // 监听中的程序
		for(int i=0, len=LISTENING_APPS.size(); i<len; i++)
			t.set(i, LISTENING_APPS.get(i));
		j.set("app", t);
		t=new FSON("[]"); // 语音播报总开关、语音接口设置
		t.set(0, data.get(TYPE.VOICE_ON_SCREEN_OFF));
		t.set(1, data.get(TYPE.VOICE_API_CHOICE));
		t.set(2, data.get(TYPE.VOICE_STAT_DEFAULT));
		j.set("voice", t);
		t=new FSON("[]"); // 强行启用语音播报的程序
		for(int i=0, len=ENABLED_VOICE_APPS.size(); i<len; i++)
			t.set(i, ENABLED_VOICE_APPS.get(i));
		j.set("voice_enabled", t);
		t=new FSON("[]"); // 强行停用语音播报的程序
		for(int i=0, len=DISABLED_VOICE_APPS.size(); i<len; i++)
			t.set(i, DISABLED_VOICE_APPS.get(i));
		j.set("voice_disabled", t);
		Text.write(j.toString(), SAVE_FILE);
	}
	static int CONFIG_VERSION = 1 ;
	static boolean loadFileToMemory(){ // 返回 true 表示数据已成功从文件转移到内存
		if((boolean)data.get(TYPE.LOADED_DATA_FROM_FILE)) return true;
	//-----
		FSON j=new FSON(Text.read(SAVE_FILE));
		if(!j.canRead()) return false;
		switch(j.get("ver",1)){
			case 2:
			case 1: // 确实不需要 break，这是为了兼容低版本数据
				data.set(TYPE.PAUSE,j.get("pause",false)); // 暂停使用
				FSON t=j.getList("app"); // 监听中的程序
				for(int i=0,len=t.length();i<len;i++)
					list.addListeningApp(t.get(i,""));
				t=j.getList("voice"); // 语音播报总开关、语音接口设置
				setDataTo(TYPE.VOICE_ON_SCREEN_OFF,t,0);
				setDataTo(TYPE.VOICE_API_CHOICE,t,1);
				setDataTo(TYPE.VOICE_STAT_DEFAULT,t,2);
				t=j.getList("voice_enabled"); // 强行启用语音播报的程序
				for(int i=0,len=t.length();i<len;i++)
					ENABLED_VOICE_APPS.add(t.get(i,""));
				t=j.getList("voice_disabled"); // 强行停用语音播报的程序
				for(int i=0,len=t.length();i<len;i++)
					DISABLED_VOICE_APPS.add(t.get(i,""));
		}
	//-----
		data.set(TYPE.LOADED_DATA_FROM_FILE,true);
		return true;
	}
	static void setDataTo(DataType dt,FSON from,int index){ // 从文件设置数据到内存，如果找不到键就从内存获取
		data.set(dt,from.get(index,data.get(dt)));
	}
	
	void chkboxbind(View...i){
		for(View e:i) ((CheckBox)e).setOnCheckedChangeListener(this);
	}
	
	String PATH_FLAG_DEVELOPER = string(DIR_data,"/FLAG_DEVELOPER_MODE");
	int devModeTimer=0;
	void tryEnterDeveloperMode(){
		devModeTimer++;
		if(devModeTimer < 10) multip("该功能尚未实装");
		if(devModeTimer == 20)
			try{
				new File(PATH_FLAG_DEVELOPER).createNewFile();
			}catch(Throwable e){}
	}
	
	void loadVersionOnTitle(){
		try{
			getWindow().setTitle(
				string(
					getString(string.app_name_main),
					getString(string.app_v),
					getPackageManager().getPackageInfo(getPackageName(),0).versionName));
		}catch(Throwable e){}
	}
	void loadDonateList(){
		new Http("get","https://raw.githubusercontent.com/LEORChn/LEORChn.github.io/master/api/dntlist.json","",""){
			@Override protected void onload(String s){
				FSON j=new FSON(s);
				if(j.canRead()){
					StringBuilder b=new StringBuilder(" ----- 感谢捐赠 ----- ");
					SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm"),
						cnf=new SimpleDateFormat("yyyy年M月d日");
					j=j.getList("data");
					for(int i=0,len=j.length();i<len;i++){
						FSON k=j.getObject(i);
						try{
							Date d=f.parse(k.get("t",""));
							if(d.getTime()>1541520000000l){ // 18年11月7号
								string(b,"感谢 ",k.get("n",""),
									" 于",cnf.format(d),
									"捐赠的￥",k.get("p",""),"；");
							}else{
								break;
							}
						}catch(Throwable e){}
					}
					if(b.indexOf("捐赠的") == -1) b = buildstring("←←←←←点击左侧成为第一位捐赠者");
					try{
						setText(fv(id.main_donate_list),b.toString());
					}catch(Throwable e){}
				}else try{
					setText(fv(id.main_donate_list),"加载失败，请前往捐款页查看捐赠者名单。");
				}catch(Throwable e){}
			}
		};
	}
}
