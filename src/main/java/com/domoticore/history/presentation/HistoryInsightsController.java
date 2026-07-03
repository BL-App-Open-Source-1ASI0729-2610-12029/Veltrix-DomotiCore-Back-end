package com.domoticore.history.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.interfaces.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history-insights")
@Tag(name = "History Insights")
public class HistoryInsightsController extends AbstractUserScopedJsonCollectionController {

    public HistoryInsightsController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "history-insights");
    }
}
