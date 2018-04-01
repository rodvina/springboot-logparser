package com.ef;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebAccessLogFileProcessor implements FileProcessor<WebAccessLogFileRecord> {

	@Autowired
	private WebLogRepository repo;
	
	@Override
	public void process(List<WebAccessLogFileRecord> fileRecords, String startDate, String duration, String threshhold) {
		// TODO convert startDate to localDateTime
		//		calculate endDate based on duration value (hourly=+1, daily=+24)
		//		convert threshhold to int
		
		//		load records to db
		this.loadAll(fileRecords);
		
	}
	
	private void loadAll(List<WebAccessLogFileRecord> fileRecords) {
		repo.save(fileRecords);
	}

}
