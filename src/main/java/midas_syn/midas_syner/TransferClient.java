package midas_syn.midas_syner;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @author sullivan
 *
 */
public class TransferClient {

	private static ArrayList<String> fileList = new ArrayList<String>();

	private String sendFilePath = Constants.SEND_FILE_PATH;

	private  int POOL_SIZE = 4; // 单个CPU的线程池大小

	private String serverIp = "";

	private int port = 0;

	private Long interval = 1000L;
	
	private String scanSuffix=".midas";
	

	/**
	 * 带参数的构造器，用户设定需要传送文件的文件夹
	 * 
	 * @param filePath
	 */
	public TransferClient(String sendFilePath) {
		this.sendFilePath = sendFilePath;
	}

	/**
	 * 带参数的构造器，用户设定需要传送文件的文件夹
	 * 
	 * @param filePath
	 */
	public TransferClient(String server, int port) {
		this.serverIp = server;
		this.port = port;

	}

	public TransferClient(String server, int port, String sendFilePath, Long interval,String scanSuffix,int threadsize) {
		this.serverIp = server;
		this.port = port;
		this.sendFilePath = sendFilePath;
		if (interval > this.interval) {
			this.interval = interval;
		}
		if(null!=scanSuffix&&!"".equals(scanSuffix)&&!"_trans_".equals(scanSuffix))
		{
			this.scanSuffix=scanSuffix;
		}
		if(threadsize<4)
		this.POOL_SIZE=threadsize;

	}

	/**
	 * 不带参数的构造器。使用默认的传送文件的文件夹
	 */
	public TransferClient() {

	}

	@SuppressWarnings(value = "all")
	public void service() {
		ExecutorService executorService = Executors.newFixedThreadPool(4);

		while (true) {
			try {
				Thread.currentThread().sleep(interval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getFilePath(sendFilePath);
			if(fileList.size()==0)
				continue;
			Vector<Integer> vector = getRandom(fileList.size());
			for (Integer integer : vector) {
				String filePath = fileList.get(integer.intValue());
				executorService.execute(new SendFileTask(serverIp, port, filePath));
				//fileList.remove(integer.intValue());
			}
		}

	}

	private void getFilePath(String dirPath) {
		fileList.clear();
		File dir = new File(dirPath);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getFilePath(files[i].getAbsolutePath());
			} else {
				if("*".equals(scanSuffix))//匹配所有文件
				{
					if (!files[i].getName().endsWith("_trans_")) {
						fileList.add(files[i].getAbsolutePath());
					}
					
				}
				else{
					if(files[i].getName().endsWith(scanSuffix.trim())){
				     fileList.add(files[i].getAbsolutePath());
					}
				}
			}
		}
	}

	private Vector<Integer> getRandom(int size) {
		Vector<Integer> v = new Vector<Integer>();
		Random r = new Random();
		boolean b = true;
		while (b) {
			int i = r.nextInt(size);
			if (!v.contains(i))
				v.add(i);
			if (v.size() == size)
				b = false;
		}
		return v;
	}



	class SendFileTask implements Runnable {
		private Socket socket = null;
		private String ip = "127.0.0.1";
		private int port = 10000;
		private String filePath = "";

		public SendFileTask(String ip, int port, String filePath) {
			this.ip = ip;
			this.port = port;
			this.filePath = filePath;
		}

		public void run() {
			System.out.println("开始发送文件:" + filePath);
			File oldfile = new File(filePath);
			File file=new File(filePath+"_trans_");
			
		
			if (createConnection()) {
			if (file.exists()) {
				file=new File(filePath+"_trans_"+Math.round(Math.random()*1000));
			}
				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				try {
		
					boolean isReady=oldfile.renameTo(file);
				    if(!isReady)
				    {
				     throw new Exception(filePath+" 文件被占用中,稍后重试上传");
				    // System.out.println(filePath+" 文件被占用中,稍后重试上传");
				   //  return ;
				    }
					DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

					dos.writeUTF(file.getName());
					dos.flush();
					dos.writeLong(file.length());
					dos.flush();

					int read = 0;
					int passedlen = 0;
					long length = file.length(); // 获得要发送文件的长度
				     String processStr="";
				
					while ((read = fis.read(buf)) != -1) {
						passedlen += read;
						String processStrtmp = passedlen * 100L / length + "%";
						if (!processStr.equals(processStrtmp) ) {
							
							System.out.println("已经完成文件 [" + file.getName() + "]百分比: "+ processStrtmp);
						   }
						processStr = processStrtmp;
						dos.write(buf, 0, read);
					}

					dos.flush();
					fis.close();
					dos.close();
					System.out.println("文件 " + filePath + "传输完成!");

					file.delete();
				} catch (Exception e){
					e.printStackTrace();
				}
				finally {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		private boolean createConnection() {
			try {
				socket = new Socket(ip, port);
				System.out.println("连接服务器成功！");
				return true;
			} catch (Exception e) {
				System.out.println("连接服务器失败！");
				return false;
			}
		}
	}

	// public static void main(String[] args){
	// while (true) {
	// try {
	// Thread.currentThread().sleep(5*1000L);
	// new TransferClient().service();
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	//
	// }
	//
	// }
}