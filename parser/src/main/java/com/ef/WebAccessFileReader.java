package com.ef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WebAccessFileReader implements FileReader<WebAccessLogFileRecord> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebAccessFileReader.class);
	@Override
	public List<WebAccessLogFileRecord> readFileRecordsAndConvert(String filename) throws IOException {
		// read a file using buffered reader, obtain contents as stream, map stream to object, then collect as list
		LOGGER.info("Reading in file, please wait...");
		
//		return Files.newBufferedReader(Paths.get(filename)).lines()
		return Files.lines(Paths.get(filename))
										.map(line -> new WebAccessLogFileRecord(line))
										.collect(Collectors.toList());
		
		
		
	}

}
