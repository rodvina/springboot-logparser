package com.ef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a web access log record
 * @author rodneyodvina
 *
 */
public class WebAccessLogFileRecord {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebAccessLogFileRecord.class);
	private String date;
	private String ip;
	private String request;
	private String status;
	private String userAgent;
	
	public WebAccessLogFileRecord(String delimitedLine) {
		super();
		LOGGER.debug("creating record..."+delimitedLine);
		String[] lineArray = delimitedLine.split("\\|");
		this.date = lineArray[0];
		this.ip = lineArray[1];
		this.request = lineArray[2];
		this.status = lineArray[3];
		this.userAgent = lineArray[4];
		
	}
	
	public WebAccessLogFileRecord(String date, String ip, String request, String status, String userAgent) {
		super();
		this.date = date;
		this.ip = ip;
		this.request = request;
		this.status = status;
		this.userAgent = userAgent;
	}

	public WebAccessLogFileRecord() {
		super();
	}

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	
}
