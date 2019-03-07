package com.jay.cloud_board;

import com.jay.cloud_board.base.Config;
import com.jay.cloud_board.bean.ConnectingSocketInfo;
import com.jay.cloud_board.tcp.Reader;
import com.jay.cloud_board.tcp.Writer;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class CloudBoardServer {

	private static final String TAG = CloudBoardServer.class.getSimpleName();
	public static volatile CopyOnWriteArrayList<ConnectingSocketInfo> sConnectingSocketInfos = new CopyOnWriteArrayList<>();

	public static void main(String args[]) {
		initLog4j();

		Writer.startWrite();
		try {
			ServerSocket serverSocket = new ServerSocket(Config.port);

			//循环接受:不同客户端连接
			while (true) {
				Socket acceptSocket = serverSocket.accept();
//				acceptSocket.setSendBufferSize(10240);
				ConnectingSocketInfo connectingSocketInfo = new ConnectingSocketInfo(acceptSocket, "");
				String newAddress = connectingSocketInfo.getSocket().getInetAddress().getHostAddress();

				//如果已经有这个ip的socketInfo,使之不可用(暂时)
				for (Iterator<ConnectingSocketInfo> iterator = sConnectingSocketInfos.iterator(); iterator.hasNext(); ) {
					ConnectingSocketInfo next = iterator.next();
					Socket socket = next.getSocket();
					String hostAddress = socket.getInetAddress().getHostAddress();
					if (newAddress.equals(hostAddress)) {
						Logger4j.d(TAG, "旧i:" + hostAddress + "-----新ip:" + newAddress);
						next.setUserId("");
						next.getSocket().close();
					}
				}

				sConnectingSocketInfos.add(connectingSocketInfo);

				Logger4j.d(TAG, "一个用户连接进来 IP地址=" + acceptSocket.getInetAddress().getHostAddress());

				Reader.read(connectingSocketInfo);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		//高效方法,暂时不用
		/*try {
			new EchoServer(Config.port).run();
		} catch (Exception e) {
			e.printStackTrace();
		}*/

	}

	private static void initLog4j() {
		Logger logger = Logger.getLogger(CloudBoardServer.class);
		SimpleLayout layout = new SimpleLayout();
		//HTMLLayout  layout = new HTMLLayout();
		FileAppender appender = null;
		try {
			//把输出端配置到out.txt
			appender = new FileAppender(layout, Config.log_path, true);
		} catch (Exception e) {
		}
		logger.addAppender(appender);//添加输出端
		//BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);//覆盖配置文件中的级别

		Logger4j.setLogger(logger);

		logger.debug("debug test");
		logger.info("info");
		logger.warn("warn");
		logger.error("error");
		logger.fatal("fatal");
	}

}
