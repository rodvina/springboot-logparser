package com.ef;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

public class WebAccessFileReaderTest {

	@Test
	public void testReadFileRecordsAndConvert() throws Exception {
		WebAccessFileReader reader = new WebAccessFileReader();
		List<WebAccessLogFileRecord> actual = reader.readFileRecordsAndConvert("/Users/rodneyodvina/Developer/egit_repo/wallethub-test/parser/src/test/resources/access.log");
		
		assertThat(actual.size(), equalTo(116484));
		
		WebAccessLogFileRecord record = actual.get(0);
		assertThat(record.getDate(), equalTo("2017-01-01 00:00:11.763"));
		assertThat(record.getIp(), equalTo("192.168.234.82"));
		assertThat(record.getRequest(), equalTo("\"GET / HTTP/1.1\""));
		assertThat(record.getStatus(), equalTo("200"));
		assertThat(record.getUserAgent(), equalTo("\"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0\""));
	}

}
