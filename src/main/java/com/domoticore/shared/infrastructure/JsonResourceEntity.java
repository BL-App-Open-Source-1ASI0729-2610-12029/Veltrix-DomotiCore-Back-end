package com.domoticore.shared.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "json_resources",
        uniqueConstraints = @UniqueConstraint(columnNames = {"collection_name", "resource_id"})
)
@Getter
@Setter
public class JsonResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    @Column(name = "collection_name", nullable = false)
    private String collectionName;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "json_payload", nullable = false, columnDefinition = "TEXT")
    private String jsonPayload;
}
