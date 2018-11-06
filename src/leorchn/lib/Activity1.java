package leorchn.lib;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import appforms.*;
import java.io.*;
import java.util.*;
import leorchn.App;
public abstract class Activity1 extends Activity implements Consts, MessageQueue.IdleHandler, Thread.UncaughtExceptionHandler, View.OnClickListener{
	
	public static final String UA="User-Agent: ",
	UA_win="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36",
	UA_android="Mozilla/5.0 (Linux; Android 4.4.4;) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
	UA_mobilebili="Mozilla/5.0 BiliDroid/4.11.7 (bbcallen@gmail.com)";
	
	// 调试模式的初始值直接链接过去就行，以后想动态修改也可以直接在这改
	public static boolean DEBUG = DEBUG_MODE_INIT; // 调试标记放在此处而不在接口中的原因是，接口中默认final，要修改就要声明数组，改起来麻烦
	
	public static String DIR_cache=App.getContext().getExternalCacheDir().getPath()+"/",
	DIR_data=App.getContext().getFilesDir().getPath()+"/",
	DIR_cache_pic=DIR_cache+"pic/",
	DIR_cache_log=DIR_cache+"log/";
//-----
	
//-----
	protected String http(String method,String url,String param,String formdata){//每个activity都可用的http，需要在其他线程
		return HttpRequest.http(method,url,param,formdata);
	}
	public static class Http extends AsyncTask<String,Void,String>{//封装型异步http，一般不用
		public boolean isfin=false;
		public String result="";
		public Http(String method,String url,String param,String formdata){
			execute(method,url,param,formdata);
		}
		@Override protected String doInBackground(String[]p){
			pl("<---   "+p[1]);
			result=HttpRequest.http(p[0],p[1],p[2],p[3]);
			isfin=true;
			return result;
		}
		@Override protected void onPostExecute(String p){pl("--->   "+p); onload(p); }
		protected void onload(String d){}
		public String pending(){return waitfor();}
		public String waitfor(){
			try{while(!isfin)Thread.sleep(200);}catch(Throwable e){}
			return result;
		}
		/* if single thread recommand usage:
			Http h=new Http("get","http://",mcok,"").waitfor();
		*/
		/* if multi-thread (like needs to update ui) recommand usage:
			new Http("get","http://",mcok,""){
				protected void onload(String d){}//update ui here
			};
		*/
	}
//-----
	protected void btnbind(View...v){ for(View btnv:v)btnv.setOnClickListener(this); }//连续绑定多个【动态】view的点击事件到本activity
	protected void btnbind(int...id){ for(int btnid:id)fv(btnid).setOnClickListener(this); }//连续绑定多个【静态】view的点击事件到本activity
	abstract public void onClick(View v);//每个窗口应该都有按钮吧？
	protected void setText(View v,String s){((TextView)v).setText(s);}
	protected void seticon(View v,android.graphics.Bitmap i){
		if(v instanceof ImageView){ ((ImageView)v).setImageBitmap(i); }
	}
	protected View fv(int id){return findViewById(id);}//查找当前activity唯一的
	protected View fv(ViewGroup vg,int id){return vg.findViewById(id);}//查找列表子项中唯一的
	protected View extractView(View v){
		try{
			((ViewGroup)v.getParent()).removeView(v);
			return v;
		}catch(Throwable e){ return null; }
	}
	protected ViewGroup inflateView(int id){return(ViewGroup)LayoutInflater.from(this).inflate(id,null);}

	protected void startActivity(Class<?>c){startActivity(new Intent(this,c));}
	public void openurl(String url){
		startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
	}
	protected static boolean visible(View v){return v.getVisibility()==View.VISIBLE;}
	protected static void visible(final View v,final boolean visible){v.post(new Runnable(){public void run(){v.setVisibility(visible?View.VISIBLE:View.GONE);}});}
	public static void tip(Object...s){new Toast1(string(s));}
	public static void multip(Object...s){Toast1.multip(string(s));}
	private Thread.UncaughtExceptionHandler defUeh;
	private Activity1 This=this;//一个默认指向当前activity的指针，在内部类中使用
	public Activity1(){
		super();
		defUeh= Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		
	}
	protected void onCreate(){ super.onCreate(null); }
	@Override protected void onCreate(Bundle sis){
		//new Theme(this).set(); //若有需要再说
		super.onCreate(sis);
		oncreate();
		addIdleHandler();
	}
	abstract protected void oncreate();
	public void addIdleHandler(){ Looper.myQueue().addIdleHandler(this); }//添加一个初始化或空闲行为，大多数在Activity.onCreate使用
	public boolean queueIdle(){return onIdle();}
	//abstract protected void onCreate(Bundle sis);
	abstract protected boolean onIdle();//每个窗口应该都用这个来初始化
//-----
	public static String string(Object...str){ return buildstring(str).toString(); }
	public static StringBuilder buildstring(Object...str){
		StringBuilder bdr=new StringBuilder();
		return string(bdr,str);
	}
	public static StringBuilder string(StringBuilder bdr,Object...str){
		for(Object s:str) bdr.append(s);
		return bdr;
	}
//-----
	@Override public void uncaughtException(final Thread thread, final Throwable ex) {
		rep = E.trace(ex);
		new AfterException();//新建线程显示消息
		//startActivity(new Intent(this,ExceptionReport.class).putExtra("info",errRep));
		//defUeh.uncaughtException(thread,ex);
	}
	private String rep; private AlertDialog excad=null;
	private class AfterException extends Thread implements DialogInterface.OnClickListener{
		public AfterException(){ this.start(); }
		public void run(){
			savelog();
			Looper.prepare();
			if(excad!=null)onClick(null,10);
			excad=new AlertDialog.Builder(This).setTitle("温和的错误提示")
				.setMessage("程序发生了错误并即将退出。以下信息已自动保存。\n"+rep)
				.setNeutralButton("关闭", this)
				.create();
			excad.show();
			Looper.loop();
		}
		public void onClick(DialogInterface p1, int p2) {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		void savelog(){
			try{
				File log=new File(DIR_cache_log);
				log.mkdirs();
				log=log.createTempFile(string(System.currentTimeMillis(),"_"),".log",log);
				Text.write(rep,log.getPath(),"utf-8");
			}catch(Exception e){}
		}
	}
	public static void pl(Object...o){tipl(o);}
	public static void tipl(Object...o){System.out.println(string(o));}
	HashMap<String,Bitmap>pics=new HashMap<>();
	
	protected class Msgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener{
		protected int vbyes=AlertDialog.BUTTON_POSITIVE,
			vbno=AlertDialog.BUTTON_NEGATIVE,
			vbmid=AlertDialog.BUTTON_NEUTRAL;
		public Msgbox(String...msgs){
			super(Activity1.this); 
			for(int i=0,len=msgs.length;i<len;i++){
				switch(i){
					case 0: setTitle(msgs[0]); break;
					case 1: setMessage(msgs[1]); break;
					case 2: setPositiveButton(msgs[2],this); setCancelable(false); break;
					case 3: setNegativeButton(msgs[3],this); break;
					case 4: setNeutralButton(msgs[4],this); break;
				}
			}show();
		}
		public void onClick(DialogInterface p1,int p2){ onClick(p2); }
		protected void onClick(int i){}
	}

	protected abstract class CheckMsgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener,DialogInterface.OnMultiChoiceClickListener {
		protected int vbyes=AlertDialog.BUTTON_POSITIVE,
		vbno=AlertDialog.BUTTON_NEGATIVE,
		vbmid=AlertDialog.BUTTON_NEUTRAL;
		boolean stat[];
		public CheckMsgbox(String[]msgs,boolean[]status,String...options){
			super(Activity1.this); 
			for(int i=0,len=msgs.length;i<len;i++){
				switch(i){
					case 0: setTitle(msgs[0]); break;
					case 1: setPositiveButton(msgs[1],this); setCancelable(false); break;
					case 2: setNegativeButton(msgs[2],this); break;
					case 3: setNeutralButton(msgs[3],this); break;
				}
			} stat=status;
			this.setMultiChoiceItems(options,status,this);
			show();
		}
		public void onClick(DialogInterface p1,int p2){ onClick(p2,stat); }
		public void onClick(DialogInterface p1,int p2,boolean p3){ stat[p2]=p3; onChange(p2,p3); }
		protected abstract void onClick(int i,boolean[]status)
		protected void onChange(int i,boolean status){}
	}
	// 以下-----系统事件覆盖区
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		PopupMenuCompat.exec(menu);
		return super.onCreateOptionsMenu(menu);
	}
	public void onPointerCaptureChanged(boolean hasCapture){}
}
class Toast1 implements Runnable{ // 这个是为了解决在高版本系统连续弹出 toast 时重叠的问题
	static long lastpost=0;
	static Handler h;
	CharSequence s;
	public Toast1(String chr){
		if(h==null) h=new Handler(Looper.getMainLooper());
		if(chr==null || chr.length()==0) return;
		s=chr;
		long now=System.currentTimeMillis();
		if(lastpost<now) lastpost=now; // 如果 lastpost 小于 now，那么把 lastpost 设为等于 now
		h.postDelayed(this, lastpost-now);// lastpost 肯定大于等于 now
		lastpost+=chr.length()>9?4000:2500;
	}
	public static void initHandler(){
		h=new Handler(Looper.getMainLooper());
	}
	public void run(){
		Toast.makeText(
			App.getContext(),
			s,
			s.length()>9?
				Toast.LENGTH_LONG:
				Toast.LENGTH_SHORT)
		.show();
	}
	static Toast mul=Toast.makeText(App.getContext(),"",Toast.LENGTH_LONG);
	public static void multip(String s){
		mul.setText(s);
		mul.show();
	}
}
