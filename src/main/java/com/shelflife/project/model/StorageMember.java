package com.shelflife.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "storage_members")
public class StorageMember {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private long storageId;

    @Column(nullable = false)
    private long userId;
}
