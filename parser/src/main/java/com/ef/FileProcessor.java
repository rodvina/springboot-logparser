package com.ef;

import java.util.List;

public interface FileProcessor<T> {

	public void process(List<T> fileRecords, String startDate, String duration, String threshhold) throws Exception;
	
}
