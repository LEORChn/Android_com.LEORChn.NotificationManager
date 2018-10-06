package appforms;
import leorchn.lib.*;
import android.view.*;
import android.provider.*;
import android.widget.*;
import android.content.*;
import android.text.*;
import android.widget.RadioGroup.*;

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
				break;
			case 1: // 加载组件
				setContentView(layout.activity_main_set);
				break;
			case 2: // 绑定组件事件
				perm_read=(CheckBox)fv(id.main_perm_read);
				perm_window=(CheckBox)fv(id.main_perm_window);
				btnbind(perm_read, perm_window);
				btnbind(id.main_app_sets, id.main_extra_voice_sets, id.main_save);
				app_mms=(CheckBox)fv(id.main_app_sms);
				app_wechat=(CheckBox)fv(id.main_app_wechat);
				app_qq=(CheckBox)fv(id.main_app_qq);
				extra_pause=(CheckBox)fv(id.main_extra_pause);
				extra_voice=(CheckBox)fv(id.main_extra_voice);
				chkboxbind(app_mms, app_wechat, app_qq, extra_pause, extra_voice);
				break;
			case 3: // 加载数据到组件
				initGlobalConfig();
				
				onResume(); // 可能在切换窗口后更新配置，重新把内存中的配置载入到界面
		}
		return hasinit++<9;
	}
	CheckBox perm_read, perm_window,
		app_mms, app_wechat, app_qq,
		extra_pause, extra_voice;
	boolean isNotificationListenerGranted(){ // 返回 true 如果通知监听器已授权
		String s=Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
		return !TextUtils.isEmpty(s) && s.contains(Listener.class.getName());
	}
	@Override protected void onResume(){ // 确保反复进出窗口时能刷新 选择框的 授权信息
		super.onResume();
		if(hasinit==0)return;
		boolean b=isNotificationListenerGranted();
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
		}
	}
	static String SAVE_FILE=string(DIR_data,"/app.setting");
	public static void initGlobalConfig(){
		if(!loadFileToMemory()){ // 若内存中未载入配置，则载入，否则自动中断
			loadDefaultFile();
			loadFileToMemory();
		}
	}
	static void loadDefaultFile(){ // 加载默认
		Files.copy("default.setting",SAVE_FILE);
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
		j.set("voice", t);
		t=new FSON("[]"); // 停用语音播报的程序（默认全部开启播报，如果总开关被启用）
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
				data.set(TYPE.VOICE_ON_SCREEN_OFF,t.get(0,false));
				data.set(TYPE.VOICE_API_CHOICE,t.get(1,0));
				t=j.getList("voice_disabled"); // 停用语音播报的程序（默认全部开启播报，如果总开关被启用）
				for(int i=0,len=t.length();i<len;i++)
					DISABLED_VOICE_APPS.add(t.get(i,""));
		}
	//-----
		data.set(TYPE.LOADED_DATA_FROM_FILE,true);
		return true;
	}
	
	void chkboxbind(View...i){
		for(View e:i) ((CheckBox)e).setOnCheckedChangeListener(this);
	}
}
