package com.jay.cloud_board.bean;

import java.net.Socket;

public class ConnectingSocketInfo {

	private Socket socket;
	private String userId;

	public ConnectingSocketInfo(Socket socket, String userId) {
		this.socket = socket;
		this.userId = userId;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "ConnectingSocketInfo{" +
				", addr='" + socket.getRemoteSocketAddress() + '\'' +
				", userId='" + userId + '\'' +
				'}';
	}
}
