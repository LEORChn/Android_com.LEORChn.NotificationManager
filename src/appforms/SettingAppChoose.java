package appforms;
import leorchn.lib.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.content.*;
import android.content.pm.*;

public class SettingAppChoose extends Activity1 implements AdapterView.OnItemClickListener{
	@Override protected void oncreate() {

	}
	int hasinit;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0:
				setContentView(layout.activity_appchooser);
				break;
			case 1:
				pm=getPackageManager();
				ld=SharedConsts.LISTENING_APPS;
				selectedApps=fv(id.appchooser_text);
				l=(ListView)fv(id.appchooser_list);
				l.setAdapter(lc);
				l.setOnItemClickListener(this);
				break;
			case 2:
				List<PackageInfo>l=pm.getInstalledPackages(1);
				for(int i=0,len=l.size();i<len;i++){
					Bean b=new Bean();
					b.r=l.get(i).applicationInfo;
					b.checked=ld.contains(l.get(i).packageName);
					lc.add(b);
				}
				lc.refresh();
				loadAppsNameToTextView();
		}
		return hasinit++<9;
	}
	View selectedApps;
	ArrayList<String> ld;
	ListView l;
	ListControl lc=new ListControl();
	PackageManager pm;
	@Override public void onItemClick(AdapterView<?> p1, View v, int p, long p4) {
		Bean b=lc.get(p);
		Holder h=(Holder)v.getTag();
		b.checked = !b.checked;
		if(b.checked){
			ld.add(b.r.packageName);
		}else{
			ld.remove(b.r.packageName);
		}
		h.c.setChecked(b.checked);
		loadAppsNameToTextView();
	}
	void loadAppsNameToTextView(){
		StringBuilder s=buildstring();
		for(int i=0,len=ld.size();i<len;i++){
			if(i>0) string(s,"、");
			try{
				String p=pm.getApplicationLabel(pm.getApplicationInfo(ld.get(i),0)).toString();
				string(s, p);
			}catch(Throwable e){}
		}
		setText(selectedApps, s.toString());
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
