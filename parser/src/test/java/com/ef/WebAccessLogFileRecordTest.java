package com.ef;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class WebAccessLogFileRecordTest {

	@Test
	public void testWebAccessLogFileRecord() {
		String delimitedLine = "date|ip|request|status|useragent";
		WebAccessLogFileRecord actual = new WebAccessLogFileRecord(delimitedLine);
		
		assertThat(actual.getDate(), equalTo("date"));
		assertThat(actual.getIp(), equalTo("ip"));
		assertThat(actual.getRequest(), equalTo("request"));
		assertThat(actual.getStatus(), equalTo("status"));
		assertThat(actual.getUserAgent(), equalTo("useragent"));
					
	}

}
