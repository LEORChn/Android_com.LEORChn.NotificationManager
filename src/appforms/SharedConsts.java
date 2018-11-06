package appforms;
import java.util.*;

public interface SharedConsts{
	public static ArrayList<String> LISTENING_APPS=new ArrayList<>(),
		ENABLED_VOICE_APPS=new ArrayList<>(),
		DISABLED_VOICE_APPS=new ArrayList<>();
	public static PkgName PKG;
	public static ListManager list;
	public static Object[]dataRaw=new Object[]{false,false,false,0,true};
	public static DataManager data;
	public static DataType TYPE;
}
class ListManager implements SharedConsts{
	static DataType[] dt={
		TYPE.LIST_LISTENING_APP
	};
	public static void addListeningApp(String pkg){
		if(!LISTENING_APPS.contains(pkg)) LISTENING_APPS.add(pkg);
	}
	public static void removeListeningApp(String pkg){
		LISTENING_APPS.remove(pkg);
	}
}
enum DataType{
	LIST_LISTENING_APP,
	LOADED_DATA_FROM_FILE,
	PAUSE,
	VOICE_ON_SCREEN_OFF,
	VOICE_API_CHOICE, // 0谷歌娘 1度娘
	VOICE_STAT_DEFAULT
}
class DataManager implements SharedConsts{
	static DataType[] dt={
		TYPE.LOADED_DATA_FROM_FILE,
		TYPE.PAUSE,
		TYPE.VOICE_ON_SCREEN_OFF,
		TYPE.VOICE_API_CHOICE,
		TYPE.VOICE_STAT_DEFAULT
	};
	public static Object get(DataType d){
		for(int i=0;i<dt.length;i++)
			if(dt[i]==d)
				return dataRaw[i];
		return null;
	}
	public static void set(DataType d,Object raw){
		for(int i=0;i<dt.length;i++)
			if(dt[i]==d)
				dataRaw[i]=raw;
	}
}
class PkgName{
	public static String
		mms="com.android.mms",
		wechat="com.tencent.mm",
		qq="com.tencent.mobileqq";
}

