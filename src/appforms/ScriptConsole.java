package appforms;
import leorchn.lib.*;
import android.view.*;
import android.widget.*;
import android.text.*;
import java.io.*;

public class ScriptConsole extends Activity1{
	@Override protected void oncreate() {

	}
	int hasinit;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0:
				setContentView(layout.activity_script_console);
				break;
			case 1:
				pkgname=(EditText)fv(id.script_console_filename);
				content=(EditText)fv(id.script_console_content);
				btnbind(id.script_console_load, id.script_console_save);
		}
		return hasinit++<9;
	}
	EditText pkgname, content;
	@Override public void onClick(View v) {
		switch(v.getId()){
			case id.script_console_load:
				load(false);
				break;
			case id.script_console_save:
				save();
				break;
		}
	}
	void load(boolean ignore){
		if(ignore || TextUtils.isEmpty(content.getText())){
			if(!TextUtils.isEmpty(pkgname.getText())){
				tip("loading ",path());
				content.setText(Text.read(path()));
			}else new Msgbox("Input error","Please input a package name first.","ok");
		}else{
			new Msgbox("Load now?","You have content maybe not save.\nContinue will be lost it all.","yes","no"){
				@Override public void onClick(int i){
					if(i==vbyes) load(true);
				}
			};
		}
	}
	void save(){
		if(!TextUtils.isEmpty(pkgname.getText()))
			if(TextUtils.isEmpty(content.getText()))
				new Msgbox("Delete?","Just because you want to save empty content.","yes","no"){
					@Override public void onClick(int i){
						if(i==vbyes) new File(path()).delete();
					}
				};
			else{
				boolean saved=Text.write(content.getText().toString(), path());
				// here to remove old script runtime from NotificationScript to make it update
				if(saved){
					NotificationScript.removeEnvironment(pkgname.getText().toString());
					tip("File saved at\n",path());
				}else new Msgbox("Save fail","Unknown error.","ok");
			}
		else new Msgbox("Input error","Please input a package name first.","ok");
	}
	String path(){
		return NotificationScript.genPath(pkgname.getText().toString());
	}
	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == 4 && content != null && !TextUtils.isEmpty(content.getText())){
			new Msgbox("Exit now?","You have content maybe not save.\nContinue will be lost it all.","yes","no"){
				@Override public void onClick(int i){
					if(i==vbyes) finish();
				}
			};
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
