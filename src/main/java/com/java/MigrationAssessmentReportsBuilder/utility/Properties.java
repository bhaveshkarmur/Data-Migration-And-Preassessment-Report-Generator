package com.java.MigrationAssessmentReportsBuilder.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource({"file:resources/report.properties","file:resources/config.properties" })
public class Properties {
	
	@Autowired
	private Environment env;

	public String getProperty(String propKeyName) {
		return env.getProperty(propKeyName);
	}

}
