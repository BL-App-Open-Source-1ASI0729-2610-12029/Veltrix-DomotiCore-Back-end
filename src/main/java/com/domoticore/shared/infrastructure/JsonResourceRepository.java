package com.domoticore.shared.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JsonResourceRepository extends JpaRepository<JsonResourceEntity, Long> {

    List<JsonResourceEntity> findByCollectionNameOrderByResourceIdAsc(String collectionName);

    List<JsonResourceEntity> findByCollectionNameAndResourceIdStartingWith(String collectionName, String prefix);

    boolean existsByCollectionNameAndResourceIdStartingWith(String collectionName, String prefix);

    Optional<JsonResourceEntity> findByCollectionNameAndResourceId(String collectionName, String resourceId);

    void deleteByCollectionNameAndResourceId(String collectionName, String resourceId);

    boolean existsByCollectionName(String collectionName);
}
