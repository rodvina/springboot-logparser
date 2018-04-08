package com.ef;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(ParserApplication.class)
@TestPropertySource(properties= {
					"startDate=2017-01-01.15:00:00", 
					"duration=hourly", 
					"threshhold=100", 
					"accesslog=/Users/rodneyodvina/Developer/egit_repo/wallethub-test/parser-batch/src/test/resources/access.log",
					"spring.datasource.username=weblog",
					"spring.datasource.password=weblog_ro"
					})
public class ParserApplicationTests {

	@Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Configuration
	static class Config {
		@Bean
		public JobLauncherTestUtils testUtils() {
			return new JobLauncherTestUtils();
		}
	}
	@Test
	public void contextLoads() {
	}

	@Ignore
	@Test
	public void testJob() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
}
