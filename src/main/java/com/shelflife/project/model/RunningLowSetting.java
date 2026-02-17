package com.shelflife.project.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "running_low_settings",
    uniqueConstraints = @UniqueConstraint(columnNames = { "storage_id", "product_id" })
)
@Schema(description = "Running Low Setting resource - defines quantity thresholds for products in storages")
public class RunningLowSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the running low setting", example = "1")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    @Schema(description = "The storage this setting belongs to")
    private Storage storage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @Schema(description = "The product this running low threshold is set for")
    private Product product;

    @Schema(
        description = "The quantity threshold - when product quantity falls to or below this value, it is considered running low",
        example = "10",
        minimum = "0"
    )
    private int runningLow;
}
