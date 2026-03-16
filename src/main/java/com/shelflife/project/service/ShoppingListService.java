package com.shelflife.project.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.purchase.ToPurchaseItem;
import com.shelflife.project.dto.shopping.CreateShoppingItemRequest;
import com.shelflife.project.dto.shopping.EditShoppingItemRequest;
import com.shelflife.project.dto.storage.AddItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.ShoppingItemExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

import jakarta.transaction.Transactional;

@Service
public class ShoppingListService {

    @Autowired
    private StorageItemService storageItemService;

    @Autowired
    private ShoppingListItemRepository repository;

    @Autowired
    private StorageAccessService storageAccessService;

    @Autowired
    private StorageGetterService storageGetterService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StorageRepository storageRepository;

    ShoppingListService(StorageItemService storageItemService) {
        this.storageItemService = storageItemService;
    }

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

    @Transactional
    public void addItemsToStorageAndRemove(final long shoppingItemId, final long storageId, User current)
            throws ItemNotFoundException, AccessDeniedException {
        ShoppingListItem item = getItem(shoppingItemId);

        if (!storageAccessService.canAccessStorage(item.getStorage().getId(), current))
            throw new AccessDeniedException("You can't access this storage");

        for(int i = 0; i < item.getAmountToBuy(); i++)
        {
            AddItemRequest request = new AddItemRequest();
            request.setProductId(item.getProduct().getId());
            request.setExpiresAt(LocalDate.now().plusDays(item.getProduct().getExpirationDaysDelta()));
            
            storageItemService.addItemToStorage(storageId, request, current);
        }

        repository.delete(item);
    }

    public List<ToPurchaseItem> getAggregatedForUser(User user) throws AccessDeniedException {
        if (user == null)
            throw new AccessDeniedException(null);

        List<Storage> storages = storageRepository.findAccessibleStorages(user.getId());
        if (storages == null || storages.isEmpty())
            return java.util.Collections.emptyList();

        List<Long> storageIds = new ArrayList<>();
        for (Storage s : storages)
            storageIds.add(s.getId());

        List<ShoppingListItem> items = repository.findByStorageIdIn(storageIds);

        Map<Long, ToPurchaseItem> agg = new HashMap<>();
        for (ShoppingListItem si : items) {
            long pid = si.getProduct().getId();
            ToPurchaseItem t = agg.get(pid);
            if (t == null) {
                t = new ToPurchaseItem();
                t.setProductId(pid);
                t.setProductName(si.getProduct().getName());
                t.setAmountToBuy(si.getAmountToBuy());
                agg.put(pid, t);
            } else {
                t.setAmountToBuy(t.getAmountToBuy() + si.getAmountToBuy());
            }
        }

        return new ArrayList<>(agg.values());
    }
}
