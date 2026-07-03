package com.domoticore.automation.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.interfaces.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/automation-recipe")
@Tag(name = "Automation Recipe")
public class AutomationRecipeController extends AbstractUserScopedJsonCollectionController {

    public AutomationRecipeController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "automation-recipe");
    }
}
