package io.github.war501head.amazonparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A simple service built to run as a MicroService that parses Amazon's autocomplete fields and returns a score between 1 and 100 as to how popular the search term is
 * <p>
 *     This score is based on how many letters it takes to find the search term in question, and if the search term when found is the first search in the list.
 * </p>
 *
 * @author Sean K.
 */
@SpringBootApplication
public class AmazonParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmazonParserApplication.class, args);
	}

}
