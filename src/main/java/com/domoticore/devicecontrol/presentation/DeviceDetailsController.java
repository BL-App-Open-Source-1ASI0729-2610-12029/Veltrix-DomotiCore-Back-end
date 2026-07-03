package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.interfaces.AbstractUserScopedJsonCollectionController;
import com.domoticore.shared.security.CurrentUserProvider;
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
}
