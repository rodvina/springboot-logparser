package com.ef;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@SpringBootApplication
public class ParserBatchApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParserBatchApplication.class);
	
	@Autowired
	StepBuilderFactory sbf;
	
	@Autowired
	JobBuilderFactory jbf;
	
	@Value("${startDate}")String startDate;
	@Value("${duration}")String duration;
	@Value("${threshhold}")String threshhold;
	
	@Bean
	public ItemReader<WebAccessLogFileRecord> fileReader(@Value("file://${accesslog}")Resource in) {
		return new FlatFileItemReaderBuilder<WebAccessLogFileRecord>()
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
	
	@Bean
	public ItemReader<IpAddrCountRecord> dbReader(DataSource ds) throws Exception {
		
		final String SELECT_BY_DATE_AND_THRESHHOLD = "SELECT IP_ADDR, count(IP_ADDR) CNT " + 
				"	FROM dbo.WEB_LOG " + 
				"	where LOG_DT between ? and ? " + 
				"	group by IP_ADDR " + 
				"	having count(IP_ADDR) >= ? ";
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");
		LocalDateTime start = LocalDateTime.parse(startDate, formatter);
		LocalDateTime end = Util.calculateEndDt(start, duration);
		Integer threshholdValue = Integer.parseInt(threshhold);
		
		return new JdbcCursorItemReaderBuilder<IpAddrCountRecord>()
					.dataSource(ds)
					.name("dbReader")
					.sql(SELECT_BY_DATE_AND_THRESHHOLD)
					.queryArguments(new Object[] {start, end, threshholdValue})
					.rowMapper((rs, rowNum) -> {
						IpAddrCountRecord record = new IpAddrCountRecord();
						record.setIp(rs.getString("IP_ADDR"));
						record.setCount(rs.getInt("CNT"));
						return record;
					})
					.build();
	}
	
	@Bean
	public ItemWriter<IpAddrCountRecord> consoleWriter() {
		
		return new ItemWriter<IpAddrCountRecord>() {
			@Override
			public void write(List<? extends IpAddrCountRecord> items) throws Exception {
				LOGGER.info("IPs that made more than " + threshhold + " requests starting from " + startDate + " for the duration of " + duration);
				items.stream().forEach(r -> LOGGER.info("  " + r.getIp() + ":  " + r.getCount()));
				LOGGER.info("-------------------------------");
				
			}
		};	
	}
	
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
	public Step step2(ItemReader<? extends IpAddrCountRecord> dbReader, 
					ItemWriter<? super IpAddrCountRecord> consoleWriter) {
		return sbf.get("db-query-to-output")
				  .<IpAddrCountRecord, IpAddrCountRecord>chunk(2000)
				  .reader(dbReader)
				  .writer(consoleWriter)
				  .build();
	}
	
	@Bean
	public Job job(Step step1, Step step2) {	
		return jbf.get("parser")
				  .incrementer(new RunIdIncrementer())
				  .start(step1)
				  .next(step2)
				  .build();
	}
	
	static class Util {
		public static LocalDateTime calculateEndDt(LocalDateTime start, String duration) throws Exception {
			LocalDateTime end;
			
			switch (duration) {
				case "hourly":
					end = start.plusHours(1).minusSeconds(1);
					break;
				case "daily":
					end = start.plusHours(24).minusSeconds(1);
					break;
				default:
					throw new Exception("Invalid duration. Use [hourly] or [daily]");
			}
			return end;
		}
	}
	
	public static void main(String[] args) {

		SpringApplication.run(ParserBatchApplication.class, args);
	}
}