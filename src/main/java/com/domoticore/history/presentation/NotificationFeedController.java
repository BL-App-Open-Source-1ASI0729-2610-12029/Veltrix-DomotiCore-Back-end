package com.domoticore.history.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.interfaces.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification-feed")
@Tag(name = "Notification Feed")
public class NotificationFeedController extends AbstractJsonCollectionController {

    public NotificationFeedController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "notification-feed");
    }
}
