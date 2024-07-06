package dev.thural.quietspace.bootstrap;

import dev.thural.quietspace.entity.Role;
import dev.thural.quietspace.repository.RoleRepository;
import dev.thural.quietspace.utils.enums.RoleType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleLoader implements CommandLineRunner {
    private final RoleRepository roleRepository;

    public RoleLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadUserRole();
    }

    private void loadUserRole() {
        if (roleRepository.findByName(RoleType.USER.toString()).isEmpty()) {
            roleRepository.save(Role.builder()
                    .name(RoleType.USER.toString())
                    .build());
        }
    }
}
