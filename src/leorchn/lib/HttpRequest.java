/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package leorchn.lib;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class HttpRequest {
	/**	通过调用服务器接口，获得文本数据
		@param method HTTP请求的协议方法
		@param url HTTP请求的地址
		@param header HTTP请求的请求头
		@param formdata HTTP请求尾部携带的额外信息
		@return 服务器返回的文本数据，正常情况下。
		<br/>错误信息，连接发生错误的情况下。
	*/
	public static String http(String method,String url,String reqHeader,String formdata){
		String v8="";//方法：GET、POST等
		try{
			InputStream s=getdataInputStream(method,url,reqHeader,formdata);
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			byte[] readbyte = new byte[1024];
			int readcount;
			while(true){
				if((readcount=s.read(readbyte)) == -1)break;
				outstream.write(readbyte, 0, readcount);
			}
			v8 = new String(outstream.toByteArray(), "UTF8");
			outstream.close();
			s.close();
		}catch(Exception e){
			return E.trace(e);
		}
		return v8;
	}
	/**	通过调用服务器接口，获得输入流。请自行决定何时关闭输入流
		@param method HTTP请求的协议方法
		@param url HTTP请求的地址
		@param header HTTP请求的请求头
		@param formdata HTTP请求尾部携带的额外信息
		@return 服务器返回的数据的输入流，正常情况下。
		<br/>错误信息的文本输入流，连接发生错误的情况下。
	*/
	public static InputStream getdataInputStream(String method,String url,String reqHeader,String formdata){
		InputStream s=null;
		try{
			HttpURLConnection h=(HttpURLConnection)new URL(url).openConnection();
			h.setRequestMethod(method.toUpperCase());
			h.setDoOutput(!formdata.isEmpty());//DoOutput：主体发送开关，如果没有主体则关闭
			h.setConnectTimeout(5000);
			for(String 分条:reqHeader.split("\n"))//自动分割请求头并逐条添加。注意：请不要故意添加错误的请求头
				if(分条.contains(":")){String[]dat=分条.split(":",2);//sys.o.pl("addRequestHead:"+分条.split(": ")[0].trim()+","+分条.split(": ")[1].trim());
					h.setRequestProperty(dat[0].trim(),dat[1].trim());}

			if(!formdata.isEmpty()) h.getOutputStream().write(formdata.getBytes("UTF8"));
			switch(h.getResponseCode()){
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_CREATED:
				case HttpURLConnection.HTTP_ACCEPTED:
					s = h.getInputStream();
					break;
				default:
					s = h.getErrorStream();
			}//不能在此关闭输入流，会造成错误
		}catch(Throwable e){ s=new ByteArrayInputStream(E.trace(e).getBytes()); }
		return s;
	}
	/**	通过解析 deflate 格式压缩过的输入流，获得文本数据。其内部将会自动关闭输入的输入流
		@param is deflate 格式压缩过的输入流
		@return 服务器返回的文本数据，正常情况下。
	*/
	public static String deflateDecoder(InputStream is){
		try{
			byte[]v0=new byte[1024],buffer=new byte[4096];
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int v4;
			while(true){
				if((v4 = is.read(v0)) == -1)break;
				outputStream.write(v0, 0, v4);
			}
			is.close();//必须在此处才能关闭输入流
			Inflater inflater = new Inflater(true);
			inflater.setInput(outputStream.toByteArray());

			outputStream = new ByteArrayOutputStream(outputStream.toByteArray().length);
			while(!inflater.finished()){
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			byte[] output = outputStream.toByteArray();
			outputStream.close();
			return new String(output, "UTF-8");
		}catch(Throwable e){
			return E.trace(e);
		}
    }
}
