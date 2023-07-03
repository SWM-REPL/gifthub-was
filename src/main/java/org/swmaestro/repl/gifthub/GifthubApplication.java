package org.swmaestro.repl.gifthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GifthubApplication {

	public static void main(String[] args) {
		SpringApplication.run(GifthubApplication.class, args);
	}

}
