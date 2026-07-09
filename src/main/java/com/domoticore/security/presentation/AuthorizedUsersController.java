package com.domoticore.security.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.presentation.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.PlatformPermission;
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

    @Override
    protected void beforeCreate() {
        currentUserProvider.requirePermission(PlatformPermission.SETTINGS_AUTHORIZED_USERS);
    }

    @Override
    protected void beforePatch() {
        currentUserProvider.requirePermission(PlatformPermission.SETTINGS_AUTHORIZED_USERS);
    }

    @Override
    protected void beforeDelete() {
        currentUserProvider.requirePermission(PlatformPermission.SETTINGS_AUTHORIZED_USERS);
    }
}
