package com.shelflife.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.shelflife.project.dto.storage.StorageSummary;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StorageRepositorySummaryTests {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageMemberRepository storageMemberRepository;

    User owner;
    User memberUser;
    User user;

    Storage ownerStorage;
    StorageMember memberObj;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setAdmin(false);
        owner.setEmail("owner@test.test");
        owner.setUsername("owner");
        owner.setPassword("asd");
        userRepository.save(owner);

        memberUser = new User();
        memberUser.setAdmin(false);
        memberUser.setEmail("member@test.test");
        memberUser.setUsername("member");
        memberUser.setPassword("asd");
        userRepository.save(memberUser);

        user = new User();
        user.setAdmin(false);
        user.setEmail("user@test.test");
        user.setUsername("user");
        user.setPassword("asd");
        userRepository.save(user);

        ownerStorage = new Storage();
        ownerStorage.setName("Fridge");
        ownerStorage.setOwner(owner);
        storageRepository.save(ownerStorage);

        memberObj = new StorageMember();
        memberObj.setStorage(ownerStorage);
        memberObj.setUser(memberUser);
        memberObj.setAccepted(true);
        storageMemberRepository.save(memberObj);
    }

    @Test
    void returnsStorages() {
        Page<StorageSummary> storages = storageRepository.searchAllSummaries("", Pageable.unpaged());
        
        assertEquals(1, storages.get().findFirst().get().getMemberCount());
        assertEquals(1, storages.getSize());
    }

    @Test
    void returnsEmptyForInvalidUser() {
        Page<StorageSummary> storages = storageRepository.findAccessibleStorageSummaries(0, "",
                Pageable.unpaged());

        assertEquals(0, storages.getSize());
    }

    @Test
    void returnsEmptyAsNonMember() {
        Page<StorageSummary> storages = storageRepository.findAccessibleStorageSummaries(user.getId(), "",
                Pageable.unpaged());

        assertEquals(0, storages.getSize());
    }

    @Test
    void returnsEmptyAsNotAcceptedMember() {
        StorageMember userMember = new StorageMember();
        userMember.setAccepted(false);
        userMember.setStorage(ownerStorage);
        userMember.setUser(user);
        storageMemberRepository.save(userMember);

        Page<StorageSummary> storages = storageRepository.findAccessibleStorageSummaries(user.getId(), "",
                Pageable.unpaged());
        assertEquals(0, storages.getSize());
    }

    @Test
    void returnsStoragesAsOwner() {
        Page<StorageSummary> storages = storageRepository.findAccessibleStorageSummaries(owner.getId(), "",
                Pageable.unpaged());
        assertEquals(1, storages.getSize());
    }

    @Test
    void returnsStoragesAsMember() {
        Page<StorageSummary> storages = storageRepository.findAccessibleStorageSummaries(memberUser.getId(), "",
                Pageable.unpaged());
        assertEquals(1, storages.getSize());
    }
}
