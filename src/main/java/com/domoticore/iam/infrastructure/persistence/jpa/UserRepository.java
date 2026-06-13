package com.domoticore.iam.infrastructure.persistence.jpa;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.valueobjects.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAddress(Email emailAddress);

    boolean existsByEmailAddress(Email emailAddress);
}
