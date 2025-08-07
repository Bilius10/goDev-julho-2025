package br.com.senior.transport_logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TransportLogisticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransportLogisticsApplication.class, args);
	}

}
