package com.domoticore.security.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.presentation.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/smart-locks")
@Tag(name = "Smart Locks")
public class SmartLocksController extends AbstractJsonCollectionController {

    public SmartLocksController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "smart-locks");
    }
}
