package dev.thural.quietspace;

import dev.thural.quietspace.entity.Role;
import dev.thural.quietspace.repository.RoleRepository;
import dev.thural.quietspace.utils.enums.RoleType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
public class QuietspaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuietspaceApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName(RoleType.USER.toString()).isEmpty()) {
                roleRepository.save(Role.builder().name(RoleType.USER.toString()).build());
            }
        };
    }

}