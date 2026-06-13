package com.domoticore.automation.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.interfaces.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/automation-recipe")
@Tag(name = "Automation Recipe")
public class AutomationRecipeController extends AbstractJsonCollectionController {

    public AutomationRecipeController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "automation-recipe");
    }
}
