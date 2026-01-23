package com.shelflife.project.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.repository.RunningLowRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.repository.UserRepository;

@Component
public class RunningLowSeeder implements Seeder {
    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RunningLowRepository runningLowRepository;

    @Autowired
    private UserRepository userRepository;

    public void seed() {
        User user = userRepository.findByEmail("test@test.test").get();

        Product bread = productRepository.findByNameContainingIgnoreCase("Bread").get(0);

        RunningLowSetting setting = new RunningLowSetting();
        setting.setProduct(bread);
        setting.setStorage(storageRepository.findByOwnerId(user.getId()).get(0));
        setting.setRunningLow(2);

        runningLowRepository.save(setting);
    }
}
