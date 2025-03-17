package org.swmaestro.repl.gifthub;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableBatchProcessing
public class GifthubApplication {
    public static void main(String[] args) {
        SpringApplication.run(GifthubApplication.class, args);
    }

}