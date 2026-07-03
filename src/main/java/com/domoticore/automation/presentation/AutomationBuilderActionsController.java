package com.domoticore.automation.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.interfaces.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/automation-builder-actions")
@Tag(name = "Automation Builder Actions")
public class AutomationBuilderActionsController extends AbstractUserScopedJsonCollectionController {

    public AutomationBuilderActionsController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "automation-builder-actions");
    }
}
