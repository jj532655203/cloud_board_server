package com.jay.cloud_board.tcp;

import com.google.gson.Gson;
import com.jay.cloud_board.CloudBoardServer;
import com.jay.cloud_board.Logger4j;
import com.jay.cloud_board.base.Constant;
import com.jay.cloud_board.bean.ConnectingSocketInfo;
import com.jay.cloud_board.meeting_protocal.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

public class Writer {

	private static final String TAG = Writer.class.getSimpleName();
	private static ArrayBlockingQueue<ProtocolShell> sBlockingQueue = new ArrayBlockingQueue<>(1000);

	public static void send(ProtocolShell _protocolShell) {

		Logger4j.d(TAG, "send:" + _protocolShell.toString());

		sBlockingQueue.add(_protocolShell);
	}


	public static void startWrite() {

		Logger4j.d(TAG, "startWrite");

		Runnable sWriteRunnable = new Runnable() {
			@Override
			public void run() {

				//工作现场死循环:写
				while (true) {

					if (sBlockingQueue.isEmpty()) continue;

					//要发的协议
					ProtocolShell protocolShell = null;
					try {
						protocolShell = sBlockingQueue.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (protocolShell == null) continue;

					//确定:目标userId
					String receiverUserId = "";
					switch (protocolShell.getProtocolType()) {
						case Constant.PROTOCOL_TYPE_BEART_HEAT:
							receiverUserId = ((HeartBeatProtocol) protocolShell.getBody()).getUserId();
							break;
						case Constant.PROTOCOL_TYPE_ADD_STROKE:
							receiverUserId = ((AddStrokeProtocol) protocolShell.getBody()).getReceiverUserId();
							break;
					}
					if (receiverUserId.equals("")) continue;

					//获取目标的socket
					ConnectingSocketInfo _socketInfo = null;

					for (Iterator<ConnectingSocketInfo> iterator = CloudBoardServer.sConnectingSocketInfos.iterator();
						 iterator.hasNext(); ) {

						ConnectingSocketInfo __socketInfo = iterator.next();
						String userId = __socketInfo.getUserId();
						if (userId != null && userId.equalsIgnoreCase(receiverUserId)) {

							Socket socket = __socketInfo.getSocket();
							if (socket == null || socket.isClosed()) {
								Logger4j.e(TAG, "某个userId的socket被废弃了");
								iterator.remove();
							} else {
								_socketInfo = __socketInfo;
								break;
							}
						}
					}
					if (_socketInfo == null) {
						Logger4j.e(TAG, "放弃一个协议的发送(目标用户不在线)");
						continue;
					}

					//完整协议(数据)
					String msg = new Gson().toJson(protocolShell.getBody());

					//完整协议数据长度
					int length = msg.getBytes().length;
					if (length > 99999) {
						Logger4j.e(TAG, "协议太长,超出前后台组包规范!");
						continue;
					}

					//配上前缀信息,如"$1002"表示完整协议的byte数组长度是1002(暂时支持byte数组长度在4位数以内)
					StringBuilder lengthStr = new StringBuilder(String.valueOf(length));
					while (lengthStr.length() < 5) {
						lengthStr.insert(0, "0");
					}
					msg = "$" + lengthStr + msg;

					Logger4j.d(TAG, "要发的协议:" + msg);

					//写
					BufferedOutputStream bos = null;
					try {
						bos = new BufferedOutputStream(_socketInfo.getSocket().getOutputStream());
						bos.write(msg.getBytes());
						bos.flush();

						Logger4j.d(TAG, "写出去一个完整的协议:" + msg);

					} catch (Exception e) {
						CloudBoardServer.sConnectingSocketInfos.remove(_socketInfo);
						Logger4j.d(TAG, "转发异常" + e.getMessage());
						e.printStackTrace();
					}

				}

			}
		};

		JobExecutor.getInstance().execute(sWriteRunnable);

	}


}
