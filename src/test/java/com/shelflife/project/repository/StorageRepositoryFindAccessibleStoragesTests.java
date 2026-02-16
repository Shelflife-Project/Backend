package com.shelflife.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StorageRepositoryFindAccessibleStoragesTests {
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
    void returnsEmptyForInvalidUser() {
        List<Storage> storages = storageRepository.findAccessibleStorages(-1);

        assertEquals(0, storages.size());
    }

    @Test
    void returnsEmptyAsNonMember() {
        List<Storage> storages = storageRepository.findAccessibleStorages(user.getId());

        assertEquals(0, storages.size());
    }

    @Test
    void returnsZeroAsNotAcceptedMember() {
        StorageMember userMember = new StorageMember();
        userMember.setAccepted(false);
        userMember.setStorage(ownerStorage);
        userMember.setUser(user);
        storageMemberRepository.save(userMember);

        List<Storage> storages = storageRepository.findAccessibleStorages(user.getId());
        assertEquals(0, storages.size());
    }

    @Test
    void returnsStoragesAsOwner() {
        List<Storage> storages = storageRepository.findAccessibleStorages(owner.getId());
        assertEquals(1, storages.size());
    }

    @Test
    void returnsStoragesAsMember() {
        List<Storage> storages = storageRepository.findAccessibleStorages(memberUser.getId());
        assertEquals(1, storages.size());
    }
}
