package com.ef;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

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
		WebAccessLogFileRecord record = new WebAccessLogFileRecord(delimitedLine);
		repo.save(record);
		
		int rowCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, "dbo.WEB_LOG");
		assertThat(rowCount, equalTo(4));
		
		int matchCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "dbo.WEB_LOG", "IP_ADDR = '192.0.168.1'");
		assertThat(matchCount, equalTo(1));
		
	}

}
