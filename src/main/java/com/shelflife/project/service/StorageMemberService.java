package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    private StorageAccessService storageAccessService;

    @Autowired
    private StorageRepository storageRepository;

    public Storage getStorage(final long id) throws ItemNotFoundException {
        Optional<Storage> storage = storageRepository.findById(id);

        if (!storage.isPresent())
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storage.get();
    }

    public StorageMember getMember(final long id) throws ItemNotFoundException {
        Optional<StorageMember> member = storageMemberRepository.findById(id);

        if (!member.isPresent())
            throw new ItemNotFoundException("id", "Member with this id was not found");

        return member.get();
    }

    public List<StorageMember> getStorageMembers(final long storageId, User current)
            throws ItemNotFoundException, AccessDeniedException {

        if (!storageRepository.existsById(storageId))
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        return storageMemberRepository.findByStorageId(storageId);
    }

    public List<StorageMember> getInvites(User current) throws AccessDeniedException {
        if (current == null)
            throw new AccessDeniedException(null);

        return storageMemberRepository.findInvitesByUserId(current.getId());
    }

    @Transactional
    public void acceptInvite(final long memberId, User current)
            throws ItemNotFoundException, AccessDeniedException {
        if (current == null)
            throw new AccessDeniedException(null);

        StorageMember member = getMember(memberId);

        if (member.getUser().getId() != current.getId())
            throw new AccessDeniedException("You cant accept other users invites!");

        member.setAccepted(true);
        storageMemberRepository.save(member);
    }

    @Transactional
    public void declineInvite(final long memberId, User current)
            throws ItemNotFoundException, AccessDeniedException {
        if (current == null)
            throw new AccessDeniedException(null);

        StorageMember member = getMember(memberId);

        if (member.getUser().getId() != current.getId())
            throw new AccessDeniedException("You cant decline other users invites!");

        storageMemberRepository.delete(member);
    }

    @Transactional
    public StorageMember inviteMemberToStorage(final long storageId, final String memberEmail, User current)
            throws ItemNotFoundException, MemberException, AccessDeniedException {
        if (current == null)
            throw new AccessDeniedException(null);

        Storage storage = getStorage(storageId);
        User target = userService.getUserByEmail(memberEmail);

        if (!current.isAdmin() && storage.getOwner().getId() != current.getId())
            throw new AccessDeniedException(null);

        // You can't invite admins, or the owner
        if (target.isAdmin() || target.getId() == storage.getOwner().getId())
            throw new MemberException(true);

        if (storageMemberRepository.existsByStorageIdAndUserId(storageId, target.getId()))
            throw new MemberException(true);

        StorageMember member = new StorageMember();
        member.setUser(target);
        member.setStorage(storage);
        member.setAccepted(false);

        return storageMemberRepository.save(member);
    }

    @Transactional
    public void removeMemberFromStorage(final long storageId, final long userId) throws ItemNotFoundException {
        Optional<StorageMember> member = storageMemberRepository.findByStorageIdAndUserId(storageId, userId);

        if (!member.isPresent())
            throw new ItemNotFoundException("member", "Member was not found");

        storageMemberRepository.deleteById(member.get().getId());
    }

    @Transactional
    public void removeMemberFromStorage(final long storageId, final long userId, User current)
            throws ItemNotFoundException, AccessDeniedException {

        if(current == null)
            throw new AccessDeniedException(null);

        Storage storage = getStorage(storageId);

        if (!current.isAdmin() && storage.getOwner().getId() != current.getId())
            throw new AccessDeniedException(null);

        removeMemberFromStorage(storageId, userId);
    }

    public boolean isMemberOfStorage(final long storageId, final long userId) {
        return storageMemberRepository.existsByStorageIdAndUserId(storageId, userId);
    }

}
