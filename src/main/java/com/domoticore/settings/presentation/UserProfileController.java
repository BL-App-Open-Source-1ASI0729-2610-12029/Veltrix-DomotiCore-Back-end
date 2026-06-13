package com.domoticore.settings.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.presentation.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-profile")
@Tag(name = "User Profile")
public class UserProfileController extends AbstractJsonCollectionController {

    public UserProfileController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "user-profile");
    }
}
