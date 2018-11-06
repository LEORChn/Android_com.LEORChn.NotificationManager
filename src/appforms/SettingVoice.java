package appforms;
import android.app.*;
import android.content.pm.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import leorchn.lib.*;
import viewproxy.ListView;
import viewproxy.Spinner;

public class SettingVoice extends Activity1 implements SharedConsts, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener{
	@Override protected void oncreate() {

	}
	ViewGroup listViewHeader;
	int hasinit;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0:
				setContentView(layout.activity_setting_voice);
				break;
			case 1: // 加载列表根view
				pm=getPackageManager();
				all_lis = LISTENING_APPS;
				le = ENABLED_VOICE_APPS;
				ld = DISABLED_VOICE_APPS;
				stat_default=data.get(TYPE.VOICE_STAT_DEFAULT);
				
				l=(ListView)fv(id.voice_set_list_app);
				listViewHeader=inflateView(layout.listsub_setting_voice_header);
				
				l.setAdapter(listViewHeader, lc);
				l.setOnItemClickListener(this);
				break;
			case 2: // 绑定列表中的编辑框和按钮
				textForTest=(EditText)fv(id.voice_set_engine_test_input);
				btnbind(id.voice_set_engine_test, id.voice_set_force_speaker_help, id.voice_set_earphone_only_help, id.voice_set_stat_help);
				break;
			case 3: // 绑定列表中的单选框
				sp=(Spinner)fv(id.voice_set_engine);
				sp.setOnItemSelectedListener(this);
				spdef=(Spinner)fv(id.voice_set_stat_default);
				spdef.setOnItemSelectedListener(this);
				break;
			case 4: // 数据赋值到单选框ui
				sp.setSelection(data.get(TYPE.VOICE_API_CHOICE), true);
				spdef.setSelection(stat_default? 0: 1, true);
				for(int i=0,len=all_lis.size();i<len;i++)
					try{
						Bean b = new Bean();
						b.r = pm.getApplicationInfo(all_lis.get(i),0);
						boolean en=le.contains(b.r.packageName),
							dis=ld.contains(b.r.packageName);
						b.stat = en || dis? 
							en?
								Bean.STAT_ENABLE:
								Bean.STAT_DISABLE:
							Bean.STAT_DEFAULT;
						lc.add(b);
					}catch(Throwable e){}
				lc.refresh();
		}
		return hasinit++<9;
	}
	EditText textForTest;
	boolean stat_default=false; // 接口中的该数据 的代理
	Spinner sp, spdef;
	ArrayList<String> all_lis, le, ld;
	ListView l;
	ListControl lc=new ListControl();
	PackageManager pm;
	@Override public void onItemSelected(AdapterView<?> v, View p2, int i, long p4) {
		switch(v.getId()){
			case id.voice_set_engine:
				if(i>1) sp.setSelection(data.get(TYPE.VOICE_API_CHOICE), true);
				data.set(TYPE.VOICE_API_CHOICE, i);
				break;
			case id.voice_set_stat_default:
				stat_default=new boolean[]{true, false}[i];
				data.set(TYPE.VOICE_STAT_DEFAULT, stat_default);
				Holder[]uihs=lc.getUiHolders(); // 修改列表中的圆形选框
				for(Holder h:uihs) h.r.setChecked(stat_default);
				break;
		}
	}
	@Override public void onNothingSelected(AdapterView<?>v){}
	@Override public void onItemClick(AdapterView<?> p1, View v, int p, long p4) {
		if(p > 0) p--;
		else return; // 因为添加了 header，所以实际上会有些偏移
		Bean b=lc.get(p);
		Holder h=(Holder)v.getTag();
		switch(b.stat){
			case Bean.STAT_DEFAULT: // 当前默认，跳转到强行禁用
				ld.add(b.r.packageName);
				b.stat=Bean.STAT_DISABLE;
				break;
			case Bean.STAT_DISABLE: // 当前强行禁用，跳转到强行启用
				ld.remove(b.r.packageName);
				le.add(b.r.packageName);
				b.stat=Bean.STAT_ENABLE;
				break;
			case Bean.STAT_ENABLE: // 当前强行启用，跳转到默认
				le.remove(b.r.packageName);
				b.stat=Bean.STAT_DEFAULT;
		}
		h.r.setChecked(stat_default);
		h.c.setChecked((b.stat&2)>0);
		visible(h.r, (b.stat&1)==0);
		visible(h.c, (b.stat&1)>0);
	}
	
	class Bean{
		static final int LAYOUT=layout.listsub_app_choose,
			STAT_DEFAULT=0,
			STAT_DISABLE=1,
			STAT_ENABLE=3;
		ApplicationInfo r;
		int stat;
	}
	class ListControl extends BaseAdapter{
		//public CharSequence[]getAutofillOptions(){return null;}
		ArrayList<Bean>a=new ArrayList<>();
		ArrayList<Holder>uih=new ArrayList<>();
		public void add(Bean b){ a.add(b); }
		public void clear(){ a.clear(); refresh(); }
		public void refresh(){ notifyDataSetChanged(); }
		public Bean get(int p){ return a.get(p); }
		public Holder[] getUiHolders(){ return uih.toArray(new Holder[0]);}
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
				uih.add(d);
				d.title= fv(w, id.listsub_title);
				d.desc= fv(w, id.listsub_desc);
				d.c= (CheckBox)fv(w, id.listsub_check0);
				d.r= (RadioButton)fv(w, id.listsub_radio0);
				d.image= (ImageView)fv(w, id.listsub_image);
				v.setTag(d);
			}else{
				d=(Holder)v.getTag();
			}
			setText(d.title, b.r.loadLabel(pm).toString());
			setText(d.desc, b.r.packageName);
			visible(d.r, (b.stat&1)==0);
			visible(d.c, (b.stat&1)>0);
			switch(b.stat){
				case Bean.STAT_DEFAULT:
					d.r.setChecked(stat_default);
					break;
				case Bean.STAT_DISABLE:
					d.c.setChecked(false);
					break;
				case Bean.STAT_ENABLE:
					d.c.setChecked(true);
			}
			d.image.setImageDrawable(b.r.loadIcon(pm));
			return v;
		}
	}
	class Holder{
		View title, desc;
		CheckBox c;
		RadioButton r;
		ImageView image;
		/*	该 checkbox 同时使用 clickable=false 和 focusable=false 是因为
		 focusable=false 可以使整个 item view 响应触摸操作
		 clickable=false 可以在点击 item 根部 view 时，也能联动到本 checkbox 点击时的视觉效果
		 */
	}
	@Override public void onClick(View v){
		switch(v.getId()){
			case id.voice_set_engine_test:
				TTS[] t = TTS.getSupportedList();
				Editable edab= textForTest.getText();
				boolean isEmpty= TextUtils.isEmpty(edab) || TextUtils.isEmpty(edab.toString().trim());
				String finalText=isEmpty? textForTest.getHint().toString(): edab.toString();
				TTS.play(t[(int)data.get(TYPE.VOICE_API_CHOICE)], finalText);
				break;
			case id.voice_set_force_speaker_help:
				new Msgbox("强制扬声器播报","即使在使用耳机时，也会将扬声器作为音频输出的途径之一。","ok");
				break;
			case id.voice_set_earphone_only_help:
				new Msgbox("仅插入耳机时播报","将耳机作为第二位播报总开关，不使用耳机时自动禁用播报。","ok");
				break;
			case id.voice_set_stat_help:
				new AlertDialog.Builder(this)
					.setTitle("语音播报的启用状态")
					.setView(inflateView(layout.panel_help_check_status))
					.setPositiveButton("关闭",null)
					.show();
		}
	}

	@Override protected void onDestroy(){
		super.onDestroy();
		Holder[]uihs=lc.getUiHolders(); // 回收bitmap
		for(Holder h:uihs)
			Life.end(h.image);
		l=null;
		lc=null;
		System.gc();
	}
}
