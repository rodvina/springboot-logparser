package com.ef;

import java.util.List;

public class WebAccessLogFileProcessor implements FileProcessor<WebAccessLogFileRecord> {

	@Override
	public void process(List<WebAccessLogFileRecord> fileRecords, String startDate, String duration, String threshhold) {
		// TODO convert startDate to localDateTime
		//		calculate endDate based on duration value (hourly=+1, daily=+24)
		//		convert threshhold to int
		
	}

}
