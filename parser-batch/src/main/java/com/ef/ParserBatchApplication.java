package com.ef;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
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
						record.setComments(duration + " threshhold of " + threshhold + " crossed, appearing " + record.getCount() + " times.");
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
	public ItemWriter<IpAddrCountRecord> dbThreshholdWriter(DataSource ds) {
		final String INSERT_HOURLY_100 = "INSERT INTO dbo.WEB_LOG_HRLY_100 (IP_ADDR, COMMENTS) "
				+ "VALUES (:ip, :comments) ";
		final String INSERT_DAILY_250 = "INSERT INTO dbo.WEB_LOG_DLY_250 (IP_ADDR, COMMENTS) "
				+ "VALUES (:ip, :comments) ";
		
		String sql = null;
		if ("hourly".equals(duration) && "100".equals(threshhold)) {
			sql = INSERT_HOURLY_100;
		} else if ("daily".equals(duration) && "250".equals(threshhold)) {
			sql = INSERT_DAILY_250;
		}
		
		return sql == null ? (i) -> {} : new JdbcBatchItemWriterBuilder<IpAddrCountRecord>()
						.dataSource(ds)
						.sql(sql)
						.beanMapped()
						.build();
					
	}
	
	@Bean
	public ItemWriter<IpAddrCountRecord> compositeWriter(ItemWriter<IpAddrCountRecord> consoleWriter, ItemWriter<IpAddrCountRecord> dbThreshholdWriter) {
		List<ItemWriter<? super IpAddrCountRecord>> writerList = new ArrayList<>();
		writerList.add(consoleWriter);
		writerList.add(dbThreshholdWriter);
		return new CompositeItemWriterBuilder<IpAddrCountRecord>().delegates(writerList).build();
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
					ItemWriter<IpAddrCountRecord> compositeWriter) {
		return sbf.get("db-query-to-output")
				  .<IpAddrCountRecord, IpAddrCountRecord>chunk(2000)
				  .reader(dbReader)
				  .writer(compositeWriter)
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
