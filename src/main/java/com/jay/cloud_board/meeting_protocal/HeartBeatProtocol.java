package com.jay.cloud_board.meeting_protocal;

import com.jay.cloud_board.base.Constant;

import java.io.Serializable;

/**
 * @Description 心跳协议
 * Created by jj on 2019/3/4.
 */

public class HeartBeatProtocol implements Serializable, ITcpProtocol {

	private static final long serialVersionUid = Constant.SERIAL_UID_HEART_BEAT;
	private String userId;
	private int protocolType;

	public HeartBeatProtocol(String userId, int protocolType) {
		this.userId = userId;
		this.protocolType = protocolType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	@Override
	public String toString() {
		return "HeartBeatProtocol{" +
				"userId='" + userId + '\'' +
				", protocolType=" + protocolType +
				'}';
	}
}
