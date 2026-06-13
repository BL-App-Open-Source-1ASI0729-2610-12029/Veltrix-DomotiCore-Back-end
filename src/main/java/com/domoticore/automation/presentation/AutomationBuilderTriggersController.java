package com.domoticore.automation.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.presentation.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/automation-builder-triggers")
@Tag(name = "Automation Builder Triggers")
public class AutomationBuilderTriggersController extends AbstractJsonCollectionController {

    public AutomationBuilderTriggersController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "automation-builder-triggers");
    }
}
