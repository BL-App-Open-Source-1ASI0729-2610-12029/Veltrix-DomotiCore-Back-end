package com.domoticore.iam.application.internal.queryservices;

import com.domoticore.iam.application.queryservices.UserQueryService;
import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.queries.GetUserByIdQuery;
import com.domoticore.iam.domain.model.queries.ListUsersQuery;
import com.domoticore.iam.infrastructure.persistence.jpa.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    public List<User> handle(ListUsersQuery query) {
        return userRepository.findAll();
    }
}
