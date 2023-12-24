package com.java.MigrationAssessmentReportsBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import generate_pdf.Sample;

@SpringBootApplication
@ComponentScan(basePackages = { "generate_pdf", "com.java.MigrationAssessmentReportsBuilder"})
public class MigrationAssessmentReportsBuilderApplication implements CommandLineRunner {

	
	@Autowired
	private	LevelCalculator LevelCalulator;
	
	@Autowired
	 private ReportTable ReportTable;
	
	@Autowired
	private Sample sample;
	
	public static void mainMethod(String[] args) {
		SpringApplication.run(MigrationAssessmentReportsBuilderApplication.class, args);
	}
	
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
        
		// Find Level for all Id and Store it in
		LevelCalulator.run();
		
		// Create all table with data for report
		ReportTable.run();	
		
		sample.createReport();
		
	}

}
