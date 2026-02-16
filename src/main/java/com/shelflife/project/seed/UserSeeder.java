package com.shelflife.project.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

@Component
public class UserSeeder implements Seeder {
    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    public void seed() {
        User user = new User();
        user.setEmail("test@test.test");
        user.setUsername("test user");
        user.setPassword(encoder.encode("test123"));
        user.setAdmin(false);

        repository.save(user);

        User admin = new User();
        admin.setEmail("admin@test.test");
        admin.setUsername("test admin");
        admin.setPassword(encoder.encode("test123"));
        admin.setAdmin(true);

        repository.save(admin);
    }

    @Override
    public boolean shouldSeed() {
        return repository.count() == 0;
    }
}
