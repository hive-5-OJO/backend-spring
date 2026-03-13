package org.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling // 스케줄링 활성화
@SpringBootApplication
public class OjoApplication {
	public static void main(String[] args) {
		SpringApplication.run(OjoApplication.class, args);
	}


}