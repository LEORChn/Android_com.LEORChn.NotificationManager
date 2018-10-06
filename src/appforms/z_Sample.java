package appforms;
import leorchn.lib.*;
import android.view.*;

public class z_Sample extends Activity1{
	@Override protected void oncreate() {
		
	}
	int hasinit;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0:
				//setContentView(layout.);
				break;
			case 1:
				
		}
		return hasinit++<9;
	}
	@Override public void onClick(View v) {
		switch(v.getId()){
			//case id.:
		}
	}
}
