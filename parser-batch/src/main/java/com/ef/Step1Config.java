package com.ef;

import java.io.IOException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Batch configuration for step1: read the file and store to db.  The accesslog env property or jvm arg will have the path of the file
 * to process.  If none is specified, an empty.log file will be used, in essence skipping this step
 * 
 * @author rodneyodvina
 *
 */
@Configuration
public class Step1Config {
	private static final Logger LOGGER = LoggerFactory.getLogger(Step1Config.class);
	
	@Autowired
	StepBuilderFactory sbf;
	
	@Bean
	public Step step1(ItemReader<? extends WebAccessLogFileRecord> fileReader, 
					ItemWriter<? super WebAccessLogFileRecord> initialDbWriter) {
		return sbf.get("file-to-db")
				  .<WebAccessLogFileRecord, WebAccessLogFileRecord>chunk(2000)
				  .reader(fileReader)
				  .writer(initialDbWriter)
				  .build();
	}
	
	@Bean
	public ItemReader<WebAccessLogFileRecord> fileReader(@Value("file://${accesslog:classpath:/empty.log}")Resource in) throws IOException {
		boolean skip = false;
		if (in.contentLength() == 0) {
			LOGGER.info("No accesslog specified, no new logs to read...");
			skip = true;
		}
		
		//return null to skip fileRead if access log is not specified, nothing to load to db
		return skip ? () -> {return null;} : new FlatFileItemReaderBuilder<WebAccessLogFileRecord>()
				.name("weblog-reader")
				.resource(in)
				.targetType(WebAccessLogFileRecord.class)
				.delimited()
				.delimiter("|")
				.names(new String[]{"date", "ip", "request", "status", "userAgent"})
				.build();
	}
	
	@Bean
	public ItemWriter<WebAccessLogFileRecord> initialDbWriter(DataSource ds) {
		return new JdbcBatchItemWriterBuilder<WebAccessLogFileRecord>()
						.dataSource(ds)
						.sql("INSERT INTO dbo.WEB_LOG (LOG_DT, IP_ADDR, REQUEST, STATUS, USER_AGENT) VALUES (:date, :ip, :request, :status, :userAgent)")
						.beanMapped()
						.build();
					
	}
}
