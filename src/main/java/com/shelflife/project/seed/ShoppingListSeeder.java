package com.shelflife.project.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.Storage;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

@Component
public class ShoppingListSeeder implements Seeder {
    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;

    @Override
    public void seed() {
        Storage storage = storageRepository.findById(1L).get();
        Product cookie = productRepository.findById(3L).get();

        ShoppingListItem item = new ShoppingListItem();
        item.setStorage(storage);
        item.setAmountToBuy(3);
        item.setProduct(cookie);
        shoppingListItemRepository.save(item);
    }

    @Override
    public boolean shouldSeed() {
        return shoppingListItemRepository.count() == 0;
    }
}
