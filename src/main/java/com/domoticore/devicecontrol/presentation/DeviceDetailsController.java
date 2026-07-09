package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.presentation.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.infrastructure.security.CurrentUserProvider;
import com.domoticore.shared.infrastructure.security.PlatformPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-details")
@Tag(name = "Device Details")
public class DeviceDetailsController extends AbstractUserScopedJsonCollectionController {

    public DeviceDetailsController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "device-details");
    }

    @Override
    protected void beforeCreate() {
    }

    @Override
    protected void beforeDelete() {
        currentUserProvider.requirePermission(PlatformPermission.DEVICES_DELETE);
    }
}
