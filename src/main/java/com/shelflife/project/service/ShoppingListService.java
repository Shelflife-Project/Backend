package com.shelflife.project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.shopping.CreateShoppingItemRequest;
import com.shelflife.project.dto.shopping.EditShoppingItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.ShoppingItemExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;

import jakarta.transaction.Transactional;

@Service
public class ShoppingListService {
    @Autowired
    private ShoppingListItemRepository repository;

    @Autowired
    private StorageAccessService storageAccessService;

    @Autowired
    private StorageGetterService storageGetterService;

    @Autowired
    private ProductService productService;

    public ShoppingListItem getItem(final long id) throws ItemNotFoundException {
        Optional<ShoppingListItem> s = repository.findById(id);
        if (!s.isPresent())
            throw new ItemNotFoundException("id", "Shopping list item with this id was not found");
        return s.get();
    }

    public List<ShoppingListItem> getForStorage(final long storageId, User current)
            throws AccessDeniedException {
        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        return repository.findByStorageId(storageId);
    }

    @Transactional
    public ShoppingListItem createItem(final long storageId, CreateShoppingItemRequest request, User current)
            throws AccessDeniedException, ItemNotFoundException {
        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        Storage storage = storageGetterService.getStorage(storageId);
        Product product = productService.getProductByID(request.getProductId());

        if (repository.existsByProductIdAndStorageId(product.getId(), storage.getId()))
            throw new ShoppingItemExistsException("productId");

        if (request.getAmountToBuy() < 0)
            throw new IllegalArgumentException("amountToBuy");

        ShoppingListItem s = new ShoppingListItem();
        s.setStorage(storage);
        s.setProduct(product);
        s.setAmountToBuy(request.getAmountToBuy());

        return repository.save(s);
    }

    @Transactional
    public ShoppingListItem editItem(final long itemId, EditShoppingItemRequest request, User current)
            throws AccessDeniedException, ItemNotFoundException {
        ShoppingListItem item = getItem(itemId);

        if (!storageAccessService.canAccessStorage(item.getStorage().getId(), current))
            throw new AccessDeniedException("You can't access this storage");

        if (request.getAmountToBuy() < 0)
            throw new IllegalArgumentException("amountToBuy");

        item.setAmountToBuy(request.getAmountToBuy());
        return repository.save(item);
    }

    @Transactional
    public void deleteItem(final long itemId, User current) throws ItemNotFoundException, AccessDeniedException {
        ShoppingListItem item = getItem(itemId);

        if (!storageAccessService.canAccessStorage(item.getStorage().getId(), current))
            throw new AccessDeniedException("You can't access this storage");

        repository.delete(item);
    }

    public List<ShoppingListItem> getShoppingListItemsAggregated(User current) throws AccessDeniedException {
        Page<Storage> storages = storageGetterService.getStorages(current, "", Pageable.unpaged());

        List<ShoppingListItem> items = new ArrayList<>();

        for (Storage storage : storages) {
            List<ShoppingListItem> sItems = getForStorage(storage.getId(), current);
            items.addAll(sItems);
        }

        return items;
    }
}
