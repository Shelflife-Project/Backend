package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.MemberException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;

import jakarta.transaction.Transactional;

@Service
public class StorageMemberService {

    @Autowired
    private UserService userService;

    @Autowired
    private StorageMemberRepository storageMemberRepository;

    @Autowired
    private StorageRepository storageRepository;

    private Storage getStorage(final long id) throws ItemNotFoundException {
        Optional<Storage> storage = storageRepository.findById(id);

        if (!storage.isPresent())
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storage.get();
    }

    @Transactional
    public StorageMember addMemberToStorage(final long storageId, final String memberEmail, Authentication auth)
            throws ItemNotFoundException, MemberException, AccessDeniedException {

        Optional<Storage> storage = storageRepository.findById(storageId);
        if (!storage.isPresent())
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        User target = userService.getUserByEmail(memberEmail);

        if (storageMemberRepository.existsByStorageIdAndUserId(storageId, target.getId()))
            throw new MemberException(true);

        User current = userService.getUserByAuth(auth);

        if (!current.isAdmin() && storage.get().getOwner().getId() != current.getId())
            throw new AccessDeniedException(null);

        StorageMember member = new StorageMember();
        member.setUser(target);
        member.setStorage(storage.get());

        return storageMemberRepository.save(member);
    }

    public List<StorageMember> getStorageMembers(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {

        if (!storageRepository.existsById(storageId))
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        if (!canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

        return storageMemberRepository.findByStorageId(storageId);
    }

    @Transactional
    public void removeMemberFromStorage(final long storageId, final long userId, Authentication auth)
            throws ItemNotFoundException, MemberException {

        Optional<StorageMember> member = storageMemberRepository.findByStorageIdAndUserId(storageId, userId);

        if (!member.isPresent())
            throw new ItemNotFoundException("Member was not found");

        User current = userService.getUserByAuth(auth);
        Storage storage = getStorage(storageId);

        if (!current.isAdmin() && storage.getOwner().getId() != current.getId())
            throw new AccessDeniedException(null);

        storageMemberRepository.deleteById(member.get().getId());
    }

    public boolean canAccessStorage(final long storageId, final long userId) {
        try {
            User user = userService.getUserById(userId);
            Storage storage = getStorage(storageId);

            if (user.isAdmin())
                return true;

            if (storage.getOwner().getId() == userId)
                return true;

            return storageMemberRepository.existsByStorageIdAndUserId(storageId, userId);
        } catch (ItemNotFoundException e) {
            return false;
        }
    }

    public boolean canAccessStorage(final long storageId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            Storage storage = getStorage(storageId);

            if (user.isAdmin())
                return true;

            if (storage.getOwner().getId() == user.getId())
                return true;

            return storageMemberRepository.existsByStorageIdAndUserId(storageId, user.getId());
        } catch (ItemNotFoundException e) {
            return false;
        } catch (AccessDeniedException e) {
            return false;
        }
    }
}
