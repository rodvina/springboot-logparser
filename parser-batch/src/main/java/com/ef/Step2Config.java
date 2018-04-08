package com.ef;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Batch configuration for step2: query the db for ips based on the startDate, duration, and threshhold, write the results to console 
 * and then to separate table, if applicable.
 * 
 * @author rodneyodvina
 *
 */
@Configuration
public class Step2Config {
	private static final Logger LOGGER = LoggerFactory.getLogger(Step2Config.class);
	
	@Autowired
	StepBuilderFactory sbf;
	
	@Value("${startDate}")String startDate;
	@Value("${duration}")String duration;
	@Value("${threshhold}")String threshhold;
	
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
	public ItemReader<IpAddrCountRecord> dbReader(DataSource ds) throws Exception {
		
		final String SELECT_BY_DATE_AND_THRESHHOLD = "SELECT IP_ADDR, count(IP_ADDR) CNT " + 
				"	FROM dbo.WEB_LOG " + 
				"	where LOG_DT between ? and ? " + 
				"	group by IP_ADDR " + 
				"	having count(IP_ADDR) >= ? ";
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");
		LocalDateTime start = LocalDateTime.parse(startDate, formatter);
		LocalDateTime end = DurationUtil.calculateEndDt(start, duration);
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
	
	static class DurationUtil {
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

}
