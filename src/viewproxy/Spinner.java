package viewproxy;
import android.content.*;
import android.util.*;

public class Spinner extends android.widget.Spinner{
	
	public Spinner(Context c){ super(c); init(); }
	public Spinner(Context c,AttributeSet attrs){ super(c,attrs); init(); }
	public Spinner(Context c,AttributeSet attrs,int defStyle){ super(c,attrs,defStyle); init(); }

	//以上覆盖
	void init(){
		this.setSelection(0, true); // 如果不用这个，在 setOnItemSelectListener 时会自动触发一次
	}
}
