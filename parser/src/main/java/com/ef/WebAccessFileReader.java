package com.ef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class WebAccessFileReader implements FileReader<WebAccessLogFileRecord> {

	@Override
	public List<WebAccessLogFileRecord> readFileRecordsAndConvert(String filename) throws IOException {
		// read a file using buffered reader, obtain contents as stream, map stream to object, then collect as list
		return Files.newBufferedReader(Paths.get(filename)).lines()
										.map(line -> new WebAccessLogFileRecord(line))
										.collect(Collectors.toList());
	}

}
