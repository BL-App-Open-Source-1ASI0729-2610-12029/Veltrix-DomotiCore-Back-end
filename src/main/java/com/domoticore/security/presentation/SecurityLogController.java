package com.domoticore.security.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.interfaces.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/security-log")
@Tag(name = "Security Log")
public class SecurityLogController extends AbstractJsonCollectionController {

    public SecurityLogController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "security-log");
    }
}
