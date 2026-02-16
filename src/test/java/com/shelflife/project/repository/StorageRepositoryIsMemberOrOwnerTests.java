package com.shelflife.project.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class StorageRepositoryIsMemberOrOwnerTests {
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
    void returnsFalseWithInvalidID() {
        assertFalse(storageRepository.isMemberOrOwner(ownerStorage.getId(), -1));
    }

    @Test
    void returnsFalseAsNonMember() {
        assertFalse(storageRepository.isMemberOrOwner(ownerStorage.getId(), user.getId()));
    }

    @Test
    void returnsFalseAsNotAcceptedMember() {
        StorageMember userMember = new StorageMember();
        userMember.setAccepted(false);
        userMember.setStorage(ownerStorage);
        userMember.setUser(user);
        storageMemberRepository.save(userMember);

        assertFalse(storageRepository.isMemberOrOwner(ownerStorage.getId(), user.getId()));
    }

    @Test
    void returnsTrueAsOwner() {
        assertTrue(storageRepository.isMemberOrOwner(ownerStorage.getId(), owner.getId()));
    }

    @Test
    void returnsTrueAsMember() {
        assertTrue(storageRepository.isMemberOrOwner(ownerStorage.getId(), memberUser.getId()));
    }
}
