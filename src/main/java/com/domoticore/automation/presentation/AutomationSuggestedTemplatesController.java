package com.domoticore.automation.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.presentation.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/automation-suggested-templates")
@Tag(name = "Automation Suggested Templates")
public class AutomationSuggestedTemplatesController extends AbstractUserScopedJsonCollectionController {

    public AutomationSuggestedTemplatesController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "automation-suggested-templates");
    }
}
