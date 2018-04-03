package com.ef;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class WebAccessLogFileProcessor implements FileProcessor<WebAccessLogFileRecord> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebAccessLogFileProcessor.class);

	@Autowired
	private WebLogRepository repo;
	
	@Override
	public void process(List<WebAccessLogFileRecord> fileRecords, String startDate, String duration, String threshhold) throws Exception {

		// load records to db
		this.loadAll(fileRecords);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");
		LocalDateTime start = LocalDateTime.parse(startDate, formatter);
		LocalDateTime end = this.calculateEndDt(start, duration);
		
		// find IPs that meet date and thresshold criteria
		List<IpAddrCountRecord> results = repo.findIPByDateAndThreshhold(start, end, Integer.parseInt(threshhold));
		
		// print results to console
		results.stream().forEach(r -> LOGGER.info(r.getIp() + ":  " + r.getCount()));
		
		//TODO: if hourly 100, store to db
		
		//TODO: if daily 250, store to db
		
	}

	private LocalDateTime calculateEndDt(LocalDateTime start, String duration) throws Exception {
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
	
	private void loadAll(List<WebAccessLogFileRecord> fileRecords) {
		LOGGER.info("begin batch save of "+ fileRecords.size() + " records...");
		repo.save(fileRecords);
		LOGGER.info("batch save complete.");
	}

}
