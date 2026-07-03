package com.domoticore.security.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.interfaces.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authorized-users")
@Tag(name = "Authorized Users")
public class AuthorizedUsersController extends AbstractUserScopedJsonCollectionController {

    public AuthorizedUsersController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "authorized-users");
    }
}
