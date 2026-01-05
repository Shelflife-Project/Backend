package com.shelflife.project.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "storage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StorageItem> items = new ArrayList<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "storage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StorageMember> members = new ArrayList<>();
}
