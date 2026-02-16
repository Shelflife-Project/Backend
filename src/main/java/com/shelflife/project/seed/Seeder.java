package com.shelflife.project.seed;

public interface Seeder {
    void seed();
    
    default boolean shouldSeed() {
        return true;
    }
}
