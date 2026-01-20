package com.ureca.billing.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {
    "com.ureca.billing.core",
    "com.ureca.billing.admin",
    "com.ureca.billing.batch"
})
public class AdminModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminModuleApplication.class, args);
		
	}

}
