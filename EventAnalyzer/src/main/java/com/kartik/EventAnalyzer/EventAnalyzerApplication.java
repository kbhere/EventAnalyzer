package com.kartik.EventAnalyzer;

import com.kartik.EventAnalyzer.Service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventAnalyzerApplication implements CommandLineRunner {

	@Autowired
	EventService eventService;


	public static String filePath;

	private static final Logger logger= LoggerFactory.getLogger(EventAnalyzerApplication.class);
	public static void main(String[] args) {

		validateArguments(args);
		SpringApplication app=new SpringApplication(EventAnalyzerApplication.class);
		app.run(args);
	}


	private static void validateArguments(String[] args) {
		logger.debug("Validating filepath Argument...");
		if (args.length < 1) {
			logger.debug("Setting filepath to default");
			filePath="sample_data.txt";
		}
		else {
			filePath=args[0];
		}

	}

	@Override
	public void run(String... args) {
		eventService.process(filePath);
	}
}
