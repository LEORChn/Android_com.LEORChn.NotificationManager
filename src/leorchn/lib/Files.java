package leorchn.lib;
import java.io.*;
import static leorchn.lib.Activity1.*;

public class Files{
	/*static String formatly(String path){
		String p=path;
		p=p.replaceAll("\\\\","/");
		p=p.replaceAll("//","/");
		if(!path.equals(p)) p=formatly(p);
		return p;
	}*/
	public static boolean copy(String origin,String placeto){
		InputStream is=null;
		OutputStream bytes=null;
		try{
			pl(string("start coping ",origin," to ",placeto));
			if(origin.startsWith("/")){
				File file=new File(origin);
				if(file.isFile() && file.exists() && file.canRead()){ //判断文件存在和可读
					is = new FileInputStream(file);
				}else{
					pl("coping file: cannot find ",origin);
					return false;
				}
			}else{
				is = leorchn.App.getContext().getAssets().open(origin);
			}
			File op=new File(placeto);
			if(op.exists()){
				if(!op.canWrite()) return false;
				if(op.isDirectory() && !allDelete(op)) return false;
			}else{
				op.getParentFile().mkdirs();
				if(!op.getParentFile().isDirectory()) return false;
			}
			bytes=new FileOutputStream(op);
			int i=-1;
			while((i = is.read()) != -1) bytes.write(i);
			return true;
		}catch(Throwable e){
			pl(E.trace(e));
		}finally{
			try{
				if(is != null)is.close();
				if(bytes != null)bytes.close();
			}catch(Throwable e){
				pl("copy file exception: closing stream error: ",E.trace(e));
			}
		}
		return false;
	}
	public static boolean allDelete(String path){
		return allDelete(new File(path));
	}
	public static boolean allDelete(File dir){
		try{
			if(dir.isDirectory()){
				File[]fl=dir.listFiles();
				for(File f:fl) allDelete(f);
			}
			return dir.delete();
		}catch(Exception e){return false;}
	}
	public static boolean inAllDelete(String path){
		try{
			File f=new File(path);
			if(f.isDirectory()){
				File[]fs=f.listFiles();
				for(File fi:fs) allDelete(fi);
				for(File fi:fs) if(fi.exists()) return false;
				return true;
			}else return false;
		}catch(Exception e){return false;}
	}
}
