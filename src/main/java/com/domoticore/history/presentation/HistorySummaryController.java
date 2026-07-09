package com.domoticore.history.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.presentation.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history-summary")
@Tag(name = "History Summary")
public class HistorySummaryController extends AbstractUserScopedJsonCollectionController {

    public HistorySummaryController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "history-summary");
    }
}
