package leorchn.lib;
import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import java.io.*;
import leorchn.App;
import android.provider.*;
import android.media.*;

// 最低编译版本 应该是8，受到 MediaScannerConnection 限制
public class Media extends Impl{
	/**	通过转换其他程序获得的 content:// 链接，获得真实路径的 File 封装。可能需要读取权限
		@param a 通常这只能允许窗口内操作
		@param u Uri，需要 content:// 链接
		@return 该文件的真实路径的 File 封装
	*/
	public static File Uri2File(Activity a,Uri u){ return Impl.Uri2File(a,u); }
	/**	通过转换其他程序获得的 content:// 链接，获得真实路径。可能需要读取权限
		@param a 通常这只能允许窗口内操作
		@param u Uri，需要 content:// 链接
		@return 该文件的真实路径
	*/
	public static String Uri2StringPath(Activity a,Uri u){ return Impl.Uri2StringPath(a,u); }
	//@Deprecated public static void requestForRefreshGallery(Activity a,Uri u){ Impl.requestForRefreshGallery(a,u); }
	/**	无残留地删除图库里显示的一个图片或视频文件，通常建议在 AsyncTask 里运行。需要读写权限
		@param a 通常这只能允许窗口内操作
		@param u Uri，需要 content:// 链接
		@return true，如果删除成功
		<br/>false，如果删除失败（没有文件被删除）
	*/
	public static boolean deleteContentFile(Activity a,Uri u){ return Impl.deleteContentFile(a,u); }
}
class Impl{
	protected static File Uri2File(Activity a,Uri u){
		return new File(Uri2StringPath(a,u));
	}
	protected static String Uri2StringPath(Activity a,Uri u){
		String[]type={MediaStore.Images.Media.DATA};
		Cursor imgcur=a.managedQuery(u,type,null,null,null);
		int index=imgcur.getColumnIndexOrThrow(type[0]);
		imgcur.moveToFirst();
		return imgcur.getString(index);
	}
	/*protected static void requestForRefreshGallery(Activity a,Uri u){
		String[]p={Environment.getExternalStorageDirectory().toString()};
		if(u.getScheme().equals("content"))
			p[0]=Uri2File(a,u).getPath();
		Activity1.tip(Activity1.string("开始清理缓存\n",MediaStore.Images.Media.DATA,"=\"",p[0],"\""));
		Activity1.tip(""+
		App.getContext()
			.getContentResolver()
			.delete(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			Activity1.string(MediaStore.Images.Media.DATA,"=\"",p[0],"\""),
			null));
		/*MediaScannerConnection.scanFile(
			App.getContext(),
			p,
			null,
			new MediaScannerConnection.OnScanCompletedListener(){
				@Override public void onScanCompleted(String p1, Uri p2) {
					Activity1.tip("缓存清理完成");
				}
			});
		//App.getContext().sendBroadcast(new Intent());
	}*/
	protected static boolean deleteContentFile(Activity a,Uri u){ // 1 为成功
		return App.getContext() // 返回已成功删除的条目数，一般大于 0 即为成功
			.getContentResolver()
			.delete(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				Activity1.string(MediaStore.MediaColumns.DATA,"=\"",Uri2StringPath(a,u),"\""),
				null) > 0;
	}
}
