package br.com.senior.transport_logistics;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
		info = @Info(
				title = "LogiTrack API",
				version = "1.0",
				description = "Documentação da API LogiTrack"
		)
)
public class TransportLogisticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransportLogisticsApplication.class, args);
	}

}
