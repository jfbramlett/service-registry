package com.hbo.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Our spring boot main entry point.
 */
@SpringBootApplication
public class Application {

	/**
	 * Our main.
	 *
	 * @param args The command line arguments
	 * @throws Exception Thrown if there is an unexpected error in the application
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
}
