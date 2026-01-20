package com.ureca.billing.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableKafka
@SpringBootApplication(scanBasePackages = {
    "com.ureca.billing.core",
    "com.ureca.billing.batch"
})
public class BatchModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchModuleApplication.class, args);
	}

}
