package com.jay.cloud_board.meeting_protocal;

public class ProtocolShell {
	private int protocolType;
	private Object body;

	public ProtocolShell(int protocolType, Object body) {
		this.protocolType = protocolType;
		this.body = body;
	}

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}
}
