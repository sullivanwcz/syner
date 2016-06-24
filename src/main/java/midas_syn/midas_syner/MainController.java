package midas_syn.midas_syner;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import midas_syn.util.PropertiesUtil;

public class MainController {

	

	@SuppressWarnings(value="all")
	 public static void main(String[] args) throws Exception{  
         

		 String path=System.getProperty("user.dir") + File.separator+"setting.properties";
		Properties properties = PropertiesUtil.getProperties(path);
		final String SERVER = properties.get("REMOTE_SERVER") + "";
		final String UPLOAD_SCAN_INTERVAL = properties.get("UPLOAD_SCAN_INTERVAL") + "";
		final String THREAD_POOL_SIZE = properties.get("THREAD_POOL_SIZE") + "";
		final String DATA_FILE_DIR = properties.get("DATA_FILE_DIR") + "";
		final String UPLOAD_DIR = properties.get("UPLOAD_DIR") + "";
		final String DB_DRIVER = properties.get("DB_DRIVER") + "";
		final String TRANS_PROTOCOL= properties.get("TRANS_PROTOCOL") + "";
		final String HANDER_TYPE= properties.get("HANDER_TYPE") + "";
		final String  SERVER_PORT= properties.get("REMOTE_SERVER_PORT") + "";
		final String  LOCAL_SERVER_PORT= properties.get("LOCAL_SERVER_PORT") + "";
		
		
		   if(Constants.HANDER_TYPE_SERVER.equals(HANDER_TYPE)||Constants.HANDER_TYPE_CLIENTANDSERVER.equals(HANDER_TYPE))
	         {
	        	 new Thread(new Runnable() {
	 				public void run() {
	 					 try {
							new TransferServer(Integer.parseInt(LOCAL_SERVER_PORT)).service(DATA_FILE_DIR);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}  //开启服务端监听
	 				}
	 			}).start();
	        	
	         }
		if (Constants.HANDER_TYPE_CLIENT.equals(HANDER_TYPE)|| Constants.HANDER_TYPE_CLIENTANDSERVER.equals(HANDER_TYPE)) // 有上传服务器前提才进行上传
		{
			new Thread(new Runnable() {
				public void run() {
					new TransferClient(SERVER,Integer.parseInt(SERVER_PORT),UPLOAD_DIR,Long.parseLong(UPLOAD_SCAN_INTERVAL)).service();
				}
			}).start();

		}
      
         
}
}
