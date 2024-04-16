package com.priya.refactor.codesmelldetector;

import com.priya.refactor.codesmelldetector.processor.DuplicateCodeDetector;
import com.priya.refactor.codesmelldetector.processor.LongMethodDetector;
import com.priya.refactor.codesmelldetector.processor.LongVariableListDetector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CodeSmellDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeSmellDetectorApplication.class, args);
	}

	@Bean
	public LongMethodDetector longMethodDetector() {
		return new LongMethodDetector();
	}
	@Bean
	public DuplicateCodeDetector duplicateCodeDetector() {
		return new DuplicateCodeDetector();
	}
	@Bean
	public LongVariableListDetector longVariableListDetector() {
		return new LongVariableListDetector();
	}
}
