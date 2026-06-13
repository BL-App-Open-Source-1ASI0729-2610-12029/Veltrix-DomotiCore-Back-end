package com.domoticore.devicecontrol.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.interfaces.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices-overview")
@Tag(name = "Devices Overview")
public class DevicesOverviewController extends AbstractJsonCollectionController {

    public DevicesOverviewController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "devices-overview");
    }
}
