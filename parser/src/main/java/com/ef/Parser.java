package com.ef;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Parser {

	public static void main(String[] args) {
		SpringApplication.run(Parser.class, args);
	}
	
//	@Bean
	CommandLineRunner runner(FileReader<WebAccessLogFileRecord> reader, FileProcessor<WebAccessLogFileRecord> processor){
		return args -> {
			System.out.println("CommandLineRunner running in the Parser class...");
			Arrays.asList(args).stream().forEach(System.out::println);
			
			String startDate = System.getProperty("startDate");
			String duration = System.getProperty("duration");
			String threshhold = System.getProperty("threshhold");
			String accesslog = System.getProperty("accesslog");
			
			System.out.println("startDate="+startDate);
			System.out.println("duration="+duration);
			System.out.println("threshhold="+threshhold);
			System.out.println("accesslog="+accesslog);
			
			System.out.println("Begin processing...");
			processor.process(reader.readFileRecordsAndConvert(accesslog), startDate, duration, threshhold);
			
		};
	}
}
