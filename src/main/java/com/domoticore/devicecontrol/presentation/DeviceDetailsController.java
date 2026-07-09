package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.interfaces.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.security.CurrentUserProvider;
import com.domoticore.shared.security.PlatformPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-details")
@Tag(name = "Device Details")
public class DeviceDetailsController extends AbstractUserScopedJsonCollectionController {

    private final CurrentUserProvider currentUserProvider;

    public DeviceDetailsController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider) {
        super(userCollectionAccessService, currentUserProvider, "device-details");
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    protected void beforeDelete() {
        currentUserProvider.requirePermission(PlatformPermission.DEVICES_DELETE);
    }
}
