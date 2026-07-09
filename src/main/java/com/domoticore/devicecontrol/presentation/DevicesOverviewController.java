package com.domoticore.devicecontrol.presentation;

import com.domoticore.devicecontrol.application.DevicesOverviewRefreshService;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.infrastructure.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.presentation.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices-overview")
@Tag(name = "Devices Overview")
public class DevicesOverviewController extends AbstractUserScopedJsonCollectionController {

    private final DevicesOverviewRefreshService refreshService;

    public DevicesOverviewController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider,
            DevicesOverviewRefreshService refreshService) {
        super(userCollectionAccessService, currentUserProvider, "devices-overview");
        this.refreshService = refreshService;
    }

    @PostMapping("/refresh")
    @ApiAuthenticatedGetResponses
    @Operation(summary = "Manually refresh devices overview from latest device states")
    public JsonNode refreshOverview() {
        var user = currentUserProvider.requireUser();
        return refreshService.refresh(user, currentUserProvider.requireSegment());
    }
}
