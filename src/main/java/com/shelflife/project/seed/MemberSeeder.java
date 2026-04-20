package com.shelflife.project.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.repository.UserRepository;

@Component
public class MemberSeeder implements Seeder {
    @Autowired
    private UserRepository repository;

    @Autowired
    private StorageMemberRepository memberRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Override
    public void seed() {
        User invite = repository.findByEmail("invite@test.test").get();
        Storage storage = storageRepository.findById(1L).get();

        StorageMember member = new StorageMember();
        member.setStorage(storage);
        member.setUser(invite);
        member.setAccepted(false);
        memberRepository.save(member);
    }

    @Override
    public boolean shouldSeed() {
        return memberRepository.count() == 0;
    }
}
