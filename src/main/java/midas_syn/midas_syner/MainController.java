package midas_syn.midas_syner;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import midas_syn.util.PropertiesUtil;

/**
 * 
 * @author sullivan
 *
 */
public class MainController {

	

	@SuppressWarnings(value="all")
	 public static void main(String[] args) throws Exception{  
         

		 String path=System.getProperty("user.dir") + File.separator+"setting.properties";
		Properties properties = PropertiesUtil.getProperties(path);
		final String SERVER = properties.get("REMOTE_SERVER") + "";
		final String UPLOAD_SCAN_INTERVAL = properties.get("UPLOAD_SCAN_INTERVAL") + "";
		final int THREAD_POOL_SIZE = Integer.parseInt(properties.get("THREAD_POOL_SIZE")+"");
		final String RECEIVE_DIR = properties.get("RECEIVE_DIR") + "";
		final String UPLOAD_DIR = properties.get("UPLOAD_DIR") + "";
		final String DB_DRIVER = properties.get("DB_DRIVER") + "";
		final String TRANS_PROTOCOL= properties.get("TRANS_PROTOCOL") + "";
		final String SERVICE_TYPE= properties.get("SERVICE_TYPE") + "";
		final String  SERVER_PORT= properties.get("REMOTE_SERVER_PORT") + "";
		final String  LISTEN_PORT= properties.get("LISTEN_PORT") + "";
		final String  SCAN_SUFFIX= properties.get("SCAN_SUFFIX") + "";
		
		
		   if(Constants.HANDER_TYPE_SERVER.equals(SERVICE_TYPE)||Constants.HANDER_TYPE_CLIENTANDSERVER.equals(SERVICE_TYPE))
	         {
	        	 new Thread(new Runnable() {
	 				public void run() {
	 					 try {
							new TransferServer(Integer.parseInt(LISTEN_PORT)).service(RECEIVE_DIR);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}  //开启服务端监听
	 				}
	 			}).start();
	        	
	         }
		if (Constants.HANDER_TYPE_CLIENT.equals(SERVICE_TYPE)|| Constants.HANDER_TYPE_CLIENTANDSERVER.equals(SERVICE_TYPE)) // 有上传服务器前提才进行上传
		{
			new Thread(new Runnable() {
				public void run() {
					new TransferClient(SERVER,Integer.parseInt(SERVER_PORT),UPLOAD_DIR,Long.parseLong(UPLOAD_SCAN_INTERVAL),SCAN_SUFFIX,THREAD_POOL_SIZE).service();
				}
			}).start();

		}
      
         
}
}
