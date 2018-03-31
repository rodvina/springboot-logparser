package com.ef;

import java.io.IOException;
import java.util.List;

public interface FileReader<T> {

	public List<T> readFileRecordsAndConvert(String filename) throws IOException;
}
