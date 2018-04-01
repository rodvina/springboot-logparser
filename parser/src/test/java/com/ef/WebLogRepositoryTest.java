package com.ef;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

@ActiveProfiles("h2")
@RunWith(SpringRunner.class)
@JdbcTest
@Import(WebLogRepository.class)
public class WebLogRepositoryTest {
	
	@Autowired
	private WebLogRepository repo;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void testSave() {
		String delimitedLine = "2017-01-01 00:00:11.763|192.0.168.1|request|200|useragent";
		String delimitedLine2 = "2017-01-02 00:00:11.763|192.255.168.1|yoyo|200|useragent2";
		String delimitedLine3 = "2017-02-01 00:00:11.763|192.123.168.1|watwat|200|useragent3";
		
		WebAccessLogFileRecord record = new WebAccessLogFileRecord(delimitedLine);
		WebAccessLogFileRecord record2 = new WebAccessLogFileRecord(delimitedLine2);
		WebAccessLogFileRecord record3 = new WebAccessLogFileRecord(delimitedLine3);
		
		int[] batchCount = repo.save(Arrays.asList(new WebAccessLogFileRecord[] {record, record2, record3}));
		
		assertThat(batchCount.length, equalTo(3));
		
		int rowCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, "dbo.WEB_LOG");
		assertThat(rowCount, equalTo(6));
		
		int matchCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "dbo.WEB_LOG", "IP_ADDR = '192.0.168.1'");
		assertThat(matchCount, equalTo(1));
		
	}
	
	@Test
	public void testFindByIp() {
		List<WebAccessLogFileRecord> actual = repo.findByIpAddr("192.168.234.82");
		
		assertThat(actual.size(), equalTo(2));
		
	}

}
