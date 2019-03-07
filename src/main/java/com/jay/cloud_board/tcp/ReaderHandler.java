package com.jay.cloud_board.tcp;

import com.google.gson.Gson;
import com.jay.cloud_board.CloudBoardServer;
import com.jay.cloud_board.Logger4j;
import com.jay.cloud_board.base.Constant;
import com.jay.cloud_board.bean.ConnectingSocketInfo;
import com.jay.cloud_board.meeting_protocal.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;


public class ReaderHandler {


	private static final String TAG = ReaderHandler.class.getSimpleName();

	public void handleProtocol(ConnectingSocketInfo connectingSocketInfo, String jsonStr) {
		Logger4j.d(TAG, "handleProtocol 协议jsonStr=" + jsonStr);

		ProxyProtocol protocol = new Gson().fromJson(jsonStr, ProxyProtocol.class);
		int protocolType = protocol.getProtocolType();

		Logger4j.d(TAG, "handleProtocol 协议type=" + protocolType);

		switch (protocolType) {

			//心跳 协议
			case Constant.PROTOCOL_TYPE_BEART_HEAT:

				//给客户端回复心跳协议
				HeartBeatProtocol _hartBeatProtocol = new Gson().fromJson(jsonStr, HeartBeatProtocol.class);
				HeartBeatProtocol heartBeatProtocol = new HeartBeatProtocol(_hartBeatProtocol.getUserId(), _hartBeatProtocol.getProtocolType());
				Writer.send(new ProtocolShell(heartBeatProtocol.getProtocolType(), heartBeatProtocol));
				break;

			//登录/切换账号 协议
			case Constant.PROTOCOL_TYPE_LOGIN:
				String userId = new Gson().fromJson(jsonStr, LoginProtocol.class).getUserId();

				//新账号推掉旧账号
				for (Iterator<ConnectingSocketInfo> iterator = CloudBoardServer.sConnectingSocketInfos.iterator(); iterator.hasNext(); ) {
					ConnectingSocketInfo next = iterator.next();
					if (next.getUserId() != null && next.getUserId().equalsIgnoreCase(userId)) {
						Logger4j.d(TAG, "push an old socket out:" + userId);
						next.setUserId(Constant.LOST);
//						next.getSocket().close();
					}
				}

				connectingSocketInfo.setUserId(userId);
				break;

			//新增笔划协议
			case Constant.PROTOCOL_TYPE_ADD_STROKE:
				Logger4j.d(TAG, "handle add stroke protocol:");
				AddStrokeProtocol addStrokeProtocol = new Gson().fromJson(jsonStr, AddStrokeProtocol.class);
				Writer.send(new ProtocolShell(addStrokeProtocol.getProtocolType(), addStrokeProtocol));
				break;

		}

	}
}
