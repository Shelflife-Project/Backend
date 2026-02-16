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

        log.info("Starting database seeding...");

        if (userSeeder.shouldSeed()) {
            userSeeder.seed();
            log.info("Users successfully seeded");
        } else {
            log.info("Users already exist; skipping user seeder.");
        }

        if (productSeeder.shouldSeed()) {
            productSeeder.seed();
            log.info("Products successfully seeded");
        } else {
            log.info("Products already exist; skipping product seeder.");
        }

        if (storageSeeder.shouldSeed()) {
            storageSeeder.seed();
            log.info("Storages successfully seeded");
        } else {
            log.info("Storages already exist; skipping storage seeder.");
        }

        if (storageItemSeeder.shouldSeed()) {
            storageItemSeeder.seed();
            log.info("Storage items successfully seeded");
        } else {
            log.info("Storage items already exist; skipping storage item seeder.");
        }

        if (runningLowSeeder.shouldSeed()) {
            runningLowSeeder.seed();
            log.info("Running low successfully seeded");
        } else {
            log.info("Running low settings already exist; skipping running low seeder.");
        }

        log.info("Database seeding process finished");
    }
}
