package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.interfaces.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-details")
@Tag(name = "Device Details")
public class DeviceDetailsController extends AbstractJsonCollectionController {

    public DeviceDetailsController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "device-details");
    }
}
