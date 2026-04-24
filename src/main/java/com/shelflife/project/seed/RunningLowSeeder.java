package com.shelflife.project.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
        Product bread = productRepository.searchProducts("Bread", Pageable.unpaged()).toList().get(0);

        RunningLowSetting setting = new RunningLowSetting();
        setting.setProduct(bread);
        setting.setStorage(storageRepository.findByOwnerId(user.getId()).get(0));
        setting.setRunningLow(2);

        runningLowRepository.save(setting);
    }

    @Override
    public boolean shouldSeed() {
        if (userRepository.findByEmail("test@test.test").isEmpty())
            return false;

        if (productRepository.searchProducts("Bread", Pageable.unpaged()).toList().size() == 0)
            return false;

        return runningLowRepository.count() == 0;
    }
}
