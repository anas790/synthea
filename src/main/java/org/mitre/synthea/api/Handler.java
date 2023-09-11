package org.mitre.synthea.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Handler {
  public static void main(String[] args) {
		SpringApplication.run(Handler.class, args);
	}
}
