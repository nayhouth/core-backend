package com.corebackend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    @Column(nullable = false)
    private Long modNo;

    @Column(nullable = false)
    private boolean isDeleted;

    private LocalDateTime deletedAt;
    private String deletedBy;
}