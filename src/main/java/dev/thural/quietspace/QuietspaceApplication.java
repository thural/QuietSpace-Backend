package dev.thural.quietspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class QuietspaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuietspaceApplication.class, args);
    }

}