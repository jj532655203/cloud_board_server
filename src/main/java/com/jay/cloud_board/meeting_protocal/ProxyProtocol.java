package com.jay.cloud_board.meeting_protocal;

import com.jay.cloud_board.base.Constant;

import java.io.Serializable;

/**
 * Created by jj on 2019/3/5.
 */

public class ProxyProtocol implements Serializable, ITcpProtocol {

	private static final long serialVesionUid = Constant.SERIAL_UID_PROXY_PROTOCOL;
	private int protocolType;
	private String userId;
	private String receiverUserId;


	public String getReceiverUserId() {
		return receiverUserId;
	}

	public void setReceiverUserId(String receiverUserId) {
		this.receiverUserId = receiverUserId;
	}

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "ProxyProtocol{" +
				"protocolType=" + protocolType +
				"userId=" + userId +
				"receiverUserId=" + receiverUserId +
				'}';
	}
}
