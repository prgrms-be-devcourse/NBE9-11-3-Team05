package com.team05.petmeeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class PetMeetingApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetMeetingApplication.class, args);
	}

}
