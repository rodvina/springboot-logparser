package com.ef;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class ParserApplication {
	
	@Autowired
	JobBuilderFactory jbf;
	
	@Bean
	public Job job(Step step1, Step step2) {	
		return jbf.get("parser")
				  .incrementer(new RunIdIncrementer())
				  .start(step1)
				  .next(step2)
				  .build();
	}
	
	public static void main(String[] args) {
		
		SpringApplication.run(ParserApplication.class, args);
	}
}
