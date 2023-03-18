package com.cafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.cafe"
})
public class CafeManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeManagementSystemApplication.class, args);
	}

}
