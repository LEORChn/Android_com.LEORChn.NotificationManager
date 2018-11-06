package leorchn.lib;
import android.net.*;
import android.text.*;
import static leorchn.lib.Activity1.*;
import android.media.*;
import android.content.*;

public enum TTS {
	GOOGLE, BAIDU;
	public static TTS[] getSupportedList(){
		return new TTS[]{GOOGLE, BAIDU};
	}
	static MediaPlayer ttsp;
	static MediaPlayer.OnPreparedListener opl=new MediaPlayer.OnPreparedListener(){
		@Override public void onPrepared(MediaPlayer v){
			v.start();
		}
	};
	static MediaPlayer.OnCompletionListener memmgr=new MediaPlayer.OnCompletionListener(){
		@Override public void onCompletion(MediaPlayer v){
			v.reset();
		}
	};
	public static void play(TTS engine,String text){
		if(ttsp == null)
			ttsp = new MediaPlayer();
		else
			ttsp.reset();
		try{
			ttsp.setOnPreparedListener(opl);
			ttsp.setDataSource(url(engine, text));
			ttsp.prepareAsync();
			ttsp.setOnCompletionListener(memmgr);
		}catch(Throwable e){}
	}
	public static String url(TTS engine,String text){
		if(TextUtils.isEmpty(text)) return "";
		text = Uri.encode(text);
		switch(engine){
			case GOOGLE:
				// 谷歌的参数大概是，编码=u8，语言=汉语，第三方客户端=tw-ob，用这个客户端来源似乎可以绕过人机验证。。
				return string("https://translate.google.cn/translate_tts?ie=UTF-8&tl=zh-CN&client=tw-ob&q=",text);
			case BAIDU:
				// 百度的参数大概是，语言=汉语，速度=5，来源=wise，不知道wise是啥。。
				return string("https://fanyi.baidu.com/gettts?lan=zh&spd=5&source=wise&text=",text);
		}
		return "";
	}
}
