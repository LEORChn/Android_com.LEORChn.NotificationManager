package viewproxy;
import android.content.*;
import android.util.*;
import android.widget.*;
import android.text.*;
import android.graphics.*;

public class AutoScrollTextView extends TextView{
	public AutoScrollTextView(Context c){
		super(c);
		init();
	}
	public AutoScrollTextView(Context c, AttributeSet attrs){
		super(c,attrs);
		init();
	}
	public AutoScrollTextView(Context c, AttributeSet attrs, int defStyle){
		super(c,attrs,defStyle);
		init();
	}//类所需api 1，以上强行劫持所有构造器
	static final int MARQUEE_FOREVER=0xffffffff;
	void init(){
		
		setEllipsize(TextUtils.TruncateAt.MARQUEE);
		setFadingEdgeLength(25);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setHorizontallyScrolling(true);
		setMarqueeRepeatLimit(MARQUEE_FOREVER);
		setSingleLine(true);
	}
	@Override public boolean isFocused(){
		return true;//永远表示自己拥有焦点
	}
	@Override protected void onFocusChanged(boolean focused,int direction,Rect previouslyFocusedRect){
		//永远不会丢失焦点
		super.onFocusChanged(true,direction,previouslyFocusedRect);
	}

	@Override
	protected float getLeftFadingEdgeStrength() {
		// TODO: Implement this method
		return 0.5f;
	}

	@Override
	protected float getRightFadingEdgeStrength() {
		// TODO: Implement this method
		return 0.5f;
	}
	
}
