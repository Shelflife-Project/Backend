package com.shelflife.project.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedRunner implements ApplicationRunner {

    @Autowired
    private UserSeeder userSeeder;

    @Autowired
    private ProductSeeder productSeeder;

    @Autowired
    private StorageSeeder storageSeeder;

    @Autowired
    private StorageItemSeeder storageItemSeeder;

    @Autowired
    private RunningLowSeeder runningLowSeeder;

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.getOptionValues("seed") == null)
            return;

        userSeeder.seed();
        log.info("Users successfully seeded");

        productSeeder.seed();
        log.info("Products successfully seeded");

        storageSeeder.seed();
        log.info("Storages successfully seeded");

        storageItemSeeder.seed();
        log.info("Storage items successfully seeded");

        runningLowSeeder.seed();
        log.info("Running low successfully seeded");
    }
}
