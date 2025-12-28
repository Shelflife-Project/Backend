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
@Table(name = "storages")
public class Storage {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private long ownerId;

    @Column(nullable = false)
    private String name;
}
