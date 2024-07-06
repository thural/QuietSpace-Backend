package dev.thural.quietspace;

import dev.thural.quietspace.entity.Role;
import dev.thural.quietspace.repository.RoleRepository;
import dev.thural.quietspace.utils.enums.RoleType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class QuietspaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuietspaceApplication.class, args);
    }

}