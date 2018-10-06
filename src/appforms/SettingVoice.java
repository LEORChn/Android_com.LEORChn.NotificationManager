package appforms;
import android.content.pm.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import leorchn.lib.*;
import viewproxy.ListView;

public class SettingVoice extends Activity1 implements AdapterView.OnItemClickListener{
	@Override protected void oncreate() {

	}
	ViewGroup listViewHeader;
	int hasinit;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0:
				setContentView(layout.activity_setting_voice);
				break;
			case 1:
				pm=getPackageManager();
				all_lis=SharedConsts.LISTENING_APPS;
				ld=SharedConsts.DISABLED_VOICE_APPS;
				l=(ListView)fv(id.voice_set_list_app);
				listViewHeader=inflateView(layout.listsub_setting_voice_header);
				
				l.setAdapter(listViewHeader, lc);
				l.setOnItemClickListener(this);
				sp=(Spinner)fv(id.voice_set_engine);
				break;
			case 3:
				for(int i=0,len=all_lis.size();i<len;i++)
					try{
						Bean b = new Bean();
						b.r = pm.getApplicationInfo(all_lis.get(i),0);
						b.checked = ! ld.contains(b.r.packageName);
						lc.add(b);
					}catch(Throwable e){}
				lc.refresh();
		}
		return hasinit++<9;
	}
	Spinner sp;
	ArrayList<String> all_lis, ld;
	ListView l;
	ListControl lc=new ListControl();
	PackageManager pm;
	@Override public void onItemClick(AdapterView<?> p1, View v, int p, long p4) {
		if(p > 0) p--;
		else return; // 因为添加了 header，所以实际上会有些偏移
		Bean b=lc.get(p);
		Holder h=(Holder)v.getTag();
		b.checked = !b.checked;
		if(b.checked){
			ld.remove(b.r.packageName);
		}else{
			ld.add(b.r.packageName);
		}
		h.c.setChecked(b.checked);
	}
	class Bean{
		static final int LAYOUT=layout.listsub_app_choose;
		ApplicationInfo r;
		boolean checked;
	}
	class ListControl extends BaseAdapter{
		//public CharSequence[]getAutofillOptions(){return null;}
		ArrayList<Bean>a=new ArrayList<>();
		public void add(Bean b){ a.add(b); }
		public void clear(){ a.clear(); refresh(); }
		public void refresh(){ notifyDataSetChanged(); }
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
				d.c= (CheckBox)fv(w, id.listsub_check0);
				d.image= (ImageView)fv(w, id.listsub_image);
				v.setTag(d);
			}else{
				d=(Holder)v.getTag();
			}
			setText(d.title, b.r.loadLabel(pm).toString());
			setText(d.desc, b.r.packageName);
			d.c.setChecked(b.checked);
			d.image.setImageDrawable(b.r.loadIcon(pm));
			//此处设置图片链接
			return v;
		}
	}
	class Holder{
		View title, desc;
		CheckBox c;
		ImageView image;
		/*	该 checkbox 同时使用 clickable=false 和 focusable=false 是因为
		 focusable=false 可以使整个 item view 响应触摸操作
		 clickable=false 可以在点击 item 根部 view 时，也能联动到本 checkbox 点击时的视觉效果
		 */
	}
	@Override public void onClick(View v){
		switch(v.getId()){
				//case id.
		}
	}

}
