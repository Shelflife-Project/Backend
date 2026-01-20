package com.shelflife.project.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shelflife.project.model.Storage;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.repository.UserRepository;

@Component
public class StorageSeeder implements Seeder {
    @Autowired
    private StorageRepository repository;

    @Autowired
    private UserRepository userRepository;

    public void seed() {
        Storage userStorage = new Storage();
        userStorage.setName("Shelf");
        userStorage.setOwner(userRepository.findByEmail("test@test.test").get());

        repository.save(userStorage);

        Storage adminStorage = new Storage();
        adminStorage.setName("Fridge");
        adminStorage.setOwner(userRepository.findByEmail("admin@test.test").get());

        repository.save(adminStorage);
    }
}
