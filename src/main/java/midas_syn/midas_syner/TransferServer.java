package midas_syn.midas_syner;
import java.io.BufferedInputStream;  
    import java.io.BufferedOutputStream;  
    import java.io.DataInputStream;  
    import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;  
    import java.net.ServerSocket;  
    import java.net.Socket;  
    import java.util.concurrent.ExecutorService;  
    import java.util.concurrent.Executors;  
      
    
    /**
     * 
     * @author sullivan
     *
     */
    public class TransferServer {  
      
        private int defaultBindPort = Constants.DEFAULT_BIND_PORT;    //默认监听端口号为10000  
        private int tryBindTimes = 0;           //初始的绑定端口的次数设定为0  
          
        private ServerSocket serverSocket;      //服务套接字等待对方的连接和文件发送  
          
        private ExecutorService executorService;    //线程池  
        private final int POOL_SIZE = 4;            //单个CPU的线程池大小   
        private int port=1025;
          
        /** 
         * 不带参数的构造器，选用默认的端口号 
         * @throws Exception 
         */  
        public TransferServer() throws Exception{  
            try {  
                this.bingToServerPort(defaultBindPort);  
                executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);  
                System.out.println("开辟线程数 ： " + Runtime.getRuntime().availableProcessors() * POOL_SIZE);  
            } catch (Exception e) {  
                throw new Exception("绑定端口不成功!");  
            }  
        }  
          
        /** 
         * 带参数的构造器，选用用户指定的端口号 
         * @param port 
         * @throws Exception 
         */  
        public TransferServer(int port) throws Exception{  
            try {  
            	if (port>this.port) {
            		this.port=port;					
				}
                this.bingToServerPort(this.port);  
                executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);  
            } catch (Exception e) {  
                throw new Exception("绑定端口不成功!");  
            }  
        }  
          
        private void bingToServerPort(int port) throws Exception{  
            try {  
                serverSocket = new ServerSocket(port);  
                System.out.println("服务端口号:"+port);  
                System.out.println("服务启动!");  
            } catch (Exception e) {  
                this.tryBindTimes = this.tryBindTimes + 1;  
                port = port + this.tryBindTimes;  
                if(this.tryBindTimes >= 20){  
                    throw new Exception("您已经尝试很多次了，但是仍无法绑定到指定的端口!请重新选择绑定的默认端口号");  
                }  
                //递归绑定端口  
                this.bingToServerPort(port);  
            }  
        }  
          
        public void service(String savePath){  
            Socket socket = null;  
            while (true) {  
                try {  
                    socket = serverSocket.accept();  
                    executorService.execute(new Handler(socket,savePath));  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
          
      
        class Handler implements Runnable{  
            private Socket socket;  
            private String receivePath=null;
 
              
            public Handler(Socket socket,String receivePath){  
                this.socket = socket;  
                this.receivePath=receivePath;
            }  
      
            public void run() {  
                  
                System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());  
                  
                DataInputStream dis = null;  
                DataOutputStream dos = null;  
      
                int bufferSize = 8192;  
                byte[] buf = new byte[bufferSize];  
                  
                try {  
                    dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));  
                   String savePath = receivePath+File.separator+ dis.readUTF();  
                   savePath=savePath.substring(0,savePath.lastIndexOf("_trans_"));
                    long length = dis.readLong();  
                    File dirpath=new File(receivePath);
                    File filepath=new File(savePath);
                    if (!dirpath.exists()) {
                    	dirpath.mkdirs();						
					}
                    if (filepath.exists()) {
                    	savePath=savePath+Math.round(Math.random()*1000);
					}
                    dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savePath)));  
                  
                    int read = 0;  
                    long passedlen = 0;  
                    String processStr="";
                    while ((read = dis.read(buf)) != -1) {  
                        passedlen += read;  
                        dos.write(buf, 0, read);  
					String processStrtmp = passedlen * 100L / length + "%";
					if (!processStr.equals(processStrtmp) ) {
						
						System.out.println("文件[" + savePath + "]已经接收: " + processStrtmp);
					   }
					processStr = processStrtmp;
                    }  
                    System.out.println("文件: " + savePath + "接收完成!");  
                      
                } catch (Exception e) {  
                    e.printStackTrace();  
                    System.out.println("接收文件失败!");  
                }finally{  
                    try {  
                        if(dos != null){  
                            dos.close();  
                        }  
                        if(dis != null){  
                            dis.close();  
                        }  
                        if(socket != null){  
                            socket.close();  
                        }  
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }  
                }  
            }  
        }  
          
//        public static void main(String[] args) throws Exception{  
//            new TransferServer().service();  
//        }  
    }  