package com.domoticore.iam.application.queryservices;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.domain.model.queries.GetUserByIdQuery;
import com.domoticore.iam.domain.model.queries.ListUsersQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {

    Optional<User> handle(GetUserByIdQuery query);

    List<User> handle(ListUsersQuery query);
}
