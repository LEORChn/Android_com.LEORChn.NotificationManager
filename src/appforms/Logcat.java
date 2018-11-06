package appforms;
import android.view.*;
import android.widget.*;
import leorchn.lib.*;
import viewproxy.ListView;
import java.util.*;
import android.os.*;
import java.io.*;
import android.content.*;

public class Logcat extends Activity1{
	@Override protected void oncreate() {

	}
	int hasinit;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0:
				setContentView(layout.activity_logcat);
				break;
			case 1:
				btnbind(id.logcat_save, id.logcat_lock_bottom);
				break;
			case 2:
				l=(ListView)fv(id.logcat_logs);
				l.setAdapter(lc);
				break;
			case 3:
				startPullLogs();
				
		}
		return hasinit++<9;
	}
	ListView l;
	ListControl lc=new ListControl();
	boolean isWindowDestroyed=false, lockBottom=false;
	AsyncTask<Void, String, Void>t;
	void startPullLogs(){
		t=new AsyncTask<Void, String, Void>(){
			@Override protected Void doInBackground(Void[]z) {
				try{
					BufferedReader br=new BufferedReader(
						new InputStreamReader(
							Runtime.getRuntime().exec("logcat -v threadtime").getInputStream()
						), 20);
					while(!isWindowDestroyed){
						String ln=br.readLine();
						if(ln == null) break;
						publishProgress(ln);
					}
					br.close();
				}catch(Throwable e){}
				return null;
			}
			@Override protected void onProgressUpdate(String[]s) {
				if(isWindowDestroyed) return;
				try{ // 一般也就第一行会因为格式不对而报错 ArrayIndexOutOfBoundsException
					lc.add(s[0]);
				}catch(Throwable e){
					//pl(E.trace(e));
				}
			}
			@Override protected void onPostExecute(Void v){
				if(isWindowDestroyed) return;
				lc.add("a b c d e 日志界面: 日志流已被远程断开");
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	@Override protected void onDestroy() {
		isWindowDestroyed = true;
		super.onDestroy();
	}

	class Bean{
		static final int LAYOUT=layout.listsub_log_item;
		int type;
		String time, sender;
		StringBuilder bld;
	}
	class ListControl extends BaseAdapter{
		//public CharSequence[]getAutofillOptions(){return null;}
		ArrayList<Bean>a=new ArrayList<>();
		Bean lastbean;
		String lasttime="";
		Holder bottomholder;
		public void add(String infoline){
			String[]line = infoline.split(":\\s+",2),
				info = line[0].split("\\s+");
			String curTime=string(info[0], " ", info[1]);
			if(lasttime.equals(curTime)){
				lastbean.bld.append(string("\n", line[1]));
				if(bottomholder == null || bottomholder.index < a.size()-1) return;
				((TextView)bottomholder.desc).append(string("\n", line[1]));
			}else{
				Bean b = new Bean();
				b.time = curTime;
				b.type = typeConvert(info[4]);
				b.sender = info[5];
				b.bld = buildstring(line[1]);
				a.add(b);
				lastbean = b;
				lasttime = lastbean.time;
				refresh();
			}
			lockOnBottom();
		}
		int[] typeDict={0x4033b5e5, 0x40ff0000, 0x4000ff00, 0, 0x40ffc000};
		int typeConvert(String s){
			String[]p={"d","e","i","v","w"};
			for(int i=0;i<p.length;i++)
				if(p[i].equals(s.toLowerCase()))
					return i;
			return 0;
		}
		public void clear(){ a.clear(); refresh(); }
		public void refresh(){ notifyDataSetChanged(); }
		public void lockOnBottom(){ if(lockBottom) l.setSelection(getCount()-1); }
		public Bean get(int p){ return a.get(p); }
		@Override public int getCount(){ return a.size(); }
		@Override public Object getItem(int p){ return null; }
		@Override public long getItemId(int p){ return 0; }
		@Override public View getView(int p, View v, ViewGroup p3){
			Bean b=get(p);
			Holder d;
			if(v==null){
				v= inflateView(Bean.LAYOUT);
				ViewGroup w= (ViewGroup)v;
				d= new Holder();
				d.title= fv(w, id.listsub_title);
				d.desc= fv(w, id.listsub_desc);
				v.setTag(d);
			}else{
				d=(Holder)v.getTag();
			}
			setText(d.title, b.sender);
			setText(d.desc, b.bld.toString());
			d.desc.setBackgroundColor(typeDict[b.type]);
			d.index= p;
			if(p == a.size()-1) bottomholder = d;
			return v;
		}
	}
	class Holder{
		int index=0;
		View title, desc;
	}
	@Override public void onClick(View v) {
		switch(v.getId()){
			case id.logcat_lock_bottom:
				lockBottom=((CompoundButton)v).isChecked();
				lc.lockOnBottom();
				if(!lockBottom) l.setSelection(0);
				break;
			case id.logcat_save:
				StringBuilder s=buildstring("");
				for(int i=0,len=lc.getCount();i<len;i++){
					Bean b=lc.get(i);
					string(s," ", b.time, " ", b.type, " ", b.sender, "\n", b.bld.toString().trim(), "\n");
				}
				String logpath=string(DIR_cache, "/current.log");
				boolean saved=Text.write(s.toString(), logpath);
				if(saved){
					tip("已保存到文件\n/current.log");
					Intent i=new Intent(Intent.ACTION_SEND)
						.setDataAndType(android.net.Uri.parse(string("file://", logpath)),"text/*");
					startActivity(i);
				}
				break;
		}
	}
	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU) pl("you pressed menu key.");
		return super.onKeyDown(keyCode, event);
	}
}
