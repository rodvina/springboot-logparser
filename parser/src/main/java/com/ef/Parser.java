package com.ef;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Parser {
	private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);
	
	public static void main(String[] args) {
		SpringApplication.run(Parser.class, args);
	}
	
	@Bean
	CommandLineRunner runner(FileReader<WebAccessLogFileRecord> reader, FileProcessor<WebAccessLogFileRecord> processor){
		return args -> {
			LOGGER.info("CommandLineRunner running in the Parser class...");
			Arrays.asList(args).stream().forEach(LOGGER::info);
			
			String startDate = System.getProperty("startDate");
			String duration = System.getProperty("duration");
			String threshhold = System.getProperty("threshhold");
			String accesslog = System.getProperty("accesslog");
			
			LOGGER.info("startDate="+startDate);
			LOGGER.info("duration="+duration);
			LOGGER.info("threshhold="+threshhold);
			LOGGER.info("accesslog="+accesslog);
			
			LOGGER.info("Begin processing...");
			
			processor.process(reader.readFileRecordsAndConvert(accesslog), startDate, duration, threshhold);
			
		};
	}
}
