package com.domoticore.security.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.presentation.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/smart-locks")
@Tag(name = "Smart Locks")
public class SmartLocksController extends AbstractUserScopedJsonCollectionController {

    public SmartLocksController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "smart-locks");
    }
}
